package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.ref.WeakReference;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * 代表Lua的table类型 (最终的组合模型)。
 *
 * <p>LuaTable 是一个 LuaValue，继承 DynamicObject 来存储哈希属性，
 * 以及一个 Object[] 来存储数组部分。
 * <p>它完整地实现了 InteropLibrary，可以作为对象、数组和哈希表与外界交互。
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaTable extends LuaValue {

    // --- 内部状态 ---
    private Object[] arrayPart;
    private int arraySize; // Lua's sequence length (#t)
    private int arrayLimit; // 数组部分曾用到的最大 1-based 索引（近似 Lua 的 sizearray，用于 next 的键有效性判断）

    /** 当元表的 {@code __mode} 含 'v' 时，值为弱引用（可被 JVM GC 回收）。 */
    private boolean weakValues;

    // 遍历缓存：缓存形状对应的哈希键数组及 (键 -> 位置) 索引，避免 next 每次
    // 重建键数组并做 O(n) 线性查找，导致 pairs 遍历整体退化为 O(n^2)。
    // 当 shape 身份改变（哈希部分结构变化）时失效重建。
    private Shape cachedHashShape;
    private Object[] cachedHashKeyArray;
    private java.util.HashMap<Object, Integer> cachedHashKeyIndex;

    /**
     * 创建一个新的、空的 LuaTable。
     */
    public LuaTable(Shape shape) {
        super(shape);
        this.arrayPart = new Object[4]; // 初始容量
        this.arraySize = 0;
    }

    // --- Getters (供AST节点和内部使用) ---

    public DynamicObject getProperties() {
        return this;
    }

    public Object[] getArrayPart() {
        return this.arrayPart;
    }



    // --- 核心高性能读写 (由AST节点调用) ---

    /**
     * 提供一个统一的、高性能的内部接口来读取table的值。
     * 这个方法会被AST节点 (LuaReadTableNode) 和 Interop 消息 (readHashValue) 调用。
     *
     * <p>它实现了Lua的查找逻辑：优先检查键是否为有效的数组索引。
     *
     * @param key    The key to read (can be any Object).
     * @param objLib 一个用于操作内部 DynamicObject 的库实例。
     *               调用者 (通常是AST节点) 应该传入一个*缓存的*库以获得最佳性能。
     * @return The value associated with the key, or {@link LuaNil#SINGLETON} if not found.
     */
    public Object rawget(Object key, DynamicObjectLibrary objLib) {
        // --- 1. 快速路径：尝试作为数组索引读取 ---
        // 整数键优先读取数组部分；稀疏整数键仍可能存储在哈希部分。
        int arrayIndex = toArrayIndex(key);
        if (arrayIndex > 0) {
            int index = arrayIndex - 1;
            if (index < this.arraySize) {
                Object value = this.arrayPart[index];
                if (value != null) {
                    Object unwrapped = unwrapWeak(value);
                    if (unwrapped != LuaNil.SINGLETON) {
                        return unwrapped;
                    }
                }
            }
            key = (long) arrayIndex;
        }

        // --- 2. 回退路径：作为哈希/属性读取 ---

        // 如果键不是一个有效的数组索引，或者索引越界，
        // 我们就在内部的 DynamicObject (properties) 中查找它。
        // objLib.getOrDefault 是一个高性能的操作，它利用了 Truffle 的 Shape 优化。
        return unwrapWeak(objLib.getOrDefault(this, normalizeHashKey(key), LuaNil.SINGLETON));
    }

    public Object rawget(Object key) {
        return rawget(key,DynamicObjectLibrary.getUncached());
    }

    public Object rawget(long key) {
        return rawget(Long.valueOf(key), DynamicObjectLibrary.getUncached());
    }

    public void rawset(Object key, Object value) {
        rawset(key, value, DynamicObjectLibrary.getUncached());
    }

    public void rawset(Object key, Object value, DynamicObjectLibrary objLib) {
        // 标准 Lua 语义：table 索引不能为 nil 或 NaN
        if (key == null || key == LuaNil.SINGLETON) {
            throw LuaException.create("table index is nil", null);
        }
        if (key instanceof Double doubleKey && doubleKey.isNaN()) {
            throw LuaException.create("table index is NaN", null);
        }
        int arrayIndex = toArrayIndex(key);
        Object normalizedKey = normalizePropertyKey(key, arrayIndex);
        if (arrayIndex > 0 && shouldUseArrayPart(arrayIndex)) {
            arrayRawSet(arrayIndex, value);
            objLib.removeKey(this, normalizedKey);
            return;
        }
        if (value == LuaNil.SINGLETON) {
            // 标准 Lua 在把已存在的键赋为 nil 时保留一个“死键”（dead key），
            // 以便在遍历过程中删除当前键后 next 仍能定位后续键。若键不存在则不创建。
            if (objLib.containsKey(this, normalizedKey)) {
                objLib.put(this, normalizedKey, LuaNil.SINGLETON);
            }
        } else {
            objLib.put(this, normalizedKey, weakValues ? wrapWeakValue(value) : value);
        }
    }

    public void rawset(long key, Object value) {
        rawset(Long.valueOf(key), value, DynamicObjectLibrary.getUncached());
    }

    // --- 数组部分的具体实现 ---

    public void arrayRawSet(int luaIndex, Object value) {
        int index = luaIndex - 1; // 将基于1的Lua索引转换为基于0的Java索引
        if (value == LuaNil.SINGLETON) { // Deletion
            if (index < arraySize) {
                arrayPart[index] = null;
                if (index == arraySize - 1) {
                    recalculateArraySizeBoundary();
                }
            }
        } else { // Write or update
            ensureArrayCapacity(index);
            arrayPart[index] = weakValues ? wrapWeakValue(value) : value;
            if (index >= arraySize) {
                arraySize = index + 1;
            }
            if (luaIndex > arrayLimit) {
                arrayLimit = luaIndex;
            }
        }
    }

    /**
     * 直接、无检查地从数组部分读取一个值 (假设调用者已验证索引)。
     * “Raw”意味着它不处理元方法。
     * 这是一个供高性能AST节点调用的内部方法。
     *
     * @param luaIndex 1-based Lua 索引。
     * @return The value at the index, or {@link LuaNil#SINGLETON}.
     */
    public Object arrayRawGet(int luaIndex) {
        int index = luaIndex - 1; // 1. 索引转换

        if (index >= 0 && index < this.arraySize) { // 2. 边界检查
            Object value = this.arrayPart[index];
            return value == null ? LuaNil.SINGLETON : unwrapWeak(value); // 3. null -> nil 转换
        }

        // 如果数组部分越界，返回 nil
        return LuaNil.SINGLETON;
    }

    // ... 其他辅助方法 (isArrayIndex, ensureArrayCapacity, recalculateArraySizeBoundary) ...
    // ... 这些方法的实现与之前的回复相同 ...

    // --- 继承自 LuaValue ---
    @Override
    public String getTypeName() {
        return "table";
    }

    // =================================================================
    // InteropLibrary 实现 (使 table 能与外部世界交互)
    // =================================================================

    // === 成员协议 (用于字符串键) ===
    @ExportMessage
    boolean hasMembers() {
        return true;
    }

    // 已存在的成员可修改，不存在的成员可插入；二者互斥是 interop 的契约。
    @ExportMessage
    boolean isMemberModifiable(String member,
                               @Cached.Shared("fromStr") @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                               @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return objLib.containsKey(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING));
    }

    @ExportMessage
    boolean isMemberInsertable(String member,
                               @Cached.Shared("fromStr") @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                               @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return !objLib.containsKey(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING));
    }

    @ExportMessage
    Object getMembers(boolean includeInternal, @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return getMembersBoundary(objLib);
    }

    @ExportMessage
    boolean isMemberReadable(String member,
                             @Cached.Shared("fromStr") @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                             @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return objLib.containsKey(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING));
    }

    @ExportMessage
    Object readMember(String member,
                      @Cached(value = "create()", neverDefault = true) LuaCoerceToIntegerNode coerceNode,
                      @Cached.Shared("fromStr") @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                      @CachedLibrary("this") DynamicObjectLibrary objLib) throws UnknownIdentifierException {
        Object value = objLib.getOrDefault(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING), null);
        if (value == null) {
            throw UnknownIdentifierException.create(member);
        }
        return com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.normalizeHostValue(value);
    }

    @ExportMessage
    void writeMember(String member, Object value,
                     @Cached.Shared("fromStr") @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                     @CachedLibrary("this") DynamicObjectLibrary objLib) {

        // 2. 写入（宿主 null 统一存成 Lua nil）
        objLib.put(this, fromJavaStringNode.execute(member,LuaLanguage.STRING_ENCODING),
                com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.normalizeHostValue(value));
    }

    // ... isMemberModifiable, isMemberInsertable, writeMember 的实现 ...

    // === 数组协议 (用于整数键) ===
    @ExportMessage
    boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    public int getArraySize() {
        return this.arraySize;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
        long luaIndex = index + 1;
        return luaIndex > 0 && luaIndex <= this.arraySize && this.arrayPart[(int) index] != null
                && unwrapWeak(this.arrayPart[(int) index]) != LuaNil.SINGLETON;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        return unwrapWeak(this.arrayPart[(int) index]);
    }

    // === 哈希表协议 (用于任意对象键) ===
    @ExportMessage
    boolean hasHashEntries() {
        return true;
    }

    @ExportMessage
    @TruffleBoundary
    long getHashSize(@CachedLibrary("this") DynamicObjectLibrary objLib) {
        // A simplified version. A correct version would need to handle overlaps.
        // 跳过“死键”（值为 nil 的保留键）。
        long size = this.arraySize;
        for (Object key : objLib.getKeyArray(this)) {
            if (objLib.getOrDefault(this, key, LuaNil.SINGLETON) != LuaNil.SINGLETON) {
                size++;
            }
        }
        return size;
    }

    @ExportMessage
    boolean isHashEntryReadable(Object key, @CachedLibrary("this") DynamicObjectLibrary objLib) {
        Object value = rawget(key, objLib);
        return value != LuaNil.SINGLETON;
    }

    @ExportMessage
    Object readHashValue(Object key, @CachedLibrary("this") DynamicObjectLibrary objLib){
        return rawget(key, objLib);
    }

    @ExportMessage
    @TruffleBoundary
    Object getHashEntriesIterator(@CachedLibrary("this") DynamicObjectLibrary objLib) {
        Object[] rawHashKeys = objLib.getKeyArray(this);
        List<Object> liveHashKeys = new ArrayList<>(rawHashKeys.length);
        for (Object key : rawHashKeys) {
            if (objLib.getOrDefault(this, key, LuaNil.SINGLETON) != LuaNil.SINGLETON) {
                liveHashKeys.add(key);
            }
        }
        List<Object> arrayKeyList = new ArrayList<>();
        for (int i = 0; i < this.arraySize; i++) {
            if (this.arrayPart[i] != null) {
                arrayKeyList.add((double) (i + 1));
            }
        }
        return new LuaTableIterator(arrayKeyList.toArray(), liveHashKeys.toArray());
    }

    // === 辅助方法 ===
    private static boolean isArrayIndex(double d) {
        return d > 0 && d <= Integer.MAX_VALUE && d == (int) d;
    }

    /**
     * 当元表 {@code __mode} 含 'v' 时把值包成 {@link WeakReference}，
     * 使 JVM GC 可以回收仅被弱表引用的对象（Lua 弱表语义）。
     */
    static Object wrapWeakValue(Object value) {
        if (value instanceof LuaValue v && v != LuaNil.SINGLETON && !(v instanceof LuaBoolean)) {
            return new WeakReference<>(value);
        }
        // 数字/字符串/布尔等立即值不由弱引用持有（Lua 中它们不会被回收）。
        return value;
    }

    /** 读取时解开弱引用；引用已被 GC 清除则视为 nil。 */
    static Object unwrapWeak(Object value) {
        if (value instanceof WeakReference<?> ref) {
            Object referent = ref.get();
            return referent == null ? LuaNil.SINGLETON : referent;
        }
        return value;
    }

    /**
     * 元表变更时更新弱值模式，并把既有条目在强引用与弱引用之间转换。
     * （测试里 {@code setmetatable(x, {__mode='kv'})} 通常发生在元素写入之后。）
     */
    void updateWeakMode(LuaTable metatable) {
        boolean weak = false;
        if (metatable != null) {
            Object mode = metatable.rawget(LuaMetatables.__MODE);
            if (mode instanceof TruffleString modeString) {
                weak = modeString.toJavaStringUncached().indexOf('v') >= 0;
            }
        }
        if (weak == this.weakValues) {
            return;
        }
        DynamicObjectLibrary objLib = DynamicObjectLibrary.getUncached();
        if (weak) {
            for (int i = 0; i < arrayPart.length; i++) {
                if (arrayPart[i] != null && !(arrayPart[i] instanceof WeakReference)) {
                    arrayPart[i] = wrapWeakValue(arrayPart[i]);
                }
            }
            for (Object key : objLib.getKeyArray(this)) {
                Object v = objLib.getOrDefault(this, key, LuaNil.SINGLETON);
                if (v != LuaNil.SINGLETON && !(v instanceof WeakReference)) {
                    objLib.put(this, key, wrapWeakValue(v));
                }
            }
        } else {
            for (int i = 0; i < arrayPart.length; i++) {
                if (arrayPart[i] instanceof WeakReference<?> ref) {
                    arrayPart[i] = ref.get();
                }
            }
            for (Object key : objLib.getKeyArray(this)) {
                Object v = objLib.getOrDefault(this, key, LuaNil.SINGLETON);
                if (v instanceof WeakReference<?> ref) {
                    Object referent = ref.get();
                    objLib.put(this, key, referent == null ? LuaNil.SINGLETON : referent);
                }
            }
        }
        this.weakValues = weak;
    }

    private static boolean isArrayIndex(long value) {
        return value > 0 && value <= Integer.MAX_VALUE;
    }

    private static int toArrayIndex(Object key) {
        if (key instanceof Integer integer && integer > 0) {
            return integer;
        }
        if (key instanceof Long longValue && isArrayIndex(longValue)) {
            return longValue.intValue();
        }
        if (key instanceof Double doubleValue && isArrayIndex(doubleValue)) {
            return doubleValue.intValue();
        }
        return -1;
    }

    private boolean shouldUseArrayPart(int arrayIndex) {
        return arrayIndex <= (long) arraySize + 1 || arrayIndex <= arrayPart.length;
    }

    private static Object normalizePropertyKey(Object key, int arrayIndex) {
        if (arrayIndex > 0) {
            return (long) arrayIndex;
        }
        return normalizeHashKey(key);
    }

    /**
     * 规范化哈希键：
     * <ul>
     *   <li>字符串统一为 TruffleString；</li>
     *   <li>布尔统一为 LuaBoolean 单例，使字面量 true/false 与比较运算产生的
     *       java.lang.Boolean 指向同一键；</li>
     *   <li>值等于整数的浮点键（如 2.0）转换为整数键，符合 Lua 的键等价规则。</li>
     * </ul>
     */
    static Object normalizeHashKey(Object key) {
        if (key instanceof String string) {
            return toTruffleString(string);
        }
        if (key instanceof Boolean bool) {
            return LuaBoolean.valueOf(bool);
        }
        if (key instanceof Integer integer) {
            return integer.longValue();
        }
        if (key instanceof Double number && isExactInteger(number)) {
            return (long) (double) number;
        }
        return key;
    }

    /** 判断浮点数是否为可精确表示为 Lua 整数的值（位于 [-2^63, 2^63)）。 */
    private static boolean isExactInteger(double value) {
        if (value != Math.rint(value)) {
            return false;
        }
        return value >= -9.223372036854775808E18 && value < 9.223372036854775808E18;
    }

    private void ensureArrayCapacity(int index) {
        if (index >= arrayPart.length) {
            growArray(index + 1);
        }
    }

    @TruffleBoundary
    private void growArray(int minCapacity) {
        int oldCapacity = arrayPart.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity < minCapacity || newCapacity < 4) { // Min capacity of 4
            newCapacity = Math.max(minCapacity, 4);
        }
        arrayPart = Arrays.copyOf(arrayPart, newCapacity);
    }

    @TruffleBoundary
    private void recalculateArraySizeBoundary() {
        int newSize = arrayPart.length;
        while (newSize > 0 && arrayPart[newSize - 1] == null) {
            newSize--;
        }
        this.arraySize = newSize;
    }

    @TruffleBoundary
    private Object getMembersBoundary(DynamicObjectLibrary objLib) {
        Object[] hashKeys = objLib.getKeyArray(this);
        List<Object> allKeys = new ArrayList<>(hashKeys.length);
        for (Object key : hashKeys) {
            // 成员协议只接受字符串键；数字键属于数组协议，不能出现在成员列表中。
            if (!(key instanceof TruffleString) && !(key instanceof LuaString)) {
                continue;
            }
            if (objLib.getOrDefault(this, key, LuaNil.SINGLETON) != LuaNil.SINGLETON) {
                allKeys.add(key);
            }
        }
        return new LuaTableKeys(allKeys.toArray());
    }

    /**
     * 【关键实现】在 DynamicObject 中查找下一个键。
     * @param key 上一个键，如果是 nil，则返回第一个键。
     * @param objLib 用于操作 DynamicObject 的库。
     * @return 下一个键，如果没有则返回 LuaNil.SINGLETON。
     */
    @TruffleBoundary
    public Object nextKey(Object key, DynamicObjectLibrary objLib) {

        // --- 状态 1: 遍历的开始 (key 是 nil) ---
        if (key == null || key == LuaNil.SINGLETON) {
            // a. 先从数组部分开始找第一个非 nil 的元素
            for (int i = 0; i < this.arrayPart.length; i++) {
                if (this.arrayPart[i] != null) {
                    return (long) (i + 1); // Lua 索引是从 1 开始的（Long 才是 Lua 数字，Integer 会退化成 userdata）
                }
            }
            // b. 数组部分为空，从哈希部分找第一个“活键”
            return nextLiveHashKey(objLib.getKeyArray(this), 0, objLib);
        }

        // --- 状态 2: 当前 key 落在数组部分（1..arrayLimit 为合法数组索引） ---
        int arrayKey = toArrayIndex(key);
        if (arrayKey > 0 && arrayKey <= this.arrayLimit) {
            // 从当前索引的下一个开始继续扫描数组部分（当前键即使已被删除也能继续）
            for (int i = arrayKey; i < this.arrayPart.length; i++) {
                if (this.arrayPart[i] != null) {
                    return (long) (i + 1);
                }
            }
            // 数组部分遍历完毕，转到哈希部分
            return nextLiveHashKey(objLib.getKeyArray(this), 0, objLib);
        }

        // --- 状态 3: 当前 key 在哈希部分 ---
        Object[] propKeys = cachedHashKeys(objLib);
        Integer index = hashKeyIndexMap(propKeys).get(normalizeHashKey(key));
        if (index == null) {
            // 键不存在：标准 Lua 抛出 "invalid key to 'next'"
            throw LuaException.create("invalid key to 'next'", null);
        }
        return nextLiveHashKey(propKeys, index + 1, objLib);
    }

    /**
     * 返回当前 shape 对应的哈希键数组，按 shape 身份缓存，避免每次 next 都重建数组。
     */
    @TruffleBoundary
    private Object[] cachedHashKeys(DynamicObjectLibrary objLib) {
        Shape shape = this.getShape();
        if (shape == this.cachedHashShape && this.cachedHashKeyArray != null) {
            return this.cachedHashKeyArray;
        }
        Object[] propKeys = objLib.getKeyArray(this);
        this.cachedHashShape = shape;
        this.cachedHashKeyArray = propKeys;
        this.cachedHashKeyIndex = null;
        return propKeys;
    }

    /**
     * 返回哈希键数组中每个键的位置索引。结果与键数组一同按 shape 缓存，
     * 使 next 的查找降为 O(1)。
     */
    @TruffleBoundary
    private java.util.HashMap<Object, Integer> hashKeyIndexMap(Object[] propKeys) {
        if (this.cachedHashKeyIndex != null && this.cachedHashKeyArray == propKeys) {
            return this.cachedHashKeyIndex;
        }
        java.util.HashMap<Object, Integer> map = new java.util.HashMap<>(Math.max(16, propKeys.length * 2));
        for (int i = 0; i < propKeys.length; i++) {
            map.putIfAbsent(propKeys[i], i);
        }
        this.cachedHashKeyIndex = map;
        return map;
    }

    /**
     * 从 {@code from} 起返回第一个值为非 nil 的哈希键，跳过“死键”（值为 nil 的保留键）。
     * 死键仅用于在遍历中删除当前键后仍能定位后续键。
     */
    private Object nextLiveHashKey(Object[] propKeys, int from, DynamicObjectLibrary objLib) {
        for (int i = from; i < propKeys.length; i++) {
            if (objLib.getOrDefault(this, propKeys[i], LuaNil.SINGLETON) != LuaNil.SINGLETON) {
                return propKeys[i];
            }
        }
        return LuaNil.SINGLETON;
    }


    // 检查本表作为“被设置对象的元表”时是否带有 __metatable 保护字段
    @TruffleBoundary
    public Object getMetatableProtection() {
        // __metatable 是元表自身上的一个普通字段（TruffleString 键）。
        Object protection = this.rawget(LuaMetatables.__METATABLE);
        return protection == LuaNil.SINGLETON ? null : protection;
    }

}

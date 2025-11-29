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
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // 我们检查 key 是否是一个可以被无损转换为整数的 Double
        if (key instanceof Integer) {
            int dKey = (Integer) key;
            if (isArrayIndex(dKey)) {
                // 将 1-based Lua 索引转换为 0-based Java 数组索引
                int index = dKey - 1;

                // 检查索引是否在当前数组的有效范围内
                if (index < this.arraySize) {
                    Object value = this.arrayPart[index];
                    // 在数组中，我们用 Java null 来代表 Lua nil
                    return value == null ? LuaNil.SINGLETON : value;
                }
                // 如果索引超出了当前数组大小，它仍然可能被存储在哈希部分
                // (例如 t[1000] = 5)，所以我们不能在这里提前返回 nil。
            }
        }

        // --- 2. 回退路径：作为哈希/属性读取 ---

        // 如果键不是一个有效的数组索引，或者索引越界，
        // 我们就在内部的 DynamicObject (properties) 中查找它。
        // objLib.getOrDefault 是一个高性能的操作，它利用了 Truffle 的 Shape 优化。

        return objLib.getOrDefault(this, key, LuaNil.SINGLETON);
    }

    public void rawset(Object key, Object value) {
        if (key instanceof Double && isArrayIndex((Double) key)) {
            arrayRawSet(((Double) key).intValue(), value);
            return;
        }
        if (value == LuaNil.SINGLETON) {
            DynamicObjectLibrary.getUncached().removeKey(this, key);
        } else {
            DynamicObjectLibrary.getUncached().put(this, key, value);
        }
    }

    public void rawset(Object key, Object value, DynamicObjectLibrary objLib) {
        if (key instanceof Double && isArrayIndex((Double) key)) {
            arrayRawSet(((Double) key).intValue(), value);
            return;
        }
        if (value == LuaNil.SINGLETON) {
            objLib.removeKey(this, key);
        } else {
            objLib.put(this, key, value);
        }
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
            arrayPart[index] = value;
            if (index >= arraySize) {
                arraySize = index + 1;
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
            return value == null ? LuaNil.SINGLETON : value; // 3. null -> nil 转换
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

    @ExportMessage boolean isMemberModifiable(String member) { return true; }

    @ExportMessage boolean isMemberInsertable(String member) { return true; }

    @ExportMessage
    Object getMembers(boolean includeInternal, @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return getMembersBoundary(objLib);
    }

    @ExportMessage
    boolean isMemberReadable(String member,
                             @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                             @CachedLibrary("this") DynamicObjectLibrary objLib) {
        return objLib.containsKey(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING));
    }

    @ExportMessage
    Object readMember(String member,
                      @Cached("create()") LuaCoerceToIntegerNode coerceNode,
                      @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                      @CachedLibrary("this") DynamicObjectLibrary objLib) throws UnknownIdentifierException {
        Object value = objLib.getOrDefault(this, fromJavaStringNode.execute(member, LuaLanguage.STRING_ENCODING), null);
        if (value == null) {
            throw UnknownIdentifierException.create(member);
        }
        return value;
    }

    @ExportMessage
    void writeMember(String member, Object value,
                     @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                     @CachedLibrary("this") DynamicObjectLibrary objLib) {

        // 2. 写入
        //    注意：这里可能还需要处理 value 的类型转换（如果需要的话）
        objLib.put(this, fromJavaStringNode.execute(member,LuaLanguage.STRING_ENCODING), value);
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
        return luaIndex > 0 && luaIndex <= this.arraySize && this.arrayPart[(int) index] != null;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        return this.arrayPart[(int) index];
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
        return this.arraySize + objLib.getKeyArray(this).length;
    }

    @ExportMessage
    boolean isHashEntryReadable(Object key, @CachedLibrary("this") DynamicObjectLibrary objLib) {
        Object value = rawget(key, objLib);
        return value != LuaNil.SINGLETON;
    }

    @ExportMessage
    Object readHashValue(Object key, @CachedLibrary("this") DynamicObjectLibrary objLib){
        Object value = rawget(key, objLib);
        if (value == LuaNil.SINGLETON) {
            //throw UnknownIdentifierException.create(key.toString());
        }
        return value;
    }

    @ExportMessage
    @TruffleBoundary
    Object getHashEntriesIterator(@CachedLibrary("this") DynamicObjectLibrary objLib) {
        Object[] hashKeys = objLib.getKeyArray(this);
        List<Object> arrayKeyList = new ArrayList<>();
        for (int i = 0; i < this.arraySize; i++) {
            if (this.arrayPart[i] != null) {
                arrayKeyList.add((double) (i + 1));
            }
        }
        return new LuaTableIterator(arrayKeyList.toArray(), hashKeys);
    }

    // === 辅助方法 ===
    private static boolean isArrayIndex(double d) {
        return d > 0 && d <= Integer.MAX_VALUE && d == (int) d;
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
        List<Object> allKeys = new ArrayList<>(hashKeys.length + this.arraySize);
        allKeys.addAll(Arrays.asList(hashKeys));
        for (int i = 0; i < this.arraySize; i++) {
            if (this.arrayPart[i] != null) {
                allKeys.add((double) (i + 1));
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
                    return i + 1; // Lua 索引是从 1 开始的
                }
            }
            // b. 如果数组部分是空的，再从哈希部分找第一个
            Object[] propKeys = objLib.getKeyArray(this);
            if (propKeys.length > 0) {
                return propKeys[0];
            }
            // c. 如果都找不到，说明 table 是空的
            return LuaNil.SINGLETON;
        }

        // --- 状态 2: 当前 key 是一个数组索引 ---
        if (key instanceof Integer) {
            int longKey = (int) key;
            // 检查是否是一个有效的、我们正在处理的数组索引
            if (longKey >= 1 && longKey <= this.arrayPart.length) {
                int javaIndex = longKey - 1;

                // a. 从当前位置的【下一个】开始，继续在数组部分查找
                for (int i = javaIndex + 1; i < this.arrayPart.length; i++) {
                    if (this.arrayPart[i] != null) {
                        return i + 1;
                    }
                }
                // b. 数组部分已经遍历完毕，开始遍历哈希部分
                Object[] propKeys = objLib.getKeyArray(this);
                if (propKeys.length > 0) {
                    return propKeys[0];
                }
                // c. 哈希部分也是空的
                return LuaNil.SINGLETON;
            }
        }

        // --- 状态 3: 当前 key 在哈希部分 ---
        // 这个实现仍然是低效的，但在 @TruffleBoundary 中是可以接受的。
        // 一个生产级的实现需要一个更高效的方式来找到“下一个”哈希键。
        Object[] propKeys = objLib.getKeyArray(this);
        // 将数组转为 List 以使用 indexOf
        List<Object> propKeyList = Arrays.asList(propKeys);

        int index = propKeyList.indexOf(key);

        if (index != -1 && index + 1 < propKeyList.size()) {
            // 找到了，并且不是最后一个，返回下一个
            return propKeyList.get(index + 1);
        }

        // 哈希部分也遍历完了
        return LuaNil.SINGLETON;
    }


    // 检查 __metatable 字段
    @TruffleBoundary
    public Object getMetatableProtection() {
        if (this.getMetatable() == null) {
            return null;
        }
        // 使用非缓存库来读取 __metatable 字段
        return DynamicObjectLibrary.getUncached().getOrDefault(this.getMetatable(), "__metatable", null);
    }

}
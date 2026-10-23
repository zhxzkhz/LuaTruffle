package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode;
import com.zhhz.truffle.lua.nodes.util.LuaToMemberNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;
import com.zhhz.truffle.lua.runtime.LuaValue;

/**
 * 读取 table 成员的节点，例如 `t[k]`。
 *
 * <p>这个节点为整数键（数组访问）和非整数键（哈希访问）提供了特化的快速路径。
 */
@SuppressWarnings("deprecation")
@NodeInfo(shortName = "read_table")
@NodeChild(value = "tableNode", type = LuaExpressionNode.class)
@NodeChild(value = "keyNode", type = LuaExpressionNode.class)
public abstract class LuaReadTableNode extends LuaExpressionNode {

    static final int CACHE_LIMIT = 4;

    /**
     * 返回代表 "table" 的子表达式节点。
     * 这个抽象方法的【具体实现】将由 Truffle DSL 在生成的
     * `LuaReadTableNodeGen` 类中自动提供。
     */
    protected abstract LuaExpressionNode getTableNode();

    /**
     * 返回代表 "key" 的子表达式节点。
     */
    protected abstract LuaExpressionNode getKeyNode();

    // --- 快速路径特化 (保持不变，但增加慢路径调用) ---

    // 特化1: 整数键
    @Specialization(guards = "isArrayIndex(key)")
    protected Object readArray(LuaTable table, long key,
                                @CachedLibrary(limit = "CACHE_LIMIT") DynamicObjectLibrary objLib) {
        Object result = table.rawget(key, objLib);

        if (result == LuaNil.SINGLETON) {
            return readWithMetatable(table, key);
        }
        return result;
    }

    // 特化2: 字符串键 (这是一个很好的、常见情况的优化)
    @Specialization(replaces = "readArray", limit = "CACHE_LIMIT")
    protected Object readStringProperty(LuaTable table, String key,
                                        @CachedLibrary("table") DynamicObjectLibrary objLib) {
        Object result = table.rawget(key, objLib);

        // 【修改】如果直接没找到，就进入慢路径
        if (result == LuaNil.SINGLETON) {
            return readWithMetatable(table, key);
        }
        return result;
    }

    // 特化3: 通用对象键
    @Specialization(replaces = "readStringProperty", limit = "CACHE_LIMIT")
    protected Object readObjectProperty(LuaTable table, Object key,
                                        @CachedLibrary("table") DynamicObjectLibrary objLib) {
        Object result = table.rawget(key, objLib);

        // 【修改】如果直接没找到，就进入慢路径
        if (result == LuaNil.SINGLETON) {
            return readWithMetatable(table, key);
        }
        return result;
    }

    // userdata 对象处理方法：宿主对象既支持成员（字段/方法），也支持数组元素
    // （Lua 下标从 1 开始，映射到宿主的 0 基下标）。
    @Specialization(replaces = "readObjectProperty", guards = "isForeignObject(object)", limit = "CACHE_LIMIT")
    protected static Object doHostObject(TruffleObject object, Object key,
                                         @Bind Node node,
                                          @CachedLibrary("object") InteropLibrary objects,
                                          @Cached LuaToMemberNode asMember) {
        try {
            long arrayIndex = arrayIndex(key);
            if (arrayIndex != Long.MIN_VALUE && objects.hasArrayElements(object)) {
                if (arrayIndex >= 1) {
                    long hostIndex = arrayIndex - 1;
                    if (hostIndex < objects.getArraySize(object) && objects.isArrayElementReadable(object, hostIndex)) {
                        return LuaTypesUtil.normalizeHostValue(objects.readArrayElement(object, hostIndex));
                    }
                }
                return LuaNil.SINGLETON;
            }
            return LuaTypesUtil.normalizeHostValue(objects.readMember(object, asMember.execute(node, key)));
        } catch (UnsupportedMessageException | UnknownIdentifierException | com.oracle.truffle.api.interop.InvalidArrayIndexException e) {
            throw LuaException.undefinedProperty(node, key);
        }
    }

    /** 把 Lua 数字键转成宿主数组下标；非整数键返回 {@link Long#MIN_VALUE}。 */
    private static long arrayIndex(Object key) {
        if (key instanceof Long longKey) {
            return longKey;
        }
        if (key instanceof Double doubleKey) {
            double value = doubleKey;
            if (value == Math.rint(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
                return (long) value;
            }
        }
        return Long.MIN_VALUE;
    }

    // --- 回退路径 (Fallback) ---
    // 注意：@CachedLibrary 对 DynamicObjectLibrary 按 Shape 缓存，而每次 put 都会产生新 Shape，
    // 因此缓存条目会不断失效、耗尽 limit 后落到这里。必须先做一次 uncached rawget，
    // 否则已存在的键会被误判为 nil（触发元表逻辑）。
    @Fallback
    protected Object doNonTable(Object object, Object key) {
        if (object instanceof LuaTable luaTable) {
            Object result = luaTable.rawget(key, DynamicObjectLibrary.getUncached());
            if (result != LuaNil.SINGLETON) {
                return result;
            }
        }
        return readWithMetatable(this, object, key, null);
    }

    @TruffleBoundary
    public static Object readUncached(Node node, Object table, Object key) {
        return readUncached(node, table, key, null);
    }

    @TruffleBoundary
    public static Object readUncached(Node node, Object table, Object key, String indexSuffix) {
        if (table instanceof LuaTable luaTable) {
            Object result = luaTable.rawget(key, DynamicObjectLibrary.getUncached());
            if (result != LuaNil.SINGLETON) {
                return result;
            }
        } else if (table instanceof TruffleObject && !(table instanceof LuaValue)) {
            // 与 doHostObject 特化一致：宿主对象支持成员与（1 基映射的）数组元素。
            InteropLibrary interop = InteropLibrary.getUncached();
            try {
                long arrayIndex = arrayIndex(key);
                if (arrayIndex != Long.MIN_VALUE && interop.hasArrayElements(table)) {
                    if (arrayIndex >= 1) {
                        long hostIndex = arrayIndex - 1;
                        if (hostIndex < interop.getArraySize(table) && interop.isArrayElementReadable(table, hostIndex)) {
                            return LuaTypesUtil.normalizeHostValue(interop.readArrayElement(table, hostIndex));
                        }
                    }
                    return LuaNil.SINGLETON;
                }
                String member = com.zhhz.truffle.lua.nodes.util.LuaToMemberNodeGen.getUncached().execute(node, key);
                return LuaTypesUtil.normalizeHostValue(interop.readMember(table, member));
            } catch (UnsupportedMessageException | UnknownIdentifierException | com.oracle.truffle.api.interop.InvalidArrayIndexException e) {
                if (node != null) {
                    throw LuaException.undefinedProperty(node, key);
                }
            }
        }
        return readWithMetatable(node, table, key, indexSuffix);
    }


    // --- 慢路径：元表处理 ---

    /**
     * 【新增】处理元表查找的慢路径方法。
     * 使用 @TruffleBoundary 告诉 JIT 编译器不要尝试编译和内联这个方法。
     */
    @TruffleBoundary
    private Object readWithMetatable(Object table, Object key) {
        return readWithMetatable(this, table, key, null);
    }

    private static Object readWithMetatable(Node node, Object table, Object key, String indexSuffix) {
        LuaContext context = LuaContext.get(node);
        DynamicObjectLibrary objLib = DynamicObjectLibrary.getUncached();
        Object current = table;
        for (int loop = 0; loop < MAX_TAG_LOOP; loop++) {
            Object indexValue;
            if (current instanceof LuaTable luaTable) {
                Object result = luaTable.rawget(key, objLib);
                if (result != LuaNil.SINGLETON) {
                    return result;
                }
                LuaTable metatable = context.getEffectiveMetatable(current);
                if (metatable == null) {
                    return LuaNil.SINGLETON;
                }
                indexValue = metatable.rawget(LuaMetatables.__INDEX);
                if (indexValue == LuaNil.SINGLETON) {
                    return LuaNil.SINGLETON;
                }
            } else {
                LuaTable metatable = context.getEffectiveMetatable(current);
                if (metatable == null) {
                    return finishMissingIndex(node, table, indexSuffix);
                }
                indexValue = metatable.rawget(LuaMetatables.__INDEX);
                if (indexValue == LuaNil.SINGLETON) {
                    return finishMissingIndex(node, table, indexSuffix);
                }
            }

            if (indexValue instanceof LuaFunction) {
                try {
                    Object result = InteropLibrary.getUncached().execute(indexValue, current, key);
                    return LuaMultiValue.firstValue(result);
                } catch (LuaException luaError) {
                    // 标准 Lua 中元方法抛出的错误对象原样向上传播（不做包装），
                    // 例如向上抛出一个 table 作为错误对象时必须保持其身份。
                    throw luaError;
                } catch (Exception e) {
                    throw indexMetamethodError(node, e);
                }
            }

            // 既不是函数也不是表：继续以该值为目标重复（与 luaV_gettable 一致）。
            current = indexValue;
        }
        throw LuaException.create("gettable chain too long; possible loop", node);
    }


    /**
     * 当对象没有可用 {@code __index}（既无元表，元表也无 __index）时的收尾行为：
     * table 索引缺失键返回 nil；string 经字符串元表返回 nil；
     * 其余类型（nil/number/boolean/function 等）按 Lua 语义报 "attempt to index a X value"。
     */
    private static Object finishMissingIndex(Node node, Object table, String indexSuffix) {
        String typeName = LuaTypesUtil.getTypeName(table);
        if ("table".equals(typeName) || "string".equals(typeName)) {
            return LuaNil.SINGLETON;
        }
        String suffix = indexSuffix != null ? indexSuffix : indexNameSuffix(node);
        throw missingIndexError(node, typeName, suffix);
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static RuntimeException missingIndexError(Node node, String typeName, String suffix) {
        return LuaException.runtimeError("attempt to index a " + typeName + " value" + suffix, node);
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static RuntimeException indexMetamethodError(Node node, Exception cause) {
        return LuaException.create("error in metamethod '__index': " + LuaException.messageOf(cause), node);
    }

    /** 依据被索引表达式的来源，构造类似 {@code (global 'X')} 的名字后缀。 */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static String indexNameSuffix(Node node) {
        if (!(node instanceof LuaReadTableNode readNode)) {
            return "";
        }
        LuaExpressionNode tableNode = readNode.getTableNode();
        if (tableNode == null) {
            return "";
        }
        TruffleString name = tableNode.getUnresolvedVariableName();
        if (name == null) {
            return "";
        }
        String what;
        if (tableNode instanceof LuaReadGlobalVariableNode) {
            what = "global";
        } else if (tableNode instanceof LuaReadLocalVariableNode) {
            what = "local";
        } else if (tableNode instanceof LuaReadUpvalueNode) {
            what = "upvalue";
        } else if (tableNode instanceof LuaReadTableNode) {
            what = "field";
        } else {
            return "";
        }
        return " (" + what + " '" + name.toJavaStringUncached() + "')";
    }

    /** 对应 Lua 的 MAXTAGLOOP：__index 链的最大迭代次数。 */
    private static final int MAX_TAG_LOOP = 2000;

    // --- 卫语句 ---

    protected static boolean isArrayIndex(long l) {
        // Lua 数组索引从 1 开始
        return l >= 1 && l <= Integer.MAX_VALUE;
    }
    // 你的旧卫语句是 double，最好特化 long 和 double
    protected static boolean isArrayIndex(double d) {
        return d >= 1 && d <= Integer.MAX_VALUE && d == (long) d;
    }

    protected static boolean isForeignObject(Object value) {
        return !(value instanceof LuaValue);
    }


    @Override
    public TruffleString getUnresolvedVariableName() {
        return getKeyNode().getUnresolvedVariableName();
    }

    /**
     * 字段访问的运行时错误后缀：常量字符串键为 {@code (field 'x')}，
     * 动态键（如 {@code a[b]}）为 {@code (field '?')}，与标准 Lua 的 varinfo 一致。
     */
    private String errorSuffix;

    @Override
    public String getErrorSuffix() {
        String suffix = errorSuffix;
        if (suffix == null) {
            suffix = computeErrorSuffix();
            errorSuffix = suffix;
        }
        return suffix;
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private String computeErrorSuffix() {
        LuaExpressionNode keyNode = getKeyNode();
        if (keyNode instanceof LuaStringLiteralNode literal) {
            TruffleString name = literal.getUnresolvedVariableName();
            if (name != null) {
                return " (field '" + name.toJavaStringUncached() + "')";
            }
        }
        return " (field '?')";
    }
}

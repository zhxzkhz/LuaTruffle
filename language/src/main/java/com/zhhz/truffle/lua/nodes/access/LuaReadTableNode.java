package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 读取 table 成员的节点，例如 `t[k]`。
 *
 * <p>这个节点为整数键（数组访问）和非整数键（哈希访问）提供了特化的快速路径。
 */
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
    protected Object readArray(LuaTable table, long key) { // 使用 long 更精确
        Object result = table.arrayRawGet((int) key);

        // 【修改】如果直接没找到，就进入慢路径
        if (result == LuaNil.SINGLETON) {
            return readWithMetatable(table, key);
        }
        return result;
    }

    // 特化2: 字符串键 (这是一个很好的、常见情况的优化)
    @Specialization(replaces = "readArray", limit = "CACHE_LIMIT")
    protected Object readStringProperty(LuaTable table, String key,
                                        @CachedLibrary("table") DynamicObjectLibrary objLib) {
        Object result = objLib.getOrDefault(table, key, LuaNil.SINGLETON);

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
        Object result = objLib.getOrDefault(table, key, LuaNil.SINGLETON);

        // 【修改】如果直接没找到，就进入慢路径
        if (result == LuaNil.SINGLETON) {
            return readWithMetatable(table, key);
        }
        return result;
    }

    // --- 慢路径：元表处理 ---

    /**
     * 【新增】处理元表查找的慢路径方法。
     * 使用 @TruffleBoundary 告诉 JIT 编译器不要尝试编译和内联这个方法。
     */
    @TruffleBoundary
    private Object readWithMetatable(Object table, Object key) {
        LuaTable metatable = getContext().getEffectiveMetatable(table);

        if (metatable == null) {
            return LuaNil.SINGLETON; // 没有元表，查找结束
        }

        // 我们需要一个非缓存的库来安全地操作元表
        DynamicObjectLibrary objLib = DynamicObjectLibrary.getUncached();
        Object indexValue = objLib.getOrDefault(metatable, LuaMetatables.__INDEX, LuaNil.SINGLETON);

        if (indexValue == LuaNil.SINGLETON) {
            return LuaNil.SINGLETON; // 元表中没有 __index，查找结束
        }

        // --- 处理 __index 的两种情况 ---

        if (indexValue instanceof LuaTable) {
            // 情况 A: __index 是一个表，递归地在这个表中查找
            // 为了防止无限循环，我们限制查找深度
            return findInIndexTableChain((LuaTable) indexValue, key);
        }

        if (indexValue instanceof LuaFunction) {
            // 情况 B: __index 是一个函数，调用它
            try {
                // 使用非缓存的 InteropLibrary 来执行调用
                Object result = InteropLibrary.getUncached().execute(indexValue, table, key);
                // 【重要】Lua 的元方法可以返回多值，我们需要处理这种情况
                if (result instanceof Object[] objects) {
                    return objects[0]; // 只取第一个值
                }
                return result;
            } catch (Exception e) {
                // 包装在 __index 方法中发生的错误
                throw LuaException.create("error in __index metamethod\n" +  e.getMessage(),this);
            }
        }

        // 如果 __index 既不是 table 也不是 function，忽略它
        return LuaNil.SINGLETON;
    }

    /**
     * 辅助方法，用于在 __index 表链中进行查找。
     */
    @TruffleBoundary
    private Object findInIndexTableChain(LuaTable currentTable, Object key) {
        // 使用一个循环来处理 table -> metatable -> __index -> table 的链条
        // 设置一个最大深度以防止无限循环 (e.g., t.meta.__index = t.meta)
        for (int i = 0; i < 100; i++) {
            // 检查当前 table 是否有这个 key
            Object result = DynamicObjectLibrary.getUncached().getOrDefault(currentTable, key, LuaNil.SINGLETON);
            if (result != LuaNil.SINGLETON) {
                return result; // 找到了！
            }

            // 没找到，继续查找链条
            LuaTable metatable = currentTable.getMetatable();
            if (metatable == null) {
                return LuaNil.SINGLETON; // 链条中断
            }

            Object nextIndex = DynamicObjectLibrary.getUncached().getOrDefault(metatable, "__index", LuaNil.SINGLETON);
            if (nextIndex instanceof LuaTable) {
                // 链条的下一环是一个 table，继续循环
                currentTable = (LuaTable) nextIndex;
            } else {
                // 链条的下一环不是 table（可能是函数或 nil），查找结束
                return LuaNil.SINGLETON;
            }
        }
        // 超过最大深度，抛出错误
        throw LuaException.create("metatable __index chain too deep",this);
    }

    // --- 回退路径 (Fallback) ---
    @Fallback
    protected Object doNonTable(Object object, Object key) {
        return readWithMetatable(object, key);
        //throw LuaException.create("attempt to index a " + getTypeName(table) + " value " + String.format(" (global '%s')", getUnresolvedVariableName()),this);
    }

    // --- 卫语句 ---

    protected static boolean isArrayIndex(long l) {
        // Lua 数组索引从 1 开始
        return l >= 1;
    }
    // 你的旧卫语句是 double，最好特化 long 和 double
    protected static boolean isArrayIndex(double d) {
        return d >= 1 && d == (long) d;
    }


    @Override
    public TruffleString getUnresolvedVariableName() {
        return getKeyNode().getUnresolvedVariableName();
    }
}
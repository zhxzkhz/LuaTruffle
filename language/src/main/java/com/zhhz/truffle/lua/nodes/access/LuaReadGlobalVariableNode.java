package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

/**
 * 读取一个全局变量的值。
 * 它在执行时，从当前的 LuaContext 中获取全局表，并从中读取值。
 */
public abstract class LuaReadGlobalVariableNode extends LuaExpressionNode {

    private final TruffleString name;

    public LuaReadGlobalVariableNode(TruffleString name) {
        this.name = name;
    }

    protected TruffleString getName() {
        return name;
    }

    /**
     * 这是此节点的唯一特化，也是它的快速路径。
     *
     * @param globals   通过 @Cached 缓存的对全局表的直接引用。这假设在单个上下文中全局表是不变的。
     * @param tableLib  一个缓存的 DynamicObjectLibrary 实例，用于对全局表进行高性能的属性读取。
     * @return The value of the global variable.
     */
    @Specialization
    protected Object readGlobal(
            @Cached(value = "getContext().getGlobals()", allowUncached = true) LuaTable globals,
            @CachedLibrary(limit = "3") DynamicObjectLibrary tableLib
    ) {
        // 【核心逻辑】使用缓存的库，从缓存的全局表中，读取变量
        // 我们委托给 LuaTable 的高性能 read 方法
        var result = globals.rawget(getName(), tableLib);
        if (result == null){
            return LuaNil.SINGLETON;
        }
        return result;
    }

    // 【关键】实现描述接口
    @Override
    public TruffleString getUnresolvedVariableName() {
        return this.name;
    }
}
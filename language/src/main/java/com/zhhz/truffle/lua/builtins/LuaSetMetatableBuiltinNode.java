package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;

/**
 * 实现 `setmetatable(table, metatable)` 内置函数。
 */
@NodeInfo(shortName = "setmetatable")
public abstract class LuaSetMetatableBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    public Object doSet(Object[] arguments) {
        // 1. 【参数数量检查】
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #1 to 'setmetatable' (table expected, got no value)",this);
        }

        Object tableArg = arguments[0];
        Object metatableArg = arguments[1];

        // 2. 【第一个参数类型检查】
        //    通常只能为 table 设置元表。
        //    (完整的实现也需要处理 userdata)
        if (!(tableArg instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'setmetatable' (table expected, got " + getTypeName(tableArg) + ")",this);
        }

        // 【核心修改】检查保护时，也应该通过 Context
        LuaTable currentMetatable = getContext().getEffectiveMetatable(table);
        if (currentMetatable != null) {
            // 从有效的元表中获取保护字段
            Object protection = currentMetatable.getMetatableProtection();
            if (protection != null) {
                throw LuaException.create("cannot change a protected metatable",this);
            }
        }

        // 4. 【第二个参数类型检查】
        LuaTable newMetatable;
        if (metatableArg == null || metatableArg == LuaNil.SINGLETON) {
            // 如果第二个参数是 nil，表示移除元表
            newMetatable = null;
        } else if (metatableArg instanceof LuaTable) {
            newMetatable = (LuaTable) metatableArg;
        } else {
            // 如果是其他任何类型，都报错
            throw LuaException.create("bad argument #2 to 'setmetatable' (table or nil expected)",this);
        }

        // 5. 【执行设置】
        table.setMetatable(newMetatable);

        // 6. 【返回第一个参数】
        return table;
    }


}
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName;

/**
 * 实现 `setmetatable(table, metatable)` 内置函数。
 */
@GenerateInline(false)
@NodeInfo(shortName = "setmetatable")
public abstract class LuaSetMetatableBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    public Object doSet(Object[] arguments) {
        Object tableArg = arguments.length > 0 ? arguments[0] : null;

        // 1. 【第一个参数类型检查】（与 luaL_checktype(L, 1, LUA_TTABLE) 一致）
        if (!(tableArg instanceof LuaTable table)) {
            String got = arguments.length == 0 ? "no value" : getArgumentTypeName(tableArg);
            throw LuaTypesUtil.argumentError("setmetatable", 1, "table expected, got " + got);
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

        // 2. 【第二个参数类型检查】缺省（LUA_TNONE）与显式 nil 不同：
        //    缺省不算 nil，报 "nil or table expected"。
        if (arguments.length < 2) {
            throw LuaTypesUtil.argumentError("setmetatable", 2, "nil or table expected");
        }
        Object metatableArg = arguments[1];
        LuaTable newMetatable;
        if (metatableArg == null || metatableArg == LuaNil.SINGLETON) {
            // 如果第二个参数是 nil，表示移除元表
            newMetatable = null;
        } else if (metatableArg instanceof LuaTable mt) {
            newMetatable = mt;
        } else {
            // 如果是其他任何类型，都报错
            throw LuaTypesUtil.argumentError("setmetatable", 2, "nil or table expected");
        }

        // 3. 【执行设置】
        table.setMetatable(newMetatable);

        // 4. 【返回第一个参数】
        return table;
    }


}
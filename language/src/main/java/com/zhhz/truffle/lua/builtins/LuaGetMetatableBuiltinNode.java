package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.util.Objects;

/**
 * 实现 `getmetatable(table)` 内置函数。
 */
@NodeInfo(shortName = "getmetatable")
public abstract class LuaGetMetatableBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doGet(Object[] arguments) {

        if (arguments.length == 0){
            throw LuaException.create("bad argument #1 to 'getmetatable' (value expected)", this);
        }

        if (arguments[0] == LuaNil.SINGLETON) {
            return LuaNil.SINGLETON; // getmetatable(nil) -> nil
        }

        Object value = arguments[0];

        // 【核心修改】从上下文中获取这个值的有效元表
        LuaTable metatable = getContext().getEffectiveMetatable(value);

        if (metatable == null) {
            return LuaNil.SINGLETON; // 没有找到元表
        }

        // 【处理 __metatable 保护】逻辑保持不变
        Object protection = metatable.getMetatableProtection(); // 假设 LuaTable 有这个方法
        // 如果 __metatable 字段存在，返回该字段的值
        // 否则，返回真正的元表
        return Objects.requireNonNullElse(protection, metatable);
    }
}
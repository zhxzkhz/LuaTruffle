package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaTable;

@NodeInfo(shortName = "rawget")
public abstract class LuaRawGetBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doRawGet(Object[] arguments,
                           @CachedLibrary(limit = "3") DynamicObjectLibrary objLib) {
        // 1. 参数校验
        if (arguments.length < 2) {
            throw LuaException.create("bad argument #1 to 'rawget' (table expected, got no value)",this);
        }
        if (!(arguments[0] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'rawget' (table expected)",this);
        }

        Object key = arguments[1];

        // 2. 【核心】直接调用 LuaTable 的 rawget 方法
        return table.rawget(key,objLib);
    }
}
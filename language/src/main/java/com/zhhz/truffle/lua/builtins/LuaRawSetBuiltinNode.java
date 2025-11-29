package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

@NodeInfo(shortName = "rawset")
public abstract class LuaRawSetBuiltinNode extends LuaBuiltinNode {

    @Specialization
    public Object doRawSet(Object[] arguments) {
        // 1. 参数校验
        if (arguments.length < 3) {
            // ... (需要更详细的错误信息)
            throw LuaException.create("wrong number of arguments to 'rawset'",this);
        }
        if (!(arguments[0] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #1 to 'rawset' (table expected)",this);
        }

        Object key = arguments[1];
        Object value = arguments[2];

        // Lua 中不允许 nil 作为 key
        if (key == null || key == LuaNil.SINGLETON) {
            throw LuaException.create("table index is nil",this);
        }

        // 2. 【核心】直接调用 LuaTable 的 rawset 方法
        //    (注意：我们的 rawset 返回 void，但 Lua 的 rawset 返回 table)
        table.rawset(key, value);

        // 3. 【返回 table】
        return table;
    }
}

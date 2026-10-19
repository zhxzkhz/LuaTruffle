package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;
@GenerateInline(false)

@NodeInfo(shortName = "table.pack")
public abstract class LuaTablePackBuiltinNode extends LuaBuiltinNode {

    @Specialization
    protected LuaTable doPack(Object[] arguments) {
        LuaTable table = new LuaTable(getContext().getTableShape());
        for (int index = 0; index < arguments.length; index++) {
            table.arrayRawSet(index + 1, arguments[index] == null ? LuaNil.SINGLETON : arguments[index]);
        }
        table.rawset("n", (long) arguments.length);
        return table;
    }
}

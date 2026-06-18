// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins.pack;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaPathSearcherNode#doSearch}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaPathSearcherNode.class)
@SuppressWarnings("javadoc")
public final class LuaPathSearcherNodeGen extends LuaPathSearcherNode {

    private LuaPathSearcherNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doSearch(arg0Value);
    }

    @NeverDefault
    public static LuaPathSearcherNode create() {
        return new LuaPathSearcherNodeGen();
    }

}

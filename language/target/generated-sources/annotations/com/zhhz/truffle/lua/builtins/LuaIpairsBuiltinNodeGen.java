// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaIpairsBuiltinNode#doIPairs}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaIpairsBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaIpairsBuiltinNodeGen extends LuaIpairsBuiltinNode {

    private LuaIpairsBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doIPairs(arg0Value);
    }

    @NeverDefault
    public static LuaIpairsBuiltinNode create() {
        return new LuaIpairsBuiltinNodeGen();
    }

}

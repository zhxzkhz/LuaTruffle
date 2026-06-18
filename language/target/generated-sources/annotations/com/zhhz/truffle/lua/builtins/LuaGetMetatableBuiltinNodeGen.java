// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaGetMetatableBuiltinNode#doGet}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaGetMetatableBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaGetMetatableBuiltinNodeGen extends LuaGetMetatableBuiltinNode {

    private LuaGetMetatableBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doGet(arg0Value);
    }

    @NeverDefault
    public static LuaGetMetatableBuiltinNode create() {
        return new LuaGetMetatableBuiltinNodeGen();
    }

}

// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaRawSetBuiltinNode#doRawSet}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaRawSetBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaRawSetBuiltinNodeGen extends LuaRawSetBuiltinNode {

    private LuaRawSetBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doRawSet(arg0Value);
    }

    @NeverDefault
    public static LuaRawSetBuiltinNode create() {
        return new LuaRawSetBuiltinNodeGen();
    }

}

// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaErrorBuiltinNode#doError}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaErrorBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaErrorBuiltinNodeGen extends LuaErrorBuiltinNode {

    private LuaErrorBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doError(arg0Value);
    }

    @NeverDefault
    public static LuaErrorBuiltinNode create() {
        return new LuaErrorBuiltinNodeGen();
    }

}

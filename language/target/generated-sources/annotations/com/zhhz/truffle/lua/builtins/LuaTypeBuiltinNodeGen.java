// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaTypeBuiltinNode#doType}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaTypeBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaTypeBuiltinNodeGen extends LuaTypeBuiltinNode {

    private LuaTypeBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doType(arg0Value);
    }

    @NeverDefault
    public static LuaTypeBuiltinNode create() {
        return new LuaTypeBuiltinNodeGen();
    }

}

// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaSetMetatableBuiltinNode#doSet}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaSetMetatableBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaSetMetatableBuiltinNodeGen extends LuaSetMetatableBuiltinNode {

    private LuaSetMetatableBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doSet(arg0Value);
    }

    @NeverDefault
    public static LuaSetMetatableBuiltinNode create() {
        return new LuaSetMetatableBuiltinNodeGen();
    }

}

// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaCollectGarbageBuiltinNode#doGarbage}
 *     Activation probability: 1.00000
 *     With/without class size: 16/0 bytes
 * </pre> */
@GeneratedBy(LuaCollectGarbageBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaCollectGarbageBuiltinNodeGen extends LuaCollectGarbageBuiltinNode {

    private LuaCollectGarbageBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        return doGarbage(arg0Value);
    }

    @NeverDefault
    public static LuaCollectGarbageBuiltinNode create() {
        return new LuaCollectGarbageBuiltinNodeGen();
    }

}

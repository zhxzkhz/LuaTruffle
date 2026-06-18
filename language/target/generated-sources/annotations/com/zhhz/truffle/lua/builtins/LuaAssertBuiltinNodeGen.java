// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNodeGen;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaAssertBuiltinNode#doAssert}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaAssertBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaAssertBuiltinNodeGen extends LuaAssertBuiltinNode {

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaAssertBuiltinNode#doAssert}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaAssertBuiltinNode#doAssert}
     *   Parameter: {@link LuaCoerceToStringNode} toStringNode</pre> */
    @Child private LuaCoerceToStringNode toStringNode_;

    private LuaAssertBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaAssertBuiltinNode.doAssert(Object[], LuaCoerceToStringNode)] */) {
            {
                LuaCoerceToStringNode toStringNode__ = this.toStringNode_;
                if (toStringNode__ != null) {
                    return doAssert(arg0Value, toStringNode__);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        LuaCoerceToStringNode toStringNode__ = this.insert((LuaCoerceToStringNodeGen.create()));
        Objects.requireNonNull(toStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.toStringNode_ = toStringNode__;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaAssertBuiltinNode.doAssert(Object[], LuaCoerceToStringNode)] */;
        this.state_0_ = state_0;
        return doAssert(arg0Value, toStringNode__);
    }

    @NeverDefault
    public static LuaAssertBuiltinNode create() {
        return new LuaAssertBuiltinNodeGen();
    }

}

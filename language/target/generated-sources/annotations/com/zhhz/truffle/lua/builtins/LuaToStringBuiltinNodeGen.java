// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNodeGen;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaToStringBuiltinNode#toString}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaToStringBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaToStringBuiltinNodeGen extends LuaToStringBuiltinNode {

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaToStringBuiltinNode#toString}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaToStringBuiltinNode#toString}
     *   Parameter: {@link LuaCoerceToStringNode} toStringNode</pre> */
    @Child private LuaCoerceToStringNode toStringNode_;

    private LuaToStringBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaToStringBuiltinNode.toString(Object[], LuaCoerceToStringNode)] */) {
            {
                LuaCoerceToStringNode toStringNode__ = this.toStringNode_;
                if (toStringNode__ != null) {
                    return toString(arg0Value, toStringNode__);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private TruffleString executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        LuaCoerceToStringNode toStringNode__ = this.insert((LuaCoerceToStringNodeGen.create()));
        Objects.requireNonNull(toStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.toStringNode_ = toStringNode__;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaToStringBuiltinNode.toString(Object[], LuaCoerceToStringNode)] */;
        this.state_0_ = state_0;
        return toString(arg0Value, toStringNode__);
    }

    @NeverDefault
    public static LuaToStringBuiltinNode create() {
        return new LuaToStringBuiltinNodeGen();
    }

}

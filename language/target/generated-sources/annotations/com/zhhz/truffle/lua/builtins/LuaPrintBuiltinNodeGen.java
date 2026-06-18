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
 *   Specialization {@link LuaPrintBuiltinNode#printWithoutArgs}
 *     Activation probability: 0.65000
 *     With/without class size: 11/0 bytes
 *   Specialization {@link LuaPrintBuiltinNode#printWithArgs}
 *     Activation probability: 0.35000
 *     With/without class size: 11/4 bytes
 * </pre> */
@GeneratedBy(LuaPrintBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaPrintBuiltinNodeGen extends LuaPrintBuiltinNode {

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaPrintBuiltinNode#printWithoutArgs}
     *   1: SpecializationActive {@link LuaPrintBuiltinNode#printWithArgs}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaPrintBuiltinNode#printWithArgs}
     *   Parameter: {@link LuaCoerceToStringNode} toStringNode</pre> */
    @Child private LuaCoerceToStringNode printWithArgs_toStringNode_;

    private LuaPrintBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaPrintBuiltinNode.printWithoutArgs(Object[])] || SpecializationActive[LuaPrintBuiltinNode.printWithArgs(Object[], LuaCoerceToStringNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaPrintBuiltinNode.printWithoutArgs(Object[])] */) {
                if ((arg0Value.length == 0)) {
                    return printWithoutArgs(arg0Value);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaPrintBuiltinNode.printWithArgs(Object[], LuaCoerceToStringNode)] */) {
                {
                    LuaCoerceToStringNode toStringNode__ = this.printWithArgs_toStringNode_;
                    if (toStringNode__ != null) {
                        return printWithArgs(arg0Value, toStringNode__);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaPrintBuiltinNode.printWithArgs(Object[], LuaCoerceToStringNode)] */) {
            if ((arg0Value.length == 0)) {
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaPrintBuiltinNode.printWithoutArgs(Object[])] */;
                this.state_0_ = state_0;
                return printWithoutArgs(arg0Value);
            }
        }
        LuaCoerceToStringNode toStringNode__ = this.insert((LuaCoerceToStringNodeGen.create()));
        Objects.requireNonNull(toStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.printWithArgs_toStringNode_ = toStringNode__;
        state_0 = state_0 & 0xfffffffe /* remove SpecializationActive[LuaPrintBuiltinNode.printWithoutArgs(Object[])] */;
        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaPrintBuiltinNode.printWithArgs(Object[], LuaCoerceToStringNode)] */;
        this.state_0_ = state_0;
        return printWithArgs(arg0Value, toStringNode__);
    }

    @NeverDefault
    public static LuaPrintBuiltinNode create() {
        return new LuaPrintBuiltinNodeGen();
    }

}

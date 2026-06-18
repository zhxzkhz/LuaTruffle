// CheckStyle: start generated
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import java.lang.invoke.VarHandle;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaToNumberBuiltinNode#doToNumber}
 *     Activation probability: 1.00000
 *     With/without class size: 24/4 bytes
 * </pre> */
@GeneratedBy(LuaToNumberBuiltinNode.class)
@SuppressWarnings("javadoc")
public final class LuaToNumberBuiltinNodeGen extends LuaToNumberBuiltinNode {

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaToNumberBuiltinNode#doToNumber}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private ToNumberData toNumber_cache;

    private LuaToNumberBuiltinNodeGen() {
    }

    @Override
    public Object execute(Object[] arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaToNumberBuiltinNode.doToNumber(Object[], LuaCoerceToNumberNode)] */) {
            ToNumberData s0_ = this.toNumber_cache;
            if (s0_ != null) {
                return doToNumber(arg0Value, s0_.toNumberNode_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object[] arg0Value) {
        int state_0 = this.state_0_;
        ToNumberData s0_ = this.insert(new ToNumberData());
        s0_.toNumberNode_ = s0_.insert((LuaCoerceToNumberNode.create()));
        VarHandle.storeStoreFence();
        this.toNumber_cache = s0_;
        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaToNumberBuiltinNode.doToNumber(Object[], LuaCoerceToNumberNode)] */;
        this.state_0_ = state_0;
        return doToNumber(arg0Value, s0_.toNumberNode_);
    }

    @NeverDefault
    public static LuaToNumberBuiltinNode create() {
        return new LuaToNumberBuiltinNodeGen();
    }

    @GeneratedBy(LuaToNumberBuiltinNode.class)
    @DenyReplace
    private static final class ToNumberData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaToNumberBuiltinNode#doToNumber}
         *   Parameter: {@link LuaCoerceToNumberNode} toNumberNode</pre> */
        @Child LuaCoerceToNumberNode toNumberNode_;

        ToNumberData() {
        }

    }
}

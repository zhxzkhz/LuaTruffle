// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import java.lang.invoke.VarHandle;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaUnaryMinusNode#negateNumber(long)}
 *     Activation probability: 0.48333
 *     With/without class size: 9/0 bytes
 *   Specialization {@link LuaUnaryMinusNode#negateNumber(double)}
 *     Activation probability: 0.33333
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaUnaryMinusNode#negateGeneric}
 *     Activation probability: 0.18333
 *     With/without class size: 8/8 bytes
 * </pre> */
@GeneratedBy(LuaUnaryMinusNode.class)
@SuppressWarnings("javadoc")
public final class LuaUnaryMinusNodeGen extends LuaUnaryMinusNode {

    @Child private LuaExpressionNode operandNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaUnaryMinusNode#negateNumber(long)}
     *   1: SpecializationActive {@link LuaUnaryMinusNode#negateGeneric}
     *   2: SpecializationActive {@link LuaUnaryMinusNode#negateNumber(double)}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private NegateGenericData negateGeneric_cache;

    private LuaUnaryMinusNodeGen(LuaExpressionNode operandNode) {
        this.operandNode_ = operandNode;
    }

    @Override
    protected LuaExpressionNode getOperandNode() {
        return this.operandNode_;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b110) == 0 /* only-active SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] && SpecializationActive[LuaUnaryMinusNode.negateNumber(double)] && SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_long0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        long operandNodeValue_;
        try {
            operandNodeValue_ = this.operandNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] */;
        return negateNumber(operandNodeValue_);
    }

    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object operandNodeValue_ = this.operandNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] || SpecializationActive[LuaUnaryMinusNode.negateNumber(double)] || SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] */ && operandNodeValue_ instanceof Long) {
                long operandNodeValue__ = (long) operandNodeValue_;
                return negateNumber(operandNodeValue__);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaUnaryMinusNode.negateNumber(double)] */ && operandNodeValue_ instanceof Double) {
                double operandNodeValue__ = (double) operandNodeValue_;
                return negateNumber(operandNodeValue__);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
                NegateGenericData s2_ = this.negateGeneric_cache;
                if (s2_ != null) {
                    return negateGeneric(operandNodeValue_, s2_.coerce_, s2_.metatableNode_);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(operandNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize(Object operandNodeValue) {
        int state_0 = this.state_0_;
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */ && operandNodeValue instanceof Long) {
            long operandNodeValue_ = (long) operandNodeValue;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaUnaryMinusNode.negateNumber(long)] */;
            this.state_0_ = state_0;
            return negateNumber(operandNodeValue_);
        }
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */ && operandNodeValue instanceof Double) {
            double operandNodeValue_ = (double) operandNodeValue;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaUnaryMinusNode.negateNumber(double)] */;
            this.state_0_ = state_0;
            return negateNumber(operandNodeValue_);
        }
        NegateGenericData s2_ = this.insert(new NegateGenericData());
        s2_.coerce_ = s2_.insert((LuaCoerceToNumberNode.create()));
        s2_.metatableNode_ = s2_.insert((LuaMetatableNode.create()));
        VarHandle.storeStoreFence();
        this.negateGeneric_cache = s2_;
        state_0 = state_0 & 0xfffffffa /* remove SpecializationActive[LuaUnaryMinusNode.negateNumber(long)], SpecializationActive[LuaUnaryMinusNode.negateNumber(double)] */;
        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaUnaryMinusNode.negateGeneric(Object, LuaCoerceToNumberNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return negateGeneric(operandNodeValue, s2_.coerce_, s2_.metatableNode_);
    }

    @NeverDefault
    public static LuaUnaryMinusNode create(LuaExpressionNode operandNode) {
        return new LuaUnaryMinusNodeGen(operandNode);
    }

    @GeneratedBy(LuaUnaryMinusNode.class)
    @DenyReplace
    private static final class NegateGenericData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaUnaryMinusNode#negateGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerce</pre> */
        @Child LuaCoerceToNumberNode coerce_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaUnaryMinusNode#negateGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        NegateGenericData() {
        }

    }
}

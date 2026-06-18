// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.binary;

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
 *   Specialization {@link LuaPowerNode#doDouble}
 *     Activation probability: 0.48333
 *     With/without class size: 9/0 bytes
 *   Specialization {@link LuaPowerNode#doLong}
 *     Activation probability: 0.33333
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaPowerNode#doGeneric}
 *     Activation probability: 0.18333
 *     With/without class size: 9/12 bytes
 * </pre> */
@GeneratedBy(LuaPowerNode.class)
@SuppressWarnings("javadoc")
public final class LuaPowerNodeGen extends LuaPowerNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaPowerNode#doDouble}
     *   1: SpecializationActive {@link LuaPowerNode#doLong}
     *   2: SpecializationExcluded {@link LuaPowerNode#doLong}
     *   3: SpecializationActive {@link LuaPowerNode#doGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private FallbackData fallback_cache;

    private LuaPowerNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaPowerNode.doDouble(double, double)] */) && leftNodeValue instanceof Double && rightNodeValue instanceof Double) {
            return false;
        }
        if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaPowerNode.doLong(long, long)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Long) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b1001) == 0 /* only-active SpecializationActive[LuaPowerNode.doLong(long, long)] */ && ((state_0 & 0b1011) != 0  /* is-not SpecializationActive[LuaPowerNode.doDouble(double, double)] && SpecializationActive[LuaPowerNode.doLong(long, long)] && SpecializationActive[LuaPowerNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_long_long0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        long leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return executeAndSpecialize(ex.getResult(), rightNodeValue);
        }
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b10) != 0 /* is SpecializationActive[LuaPowerNode.doLong(long, long)] */;
        try {
            return LuaPowerNode.doLong(leftNodeValue_, rightNodeValue_);
        } catch (ArithmeticException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            state_0 = this.state_0_;
            state_0 = state_0 & 0xfffffffd /* remove SpecializationActive[LuaPowerNode.doLong(long, long)] */;
            state_0 = state_0 | 0b100 /* add SpecializationExcluded  */;
            this.state_0_ = state_0;
            return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
        }
    }

    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if ((state_0 & 0b1011) != 0 /* is SpecializationActive[LuaPowerNode.doDouble(double, double)] || SpecializationActive[LuaPowerNode.doLong(long, long)] || SpecializationActive[LuaPowerNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaPowerNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaPowerNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaPowerNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    try {
                        return LuaPowerNode.doLong(leftNodeValue__, rightNodeValue__);
                    } catch (ArithmeticException ex) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        state_0 = this.state_0_;
                        state_0 = state_0 & 0xfffffffd /* remove SpecializationActive[LuaPowerNode.doLong(long, long)] */;
                        state_0 = state_0 | 0b100 /* add SpecializationExcluded  */;
                        this.state_0_ = state_0;
                        return executeAndSpecialize(leftNodeValue__, rightNodeValue__);
                    }
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaPowerNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
                FallbackData s2_ = this.fallback_cache;
                if (s2_ != null) {
                    if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                        return doGeneric(leftNodeValue_, rightNodeValue_, s2_.coerceLeft_, s2_.coerceRight_, s2_.metatableNode_);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize(Object leftNodeValue, Object rightNodeValue) {
        int state_0 = this.state_0_;
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Double) {
                double rightNodeValue_ = (double) rightNodeValue;
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaPowerNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaPowerNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b100)) == 0 /* is-not SpecializationExcluded  */ && leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaPowerNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                try {
                    return LuaPowerNode.doLong(leftNodeValue_, rightNodeValue_);
                } catch (ArithmeticException ex) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    state_0 = this.state_0_;
                    state_0 = state_0 & 0xfffffffd /* remove SpecializationActive[LuaPowerNode.doLong(long, long)] */;
                    state_0 = state_0 | 0b100 /* add SpecializationExcluded  */;
                    this.state_0_ = state_0;
                    return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
                }
            }
        }
        FallbackData s2_ = this.insert(new FallbackData());
        s2_.coerceLeft_ = s2_.insert((LuaCoerceToNumberNode.create()));
        s2_.coerceRight_ = s2_.insert((LuaCoerceToNumberNode.create()));
        s2_.metatableNode_ = s2_.insert((LuaMetatableNode.create()));
        VarHandle.storeStoreFence();
        this.fallback_cache = s2_;
        state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaPowerNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return doGeneric(leftNodeValue, rightNodeValue, s2_.coerceLeft_, s2_.coerceRight_, s2_.metatableNode_);
    }

    @NeverDefault
    public static LuaPowerNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaPowerNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaPowerNode.class)
    @DenyReplace
    private static final class FallbackData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaPowerNode#doGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceLeft</pre> */
        @Child LuaCoerceToNumberNode coerceLeft_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaPowerNode#doGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceRight</pre> */
        @Child LuaCoerceToNumberNode coerceRight_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaPowerNode#doGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        FallbackData() {
        }

    }
}

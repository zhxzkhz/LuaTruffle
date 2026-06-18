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
 *   Specialization {@link LuaMulNode#doDouble(double, double)}
 *     Activation probability: 0.32000
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaMulNode#doLong}
 *     Activation probability: 0.26000
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaMulNode#doDouble(long, double)}
 *     Activation probability: 0.20000
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaMulNode#doDouble(double, long)}
 *     Activation probability: 0.14000
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaMulNode#doGeneric}
 *     Activation probability: 0.08000
 *     With/without class size: 6/12 bytes
 * </pre> */
@GeneratedBy(LuaMulNode.class)
@SuppressWarnings("javadoc")
public final class LuaMulNodeGen extends LuaMulNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaMulNode#doDouble(double, double)}
     *   1: SpecializationActive {@link LuaMulNode#doLong}
     *   2: SpecializationActive {@link LuaMulNode#doDouble(long, double)}
     *   3: SpecializationActive {@link LuaMulNode#doDouble(double, long)}
     *   4: SpecializationActive {@link LuaMulNode#doGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private FallbackData fallback_cache;

    private LuaMulNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, double)] */) && leftNodeValue instanceof Double && rightNodeValue instanceof Double) {
            return false;
        }
        if (leftNodeValue instanceof Long) {
            if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaMulNode.doLong(long, long)] */) && rightNodeValue instanceof Long) {
                return false;
            }
            if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaMulNode.doDouble(long, double)] */) && rightNodeValue instanceof Double) {
                return false;
            }
        }
        if (!((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, long)] */) && leftNodeValue instanceof Double && rightNodeValue instanceof Long) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b11101) == 0 /* only-active SpecializationActive[LuaMulNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaMulNode.doDouble(double, double)] && SpecializationActive[LuaMulNode.doLong(long, long)] && SpecializationActive[LuaMulNode.doDouble(long, double)] && SpecializationActive[LuaMulNode.doDouble(double, long)] && SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else if ((state_0 & 0b11011) == 0 /* only-active SpecializationActive[LuaMulNode.doDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaMulNode.doDouble(double, double)] && SpecializationActive[LuaMulNode.doLong(long, long)] && SpecializationActive[LuaMulNode.doDouble(long, double)] && SpecializationActive[LuaMulNode.doDouble(double, long)] && SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long1(state_0, frameValue);
        } else if ((state_0 & 0b10111) == 0 /* only-active SpecializationActive[LuaMulNode.doDouble(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaMulNode.doDouble(double, double)] && SpecializationActive[LuaMulNode.doLong(long, long)] && SpecializationActive[LuaMulNode.doDouble(long, double)] && SpecializationActive[LuaMulNode.doDouble(double, long)] && SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long2(state_0, frameValue);
        } else {
            return executeGeneric_generic3(state_0, frameValue);
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
        assert (state_0 & 0b10) != 0 /* is SpecializationActive[LuaMulNode.doLong(long, long)] */;
        return LuaMulNode.doLong(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        long leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return executeAndSpecialize(ex.getResult(), rightNodeValue);
        }
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaMulNode.doDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaMulNode.doDouble(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long2(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaMulNode.doDouble(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, double)] || SpecializationActive[LuaMulNode.doLong(long, long)] || SpecializationActive[LuaMulNode.doDouble(long, double)] || SpecializationActive[LuaMulNode.doDouble(double, long)] || SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaMulNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaMulNode.doLong(long, long)] || SpecializationActive[LuaMulNode.doDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaMulNode.doLong(long, long)] */ && rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaMulNode.doLong(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaMulNode.doDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaMulNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaMulNode.doDouble(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaMulNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
                FallbackData s4_ = this.fallback_cache;
                if (s4_ != null) {
                    if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                        return doGeneric(leftNodeValue_, rightNodeValue_, s4_.coerceLeft_, s4_.coerceRight_, s4_.metatableNode_);
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
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaMulNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaMulNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaMulNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return LuaMulNode.doLong(leftNodeValue_, rightNodeValue_);
            }
            if (rightNodeValue instanceof Double) {
                double rightNodeValue_ = (double) rightNodeValue;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaMulNode.doDouble(long, double)] */;
                this.state_0_ = state_0;
                return LuaMulNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaMulNode.doDouble(double, long)] */;
                this.state_0_ = state_0;
                return LuaMulNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        FallbackData s4_ = this.insert(new FallbackData());
        s4_.coerceLeft_ = s4_.insert((LuaCoerceToNumberNode.create()));
        s4_.coerceRight_ = s4_.insert((LuaCoerceToNumberNode.create()));
        s4_.metatableNode_ = s4_.insert((LuaMetatableNode.create()));
        VarHandle.storeStoreFence();
        this.fallback_cache = s4_;
        state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaMulNode.doGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return doGeneric(leftNodeValue, rightNodeValue, s4_.coerceLeft_, s4_.coerceRight_, s4_.metatableNode_);
    }

    @NeverDefault
    public static LuaMulNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaMulNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaMulNode.class)
    @DenyReplace
    private static final class FallbackData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaMulNode#doGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceLeft</pre> */
        @Child LuaCoerceToNumberNode coerceLeft_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaMulNode#doGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceRight</pre> */
        @Child LuaCoerceToNumberNode coerceRight_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaMulNode#doGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        FallbackData() {
        }

    }
}

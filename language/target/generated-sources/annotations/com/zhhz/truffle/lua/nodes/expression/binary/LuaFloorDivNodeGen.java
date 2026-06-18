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
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import java.lang.invoke.VarHandle;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaFloorDivNode#doLong}
 *     Activation probability: 0.27381
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaFloorDivNode#doLongZero}
 *     Activation probability: 0.23095
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaFloorDivNode#doDouble}
 *     Activation probability: 0.18810
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaFloorDivNode#doLongDouble}
 *     Activation probability: 0.14524
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaFloorDivNode#doDoubleLong}
 *     Activation probability: 0.10238
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaFloorDivNode#floorDivGeneric}
 *     Activation probability: 0.05952
 *     With/without class size: 5/12 bytes
 * </pre> */
@GeneratedBy(LuaFloorDivNode.class)
@SuppressWarnings("javadoc")
public final class LuaFloorDivNodeGen extends LuaFloorDivNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaFloorDivNode#doLong}
     *   1: SpecializationActive {@link LuaFloorDivNode#doLongZero}
     *   2: SpecializationActive {@link LuaFloorDivNode#doDouble}
     *   3: SpecializationActive {@link LuaFloorDivNode#doLongDouble}
     *   4: SpecializationActive {@link LuaFloorDivNode#doDoubleLong}
     *   5: SpecializationActive {@link LuaFloorDivNode#floorDivGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private FloorDivGenericData floorDivGeneric_cache;

    private LuaFloorDivNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111100) == 0 /* only-active SpecializationActive[LuaFloorDivNode.doLong(long, long)] && SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaFloorDivNode.doLong(long, long)] && SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] && SpecializationActive[LuaFloorDivNode.doDouble(double, double)] && SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] && SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] && SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else if ((state_0 & 0b110111) == 0 /* only-active SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaFloorDivNode.doLong(long, long)] && SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] && SpecializationActive[LuaFloorDivNode.doDouble(double, double)] && SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] && SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] && SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
            return executeGeneric_long1(state_0, frameValue);
        } else if ((state_0 & 0b101111) == 0 /* only-active SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaFloorDivNode.doLong(long, long)] && SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] && SpecializationActive[LuaFloorDivNode.doDouble(double, double)] && SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] && SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] && SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
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
        if ((state_0 & 0b11) != 0 /* is SpecializationActive[LuaFloorDivNode.doLong(long, long)] || SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaFloorDivNode.doLong(long, long)] */) {
                if ((rightNodeValue_ != 0)) {
                    return doLong(leftNodeValue_, rightNodeValue_);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */) {
                if ((rightNodeValue_ == 0)) {
                    return doLongZero(leftNodeValue_, rightNodeValue_);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return doLongDouble(leftNodeValue_, rightNodeValue__);
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
        assert (state_0 & 0b10000) != 0 /* is SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaFloorDivNode.doLong(long, long)] || SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] || SpecializationActive[LuaFloorDivNode.doDouble(double, double)] || SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] || SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] || SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b11) != 0 /* is SpecializationActive[LuaFloorDivNode.doLong(long, long)] || SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaFloorDivNode.doLong(long, long)] */) {
                        if ((rightNodeValue__ != 0)) {
                            return doLong(leftNodeValue__, rightNodeValue__);
                        }
                    }
                    if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */) {
                        if ((rightNodeValue__ == 0)) {
                            return doLongZero(leftNodeValue__, rightNodeValue__);
                        }
                    }
                }
            }
            if ((state_0 & 0b1100) != 0 /* is SpecializationActive[LuaFloorDivNode.doDouble(double, double)] || SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaFloorDivNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
                FloorDivGenericData s5_ = this.floorDivGeneric_cache;
                if (s5_ != null) {
                    return floorDivGeneric(leftNodeValue_, rightNodeValue_, s5_.coerceLeft_, s5_.coerceRight_, s5_.metatableNode_);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public long executeLong(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */) {
            return LuaTypesGen.expectLong(this.executeGeneric(frameValue));
        }
        long leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return LuaTypesGen.expectLong(executeAndSpecialize(ex.getResult(), rightNodeValue));
        }
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectLong(executeAndSpecialize(leftNodeValue_, ex.getResult()));
        }
        if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */) {
            if ((rightNodeValue_ == 0)) {
                return doLongZero(leftNodeValue_, rightNodeValue_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectLong(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b111101) == 0 /* only-active SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaFloorDivNode.doLong(long, long)] && SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] && SpecializationActive[LuaFloorDivNode.doDouble(double, double)] && SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] && SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] && SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */)) {
                this.executeLong(frameValue);
                return;
            }
            this.executeGeneric(frameValue);
            return;
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return;
        }
    }

    private Object executeAndSpecialize(Object leftNodeValue, Object rightNodeValue) {
        int state_0 = this.state_0_;
        if (leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                if ((rightNodeValue_ != 0)) {
                    state_0 = state_0 | 0b1 /* add SpecializationActive[LuaFloorDivNode.doLong(long, long)] */;
                    this.state_0_ = state_0;
                    return doLong(leftNodeValue_, rightNodeValue_);
                }
                if ((rightNodeValue_ == 0)) {
                    state_0 = state_0 | 0b10 /* add SpecializationActive[LuaFloorDivNode.doLongZero(long, long)] */;
                    this.state_0_ = state_0;
                    return doLongZero(leftNodeValue_, rightNodeValue_);
                }
            }
        }
        if (rightNodeValue instanceof Double) {
            double rightNodeValue_ = (double) rightNodeValue;
            if (leftNodeValue instanceof Double) {
                double leftNodeValue_ = (double) leftNodeValue;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaFloorDivNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return doDouble(leftNodeValue_, rightNodeValue_);
            }
            if (leftNodeValue instanceof Long) {
                long leftNodeValue_ = (long) leftNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaFloorDivNode.doLongDouble(long, double)] */;
                this.state_0_ = state_0;
                return doLongDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaFloorDivNode.doDoubleLong(double, long)] */;
                this.state_0_ = state_0;
                return doDoubleLong(leftNodeValue_, rightNodeValue_);
            }
        }
        FloorDivGenericData s5_ = this.insert(new FloorDivGenericData());
        s5_.coerceLeft_ = s5_.insert((LuaCoerceToNumberNode.create()));
        s5_.coerceRight_ = s5_.insert((LuaCoerceToNumberNode.create()));
        s5_.metatableNode_ = s5_.insert((LuaMetatableNode.create()));
        VarHandle.storeStoreFence();
        this.floorDivGeneric_cache = s5_;
        state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaFloorDivNode.floorDivGeneric(Object, Object, LuaCoerceToNumberNode, LuaCoerceToNumberNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return floorDivGeneric(leftNodeValue, rightNodeValue, s5_.coerceLeft_, s5_.coerceRight_, s5_.metatableNode_);
    }

    @NeverDefault
    public static LuaFloorDivNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaFloorDivNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaFloorDivNode.class)
    @DenyReplace
    private static final class FloorDivGenericData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaFloorDivNode#floorDivGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceLeft</pre> */
        @Child LuaCoerceToNumberNode coerceLeft_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaFloorDivNode#floorDivGeneric}
         *   Parameter: {@link LuaCoerceToNumberNode} coerceRight</pre> */
        @Child LuaCoerceToNumberNode coerceRight_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaFloorDivNode#floorDivGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        FloorDivGenericData() {
        }

    }
}

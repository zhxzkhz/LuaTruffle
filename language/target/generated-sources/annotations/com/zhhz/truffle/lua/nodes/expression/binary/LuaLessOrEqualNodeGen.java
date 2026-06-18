// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.CompareCharsUTF16Node;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaLessOrEqualNode#doLong}
 *     Activation probability: 0.27381
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaLessOrEqualNode#doDouble}
 *     Activation probability: 0.23095
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLessOrEqualNode#doMixed1}
 *     Activation probability: 0.18810
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLessOrEqualNode#doMixed2}
 *     Activation probability: 0.14524
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaLessOrEqualNode#lessThanOrEqualStrings}
 *     Activation probability: 0.10238
 *     With/without class size: 6/4 bytes
 *   Specialization {@link LuaLessOrEqualNode#typeError}
 *     Activation probability: 0.05952
 *     With/without class size: 5/4 bytes
 * </pre> */
@GeneratedBy(LuaLessOrEqualNode.class)
@SuppressWarnings("javadoc")
public final class LuaLessOrEqualNodeGen extends LuaLessOrEqualNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaLessOrEqualNode#doLong}
     *   1: SpecializationActive {@link LuaLessOrEqualNode#doDouble}
     *   2: SpecializationActive {@link LuaLessOrEqualNode#doMixed1}
     *   3: SpecializationActive {@link LuaLessOrEqualNode#doMixed2}
     *   4: SpecializationActive {@link LuaLessOrEqualNode#lessThanOrEqualStrings}
     *   5: SpecializationActive {@link LuaLessOrEqualNode#typeError}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLessOrEqualNode#lessThanOrEqualStrings}
     *   Parameter: {@link CompareCharsUTF16Node} compareNode</pre> */
    @Child private CompareCharsUTF16Node lessThanOrEqualStrings_compareNode_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLessOrEqualNode#typeError}
     *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
    @Child private LuaMetatableNode fallback_metatableNode_;

    private LuaLessOrEqualNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Long) {
            return false;
        }
        if (leftNodeValue instanceof Double) {
            if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] */) && rightNodeValue instanceof Double) {
                return false;
            }
            if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */) && rightNodeValue instanceof Long) {
                return false;
            }
        }
        if (!((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Double) {
            return false;
        }
        if (!((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) && leftNodeValue instanceof TruffleString && rightNodeValue instanceof TruffleString) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111110) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else if ((state_0 & 0b111011) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long1(state_0, frameValue);
        } else if ((state_0 & 0b110111) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
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
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */;
        return LuaLessOrEqualNode.doLong(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaLessOrEqualNode.doMixed1(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long2(int state_0__, VirtualFrame frameValue) {
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaLessOrEqualNode.doMixed2(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] || SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] || SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] || SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] || SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] || SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessOrEqualNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] || SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] */ && rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaLessOrEqualNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessOrEqualNode.doMixed1(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaLessOrEqualNode.doMixed2(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.lessThanOrEqualStrings_compareNode_;
                        if (compareNode__ != null) {
                            return LuaLessOrEqualNode.lessThanOrEqualStrings(leftNodeValue__, rightNodeValue__, compareNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
                {
                    LuaMetatableNode metatableNode__ = this.fallback_metatableNode_;
                    if (metatableNode__ != null) {
                        Node node__ = (this);
                        if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                            return LuaLessOrEqualNode.typeError(leftNodeValue_, rightNodeValue_, metatableNode__, node__);
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            return LuaTypesGen.expectBoolean(this.executeGeneric(frameValue));
        }
        if ((state_0 & 0b11110) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long_long4(state_0, frameValue);
        } else if ((state_0 & 0b11011) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long5(state_0, frameValue);
        } else if ((state_0 & 0b10111) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long6(state_0, frameValue);
        } else {
            return executeBoolean_generic7(state_0, frameValue);
        }
    }

    private boolean executeBoolean_long_long4(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        long leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return LuaTypesGen.expectBoolean(executeAndSpecialize(ex.getResult(), rightNodeValue));
        }
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, ex.getResult()));
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */;
        return LuaLessOrEqualNode.doLong(leftNodeValue_, rightNodeValue_);
    }

    private boolean executeBoolean_long5(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, ex.getResult()));
        }
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaLessOrEqualNode.doMixed1(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    private boolean executeBoolean_long6(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        long leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return LuaTypesGen.expectBoolean(executeAndSpecialize(ex.getResult(), rightNodeValue));
        }
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaLessOrEqualNode.doMixed2(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    private boolean executeBoolean_generic7(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if ((state_0 & 0b11111) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] || SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] || SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] || SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] || SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessOrEqualNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] || SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] */ && rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaLessOrEqualNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */ && rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessOrEqualNode.doMixed1(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Double) {
                    double rightNodeValue__ = (double) rightNodeValue_;
                    return LuaLessOrEqualNode.doMixed2(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.lessThanOrEqualStrings_compareNode_;
                        if (compareNode__ != null) {
                            return LuaLessOrEqualNode.lessThanOrEqualStrings(leftNodeValue__, rightNodeValue__, compareNode__);
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b100000) == 0 /* only-active SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] && SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] && SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] && SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] && SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
                this.executeBoolean(frameValue);
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
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaLessOrEqualNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return LuaLessOrEqualNode.doLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Double) {
                double rightNodeValue_ = (double) rightNodeValue;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaLessOrEqualNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaLessOrEqualNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaLessOrEqualNode.doMixed1(double, long)] */;
                this.state_0_ = state_0;
                return LuaLessOrEqualNode.doMixed1(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Double) {
                double rightNodeValue_ = (double) rightNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaLessOrEqualNode.doMixed2(long, double)] */;
                this.state_0_ = state_0;
                return LuaLessOrEqualNode.doMixed2(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof TruffleString) {
            TruffleString leftNodeValue_ = (TruffleString) leftNodeValue;
            if (rightNodeValue instanceof TruffleString) {
                TruffleString rightNodeValue_ = (TruffleString) rightNodeValue;
                CompareCharsUTF16Node compareNode__ = this.insert((CompareCharsUTF16Node.create()));
                Objects.requireNonNull(compareNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.lessThanOrEqualStrings_compareNode_ = compareNode__;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaLessOrEqualNode.lessThanOrEqualStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */;
                this.state_0_ = state_0;
                return LuaLessOrEqualNode.lessThanOrEqualStrings(leftNodeValue_, rightNodeValue_, compareNode__);
            }
        }
        {
            Node node__ = null;
            LuaMetatableNode metatableNode__ = this.insert((LuaMetatableNode.create()));
            Objects.requireNonNull(metatableNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
            VarHandle.storeStoreFence();
            this.fallback_metatableNode_ = metatableNode__;
            node__ = (this);
            state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaLessOrEqualNode.typeError(Object, Object, LuaMetatableNode, Node)] */;
            this.state_0_ = state_0;
            return LuaLessOrEqualNode.typeError(leftNodeValue, rightNodeValue, metatableNode__, node__);
        }
    }

    @NeverDefault
    public static LuaLessOrEqualNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaLessOrEqualNodeGen(leftNode, rightNode);
    }

}

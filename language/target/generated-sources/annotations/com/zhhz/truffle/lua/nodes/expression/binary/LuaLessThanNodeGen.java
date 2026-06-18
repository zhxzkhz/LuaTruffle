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
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.CompareCharsUTF16Node;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaLessThanNode#lessThanNumbers}
 *     Activation probability: 0.21250
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLessThanNode#doLong}
 *     Activation probability: 0.18750
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLessThanNode#doDouble}
 *     Activation probability: 0.16250
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaLessThanNode#doLongDouble}
 *     Activation probability: 0.13750
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaLessThanNode#doDoubleLong}
 *     Activation probability: 0.11250
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaLessThanNode#lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)}
 *     Activation probability: 0.08750
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaLessThanNode#lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)}
 *     Activation probability: 0.06250
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaLessThanNode#typeError}
 *     Activation probability: 0.03750
 *     With/without class size: 4/4 bytes
 * </pre> */
@GeneratedBy(LuaLessThanNode.class)
@SuppressWarnings("javadoc")
public final class LuaLessThanNodeGen extends LuaLessThanNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaLessThanNode#lessThanNumbers}
     *   1: SpecializationActive {@link LuaLessThanNode#doLong}
     *   2: SpecializationActive {@link LuaLessThanNode#doDouble}
     *   3: SpecializationActive {@link LuaLessThanNode#doLongDouble}
     *   4: SpecializationActive {@link LuaLessThanNode#doDoubleLong}
     *   5: SpecializationActive {@link LuaLessThanNode#lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)}
     *   6: SpecializationActive {@link LuaLessThanNode#lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)}
     *   7: SpecializationActive {@link LuaLessThanNode#typeError}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLessThanNode#lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)}
     *   Parameter: {@link CompareCharsUTF16Node} compareNode</pre> */
    @Child private CompareCharsUTF16Node lessThanStrings0_compareNode_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaLessThanNode#lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)}
     *   Parameter: {@link CompareCharsUTF16Node} compareNode</pre> */
    @Child private CompareCharsUTF16Node lessThanStrings1_compareNode_;
    @Child private FallbackData fallback_cache;

    private LuaLessThanNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] */) && leftNodeValue instanceof LuaNumber && rightNodeValue instanceof LuaNumber) {
            return false;
        }
        if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessThanNode.doLong(long, long)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Long) {
            return false;
        }
        if (rightNodeValue instanceof Double) {
            if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessThanNode.doDouble(double, double)] */) && leftNodeValue instanceof Double) {
                return false;
            }
            if (!((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */) && leftNodeValue instanceof Long) {
                return false;
            }
        }
        if (!((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */) && leftNodeValue instanceof Double && rightNodeValue instanceof Long) {
            return false;
        }
        if (!((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] */) && leftNodeValue instanceof LuaString && rightNodeValue instanceof LuaString) {
            return false;
        }
        if (!((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) && leftNodeValue instanceof TruffleString && rightNodeValue instanceof TruffleString) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b11111101) == 0 /* only-active SpecializationActive[LuaLessThanNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else if ((state_0 & 0b11110111) == 0 /* only-active SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long1(state_0, frameValue);
        } else if ((state_0 & 0b11101111) == 0 /* only-active SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
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
        assert (state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessThanNode.doLong(long, long)] */;
        return LuaLessThanNode.doLong(leftNodeValue_, rightNodeValue_);
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaLessThanNode.doLongDouble(leftNodeValue_, rightNodeValue__);
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
        assert (state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaLessThanNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] || SpecializationActive[LuaLessThanNode.doLong(long, long)] || SpecializationActive[LuaLessThanNode.doDouble(double, double)] || SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] || SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] || SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] || SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] || SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] */ && leftNodeValue_ instanceof LuaNumber) {
                LuaNumber leftNodeValue__ = (LuaNumber) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaNumber) {
                    LuaNumber rightNodeValue__ = (LuaNumber) rightNodeValue_;
                    return LuaLessThanNode.lessThanNumbers(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessThanNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessThanNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1100) != 0 /* is SpecializationActive[LuaLessThanNode.doDouble(double, double)] || SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessThanNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaLessThanNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaLessThanNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessThanNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof LuaString) {
                LuaString leftNodeValue__ = (LuaString) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaString) {
                    LuaString rightNodeValue__ = (LuaString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.lessThanStrings0_compareNode_;
                        if (compareNode__ != null) {
                            return LuaLessThanNode.lessThanStrings(leftNodeValue__, rightNodeValue__, compareNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__1 = this.lessThanStrings1_compareNode_;
                        if (compareNode__1 != null) {
                            return LuaLessThanNode.lessThanStrings(leftNodeValue__, rightNodeValue__, compareNode__1);
                        }
                    }
                }
            }
            if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
                FallbackData s7_ = this.fallback_cache;
                if (s7_ != null) {
                    {
                        Node node__ = (this);
                        if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                            return LuaLessThanNode.typeError(leftNodeValue_, rightNodeValue_, s7_.metatableNode_, node__);
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
        if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            return LuaTypesGen.expectBoolean(this.executeGeneric(frameValue));
        }
        if ((state_0 & 0b1111101) == 0 /* only-active SpecializationActive[LuaLessThanNode.doLong(long, long)] */ && ((state_0 & 0b1111111) != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long_long4(state_0, frameValue);
        } else if ((state_0 & 0b1110111) == 0 /* only-active SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && ((state_0 & 0b1111111) != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long5(state_0, frameValue);
        } else if ((state_0 & 0b1101111) == 0 /* only-active SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */ && ((state_0 & 0b1111111) != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
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
        assert (state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessThanNode.doLong(long, long)] */;
        return LuaLessThanNode.doLong(leftNodeValue_, rightNodeValue_);
    }

    private boolean executeBoolean_long5(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaLessThanNode.doLongDouble(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    private boolean executeBoolean_long6(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, ex.getResult()));
        }
        assert (state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaLessThanNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    private boolean executeBoolean_generic7(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if ((state_0 & 0b1111111) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] || SpecializationActive[LuaLessThanNode.doLong(long, long)] || SpecializationActive[LuaLessThanNode.doDouble(double, double)] || SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] || SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] || SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] || SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] */ && leftNodeValue_ instanceof LuaNumber) {
                LuaNumber leftNodeValue__ = (LuaNumber) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaNumber) {
                    LuaNumber rightNodeValue__ = (LuaNumber) rightNodeValue_;
                    return LuaLessThanNode.lessThanNumbers(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLessThanNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessThanNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1100) != 0 /* is SpecializationActive[LuaLessThanNode.doDouble(double, double)] || SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLessThanNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaLessThanNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaLessThanNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaLessThanNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof LuaString) {
                LuaString leftNodeValue__ = (LuaString) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaString) {
                    LuaString rightNodeValue__ = (LuaString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.lessThanStrings0_compareNode_;
                        if (compareNode__ != null) {
                            return LuaLessThanNode.lessThanStrings(leftNodeValue__, rightNodeValue__, compareNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__1 = this.lessThanStrings1_compareNode_;
                        if (compareNode__1 != null) {
                            return LuaLessThanNode.lessThanStrings(leftNodeValue__, rightNodeValue__, compareNode__1);
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
            if ((state_0 & 0b10000000) == 0 /* only-active SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] && SpecializationActive[LuaLessThanNode.doLong(long, long)] && SpecializationActive[LuaLessThanNode.doDouble(double, double)] && SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] && SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
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
        if (leftNodeValue instanceof LuaNumber) {
            LuaNumber leftNodeValue_ = (LuaNumber) leftNodeValue;
            if (rightNodeValue instanceof LuaNumber) {
                LuaNumber rightNodeValue_ = (LuaNumber) rightNodeValue;
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaLessThanNode.lessThanNumbers(LuaNumber, LuaNumber)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.lessThanNumbers(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaLessThanNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.doLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (rightNodeValue instanceof Double) {
            double rightNodeValue_ = (double) rightNodeValue;
            if (leftNodeValue instanceof Double) {
                double leftNodeValue_ = (double) leftNodeValue;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaLessThanNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
            if (leftNodeValue instanceof Long) {
                long leftNodeValue_ = (long) leftNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaLessThanNode.doLongDouble(long, double)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.doLongDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaLessThanNode.doDoubleLong(double, long)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.doDoubleLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof LuaString) {
            LuaString leftNodeValue_ = (LuaString) leftNodeValue;
            if (rightNodeValue instanceof LuaString) {
                LuaString rightNodeValue_ = (LuaString) rightNodeValue;
                CompareCharsUTF16Node compareNode__ = this.insert((CompareCharsUTF16Node.create()));
                Objects.requireNonNull(compareNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.lessThanStrings0_compareNode_ = compareNode__;
                state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaLessThanNode.lessThanStrings(LuaString, LuaString, CompareCharsUTF16Node)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.lessThanStrings(leftNodeValue_, rightNodeValue_, compareNode__);
            }
        }
        if (leftNodeValue instanceof TruffleString) {
            TruffleString leftNodeValue_ = (TruffleString) leftNodeValue;
            if (rightNodeValue instanceof TruffleString) {
                TruffleString rightNodeValue_ = (TruffleString) rightNodeValue;
                CompareCharsUTF16Node compareNode__1 = this.insert((CompareCharsUTF16Node.create()));
                Objects.requireNonNull(compareNode__1, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.lessThanStrings1_compareNode_ = compareNode__1;
                state_0 = state_0 | 0b1000000 /* add SpecializationActive[LuaLessThanNode.lessThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */;
                this.state_0_ = state_0;
                return LuaLessThanNode.lessThanStrings(leftNodeValue_, rightNodeValue_, compareNode__1);
            }
        }
        {
            Node node__ = null;
            FallbackData s7_ = this.insert(new FallbackData());
            s7_.metatableNode_ = s7_.insert((LuaMetatableNode.create()));
            node__ = (this);
            VarHandle.storeStoreFence();
            this.fallback_cache = s7_;
            state_0 = state_0 | 0b10000000 /* add SpecializationActive[LuaLessThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */;
            this.state_0_ = state_0;
            return LuaLessThanNode.typeError(leftNodeValue, rightNodeValue, s7_.metatableNode_, node__);
        }
    }

    @NeverDefault
    public static LuaLessThanNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaLessThanNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaLessThanNode.class)
    @DenyReplace
    private static final class FallbackData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaLessThanNode#typeError}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        FallbackData() {
        }

    }
}

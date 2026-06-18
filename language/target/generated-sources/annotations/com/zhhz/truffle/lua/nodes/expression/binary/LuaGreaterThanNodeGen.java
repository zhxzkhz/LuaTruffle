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
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaGreaterThanNode#doLong}
 *     Activation probability: 0.27381
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaGreaterThanNode#doDouble}
 *     Activation probability: 0.23095
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaGreaterThanNode#doLongDouble}
 *     Activation probability: 0.18810
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaGreaterThanNode#doDoubleLong}
 *     Activation probability: 0.14524
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaGreaterThanNode#greaterThanStrings}
 *     Activation probability: 0.10238
 *     With/without class size: 6/4 bytes
 *   Specialization {@link LuaGreaterThanNode#typeError}
 *     Activation probability: 0.05952
 *     With/without class size: 5/4 bytes
 * </pre> */
@GeneratedBy(LuaGreaterThanNode.class)
@SuppressWarnings("javadoc")
public final class LuaGreaterThanNodeGen extends LuaGreaterThanNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaGreaterThanNode#doLong}
     *   1: SpecializationActive {@link LuaGreaterThanNode#doDouble}
     *   2: SpecializationActive {@link LuaGreaterThanNode#doLongDouble}
     *   3: SpecializationActive {@link LuaGreaterThanNode#doDoubleLong}
     *   4: SpecializationActive {@link LuaGreaterThanNode#greaterThanStrings}
     *   5: SpecializationActive {@link LuaGreaterThanNode#typeError}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaGreaterThanNode#greaterThanStrings}
     *   Parameter: {@link CompareCharsUTF16Node} compareNode</pre> */
    @Child private CompareCharsUTF16Node greaterThanStrings_compareNode_;
    @Child private FallbackData fallback_cache;

    private LuaGreaterThanNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Long) {
            return false;
        }
        if (rightNodeValue instanceof Double) {
            if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] */) && leftNodeValue instanceof Double) {
                return false;
            }
            if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */) && leftNodeValue instanceof Long) {
                return false;
            }
        }
        if (!((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */) && leftNodeValue instanceof Double && rightNodeValue instanceof Long) {
            return false;
        }
        if (!((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) && leftNodeValue instanceof TruffleString && rightNodeValue instanceof TruffleString) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111110) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else if ((state_0 & 0b111011) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
            return executeGeneric_long1(state_0, frameValue);
        } else if ((state_0 & 0b110111) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
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
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */;
        return LuaGreaterThanNode.doLong(leftNodeValue_, rightNodeValue_);
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
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaGreaterThanNode.doLongDouble(leftNodeValue_, rightNodeValue__);
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaGreaterThanNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] || SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] || SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] || SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] || SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] || SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaGreaterThanNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] || SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaGreaterThanNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaGreaterThanNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaGreaterThanNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.greaterThanStrings_compareNode_;
                        if (compareNode__ != null) {
                            return LuaGreaterThanNode.greaterThanStrings(leftNodeValue__, rightNodeValue__, compareNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
                FallbackData s5_ = this.fallback_cache;
                if (s5_ != null) {
                    {
                        Node node__ = (this);
                        if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                            return LuaGreaterThanNode.typeError(leftNodeValue_, rightNodeValue_, s5_.metatableNode_, node__);
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
        if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */) {
            return LuaTypesGen.expectBoolean(this.executeGeneric(frameValue));
        }
        if ((state_0 & 0b11110) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long_long4(state_0, frameValue);
        } else if ((state_0 & 0b11011) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
            return executeBoolean_long5(state_0, frameValue);
        } else if ((state_0 & 0b10111) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */ && ((state_0 & 0b11111) != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */)) {
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
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */;
        return LuaGreaterThanNode.doLong(leftNodeValue_, rightNodeValue_);
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
        assert (state_0 & 0b100) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaGreaterThanNode.doLongDouble(leftNodeValue_, rightNodeValue__);
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaGreaterThanNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    private boolean executeBoolean_generic7(int state_0__, VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if ((state_0 & 0b11111) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] || SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] || SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] || SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] || SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaGreaterThanNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] || SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaGreaterThanNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaGreaterThanNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaGreaterThanNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        CompareCharsUTF16Node compareNode__ = this.greaterThanStrings_compareNode_;
                        if (compareNode__ != null) {
                            return LuaGreaterThanNode.greaterThanStrings(leftNodeValue__, rightNodeValue__, compareNode__);
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
            if ((state_0 & 0b100000) == 0 /* only-active SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaGreaterThanNode.doLong(long, long)] && SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] && SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] && SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] && SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] && SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */)) {
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
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaGreaterThanNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return LuaGreaterThanNode.doLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (rightNodeValue instanceof Double) {
            double rightNodeValue_ = (double) rightNodeValue;
            if (leftNodeValue instanceof Double) {
                double leftNodeValue_ = (double) leftNodeValue;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaGreaterThanNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaGreaterThanNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
            if (leftNodeValue instanceof Long) {
                long leftNodeValue_ = (long) leftNodeValue;
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaGreaterThanNode.doLongDouble(long, double)] */;
                this.state_0_ = state_0;
                return LuaGreaterThanNode.doLongDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaGreaterThanNode.doDoubleLong(double, long)] */;
                this.state_0_ = state_0;
                return LuaGreaterThanNode.doDoubleLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof TruffleString) {
            TruffleString leftNodeValue_ = (TruffleString) leftNodeValue;
            if (rightNodeValue instanceof TruffleString) {
                TruffleString rightNodeValue_ = (TruffleString) rightNodeValue;
                CompareCharsUTF16Node compareNode__ = this.insert((CompareCharsUTF16Node.create()));
                Objects.requireNonNull(compareNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.greaterThanStrings_compareNode_ = compareNode__;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaGreaterThanNode.greaterThanStrings(TruffleString, TruffleString, CompareCharsUTF16Node)] */;
                this.state_0_ = state_0;
                return LuaGreaterThanNode.greaterThanStrings(leftNodeValue_, rightNodeValue_, compareNode__);
            }
        }
        {
            Node node__ = null;
            FallbackData s5_ = this.insert(new FallbackData());
            s5_.metatableNode_ = s5_.insert((LuaMetatableNode.create()));
            node__ = (this);
            VarHandle.storeStoreFence();
            this.fallback_cache = s5_;
            state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaGreaterThanNode.typeError(Object, Object, LuaMetatableNode, Node)] */;
            this.state_0_ = state_0;
            return LuaGreaterThanNode.typeError(leftNodeValue, rightNodeValue, s5_.metatableNode_, node__);
        }
    }

    @NeverDefault
    public static LuaGreaterThanNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaGreaterThanNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaGreaterThanNode.class)
    @DenyReplace
    private static final class FallbackData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaGreaterThanNode#typeError}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        FallbackData() {
        }

    }
}

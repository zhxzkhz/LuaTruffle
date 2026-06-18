// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.EqualNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaEqualNode#doBoolean(boolean, boolean)}
 *     Activation probability: 0.15909
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doLong}
 *     Activation probability: 0.14545
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doDouble}
 *     Activation probability: 0.13182
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doLongDouble}
 *     Activation probability: 0.11818
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doDoubleLong}
 *     Activation probability: 0.10455
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doNil}
 *     Activation probability: 0.09091
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaEqualNode#doTruffleString}
 *     Activation probability: 0.07727
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaEqualNode#doFunction}
 *     Activation probability: 0.06364
 *     With/without class size: 4/0 bytes
 *   Specialization {@link LuaEqualNode#doBoolean(LuaBoolean, LuaBoolean)}
 *     Activation probability: 0.05000
 *     With/without class size: 4/0 bytes
 *   Specialization {@link LuaEqualNode#doGeneric}
 *     Activation probability: 0.03636
 *     With/without class size: 5/12 bytes
 *   Specialization {@link LuaEqualNode#doGeneric}
 *     Activation probability: 0.02273
 *     With/without class size: 4/4 bytes
 * </pre> */
@GeneratedBy(LuaEqualNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaEqualNodeGen extends LuaEqualNode {

    static final ReferenceField<Generic0Data> GENERIC0_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "generic0_cache", Generic0Data.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaEqualNode#doBoolean(boolean, boolean)}
     *   1: SpecializationActive {@link LuaEqualNode#doGeneric}
     *   2: SpecializationActive {@link LuaEqualNode#doGeneric}
     *   3: SpecializationActive {@link LuaEqualNode#doLong}
     *   4: SpecializationActive {@link LuaEqualNode#doDouble}
     *   5: SpecializationActive {@link LuaEqualNode#doLongDouble}
     *   6: SpecializationActive {@link LuaEqualNode#doDoubleLong}
     *   7: SpecializationActive {@link LuaEqualNode#doNil}
     *   8: SpecializationActive {@link LuaEqualNode#doTruffleString}
     *   9: SpecializationActive {@link LuaEqualNode#doFunction}
     *   10: SpecializationActive {@link LuaEqualNode#doBoolean(LuaBoolean, LuaBoolean)}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaEqualNode#doTruffleString}
     *   Parameter: {@link EqualNode} equalNode</pre> */
    @Child private EqualNode truffleString_equalNode_;
    @UnsafeAccessedField @Child private Generic0Data generic0_cache;
    @Child private Generic1Data generic1_cache;

    private LuaEqualNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b11111111110) == 0 /* only-active SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeGeneric_boolean_boolean0(state_0, frameValue);
        } else if ((state_0 & 0b11111110111) == 0 /* only-active SpecializationActive[LuaEqualNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeGeneric_long_long1(state_0, frameValue);
        } else if ((state_0 & 0b11111011111) == 0 /* only-active SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeGeneric_long2(state_0, frameValue);
        } else if ((state_0 & 0b11110111111) == 0 /* only-active SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeGeneric_long3(state_0, frameValue);
        } else {
            return executeGeneric_generic4(state_0, frameValue);
        }
    }

    private Object executeGeneric_boolean_boolean0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        boolean leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return executeAndSpecialize(ex.getResult(), rightNodeValue);
        }
        boolean rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */;
        return LuaEqualNode.doBoolean(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long_long1(int state_0__, VirtualFrame frameValue) {
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaEqualNode.doLong(long, long)] */;
        return LuaEqualNode.doLong(leftNodeValue_, rightNodeValue_);
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
        assert (state_0 & 0b100000) != 0 /* is SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaEqualNode.doLongDouble(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_long3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaEqualNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private Object generic1Boundary(int state_0, Generic1Data s10_, Object leftNodeValue_, Object rightNodeValue_) {
        EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
        Node prev_ = encapsulating_.set(this);
        try {
            {
                InteropLibrary leftInterop__ = (INTEROP_LIBRARY_.getUncached(leftNodeValue_));
                InteropLibrary rightInterop__ = (INTEROP_LIBRARY_.getUncached(rightNodeValue_));
                return LuaEqualNode.doGeneric(leftNodeValue_, rightNodeValue_, leftInterop__, rightInterop__, s10_.metatableNode_);
            }
        } finally {
            encapsulating_.set(prev_);
        }
    }

    @ExplodeLoop
    private Object executeGeneric_generic4(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] || SpecializationActive[LuaEqualNode.doLong(long, long)] || SpecializationActive[LuaEqualNode.doDouble(double, double)] || SpecializationActive[LuaEqualNode.doLongDouble(long, double)] || SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] || SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] || SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] || SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] || SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */ && leftNodeValue_ instanceof Boolean) {
                boolean leftNodeValue__ = (boolean) leftNodeValue_;
                if (rightNodeValue_ instanceof Boolean) {
                    boolean rightNodeValue__ = (boolean) rightNodeValue_;
                    return LuaEqualNode.doBoolean(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaEqualNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaEqualNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110000) != 0 /* is SpecializationActive[LuaEqualNode.doDouble(double, double)] || SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaEqualNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaEqualNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaEqualNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaEqualNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] */ && LuaTypes.isLuaNil(leftNodeValue_)) {
                LuaNil leftNodeValue__ = LuaTypes.asLuaNil(leftNodeValue_);
                if (LuaTypes.isLuaNil(rightNodeValue_)) {
                    LuaNil rightNodeValue__ = LuaTypes.asLuaNil(rightNodeValue_);
                    return LuaEqualNode.doNil(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b100000000) != 0 /* is SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        EqualNode equalNode__ = this.truffleString_equalNode_;
                        if (equalNode__ != null) {
                            return LuaEqualNode.doTruffleString(leftNodeValue__, rightNodeValue__, equalNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b1000000000) != 0 /* is SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] */ && leftNodeValue_ instanceof LuaFunction) {
                LuaFunction leftNodeValue__ = (LuaFunction) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaFunction) {
                    LuaFunction rightNodeValue__ = (LuaFunction) rightNodeValue_;
                    return LuaEqualNode.doFunction(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000000000) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] */ && leftNodeValue_ instanceof LuaBoolean) {
                LuaBoolean leftNodeValue__ = (LuaBoolean) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaBoolean) {
                    LuaBoolean rightNodeValue__ = (LuaBoolean) rightNodeValue_;
                    return LuaEqualNode.doBoolean(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                    Generic0Data s9_ = this.generic0_cache;
                    while (s9_ != null) {
                        if ((s9_.leftInterop_.accepts(leftNodeValue_)) && (s9_.rightInterop_.accepts(rightNodeValue_))) {
                            return LuaEqualNode.doGeneric(leftNodeValue_, rightNodeValue_, s9_.leftInterop_, s9_.rightInterop_, s9_.metatableNode_);
                        }
                        s9_ = s9_.next_;
                    }
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                    Generic1Data s10_ = this.generic1_cache;
                    if (s10_ != null) {
                        return this.generic1Boundary(state_0, s10_, leftNodeValue_, rightNodeValue_);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b11111111110) == 0 /* only-active SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeBoolean_boolean_boolean5(state_0, frameValue);
        } else if ((state_0 & 0b11111110111) == 0 /* only-active SpecializationActive[LuaEqualNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeBoolean_long_long6(state_0, frameValue);
        } else if ((state_0 & 0b11111011111) == 0 /* only-active SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeBoolean_long7(state_0, frameValue);
        } else if ((state_0 & 0b11110111111) == 0 /* only-active SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] && SpecializationActive[LuaEqualNode.doLong(long, long)] && SpecializationActive[LuaEqualNode.doDouble(double, double)] && SpecializationActive[LuaEqualNode.doLongDouble(long, double)] && SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] && SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] && SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] && SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] && SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */)) {
            return executeBoolean_long8(state_0, frameValue);
        } else {
            return executeBoolean_generic9(state_0, frameValue);
        }
    }

    private boolean executeBoolean_boolean_boolean5(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        boolean leftNodeValue_;
        try {
            leftNodeValue_ = this.leftNode_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Object rightNodeValue = this.rightNode_.executeGeneric(frameValue);
            return executeAndSpecialize(ex.getResult(), rightNodeValue);
        }
        boolean rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */;
        return LuaEqualNode.doBoolean(leftNodeValue_, rightNodeValue_);
    }

    private boolean executeBoolean_long_long6(int state_0__, VirtualFrame frameValue) {
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
        assert (state_0 & 0b1000) != 0 /* is SpecializationActive[LuaEqualNode.doLong(long, long)] */;
        return LuaEqualNode.doLong(leftNodeValue_, rightNodeValue_);
    }

    private boolean executeBoolean_long7(int state_0__, VirtualFrame frameValue) {
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
        assert (state_0 & 0b100000) != 0 /* is SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */;
        if (rightNodeValue_ instanceof Double) {
            double rightNodeValue__ = (double) rightNodeValue_;
            return LuaEqualNode.doLongDouble(leftNodeValue_, rightNodeValue__);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    private boolean executeBoolean_long8(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        long rightNodeValue_;
        try {
            rightNodeValue_ = this.rightNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(leftNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */;
        if (leftNodeValue_ instanceof Double) {
            double leftNodeValue__ = (double) leftNodeValue_;
            return LuaEqualNode.doDoubleLong(leftNodeValue__, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private boolean generic1Boundary0(int state_0, Generic1Data s10_, Object leftNodeValue_, Object rightNodeValue_) {
        EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
        Node prev_ = encapsulating_.set(this);
        try {
            {
                InteropLibrary leftInterop__ = (INTEROP_LIBRARY_.getUncached(leftNodeValue_));
                InteropLibrary rightInterop__ = (INTEROP_LIBRARY_.getUncached(rightNodeValue_));
                return LuaEqualNode.doGeneric(leftNodeValue_, rightNodeValue_, leftInterop__, rightInterop__, s10_.metatableNode_);
            }
        } finally {
            encapsulating_.set(prev_);
        }
    }

    @ExplodeLoop
    private boolean executeBoolean_generic9(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] || SpecializationActive[LuaEqualNode.doLong(long, long)] || SpecializationActive[LuaEqualNode.doDouble(double, double)] || SpecializationActive[LuaEqualNode.doLongDouble(long, double)] || SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] || SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] || SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] || SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] || SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */ && leftNodeValue_ instanceof Boolean) {
                boolean leftNodeValue__ = (boolean) leftNodeValue_;
                if (rightNodeValue_ instanceof Boolean) {
                    boolean rightNodeValue__ = (boolean) rightNodeValue_;
                    return LuaEqualNode.doBoolean(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaEqualNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaEqualNode.doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110000) != 0 /* is SpecializationActive[LuaEqualNode.doDouble(double, double)] || SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && rightNodeValue_ instanceof Double) {
                double rightNodeValue__ = (double) rightNodeValue_;
                if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaEqualNode.doDouble(double, double)] */ && leftNodeValue_ instanceof Double) {
                    double leftNodeValue__ = (double) leftNodeValue_;
                    return LuaEqualNode.doDouble(leftNodeValue__, rightNodeValue__);
                }
                if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */ && leftNodeValue_ instanceof Long) {
                    long leftNodeValue__ = (long) leftNodeValue_;
                    return LuaEqualNode.doLongDouble(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */ && leftNodeValue_ instanceof Double) {
                double leftNodeValue__ = (double) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return LuaEqualNode.doDoubleLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000000) != 0 /* is SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] */ && LuaTypes.isLuaNil(leftNodeValue_)) {
                LuaNil leftNodeValue__ = LuaTypes.asLuaNil(leftNodeValue_);
                if (LuaTypes.isLuaNil(rightNodeValue_)) {
                    LuaNil rightNodeValue__ = LuaTypes.asLuaNil(rightNodeValue_);
                    return LuaEqualNode.doNil(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b100000000) != 0 /* is SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] */ && leftNodeValue_ instanceof TruffleString) {
                TruffleString leftNodeValue__ = (TruffleString) leftNodeValue_;
                if (rightNodeValue_ instanceof TruffleString) {
                    TruffleString rightNodeValue__ = (TruffleString) rightNodeValue_;
                    {
                        EqualNode equalNode__ = this.truffleString_equalNode_;
                        if (equalNode__ != null) {
                            return LuaEqualNode.doTruffleString(leftNodeValue__, rightNodeValue__, equalNode__);
                        }
                    }
                }
            }
            if ((state_0 & 0b1000000000) != 0 /* is SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] */ && leftNodeValue_ instanceof LuaFunction) {
                LuaFunction leftNodeValue__ = (LuaFunction) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaFunction) {
                    LuaFunction rightNodeValue__ = (LuaFunction) rightNodeValue_;
                    return LuaEqualNode.doFunction(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10000000000) != 0 /* is SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] */ && leftNodeValue_ instanceof LuaBoolean) {
                LuaBoolean leftNodeValue__ = (LuaBoolean) leftNodeValue_;
                if (rightNodeValue_ instanceof LuaBoolean) {
                    LuaBoolean rightNodeValue__ = (LuaBoolean) rightNodeValue_;
                    return LuaEqualNode.doBoolean(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b110) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] || SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                    Generic0Data s9_ = this.generic0_cache;
                    while (s9_ != null) {
                        if ((s9_.leftInterop_.accepts(leftNodeValue_)) && (s9_.rightInterop_.accepts(rightNodeValue_))) {
                            return LuaEqualNode.doGeneric(leftNodeValue_, rightNodeValue_, s9_.leftInterop_, s9_.rightInterop_, s9_.metatableNode_);
                        }
                        s9_ = s9_.next_;
                    }
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
                    Generic1Data s10_ = this.generic1_cache;
                    if (s10_ != null) {
                        return this.generic1Boundary0(state_0, s10_, leftNodeValue_, rightNodeValue_);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeBoolean(frameValue);
        return;
    }

    private boolean executeAndSpecialize(Object leftNodeValue, Object rightNodeValue) {
        int state_0 = this.state_0_;
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof Boolean) {
            boolean leftNodeValue_ = (boolean) leftNodeValue;
            if (rightNodeValue instanceof Boolean) {
                boolean rightNodeValue_ = (boolean) rightNodeValue;
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doBoolean(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof Long) {
            long leftNodeValue_ = (long) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaEqualNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (rightNodeValue instanceof Double) {
            double rightNodeValue_ = (double) rightNodeValue;
            if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof Double) {
                double leftNodeValue_ = (double) leftNodeValue;
                state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaEqualNode.doDouble(double, double)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doDouble(leftNodeValue_, rightNodeValue_);
            }
            if (leftNodeValue instanceof Long) {
                long leftNodeValue_ = (long) leftNodeValue;
                state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaEqualNode.doLongDouble(long, double)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doLongDouble(leftNodeValue_, rightNodeValue_);
            }
        }
        if (leftNodeValue instanceof Double) {
            double leftNodeValue_ = (double) leftNodeValue;
            if (rightNodeValue instanceof Long) {
                long rightNodeValue_ = (long) rightNodeValue;
                state_0 = state_0 | 0b1000000 /* add SpecializationActive[LuaEqualNode.doDoubleLong(double, long)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doDoubleLong(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && LuaTypes.isLuaNil(leftNodeValue)) {
            LuaNil leftNodeValue_ = LuaTypes.asLuaNil(leftNodeValue);
            if (LuaTypes.isLuaNil(rightNodeValue)) {
                LuaNil rightNodeValue_ = LuaTypes.asLuaNil(rightNodeValue);
                state_0 = state_0 | 0b10000000 /* add SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doNil(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof TruffleString) {
            TruffleString leftNodeValue_ = (TruffleString) leftNodeValue;
            if (rightNodeValue instanceof TruffleString) {
                TruffleString rightNodeValue_ = (TruffleString) rightNodeValue;
                EqualNode equalNode__ = this.insert((EqualNode.create()));
                Objects.requireNonNull(equalNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.truffleString_equalNode_ = equalNode__;
                state_0 = state_0 | 0b100000000 /* add SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doTruffleString(leftNodeValue_, rightNodeValue_, equalNode__);
            }
        }
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof LuaFunction) {
            LuaFunction leftNodeValue_ = (LuaFunction) leftNodeValue;
            if (rightNodeValue instanceof LuaFunction) {
                LuaFunction rightNodeValue_ = (LuaFunction) rightNodeValue;
                state_0 = state_0 | 0b1000000000 /* add SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doFunction(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b110)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] && SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */ && leftNodeValue instanceof LuaBoolean) {
            LuaBoolean leftNodeValue_ = (LuaBoolean) leftNodeValue;
            if (rightNodeValue instanceof LuaBoolean) {
                LuaBoolean rightNodeValue_ = (LuaBoolean) rightNodeValue;
                state_0 = state_0 | 0b10000000000 /* add SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] */;
                this.state_0_ = state_0;
                return LuaEqualNode.doBoolean(leftNodeValue_, rightNodeValue_);
            }
        }
        if (((state_0 & 0b100)) == 0 /* is-not SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */) {
            while (true) {
                int count9_ = 0;
                Generic0Data s9_ = GENERIC0_CACHE_UPDATER.getVolatile(this);
                Generic0Data s9_original = s9_;
                while (s9_ != null) {
                    if ((s9_.leftInterop_.accepts(leftNodeValue)) && (s9_.rightInterop_.accepts(rightNodeValue))) {
                        break;
                    }
                    count9_++;
                    s9_ = s9_.next_;
                }
                if (s9_ == null) {
                    // assert (s9_.leftInterop_.accepts(leftNodeValue));
                    // assert (s9_.rightInterop_.accepts(rightNodeValue));
                    if (count9_ < (3)) {
                        s9_ = this.insert(new Generic0Data(s9_original));
                        InteropLibrary leftInterop__ = s9_.insert((INTEROP_LIBRARY_.create(leftNodeValue)));
                        Objects.requireNonNull(leftInterop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                        s9_.leftInterop_ = leftInterop__;
                        InteropLibrary rightInterop__ = s9_.insert((INTEROP_LIBRARY_.create(rightNodeValue)));
                        Objects.requireNonNull(rightInterop__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                        s9_.rightInterop_ = rightInterop__;
                        s9_.metatableNode_ = s9_.insert((LuaMetatableNode.create()));
                        if (!GENERIC0_CACHE_UPDATER.compareAndSet(this, s9_original, s9_)) {
                            continue;
                        }
                        this.truffleString_equalNode_ = null;
                        state_0 = state_0 & 0xfffff866 /* remove SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)], SpecializationActive[LuaEqualNode.doLong(long, long)], SpecializationActive[LuaEqualNode.doDouble(double, double)], SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)], SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)], SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)], SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)] */;
                        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */;
                        this.state_0_ = state_0;
                    }
                }
                if (s9_ != null) {
                    return LuaEqualNode.doGeneric(leftNodeValue, rightNodeValue, s9_.leftInterop_, s9_.rightInterop_, s9_.metatableNode_);
                }
                break;
            }
        }
        {
            InteropLibrary rightInterop__ = null;
            InteropLibrary leftInterop__ = null;
            {
                EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
                Node prev_ = encapsulating_.set(this);
                try {
                    Generic1Data s10_ = this.insert(new Generic1Data());
                    leftInterop__ = (INTEROP_LIBRARY_.getUncached(leftNodeValue));
                    rightInterop__ = (INTEROP_LIBRARY_.getUncached(rightNodeValue));
                    s10_.metatableNode_ = s10_.insert((LuaMetatableNode.create()));
                    VarHandle.storeStoreFence();
                    this.generic1_cache = s10_;
                    this.truffleString_equalNode_ = null;
                    this.generic0_cache = null;
                    state_0 = state_0 & 0xfffff864 /* remove SpecializationActive[LuaEqualNode.doBoolean(boolean, boolean)], SpecializationActive[LuaEqualNode.doLong(long, long)], SpecializationActive[LuaEqualNode.doDouble(double, double)], SpecializationActive[LuaEqualNode.doNil(LuaNil, LuaNil)], SpecializationActive[LuaEqualNode.doTruffleString(TruffleString, TruffleString, EqualNode)], SpecializationActive[LuaEqualNode.doFunction(LuaFunction, LuaFunction)], SpecializationActive[LuaEqualNode.doBoolean(LuaBoolean, LuaBoolean)], SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */;
                    state_0 = state_0 | 0b100 /* add SpecializationActive[LuaEqualNode.doGeneric(Object, Object, InteropLibrary, InteropLibrary, LuaMetatableNode)] */;
                    this.state_0_ = state_0;
                    return LuaEqualNode.doGeneric(leftNodeValue, rightNodeValue, leftInterop__, rightInterop__, s10_.metatableNode_);
                } finally {
                    encapsulating_.set(prev_);
                }
            }
        }
    }

    @NeverDefault
    public static LuaEqualNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaEqualNodeGen(leftNode, rightNode);
    }

    @GeneratedBy(LuaEqualNode.class)
    @DenyReplace
    private static final class Generic0Data extends Node implements SpecializationDataNode {

        @Child Generic0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaEqualNode#doGeneric}
         *   Parameter: {@link InteropLibrary} leftInterop</pre> */
        @Child InteropLibrary leftInterop_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaEqualNode#doGeneric}
         *   Parameter: {@link InteropLibrary} rightInterop</pre> */
        @Child InteropLibrary rightInterop_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaEqualNode#doGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        Generic0Data(Generic0Data next_) {
            this.next_ = next_;
        }

    }
    @GeneratedBy(LuaEqualNode.class)
    @DenyReplace
    private static final class Generic1Data extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaEqualNode#doGeneric}
         *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
        @Child LuaMetatableNode metatableNode_;

        Generic1Data() {
        }

    }
}

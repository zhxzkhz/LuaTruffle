// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaString;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaCoerceToNumberNode#doLong}
 *     Activation probability: 0.32000
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaCoerceToNumberNode#doDouble}
 *     Activation probability: 0.26000
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaCoerceToNumberNode#doString(TruffleString)}
 *     Activation probability: 0.20000
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaCoerceToNumberNode#doString(LuaString)}
 *     Activation probability: 0.14000
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaCoerceToNumberNode#doOther}
 *     Activation probability: 0.08000
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaCoerceToNumberNode.class)
@SuppressWarnings("javadoc")
public final class LuaCoerceToNumberNodeGen extends LuaCoerceToNumberNode {

    private static final Uncached UNCACHED = new Uncached();

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaCoerceToNumberNode#doLong}
     *   1: SpecializationActive {@link LuaCoerceToNumberNode#doDouble}
     *   2: SpecializationActive {@link LuaCoerceToNumberNode#doString(TruffleString)}
     *   3: SpecializationActive {@link LuaCoerceToNumberNode#doString(LuaString)}
     *   4: SpecializationActive {@link LuaCoerceToNumberNode#doOther}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaCoerceToNumberNodeGen() {
    }

    @Override
    public Object execute(Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doLong(long)] || SpecializationActive[LuaCoerceToNumberNode.doDouble(double)] || SpecializationActive[LuaCoerceToNumberNode.doString(TruffleString)] || SpecializationActive[LuaCoerceToNumberNode.doString(LuaString)] || SpecializationActive[LuaCoerceToNumberNode.doOther(Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doLong(long)] */ && arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                if ((LuaCoerceToNumberNode.isLong(arg0Value_))) {
                    return doLong(arg0Value_);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doDouble(double)] */ && arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                if ((LuaCoerceToNumberNode.isDouble(arg0Value_))) {
                    return doDouble(arg0Value_);
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doString(TruffleString)] */ && arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return doString(arg0Value_);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doString(LuaString)] */ && arg0Value instanceof LuaString) {
                LuaString arg0Value_ = (LuaString) arg0Value;
                return doString(arg0Value_);
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaCoerceToNumberNode.doOther(Object)] */) {
                if ((!(LuaCoerceToNumberNode.isNumber(arg0Value))) && (!(LuaCoerceToNumberNode.isTruffleString(arg0Value)))) {
                    return doOther(arg0Value);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Object executeAndSpecialize(Object arg0Value) {
        int state_0 = this.state_0_;
        if (arg0Value instanceof Long) {
            long arg0Value_ = (long) arg0Value;
            if ((LuaCoerceToNumberNode.isLong(arg0Value_))) {
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaCoerceToNumberNode.doLong(long)] */;
                this.state_0_ = state_0;
                return doLong(arg0Value_);
            }
        }
        if (arg0Value instanceof Double) {
            double arg0Value_ = (double) arg0Value;
            if ((LuaCoerceToNumberNode.isDouble(arg0Value_))) {
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaCoerceToNumberNode.doDouble(double)] */;
                this.state_0_ = state_0;
                return doDouble(arg0Value_);
            }
        }
        if (arg0Value instanceof TruffleString) {
            TruffleString arg0Value_ = (TruffleString) arg0Value;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaCoerceToNumberNode.doString(TruffleString)] */;
            this.state_0_ = state_0;
            return doString(arg0Value_);
        }
        if (arg0Value instanceof LuaString) {
            LuaString arg0Value_ = (LuaString) arg0Value;
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaCoerceToNumberNode.doString(LuaString)] */;
            this.state_0_ = state_0;
            return doString(arg0Value_);
        }
        if ((!(LuaCoerceToNumberNode.isNumber(arg0Value))) && (!(LuaCoerceToNumberNode.isTruffleString(arg0Value)))) {
            state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaCoerceToNumberNode.doOther(Object)] */;
            this.state_0_ = state_0;
            return doOther(arg0Value);
        }
        throw new UnsupportedSpecializationException(this, null, arg0Value);
    }

    @TruffleBoundary
    private static UnsupportedSpecializationException newUnsupportedSpecializationException1(Node thisNode_, Object arg0Value) {
        return new UnsupportedSpecializationException(thisNode_, null, arg0Value);
    }

    @NeverDefault
    public static LuaCoerceToNumberNode create() {
        return new LuaCoerceToNumberNodeGen();
    }

    @NeverDefault
    public static LuaCoerceToNumberNode getUncached() {
        return LuaCoerceToNumberNodeGen.UNCACHED;
    }

    @GeneratedBy(LuaCoerceToNumberNode.class)
    @DenyReplace
    private static final class Uncached extends LuaCoerceToNumberNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public Object execute(Object arg0Value) {
            if (arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                if ((LuaCoerceToNumberNode.isLong(arg0Value_))) {
                    return doLong(arg0Value_);
                }
            }
            if (arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                if ((LuaCoerceToNumberNode.isDouble(arg0Value_))) {
                    return doDouble(arg0Value_);
                }
            }
            if (arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return doString(arg0Value_);
            }
            if (arg0Value instanceof LuaString) {
                LuaString arg0Value_ = (LuaString) arg0Value;
                return doString(arg0Value_);
            }
            if ((!(LuaCoerceToNumberNode.isNumber(arg0Value))) && (!(LuaCoerceToNumberNode.isTruffleString(arg0Value)))) {
                return doOther(arg0Value);
            }
            throw newUnsupportedSpecializationException1(this, arg0Value);
        }

    }
}

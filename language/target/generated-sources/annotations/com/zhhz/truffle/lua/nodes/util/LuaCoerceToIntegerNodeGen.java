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

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaCoerceToIntegerNode#doLong}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaCoerceToIntegerNode#doDouble}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaCoerceToIntegerNode#doString}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaCoerceToIntegerNode#doOther}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaCoerceToIntegerNode.class)
@SuppressWarnings("javadoc")
public final class LuaCoerceToIntegerNodeGen extends LuaCoerceToIntegerNode {

    private static final Uncached UNCACHED = new Uncached();

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaCoerceToIntegerNode#doLong}
     *   1: SpecializationActive {@link LuaCoerceToIntegerNode#doDouble}
     *   2: SpecializationActive {@link LuaCoerceToIntegerNode#doString}
     *   3: SpecializationActive {@link LuaCoerceToIntegerNode#doOther}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaCoerceToIntegerNodeGen() {
    }

    @Override
    public Long execute(Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaCoerceToIntegerNode.doLong(long)] || SpecializationActive[LuaCoerceToIntegerNode.doDouble(double)] || SpecializationActive[LuaCoerceToIntegerNode.doString(TruffleString)] || SpecializationActive[LuaCoerceToIntegerNode.doOther(Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaCoerceToIntegerNode.doLong(long)] */ && arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                return doLong(arg0Value_);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaCoerceToIntegerNode.doDouble(double)] */ && arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                return doDouble(arg0Value_);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaCoerceToIntegerNode.doString(TruffleString)] */ && arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return doString(arg0Value_);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaCoerceToIntegerNode.doOther(Object)] */) {
                if ((!(LuaCoerceToIntegerNode.isLong(arg0Value))) && (!(LuaCoerceToIntegerNode.isDouble(arg0Value))) && (!(LuaCoerceToIntegerNode.isTruffleString(arg0Value)))) {
                    return doOther(arg0Value);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private Long executeAndSpecialize(Object arg0Value) {
        int state_0 = this.state_0_;
        if (arg0Value instanceof Long) {
            long arg0Value_ = (long) arg0Value;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaCoerceToIntegerNode.doLong(long)] */;
            this.state_0_ = state_0;
            return doLong(arg0Value_);
        }
        if (arg0Value instanceof Double) {
            double arg0Value_ = (double) arg0Value;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaCoerceToIntegerNode.doDouble(double)] */;
            this.state_0_ = state_0;
            return doDouble(arg0Value_);
        }
        if (arg0Value instanceof TruffleString) {
            TruffleString arg0Value_ = (TruffleString) arg0Value;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaCoerceToIntegerNode.doString(TruffleString)] */;
            this.state_0_ = state_0;
            return doString(arg0Value_);
        }
        if ((!(LuaCoerceToIntegerNode.isLong(arg0Value))) && (!(LuaCoerceToIntegerNode.isDouble(arg0Value))) && (!(LuaCoerceToIntegerNode.isTruffleString(arg0Value)))) {
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaCoerceToIntegerNode.doOther(Object)] */;
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
    public static LuaCoerceToIntegerNode create() {
        return new LuaCoerceToIntegerNodeGen();
    }

    @NeverDefault
    public static LuaCoerceToIntegerNode getUncached() {
        return LuaCoerceToIntegerNodeGen.UNCACHED;
    }

    @GeneratedBy(LuaCoerceToIntegerNode.class)
    @DenyReplace
    private static final class Uncached extends LuaCoerceToIntegerNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public Long execute(Object arg0Value) {
            if (arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                return doLong(arg0Value_);
            }
            if (arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                return doDouble(arg0Value_);
            }
            if (arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return doString(arg0Value_);
            }
            if ((!(LuaCoerceToIntegerNode.isLong(arg0Value))) && (!(LuaCoerceToIntegerNode.isDouble(arg0Value))) && (!(LuaCoerceToIntegerNode.isTruffleString(arg0Value)))) {
                return doOther(arg0Value);
            }
            throw newUnsupportedSpecializationException1(this, arg0Value);
        }

    }
}

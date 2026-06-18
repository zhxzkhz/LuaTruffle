// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaNil;
import java.lang.invoke.VarHandle;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaCoerceToStringNode#fromNil}
 *     Activation probability: 0.23929
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromBoolean}
 *     Activation probability: 0.20714
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromLong}
 *     Activation probability: 0.17500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromDouble}
 *     Activation probability: 0.14286
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromTruffleString}
 *     Activation probability: 0.11071
 *     With/without class size: 5/0 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromObject(Object, LuaMetatableNode)}
 *     Activation probability: 0.07857
 *     With/without class size: 5/4 bytes
 *   Specialization {@link LuaCoerceToStringNode#fromObject(Object)}
 *     Activation probability: 0.04643
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaCoerceToStringNode.class)
@SuppressWarnings("javadoc")
public final class LuaCoerceToStringNodeGen extends LuaCoerceToStringNode {

    private static final Uncached UNCACHED = new Uncached();

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaCoerceToStringNode#fromNil}
     *   1: SpecializationActive {@link LuaCoerceToStringNode#fromBoolean}
     *   2: SpecializationActive {@link LuaCoerceToStringNode#fromLong}
     *   3: SpecializationActive {@link LuaCoerceToStringNode#fromDouble}
     *   4: SpecializationActive {@link LuaCoerceToStringNode#fromTruffleString}
     *   5: SpecializationActive {@link LuaCoerceToStringNode#fromObject(Object, LuaMetatableNode)}
     *   6: SpecializationActive {@link LuaCoerceToStringNode#fromObject(Object)}
     * </pre> */
    @CompilationFinal private int state_0_;
    @Child private FromObjectData fromObject_cache;

    private LuaCoerceToStringNodeGen() {
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object arg0Value) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromNil(LuaNil)] */) && arg0Value instanceof LuaNil) {
            return false;
        }
        if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromBoolean(boolean)] */) && arg0Value instanceof Boolean) {
            return false;
        }
        if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromLong(long)] */) && arg0Value instanceof Long) {
            return false;
        }
        if (!((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromDouble(double)] */) && arg0Value instanceof Double) {
            return false;
        }
        if (!((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromTruffleString(TruffleString)] */) && arg0Value instanceof TruffleString) {
            return false;
        }
        if (!((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromObject(Object, LuaMetatableNode)] */) && (!(isPrimitive(arg0Value)))) {
            return false;
        }
        return true;
    }

    @Override
    public TruffleString execute(Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromNil(LuaNil)] || SpecializationActive[LuaCoerceToStringNode.fromBoolean(boolean)] || SpecializationActive[LuaCoerceToStringNode.fromLong(long)] || SpecializationActive[LuaCoerceToStringNode.fromDouble(double)] || SpecializationActive[LuaCoerceToStringNode.fromTruffleString(TruffleString)] || SpecializationActive[LuaCoerceToStringNode.fromObject(Object, LuaMetatableNode)] || SpecializationActive[LuaCoerceToStringNode.fromObject(Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromNil(LuaNil)] */ && arg0Value instanceof LuaNil) {
                LuaNil arg0Value_ = (LuaNil) arg0Value;
                return fromNil(arg0Value_);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromBoolean(boolean)] */ && arg0Value instanceof Boolean) {
                boolean arg0Value_ = (boolean) arg0Value;
                return fromBoolean(arg0Value_);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromLong(long)] */ && arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                return fromLong(arg0Value_);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromDouble(double)] */ && arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                return fromDouble(arg0Value_);
            }
            if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromTruffleString(TruffleString)] */ && arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return fromTruffleString(arg0Value_);
            }
            if ((state_0 & 0b1100000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromObject(Object, LuaMetatableNode)] || SpecializationActive[LuaCoerceToStringNode.fromObject(Object)] */) {
                if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromObject(Object, LuaMetatableNode)] */) {
                    FromObjectData s5_ = this.fromObject_cache;
                    if (s5_ != null) {
                        if ((!(isPrimitive(arg0Value)))) {
                            return fromObject(arg0Value, s5_.metaNode_);
                        }
                    }
                }
                if ((state_0 & 0b1000000) != 0 /* is SpecializationActive[LuaCoerceToStringNode.fromObject(Object)] */) {
                    if (fallbackGuard_(state_0, arg0Value)) {
                        return fromObject(arg0Value);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(arg0Value);
    }

    private TruffleString executeAndSpecialize(Object arg0Value) {
        int state_0 = this.state_0_;
        if (arg0Value instanceof LuaNil) {
            LuaNil arg0Value_ = (LuaNil) arg0Value;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaCoerceToStringNode.fromNil(LuaNil)] */;
            this.state_0_ = state_0;
            return fromNil(arg0Value_);
        }
        if (arg0Value instanceof Boolean) {
            boolean arg0Value_ = (boolean) arg0Value;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaCoerceToStringNode.fromBoolean(boolean)] */;
            this.state_0_ = state_0;
            return fromBoolean(arg0Value_);
        }
        if (arg0Value instanceof Long) {
            long arg0Value_ = (long) arg0Value;
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaCoerceToStringNode.fromLong(long)] */;
            this.state_0_ = state_0;
            return fromLong(arg0Value_);
        }
        if (arg0Value instanceof Double) {
            double arg0Value_ = (double) arg0Value;
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaCoerceToStringNode.fromDouble(double)] */;
            this.state_0_ = state_0;
            return fromDouble(arg0Value_);
        }
        if (arg0Value instanceof TruffleString) {
            TruffleString arg0Value_ = (TruffleString) arg0Value;
            state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaCoerceToStringNode.fromTruffleString(TruffleString)] */;
            this.state_0_ = state_0;
            return fromTruffleString(arg0Value_);
        }
        if ((!(isPrimitive(arg0Value)))) {
            FromObjectData s5_ = this.insert(new FromObjectData());
            s5_.metaNode_ = s5_.insert((LuaMetatableNode.create()));
            VarHandle.storeStoreFence();
            this.fromObject_cache = s5_;
            state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaCoerceToStringNode.fromObject(Object, LuaMetatableNode)] */;
            this.state_0_ = state_0;
            return fromObject(arg0Value, s5_.metaNode_);
        }
        state_0 = state_0 | 0b1000000 /* add SpecializationActive[LuaCoerceToStringNode.fromObject(Object)] */;
        this.state_0_ = state_0;
        return fromObject(arg0Value);
    }

    @NeverDefault
    public static LuaCoerceToStringNode create() {
        return new LuaCoerceToStringNodeGen();
    }

    @NeverDefault
    public static LuaCoerceToStringNode getUncached() {
        return LuaCoerceToStringNodeGen.UNCACHED;
    }

    @GeneratedBy(LuaCoerceToStringNode.class)
    @DenyReplace
    private static final class FromObjectData extends Node implements SpecializationDataNode {

        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaCoerceToStringNode#fromObject(Object, LuaMetatableNode)}
         *   Parameter: {@link LuaMetatableNode} metaNode</pre> */
        @Child LuaMetatableNode metaNode_;

        FromObjectData() {
        }

    }
    @GeneratedBy(LuaCoerceToStringNode.class)
    @DenyReplace
    private static final class Uncached extends LuaCoerceToStringNode implements UnadoptableNode {

        @TruffleBoundary
        @Override
        public TruffleString execute(Object arg0Value) {
            if (arg0Value instanceof LuaNil) {
                LuaNil arg0Value_ = (LuaNil) arg0Value;
                return fromNil(arg0Value_);
            }
            if (arg0Value instanceof Boolean) {
                boolean arg0Value_ = (boolean) arg0Value;
                return fromBoolean(arg0Value_);
            }
            if (arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                return fromLong(arg0Value_);
            }
            if (arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                return fromDouble(arg0Value_);
            }
            if (arg0Value instanceof TruffleString) {
                TruffleString arg0Value_ = (TruffleString) arg0Value;
                return fromTruffleString(arg0Value_);
            }
            if ((!(isPrimitive(arg0Value)))) {
                return fromObject(arg0Value, (LuaMetatableNodeGen.getUncached()));
            }
            return fromObject(arg0Value);
        }

    }
}

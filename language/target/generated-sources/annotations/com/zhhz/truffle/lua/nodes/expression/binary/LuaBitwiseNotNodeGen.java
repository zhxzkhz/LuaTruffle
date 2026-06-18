// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import java.lang.invoke.VarHandle;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaBitwiseNotNode#doLong}
 *     Activation probability: 0.65000
 *     With/without class size: 11/0 bytes
 *   Specialization {@link LuaBitwiseNotNode#doGeneric}
 *     Activation probability: 0.35000
 *     With/without class size: 12/8 bytes
 * </pre> */
@GeneratedBy(LuaBitwiseNotNode.class)
@SuppressWarnings("javadoc")
public final class LuaBitwiseNotNodeGen extends LuaBitwiseNotNode {

    @Child private LuaExpressionNode operandNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaBitwiseNotNode#doLong}
     *   1: SpecializationActive {@link LuaBitwiseNotNode#doGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaBitwiseNotNode#doGeneric}
     *   Parameter: {@link LuaCoerceToIntegerNode} coerceNode</pre> */
    @Child private LuaCoerceToIntegerNode fallback_coerceNode_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaBitwiseNotNode#doGeneric}
     *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
    @Child private LuaMetatableNode fallback_metatableNode_;

    private LuaBitwiseNotNodeGen(LuaExpressionNode operandNode) {
        this.operandNode_ = operandNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object operandNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doLong(long)] */) && operandNodeValue instanceof Long) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) == 0 /* only-active SpecializationActive[LuaBitwiseNotNode.doLong(long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaBitwiseNotNode.doLong(long)] && SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */)) {
            return executeGeneric_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_long0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        long operandNodeValue_;
        try {
            operandNodeValue_ = this.operandNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doLong(long)] */;
        return doLong(operandNodeValue_);
    }

    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object operandNodeValue_ = this.operandNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaBitwiseNotNode.doLong(long)] || SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doLong(long)] */ && operandNodeValue_ instanceof Long) {
                long operandNodeValue__ = (long) operandNodeValue_;
                return doLong(operandNodeValue__);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
                {
                    LuaCoerceToIntegerNode coerceNode__ = this.fallback_coerceNode_;
                    if (coerceNode__ != null) {
                        LuaMetatableNode metatableNode__ = this.fallback_metatableNode_;
                        if (metatableNode__ != null) {
                            if (fallbackGuard_(state_0, operandNodeValue_)) {
                                return doGeneric(operandNodeValue_, coerceNode__, metatableNode__);
                            }
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(operandNodeValue_);
    }

    @Override
    public long executeLong(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
            return LuaTypesGen.expectLong(this.executeGeneric(frameValue));
        }
        long operandNodeValue_;
        try {
            operandNodeValue_ = this.operandNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return LuaTypesGen.expectLong(executeAndSpecialize(ex.getResult()));
        }
        if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseNotNode.doLong(long)] */) {
            return doLong(operandNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectLong(executeAndSpecialize(operandNodeValue_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b10) == 0 /* only-active SpecializationActive[LuaBitwiseNotNode.doLong(long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaBitwiseNotNode.doLong(long)] && SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */)) {
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

    private Object executeAndSpecialize(Object operandNodeValue) {
        int state_0 = this.state_0_;
        if (operandNodeValue instanceof Long) {
            long operandNodeValue_ = (long) operandNodeValue;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaBitwiseNotNode.doLong(long)] */;
            this.state_0_ = state_0;
            return doLong(operandNodeValue_);
        }
        LuaCoerceToIntegerNode coerceNode__ = this.insert((LuaCoerceToIntegerNode.create()));
        Objects.requireNonNull(coerceNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.fallback_coerceNode_ = coerceNode__;
        LuaMetatableNode metatableNode__ = this.insert((LuaMetatableNode.create()));
        Objects.requireNonNull(metatableNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.fallback_metatableNode_ = metatableNode__;
        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaBitwiseNotNode.doGeneric(Object, LuaCoerceToIntegerNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return doGeneric(operandNodeValue, coerceNode__, metatableNode__);
    }

    @NeverDefault
    public static LuaBitwiseNotNode create(LuaExpressionNode operandNode) {
        return new LuaBitwiseNotNodeGen(operandNode);
    }

}

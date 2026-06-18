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
 *   Specialization {@link LuaBitwiseOrNode#doLong}
 *     Activation probability: 0.65000
 *     With/without class size: 11/0 bytes
 *   Specialization {@link LuaBitwiseOrNode#doGeneric}
 *     Activation probability: 0.35000
 *     With/without class size: 13/12 bytes
 * </pre> */
@GeneratedBy(LuaBitwiseOrNode.class)
@SuppressWarnings("javadoc")
public final class LuaBitwiseOrNodeGen extends LuaBitwiseOrNode {

    @Child private LuaExpressionNode leftNode_;
    @Child private LuaExpressionNode rightNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaBitwiseOrNode#doLong}
     *   1: SpecializationActive {@link LuaBitwiseOrNode#doGeneric}
     * </pre> */
    @CompilationFinal private int state_0_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaBitwiseOrNode#doGeneric}
     *   Parameter: {@link LuaCoerceToIntegerNode} coerceLeft</pre> */
    @Child private LuaCoerceToIntegerNode fallback_coerceLeft_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaBitwiseOrNode#doGeneric}
     *   Parameter: {@link LuaCoerceToIntegerNode} coerceRight</pre> */
    @Child private LuaCoerceToIntegerNode fallback_coerceRight_;
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaBitwiseOrNode#doGeneric}
     *   Parameter: {@link LuaMetatableNode} metatableNode</pre> */
    @Child private LuaMetatableNode fallback_metatableNode_;

    private LuaBitwiseOrNodeGen(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        this.leftNode_ = leftNode;
        this.rightNode_ = rightNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object leftNodeValue, Object rightNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */) && leftNodeValue instanceof Long && rightNodeValue instanceof Long) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) == 0 /* only-active SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] && SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */)) {
            return executeGeneric_long_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
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
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */;
        return doLong(leftNodeValue_, rightNodeValue_);
    }

    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object leftNodeValue_ = this.leftNode_.executeGeneric(frameValue);
        Object rightNodeValue_ = this.rightNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] || SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */ && leftNodeValue_ instanceof Long) {
                long leftNodeValue__ = (long) leftNodeValue_;
                if (rightNodeValue_ instanceof Long) {
                    long rightNodeValue__ = (long) rightNodeValue_;
                    return doLong(leftNodeValue__, rightNodeValue__);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
                {
                    LuaCoerceToIntegerNode coerceLeft__ = this.fallback_coerceLeft_;
                    if (coerceLeft__ != null) {
                        LuaCoerceToIntegerNode coerceRight__ = this.fallback_coerceRight_;
                        if (coerceRight__ != null) {
                            LuaMetatableNode metatableNode__ = this.fallback_metatableNode_;
                            if (metatableNode__ != null) {
                                if (fallbackGuard_(state_0, leftNodeValue_, rightNodeValue_)) {
                                    return doGeneric(leftNodeValue_, rightNodeValue_, coerceLeft__, coerceRight__, metatableNode__);
                                }
                            }
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(leftNodeValue_, rightNodeValue_);
    }

    @Override
    public long executeLong(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */) {
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
        if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */) {
            return doLong(leftNodeValue_, rightNodeValue_);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectLong(executeAndSpecialize(leftNodeValue_, rightNodeValue_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b10) == 0 /* only-active SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] && SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */)) {
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
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaBitwiseOrNode.doLong(long, long)] */;
                this.state_0_ = state_0;
                return doLong(leftNodeValue_, rightNodeValue_);
            }
        }
        LuaCoerceToIntegerNode coerceLeft__ = this.insert((LuaCoerceToIntegerNode.create()));
        Objects.requireNonNull(coerceLeft__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.fallback_coerceLeft_ = coerceLeft__;
        LuaCoerceToIntegerNode coerceRight__ = this.insert((LuaCoerceToIntegerNode.create()));
        Objects.requireNonNull(coerceRight__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.fallback_coerceRight_ = coerceRight__;
        LuaMetatableNode metatableNode__ = this.insert((LuaMetatableNode.create()));
        Objects.requireNonNull(metatableNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
        VarHandle.storeStoreFence();
        this.fallback_metatableNode_ = metatableNode__;
        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaBitwiseOrNode.doGeneric(Object, Object, LuaCoerceToIntegerNode, LuaCoerceToIntegerNode, LuaMetatableNode)] */;
        this.state_0_ = state_0;
        return doGeneric(leftNodeValue, rightNodeValue, coerceLeft__, coerceRight__, metatableNode__);
    }

    @NeverDefault
    public static LuaBitwiseOrNode create(LuaExpressionNode leftNode, LuaExpressionNode rightNode) {
        return new LuaBitwiseOrNodeGen(leftNode, rightNode);
    }

}

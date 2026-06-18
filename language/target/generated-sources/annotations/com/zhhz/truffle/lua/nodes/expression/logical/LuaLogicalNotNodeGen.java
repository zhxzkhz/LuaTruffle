// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.logical;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaLogicalNotNode#notFalse}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaLogicalNotNode#notNil}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaLogicalNotNode#notTruthy}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaLogicalNotNode#typeError}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaLogicalNotNode.class)
@SuppressWarnings("javadoc")
public final class LuaLogicalNotNodeGen extends LuaLogicalNotNode {

    @Child private LuaExpressionNode operandNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaLogicalNotNode#notFalse}
     *   1: SpecializationActive {@link LuaLogicalNotNode#notNil}
     *   2: SpecializationActive {@link LuaLogicalNotNode#notTruthy}
     *   3: SpecializationActive {@link LuaLogicalNotNode#typeError}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaLogicalNotNodeGen(LuaExpressionNode operandNode) {
        this.operandNode_ = operandNode;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object operandNodeValue) {
        if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLogicalNotNode.notFalse(LuaBoolean)] */) && operandNodeValue instanceof LuaBoolean) {
            return false;
        }
        if (!((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLogicalNotNode.notNil(LuaNil)] */) && LuaTypes.isLuaNil(operandNodeValue)) {
            return false;
        }
        if (!((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] */) && (!(LuaLogicalNotNode.isLuaFalse(operandNodeValue))) && (!(LuaLogicalNotNode.isLuaNil(operandNodeValue)))) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        Object operandNodeValue_ = this.operandNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaLogicalNotNode.notFalse(LuaBoolean)] || SpecializationActive[LuaLogicalNotNode.notNil(LuaNil)] || SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] || SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaLogicalNotNode.notFalse(LuaBoolean)] */ && operandNodeValue_ instanceof LuaBoolean) {
                LuaBoolean operandNodeValue__ = (LuaBoolean) operandNodeValue_;
                return notFalse(operandNodeValue__);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaLogicalNotNode.notNil(LuaNil)] */ && LuaTypes.isLuaNil(operandNodeValue_)) {
                LuaNil operandNodeValue__ = LuaTypes.asLuaNil(operandNodeValue_);
                return notNil(operandNodeValue__);
            }
            if ((state_0 & 0b1100) != 0 /* is SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] || SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */) {
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] */) {
                    if ((!(LuaLogicalNotNode.isLuaFalse(operandNodeValue_))) && (!(LuaLogicalNotNode.isLuaNil(operandNodeValue_)))) {
                        return notTruthy(operandNodeValue_);
                    }
                }
                if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */) {
                    {
                        Node node__ = (this);
                        if (fallbackGuard_(state_0, operandNodeValue_)) {
                            return typeError(operandNodeValue_, node__);
                        }
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(operandNodeValue_);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        Object operandNodeValue_ = this.operandNode_.executeGeneric(frameValue);
        if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */) {
            {
                Node node__ = (this);
                if (fallbackGuard_(state_0, operandNodeValue_)) {
                    return typeError(operandNodeValue_, node__);
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(operandNodeValue_));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b111) == 0 /* only-active SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaLogicalNotNode.notFalse(LuaBoolean)] && SpecializationActive[LuaLogicalNotNode.notNil(LuaNil)] && SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] && SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */)) {
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

    private Object executeAndSpecialize(Object operandNodeValue) {
        int state_0 = this.state_0_;
        if (operandNodeValue instanceof LuaBoolean) {
            LuaBoolean operandNodeValue_ = (LuaBoolean) operandNodeValue;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaLogicalNotNode.notFalse(LuaBoolean)] */;
            this.state_0_ = state_0;
            return notFalse(operandNodeValue_);
        }
        if (LuaTypes.isLuaNil(operandNodeValue)) {
            LuaNil operandNodeValue_ = LuaTypes.asLuaNil(operandNodeValue);
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaLogicalNotNode.notNil(LuaNil)] */;
            this.state_0_ = state_0;
            return notNil(operandNodeValue_);
        }
        if ((!(LuaLogicalNotNode.isLuaFalse(operandNodeValue))) && (!(LuaLogicalNotNode.isLuaNil(operandNodeValue)))) {
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaLogicalNotNode.notTruthy(Object)] */;
            this.state_0_ = state_0;
            return notTruthy(operandNodeValue);
        }
        {
            Node node__ = null;
            node__ = (this);
            state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaLogicalNotNode.typeError(Object, Node)] */;
            this.state_0_ = state_0;
            return typeError(operandNodeValue, node__);
        }
    }

    @NeverDefault
    public static LuaLogicalNotNode create(LuaExpressionNode operandNode) {
        return new LuaLogicalNotNodeGen(operandNode);
    }

}

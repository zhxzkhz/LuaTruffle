// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaToBooleanNode#doBoolean}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaToBooleanNode#doLuaBoolean}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaToBooleanNode#doLuaNil}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaToBooleanNode#doInterop}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaToBooleanNode.class)
@SuppressWarnings("javadoc")
public final class LuaToBooleanNodeGen extends LuaToBooleanNode {

    @Child private LuaExpressionNode child0_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaToBooleanNode#doBoolean}
     *   1: SpecializationActive {@link LuaToBooleanNode#doLuaBoolean}
     *   2: SpecializationActive {@link LuaToBooleanNode#doLuaNil}
     *   3: SpecializationActive {@link LuaToBooleanNode#doInterop}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaToBooleanNodeGen(LuaExpressionNode child0) {
        this.child0_ = child0;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b1110) == 0 /* only-active SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] && SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] && SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] && SpecializationActive[LuaToBooleanNode.doInterop(Object)] */)) {
            return executeGeneric_boolean0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_boolean0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        boolean child0Value_;
        try {
            child0Value_ = this.child0_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */;
        return LuaToBooleanNode.doBoolean(child0Value_);
    }

    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object child0Value_ = this.child0_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] || SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] || SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] || SpecializationActive[LuaToBooleanNode.doInterop(Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */ && child0Value_ instanceof Boolean) {
                boolean child0Value__ = (boolean) child0Value_;
                return LuaToBooleanNode.doBoolean(child0Value__);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] */ && child0Value_ instanceof LuaBoolean) {
                LuaBoolean child0Value__ = (LuaBoolean) child0Value_;
                return LuaToBooleanNode.doLuaBoolean(child0Value__);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] */ && LuaTypes.isLuaNil(child0Value_)) {
                LuaNil child0Value__ = LuaTypes.asLuaNil(child0Value_);
                return LuaToBooleanNode.doLuaNil(child0Value__);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaToBooleanNode.doInterop(Object)] */) {
                return LuaToBooleanNode.doInterop(child0Value_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(child0Value_);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b1110) == 0 /* only-active SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] && SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] && SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] && SpecializationActive[LuaToBooleanNode.doInterop(Object)] */)) {
            return executeBoolean_boolean2(state_0, frameValue);
        } else {
            return executeBoolean_generic3(state_0, frameValue);
        }
    }

    private boolean executeBoolean_boolean2(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        boolean child0Value_;
        try {
            child0Value_ = this.child0_.executeBoolean(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */;
        return LuaToBooleanNode.doBoolean(child0Value_);
    }

    private boolean executeBoolean_generic3(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object child0Value_ = this.child0_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] || SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] || SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] || SpecializationActive[LuaToBooleanNode.doInterop(Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */ && child0Value_ instanceof Boolean) {
                boolean child0Value__ = (boolean) child0Value_;
                return LuaToBooleanNode.doBoolean(child0Value__);
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] */ && child0Value_ instanceof LuaBoolean) {
                LuaBoolean child0Value__ = (LuaBoolean) child0Value_;
                return LuaToBooleanNode.doLuaBoolean(child0Value__);
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] */ && LuaTypes.isLuaNil(child0Value_)) {
                LuaNil child0Value__ = LuaTypes.asLuaNil(child0Value_);
                return LuaToBooleanNode.doLuaNil(child0Value__);
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaToBooleanNode.doInterop(Object)] */) {
                return LuaToBooleanNode.doInterop(child0Value_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(child0Value_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeBoolean(frameValue);
        return;
    }

    private boolean executeAndSpecialize(Object child0Value) {
        int state_0 = this.state_0_;
        if (child0Value instanceof Boolean) {
            boolean child0Value_ = (boolean) child0Value;
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaToBooleanNode.doBoolean(boolean)] */;
            this.state_0_ = state_0;
            return LuaToBooleanNode.doBoolean(child0Value_);
        }
        if (child0Value instanceof LuaBoolean) {
            LuaBoolean child0Value_ = (LuaBoolean) child0Value;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaToBooleanNode.doLuaBoolean(LuaBoolean)] */;
            this.state_0_ = state_0;
            return LuaToBooleanNode.doLuaBoolean(child0Value_);
        }
        if (LuaTypes.isLuaNil(child0Value)) {
            LuaNil child0Value_ = LuaTypes.asLuaNil(child0Value);
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaToBooleanNode.doLuaNil(LuaNil)] */;
            this.state_0_ = state_0;
            return LuaToBooleanNode.doLuaNil(child0Value_);
        }
        state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaToBooleanNode.doInterop(Object)] */;
        this.state_0_ = state_0;
        return LuaToBooleanNode.doInterop(child0Value);
    }

    @NeverDefault
    public static LuaToBooleanNode create(LuaExpressionNode child0) {
        return new LuaToBooleanNodeGen(child0);
    }

}

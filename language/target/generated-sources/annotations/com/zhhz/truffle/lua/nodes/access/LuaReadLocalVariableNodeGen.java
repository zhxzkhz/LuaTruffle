// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaTypesGen;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaReadLocalVariableNode#readLong}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaReadLocalVariableNode#readDouble}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaReadLocalVariableNode#readBoolean}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaReadLocalVariableNode#readObject}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaReadLocalVariableNode.class)
@SuppressWarnings("javadoc")
public final class LuaReadLocalVariableNodeGen extends LuaReadLocalVariableNode {

    private final int slotIndex;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaReadLocalVariableNode#readLong}
     *   1: SpecializationActive {@link LuaReadLocalVariableNode#readObject}
     *   2: SpecializationActive {@link LuaReadLocalVariableNode#readDouble}
     *   3: SpecializationActive {@link LuaReadLocalVariableNode#readBoolean}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaReadLocalVariableNodeGen(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    protected int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] || SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)] || SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] || SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] */) {
                if ((isLongOrIllegal(frameValue))) {
                    return readLong(frameValue);
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)] */) {
                if ((isDoubleOrIllegal(frameValue))) {
                    return readDouble(frameValue);
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] */) {
                if ((isBooleanOrIllegal(frameValue))) {
                    return readBoolean(frameValue);
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
                return readObject(frameValue);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(frameValue);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            return LuaTypesGen.expectBoolean(this.executeGeneric(frameValue));
        }
        if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] */) {
            if ((isBooleanOrIllegal(frameValue))) {
                return readBoolean(frameValue);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectBoolean(executeAndSpecialize(frameValue));
    }

    @Override
    public long executeLong(VirtualFrame frameValue) throws UnexpectedResultException {
        int state_0 = this.state_0_;
        if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            return LuaTypesGen.expectLong(this.executeGeneric(frameValue));
        }
        if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] */) {
            if ((isLongOrIllegal(frameValue))) {
                return readLong(frameValue);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return LuaTypesGen.expectLong(executeAndSpecialize(frameValue));
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        try {
            if ((state_0 & 0b1110) == 0 /* only-active SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */)) {
                this.executeLong(frameValue);
                return;
            } else if ((state_0 & 0b111) == 0 /* only-active SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] && SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */)) {
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

    private Object executeAndSpecialize(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            if ((isLongOrIllegal(frameValue))) {
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)] */;
                this.state_0_ = state_0;
                return readLong(frameValue);
            }
        }
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            if ((isDoubleOrIllegal(frameValue))) {
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)] */;
                this.state_0_ = state_0;
                return readDouble(frameValue);
            }
        }
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */) {
            if ((isBooleanOrIllegal(frameValue))) {
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] */;
                this.state_0_ = state_0;
                return readBoolean(frameValue);
            }
        }
        state_0 = state_0 & 0xfffffff2 /* remove SpecializationActive[LuaReadLocalVariableNode.readLong(VirtualFrame)], SpecializationActive[LuaReadLocalVariableNode.readDouble(VirtualFrame)], SpecializationActive[LuaReadLocalVariableNode.readBoolean(VirtualFrame)] */;
        state_0 = state_0 | 0b10 /* add SpecializationActive[LuaReadLocalVariableNode.readObject(VirtualFrame)] */;
        this.state_0_ = state_0;
        return readObject(frameValue);
    }

    @NeverDefault
    public static LuaReadLocalVariableNode create(int slotIndex) {
        return new LuaReadLocalVariableNodeGen(slotIndex);
    }

}

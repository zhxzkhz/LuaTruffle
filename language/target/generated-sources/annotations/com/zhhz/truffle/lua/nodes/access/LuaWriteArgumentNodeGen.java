// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaWriteArgumentNode#writeLong}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaWriteArgumentNode#writeDouble}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaWriteArgumentNode#writeBoolean}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaWriteArgumentNode#writeObject}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaWriteArgumentNode.class)
@SuppressWarnings("javadoc")
public final class LuaWriteArgumentNodeGen extends LuaWriteArgumentNode {

    private final int argumentIndex;
    private final int slotIndex;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaWriteArgumentNode#writeLong}
     *   1: SpecializationActive {@link LuaWriteArgumentNode#writeDouble}
     *   2: SpecializationActive {@link LuaWriteArgumentNode#writeBoolean}
     *   3: SpecializationActive {@link LuaWriteArgumentNode#writeObject}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaWriteArgumentNodeGen(int argumentIndex, int slotIndex) {
        this.argumentIndex = argumentIndex;
        this.slotIndex = slotIndex;
    }

    @Override
    protected int getArgumentIndex() {
        return this.argumentIndex;
    }

    @Override
    protected int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaWriteArgumentNode.writeLong(VirtualFrame)] || SpecializationActive[LuaWriteArgumentNode.writeDouble(VirtualFrame)] || SpecializationActive[LuaWriteArgumentNode.writeBoolean(VirtualFrame)] || SpecializationActive[LuaWriteArgumentNode.writeObject(VirtualFrame)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaWriteArgumentNode.writeLong(VirtualFrame)] */) {
                if ((isLongOrIllegal(frameValue))) {
                    writeLong(frameValue);
                    return;
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaWriteArgumentNode.writeDouble(VirtualFrame)] */) {
                if ((isDoubleOrIllegal(frameValue))) {
                    writeDouble(frameValue);
                    return;
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaWriteArgumentNode.writeBoolean(VirtualFrame)] */) {
                if ((isBooleanOrIllegal(frameValue))) {
                    writeBoolean(frameValue);
                    return;
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaWriteArgumentNode.writeObject(VirtualFrame)] */) {
                writeObject(frameValue);
                return;
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(frameValue);
        return;
    }

    private void executeAndSpecialize(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((isLongOrIllegal(frameValue))) {
            state_0 = state_0 | 0b1 /* add SpecializationActive[LuaWriteArgumentNode.writeLong(VirtualFrame)] */;
            this.state_0_ = state_0;
            writeLong(frameValue);
            return;
        }
        if ((isDoubleOrIllegal(frameValue))) {
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaWriteArgumentNode.writeDouble(VirtualFrame)] */;
            this.state_0_ = state_0;
            writeDouble(frameValue);
            return;
        }
        if ((isBooleanOrIllegal(frameValue))) {
            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaWriteArgumentNode.writeBoolean(VirtualFrame)] */;
            this.state_0_ = state_0;
            writeBoolean(frameValue);
            return;
        }
        state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaWriteArgumentNode.writeObject(VirtualFrame)] */;
        this.state_0_ = state_0;
        writeObject(frameValue);
        return;
    }

    @NeverDefault
    public static LuaWriteArgumentNode create(int argumentIndex, int slotIndex) {
        return new LuaWriteArgumentNodeGen(argumentIndex, slotIndex);
    }

}

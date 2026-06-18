// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaWriteLocalVariableNode#writeLong}
 *     Activation probability: 0.38500
 *     With/without class size: 8/0 bytes
 *   Specialization {@link LuaWriteLocalVariableNode#writeDouble}
 *     Activation probability: 0.29500
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaWriteLocalVariableNode#writeBoolean}
 *     Activation probability: 0.20500
 *     With/without class size: 6/0 bytes
 *   Specialization {@link LuaWriteLocalVariableNode#writeObject}
 *     Activation probability: 0.11500
 *     With/without class size: 5/0 bytes
 * </pre> */
@GeneratedBy(LuaWriteLocalVariableNode.class)
@SuppressWarnings("javadoc")
public final class LuaWriteLocalVariableNodeGen extends LuaWriteLocalVariableNode {

    private final int slotIndex;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaWriteLocalVariableNode#writeLong}
     *   1: SpecializationActive {@link LuaWriteLocalVariableNode#writeDouble}
     *   2: SpecializationActive {@link LuaWriteLocalVariableNode#writeBoolean}
     *   3: SpecializationActive {@link LuaWriteLocalVariableNode#writeObject}
     * </pre> */
    @CompilationFinal private int state_0_;

    private LuaWriteLocalVariableNodeGen(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    protected int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public void execute(VirtualFrame frameValue, Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeLong(VirtualFrame, long)] || SpecializationActive[LuaWriteLocalVariableNode.writeDouble(VirtualFrame, double)] || SpecializationActive[LuaWriteLocalVariableNode.writeBoolean(VirtualFrame, boolean)] || SpecializationActive[LuaWriteLocalVariableNode.writeObject(VirtualFrame, Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeLong(VirtualFrame, long)] */ && arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                if ((isLongOrIllegal(frameValue))) {
                    writeLong(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeDouble(VirtualFrame, double)] */ && arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                if ((isDoubleOrIllegal(frameValue))) {
                    writeDouble(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeBoolean(VirtualFrame, boolean)] */ && arg0Value instanceof Boolean) {
                boolean arg0Value_ = (boolean) arg0Value;
                if ((isBooleanOrIllegal(frameValue))) {
                    writeBoolean(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeObject(VirtualFrame, Object)] */) {
                writeObject(frameValue, arg0Value);
                return;
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(frameValue, arg0Value);
        return;
    }

    @Override
    public void executeWrite(VirtualFrame frameValue, Object arg0Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeLong(VirtualFrame, long)] || SpecializationActive[LuaWriteLocalVariableNode.writeDouble(VirtualFrame, double)] || SpecializationActive[LuaWriteLocalVariableNode.writeBoolean(VirtualFrame, boolean)] || SpecializationActive[LuaWriteLocalVariableNode.writeObject(VirtualFrame, Object)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeLong(VirtualFrame, long)] */ && arg0Value instanceof Long) {
                long arg0Value_ = (long) arg0Value;
                if ((isLongOrIllegal(frameValue))) {
                    writeLong(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeDouble(VirtualFrame, double)] */ && arg0Value instanceof Double) {
                double arg0Value_ = (double) arg0Value;
                if ((isDoubleOrIllegal(frameValue))) {
                    writeDouble(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeBoolean(VirtualFrame, boolean)] */ && arg0Value instanceof Boolean) {
                boolean arg0Value_ = (boolean) arg0Value;
                if ((isBooleanOrIllegal(frameValue))) {
                    writeBoolean(frameValue, arg0Value_);
                    return;
                }
            }
            if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaWriteLocalVariableNode.writeObject(VirtualFrame, Object)] */) {
                writeObject(frameValue, arg0Value);
                return;
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(frameValue, arg0Value);
        return;
    }

    private void executeAndSpecialize(VirtualFrame frameValue, Object arg0Value) {
        int state_0 = this.state_0_;
        if (arg0Value instanceof Long) {
            long arg0Value_ = (long) arg0Value;
            if ((isLongOrIllegal(frameValue))) {
                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaWriteLocalVariableNode.writeLong(VirtualFrame, long)] */;
                this.state_0_ = state_0;
                writeLong(frameValue, arg0Value_);
                return;
            }
        }
        if (arg0Value instanceof Double) {
            double arg0Value_ = (double) arg0Value;
            if ((isDoubleOrIllegal(frameValue))) {
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaWriteLocalVariableNode.writeDouble(VirtualFrame, double)] */;
                this.state_0_ = state_0;
                writeDouble(frameValue, arg0Value_);
                return;
            }
        }
        if (arg0Value instanceof Boolean) {
            boolean arg0Value_ = (boolean) arg0Value;
            if ((isBooleanOrIllegal(frameValue))) {
                state_0 = state_0 | 0b100 /* add SpecializationActive[LuaWriteLocalVariableNode.writeBoolean(VirtualFrame, boolean)] */;
                this.state_0_ = state_0;
                writeBoolean(frameValue, arg0Value_);
                return;
            }
        }
        state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaWriteLocalVariableNode.writeObject(VirtualFrame, Object)] */;
        this.state_0_ = state_0;
        writeObject(frameValue, arg0Value);
        return;
    }

    @NeverDefault
    public static LuaWriteLocalVariableNode create(int slotIndex) {
        return new LuaWriteLocalVariableNodeGen(slotIndex);
    }

}

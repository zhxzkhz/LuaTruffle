package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

@NodeField(name = "argumentIndex", type = int.class)
@NodeField(name = "slotIndex", type = int.class)
public abstract class LuaWriteArgumentNode extends LuaStatementNode {

    protected abstract int getArgumentIndex();
    protected abstract int getSlotIndex();

    private Object getArg(VirtualFrame frame) {
        Object[] args = frame.getArguments();
        int userArgIndex = getArgumentIndex() + 1;
        return userArgIndex < args.length ? args[userArgIndex] : null;
    }

    @Specialization(guards = "isLongOrIllegal(frame)")
    protected void writeLong(VirtualFrame frame) {
        Object value = getArg(frame);
        if (value instanceof Long l) {
            frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Long);
            frame.setLong(getSlotIndex(), l);
        } else {
            writeObjectSlow(frame, value);
        }
    }

    @Specialization(guards = "isDoubleOrIllegal(frame)")
    protected void writeDouble(VirtualFrame frame) {
        Object value = getArg(frame);
        if (value instanceof Double d) {
            frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Double);
            frame.setDouble(getSlotIndex(), d);
        } else {
            writeObjectSlow(frame, value);
        }
    }

    @Specialization(guards = "isBooleanOrIllegal(frame)")
    protected void writeBoolean(VirtualFrame frame) {
        Object value = getArg(frame);
        if (value instanceof Boolean b) {
            frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Boolean);
            frame.setBoolean(getSlotIndex(), b);
        } else {
            writeObjectSlow(frame, value);
        }
    }

    @Specialization
    protected void writeObject(VirtualFrame frame) {
        Object value = getArg(frame);
        if (value == null) value = LuaNil.SINGLETON;
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(getSlotIndex(), value);
    }

    private void writeObjectSlow(VirtualFrame frame, Object value) {
        if (value == null) value = LuaNil.SINGLETON;
        frame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(getSlotIndex(), value);
    }

    protected boolean isLongOrIllegal(VirtualFrame frame) {
        FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        return kind == FrameSlotKind.Long || kind == FrameSlotKind.Illegal;
    }

    protected boolean isDoubleOrIllegal(VirtualFrame frame) {
        FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        return kind == FrameSlotKind.Double || kind == FrameSlotKind.Illegal;
    }

    protected boolean isBooleanOrIllegal(VirtualFrame frame) {
        FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(getSlotIndex());
        return kind == FrameSlotKind.Boolean || kind == FrameSlotKind.Illegal;
    }
}

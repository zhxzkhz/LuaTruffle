package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * 负责将值写入从外部捕获的变量的节点
 */
@NodeInfo(description = "用于从外部作用域（一个上值）写入变量的节点")
@NodeField(name = "depth", type = int.class)
@NodeField(name = "slotIndex", type = int.class)
public abstract class LuaWriteUpvalueNode extends LuaWriteVariableNode {

    protected abstract int getDepth();
    protected abstract int getSlotIndex();

    @Specialization
    public void writeLong(VirtualFrame frame, long value) {
        Frame parentFrame = getParentFrame(frame, getDepth());
        parentFrame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Long);
        parentFrame.setLong(getSlotIndex(), value);
    }

    @Specialization
    public void writeDouble(VirtualFrame frame, double value) {
        Frame parentFrame = getParentFrame(frame, getDepth());
        parentFrame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Double);
        parentFrame.setDouble(getSlotIndex(), value);
    }

    @Specialization
    public void writeBoolean(VirtualFrame frame, boolean value) {
        Frame parentFrame = getParentFrame(frame, getDepth());
        parentFrame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Boolean);
        parentFrame.setBoolean(getSlotIndex(), value);
    }

    @Specialization
    public void writeObject(VirtualFrame frame, Object value) {
        Frame parentFrame = getParentFrame(frame, getDepth());
        parentFrame.getFrameDescriptor().setSlotKind(getSlotIndex(), FrameSlotKind.Object);
        parentFrame.setObject(getSlotIndex(), value);
    }

    private static Frame getParentFrame(VirtualFrame frame, int depth) {
        Frame parentFrame = (Frame) frame.getArguments()[0];
        for (int i = 1; i < depth; i++) {
            parentFrame = (Frame) parentFrame.getArguments()[0];
        }
        return parentFrame;
    }
}

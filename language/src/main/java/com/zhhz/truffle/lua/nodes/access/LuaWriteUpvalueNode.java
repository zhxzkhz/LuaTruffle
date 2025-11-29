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
    public void doWrite(VirtualFrame frame, Object value) {
        // 1. 获取当前函数的父 Frame
        Frame parentFrame = (Frame) frame.getArguments()[0];

        // 2. 根据深度，向上追溯 Frame 链
        for (int i = 1; i < getDepth(); i++) {
            parentFrame = (Frame) parentFrame.getArguments()[0];
        }

        // 3. 在找到的目标 Frame 中，使用 slotIndex 写入值
        //    这里需要一个健壮的方式来写入，因为它可能是任何类型。
        //    直接使用 setObject 是最简单的方式。

        if (value instanceof Long) {
            // 如果传入的是 Long 对象（或原生 long，它会被自动装箱为 Long）
            parentFrame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Long);
            parentFrame.setLong(this.getSlotIndex(), (Long) value);
            return;
        }

        if (value instanceof Double) {
            parentFrame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Double);
            parentFrame.setDouble(this.getSlotIndex(), (Double) value);
            return;
        }

        if (value instanceof Boolean) {
            parentFrame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Boolean);
            parentFrame.setBoolean(this.getSlotIndex(), (Boolean) value);
            return;
        }

        frame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Object);
        parentFrame.setObject(getSlotIndex(), value);
    }
}

package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * 负责将一个函数参数写入一个局部变量槽位。
 */
@NodeField(name = "argumentIndex", type = int.class)
@NodeField(name = "slotIndex", type = int.class)
public abstract class LuaWriteArgumentNode extends LuaStatementNode {

    protected abstract int getArgumentIndex();
    protected abstract int getSlotIndex();

    @Specialization
    protected void write(VirtualFrame frame) {
        Object[] args = frame.getArguments();

        int userArgIndex = this.getArgumentIndex() + 1;

        Object value = (userArgIndex < args.length) ? args[userArgIndex] : null;

        final FrameSlotKind kind = frame.getFrameDescriptor().getSlotKind(this.getSlotIndex());

        if (value instanceof Long) {
            // 如果传入的是 Long 对象（或原生 long，它会被自动装箱为 Long）
            if (kind == FrameSlotKind.Long || kind == FrameSlotKind.Illegal) {
                frame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Long);
                frame.setLong(this.getSlotIndex(), (Long) value);
                return;
            }
        }

        if (value instanceof Double) {
            if (kind == FrameSlotKind.Double || kind == FrameSlotKind.Illegal) {
                frame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Double);
                frame.setDouble(this.getSlotIndex(), (Double) value);
                return;
            }
        }

        if (value instanceof Boolean) {
            if (kind == FrameSlotKind.Boolean || kind == FrameSlotKind.Illegal) {
                frame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Boolean);
                frame.setBoolean(this.getSlotIndex(), (Boolean) value);
                return;
            }
        }

        // 【回退逻辑】如果类型不匹配，或者是一个通用 Object
        // 我们将槽位“污染”成 Object 类型
        frame.getFrameDescriptor().setSlotKind(this.getSlotIndex(), FrameSlotKind.Object);
        frame.setObject(this.getSlotIndex(), value);
    }
}
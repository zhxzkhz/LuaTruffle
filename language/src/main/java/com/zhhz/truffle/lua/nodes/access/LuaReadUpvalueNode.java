package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 读取一个Upvalue（外层作用域的局部变量）。
 */
@NodeField(name = "slotIndex", type = int.class)
@NodeField(name = "depth", type = int.class)
public abstract class LuaReadUpvalueNode extends LuaExpressionNode {

    protected abstract int getSlotIndex();
    protected abstract int getDepth();

    @Specialization
    protected Object readUpvalue(VirtualFrame frame) {
        // 1. 获取当前函数的父Frame (静态链的起点)
        Object[] args = frame.getArguments();
        // 健壮性检查：防止之前的空数组问题再次出现
        if (args.length == 0) {
            // 这里可以选择抛出内部错误，或者返回 nil
            return LuaNil.SINGLETON;
        }

        Frame parentFrame = (Frame) args[0];
        for (int i = 0; i < getDepth() - 1; i++) {
            // 防止链条断裂
            if (parentFrame == null) return LuaNil.SINGLETON;
            Object[] parentArgs = parentFrame.getArguments();
            if (parentArgs.length == 0) return LuaNil.SINGLETON;

            parentFrame = (Frame) parentArgs[0];
        }

        if (parentFrame == null) {
            return LuaNil.SINGLETON;
        }

        // 我们需要检查 parentFrame 的 Descriptor
        FrameSlotKind kind = parentFrame.getFrameDescriptor().getSlotKind(getSlotIndex());

        try {
            if (kind == FrameSlotKind.Long) {
                // 如果是 Long，读取原生 long (自动装箱为 Long 返回)
                return parentFrame.getLong(getSlotIndex());
            }
            else if (kind == FrameSlotKind.Double) {
                return parentFrame.getDouble(getSlotIndex());
            }
            else if (kind == FrameSlotKind.Boolean) {
                return parentFrame.getBoolean(getSlotIndex());
            }
            else if (kind == FrameSlotKind.Object) {
                return parentFrame.getObject(getSlotIndex());
            }
            else {
                // Illegal 或其他，视为 nil
                return LuaNil.SINGLETON;
            }
        } catch (FrameSlotTypeException e) {
            // 如果并发修改导致类型在检查后变了（极少见），回退到慢路径
            CompilerDirectives.transferToInterpreter();
            // FrameUtil.getObjectSafe 是一个慢速但在任何情况下都安全的工具方法
            // 需要 import com.oracle.truffle.api.frame.FrameUtil;
            // return FrameUtil.getObjectSafe(parentFrame, getSlotIndex());

            // 或者简单的抛出错误，因为单线程模型下不应该发生
            throw new IllegalStateException("Frame slot type changed unexpectedly");
        }
    }
}

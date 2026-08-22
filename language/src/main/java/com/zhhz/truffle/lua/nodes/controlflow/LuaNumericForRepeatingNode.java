package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * Lua 数值型 For 循环的重复执行体。
 * <p>
 * 该节点被包裹在 {@link com.oracle.truffle.api.nodes.LoopNode} 内部。
 * LoopNode 会负责统计循环次数（Loop Count），当达到阈值时触发 OSR（On-Stack Replacement）编译，
 * 将解释执行的循环替换为编译后的机器码。
 */
public class LuaNumericForRepeatingNode extends Node implements RepeatingNode {

    /** 循环变量（如 'i'）在栈帧中的 Slot 索引 */
    private final int loopVarIndex;
    private final int limitIndex;
    private final int stepIndex;

    /** 循环体内的语句节点 */
    @Child private LuaStatementNode bodyNode;

    public LuaNumericForRepeatingNode(int loopVarIndex, int limitIndex, int stepIndex, LuaStatementNode bodyNode) {
        this.loopVarIndex = loopVarIndex;
        this.limitIndex = limitIndex;
        this.stepIndex = stepIndex;
        this.bodyNode = bodyNode;
    }

    /**
     * 执行单次循环迭代。
     *
     * @param frame 当前栈帧，包含循环变量 'i' 的当前值。
     * @return boolean true 表示继续下一次迭代；false 表示循环结束。
     */
    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        if (frame.isDouble(loopVarIndex)) {
            return executeDouble(frame);
        }
        // 1. 【读取当前值】
        // 直接读取 Long 类型，因为父节点已经强制设置了 FrameSlotKind.Long，
        // 这里不会有装箱/拆箱（Boxing）开销。
        long currentValue = frame.getLong(loopVarIndex);
        long limit = frame.getLong(limitIndex);
        long step = frame.getLong(stepIndex);

        // 2. 【边界检查】
        // Lua 语义：
        // - 如果 step > 0，当 current > limit 时循环结束。
        // - 如果 step < 0，当 current < limit 时循环结束。
        if (step > 0) {
            if (currentValue > limit) return false;
        } else {
            // step 为负数的情况
            if (currentValue < limit) return false;
        }

        // 3. 【执行循环体】
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaContinueException e) {
            // Continue still performs the numeric loop update below.
        } catch (LuaBreakException e) {
            // 捕获 break 语句抛出的控制流异常。
            // 返回 false 通知 LoopNode 停止循环。
            return false;
        }
        // 注意：如果有 continue 逻辑，应该在 bodyNode 内部处理，
        // 或者在这里捕获 LuaContinueException 并直接 swallow (什么都不做)，以便执行下面的更新步骤。

        // 4. 【更新步进 (Increment)】
        // 计算下一个循环变量的值，并写回栈帧。
        // 这一步必须在 body 执行完后进行。
        // 注意溢出风险：在严谨的 Lua 实现中，这里可能需要处理 long 溢出成为 double 的情况，
        // 但 SimpleLanguage 通常假设在 long 范围内。
        if (step > 0 && currentValue > Long.MAX_VALUE - step
                || step < 0 && currentValue < Long.MIN_VALUE - step) {
            return false;
        }
        frame.setLong(loopVarIndex, currentValue + step);

        // 5. 【继续循环】
        return true;
    }

    private boolean executeDouble(VirtualFrame frame) {
        double currentValue = frame.getDouble(loopVarIndex);
        double limit = frame.getDouble(limitIndex);
        double step = frame.getDouble(stepIndex);
        if (Double.isNaN(step) || !(step > 0 ? currentValue <= limit : currentValue >= limit)) {
            return false;
        }
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaContinueException e) {
            // Continue still performs the update below.
        } catch (LuaBreakException e) {
            return false;
        }
        frame.setDouble(loopVarIndex, currentValue + step);
        return true;
    }
}

package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaCell;

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

    /** 循环变量是否被闭包捕获：为真时以 {@link LuaCell} 存储，每次迭代新建 cell。 */
    private final boolean loopCaptured;

    /** 循环体内的语句节点 */
    @Child private LuaStatementNode bodyNode;

    // debug 行钩子：for 头行（每次迭代检查触发，回边强制）与 end 行（耗尽退出触发；break 不触发）
    private final int headerLine;
    private final int endLine;

    public LuaNumericForRepeatingNode(int loopVarIndex, int limitIndex, int stepIndex, LuaStatementNode bodyNode) {
        this(loopVarIndex, limitIndex, stepIndex, bodyNode, 0, 0, false);
    }

    public LuaNumericForRepeatingNode(int loopVarIndex, int limitIndex, int stepIndex, LuaStatementNode bodyNode,
                                      int headerLine, int endLine) {
        this(loopVarIndex, limitIndex, stepIndex, bodyNode, headerLine, endLine, false);
    }

    public LuaNumericForRepeatingNode(int loopVarIndex, int limitIndex, int stepIndex, LuaStatementNode bodyNode,
                                      int headerLine, int endLine, boolean loopCaptured) {
        this.loopVarIndex = loopVarIndex;
        this.limitIndex = limitIndex;
        this.stepIndex = stepIndex;
        this.bodyNode = bodyNode;
        this.headerLine = headerLine;
        this.endLine = endLine;
        this.loopCaptured = loopCaptured;
    }

    /**
     * 执行单次循环迭代。
     *
     * @param frame 当前栈帧，包含循环变量 'i' 的当前值。
     * @return boolean true 表示继续下一次迭代；false 表示循环结束。
     */
    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        TruffleSafepoint.poll(this);
        com.zhhz.truffle.lua.runtime.LuaInterrupt.check(this);
        com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, headerLine, true);
        if (loopCaptured) {
            // 类型由 limit 槽位决定（loop 槽位此时为 Object）。
            if (frame.isDouble(limitIndex)) {
                LuaCell cell = (LuaCell) frame.getObject(loopVarIndex);
                return executeDouble(frame, (Double) cell.value);
            }
            LuaCell cell = (LuaCell) frame.getObject(loopVarIndex);
            return executeLong(frame, (Long) cell.value);
        }
        if (frame.isDouble(loopVarIndex)) {
            return executeDouble(frame, frame.getDouble(loopVarIndex));
        }
        return executeLong(frame, frame.getLong(loopVarIndex));
    }

    private boolean executeLong(VirtualFrame frame, long currentValue) {
        long limit = frame.getLong(limitIndex);
        long step = frame.getLong(stepIndex);

        // 2. 【边界检查】
        if (step > 0) {
            if (currentValue > limit) {
                com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, endLine, false);
                return false;
            }
        } else {
            if (currentValue < limit) {
                com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, endLine, false);
                return false;
            }
        }

        // 3. 【执行循环体】
        if (!runBody(frame)) {
            return false;
        }

        // 4. 【更新步进 (Increment)】
        if (step > 0 && currentValue > Long.MAX_VALUE - step
                || step < 0 && currentValue < Long.MIN_VALUE - step) {
            return false;
        }
        long next = currentValue + step;
        if (loopCaptured) {
            frame.setObject(loopVarIndex, new LuaCell(next));
        } else {
            frame.setLong(loopVarIndex, next);
        }
        return true;
    }

    private boolean executeDouble(VirtualFrame frame, double currentValue) {
        double limit = frame.getDouble(limitIndex);
        double step = frame.getDouble(stepIndex);
        if (Double.isNaN(step) || !(step > 0 ? currentValue <= limit : currentValue >= limit)) {
            com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, endLine, false);
            return false;
        }
        if (!runBody(frame)) {
            return false;
        }
        double next = currentValue + step;
        if (loopCaptured) {
            frame.setObject(loopVarIndex, new LuaCell(next));
        } else {
            frame.setDouble(loopVarIndex, next);
        }
        return true;
    }

    /** 执行循环体。返回 false 表示遇到 break（应结束循环）。 */
    private boolean runBody(VirtualFrame frame) {
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaContinueException e) {
            // continue：继续执行下方的步进更新
        } catch (LuaBreakException e) {
            return false;
        }
        return true;
    }
}

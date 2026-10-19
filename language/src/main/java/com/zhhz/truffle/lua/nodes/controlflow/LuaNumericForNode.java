package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.parser.LuaBaseParser.LocalSlot;
import com.zhhz.truffle.lua.runtime.LuaCell;

/**
 * Lua 语言中 "数值型 for 循环" (Numeric For Loop) 的 AST 节点实现。
 * <p>
 * 语法结构: {@code for var = start, limit, step do block end}
 * <p>
 * <b>核心职责：</b>
 * 1. 在循环开始前，对 start, limit, step 进行<b>一次性求值</b>。
 * 2. 初始化循环变量（存储在 VirtualFrame 中）。
 * 3. 将执行权交给 {@link LoopNode} 进行高效循环。
 */
@NodeInfo(shortName = "for", description = "The node implementing the numeric for loop")
public class LuaNumericForNode extends LuaStatementNode {

    // --- 表达式子节点 ---
    @Child private LuaExpressionNode startNode;
    @Child private LuaExpressionNode limitNode;
    @Child private LuaExpressionNode stepNode; // 步长是可选的，可能为 null

    /**
     * 循环变量在栈帧（VirtualFrame）中的索引。
     * 数值循环的变量（如 'i'）通常是局部的，直接通过 Frame Slot 访问性能最高。
     */
    private final int loopVariableIndex;
    private final int limitIndex;
    private final int stepIndex;

    /** 循环变量是否被内层闭包捕获（此时以 cell 存储，每次迭代新建 cell）。 */
    private final boolean loopCaptured;

    /** 负责将表达式结果强制转换为数字的节点 */
    @Child private LuaCoerceToNumberNode coerceNode;

    /**
     * Truffle 框架提供的循环包装器。
     * 它是实现 OSR (On-Stack Replacement) 编译优化的关键。
     */
    @Child
    private LoopNode loopNode;

    public LuaNumericForNode(int loopVariableIndex, int hookHeaderLine, int hookEndLine,
                             int limitIndex,
                             int stepIndex,
                             LuaExpressionNode startNode,
                             LuaExpressionNode limitNode,
                             LuaExpressionNode stepNode,
                             LuaStatementNode bodyNode) {
        this(loopVariableIndex, hookHeaderLine, hookEndLine, limitIndex, stepIndex,
                startNode, limitNode, stepNode, bodyNode, null);
    }

    public LuaNumericForNode(int loopVariableIndex, int hookHeaderLine, int hookEndLine,
                             int limitIndex,
                             int stepIndex,
                             LuaExpressionNode startNode,
                             LuaExpressionNode limitNode,
                             LuaExpressionNode stepNode,
                             LuaStatementNode bodyNode,
                             LocalSlot loopSlot) {
        this.loopVariableIndex = loopVariableIndex;
        this.limitIndex = limitIndex;
        this.stepIndex = stepIndex;
        this.loopCaptured = loopSlot != null && loopSlot.captured;
        this.startNode = startNode;
        this.limitNode = limitNode;
        this.stepNode = stepNode;
        // 创建转换节点，用于处理 Lua 的弱类型特性（如字符串 "10" 转数字 10）
        this.coerceNode = LuaCoerceToNumberNode.create();
        // 创建 LoopNode，包裹具体的重复执行逻辑 (RepeatingNode)
        this.loopNode = Truffle.getRuntime().createLoopNode(new LuaNumericForRepeatingNode(this.loopVariableIndex, this.limitIndex, this.stepIndex, bodyNode, hookHeaderLine, hookEndLine, this.loopCaptured));
    }


    @Override
    public void executeVoid(VirtualFrame frame) {
        // 1. 【参数求值阶段】
        // Lua 语义规定：start, limit, step 只在循环开始前计算一次。
        // 这里我们获取结果并尝试转换为 long 类型。
        Object startValue = expectNumber("initial", startNode.executeGeneric(frame), coerceNode);
        Object limitValue = expectNumber("limit", limitNode.executeGeneric(frame), coerceNode);
        Object stepValue = stepNode == null
                ? 1L
                : expectNumber("step", stepNode.executeGeneric(frame), coerceNode);

        if (startValue instanceof Long startLong && stepValue instanceof Long stepLong) {
            if (stepLong == 0) {
                throw LuaException.runtimeError("'for' step is zero", this);
            }
            Long limitLong = integerLimit(limitValue, stepLong);
            if (limitLong == null) {
                return;
            }
            frame.getFrameDescriptor().setSlotKind(loopVariableIndex, loopCaptured ? FrameSlotKind.Object : FrameSlotKind.Long);
            frame.getFrameDescriptor().setSlotKind(limitIndex, FrameSlotKind.Long);
            frame.getFrameDescriptor().setSlotKind(stepIndex, FrameSlotKind.Long);
            if (loopCaptured) {
                frame.setObject(loopVariableIndex, new LuaCell(startLong));
            } else {
                frame.setLong(loopVariableIndex, startLong);
            }
            frame.setLong(limitIndex, limitLong);
            frame.setLong(stepIndex, stepLong);
        } else {
            double startDouble = asDouble(startValue);
            double limitDouble = asDouble(limitValue);
            double stepDouble = asDouble(stepValue);
            if (stepDouble == 0.0) {
                throw LuaException.runtimeError("'for' step is zero", this);
            }
            frame.getFrameDescriptor().setSlotKind(loopVariableIndex, loopCaptured ? FrameSlotKind.Object : FrameSlotKind.Double);
            frame.getFrameDescriptor().setSlotKind(limitIndex, FrameSlotKind.Double);
            frame.getFrameDescriptor().setSlotKind(stepIndex, FrameSlotKind.Double);
            if (loopCaptured) {
                frame.setObject(loopVariableIndex, new LuaCell(startDouble));
            } else {
                frame.setDouble(loopVariableIndex, startDouble);
            }
            frame.setDouble(limitIndex, limitDouble);
            frame.setDouble(stepIndex, stepDouble);
        }

        // 4. 【启动循环】
        // LoopNode 会接管控制权，不断调用 RepeatingNode.executeRepeating
        loopNode.execute(frame);
    }

    // 辅助方法：类型转换与检查
    private Object expectNumber(String context, Object value, LuaCoerceToNumberNode coerce) {
        Object coercedValue = coerce.execute(value);
        if (coercedValue instanceof Long || coercedValue instanceof Double) {
            return coercedValue;
        }

        // 【慢速路径 / 错误处理】
        // 如果转换失败（不是数字），转入解释器模式并抛出异常。
        // 这里的 invalidate() 告诉编译器：不要把抛出异常的代码编译进去，保持主路径的高效。
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw createException(context, value);
    }

    private static double asDouble(Object value) {
        return value instanceof Double number ? number : ((Long) value).doubleValue();
    }

    private static Long integerLimit(Object value, long step) {
        if (value instanceof Long integer) {
            return integer;
        }

        double limit = (Double) value;
        if (Double.isNaN(limit)) {
            return null;
        }
        if (step > 0) {
            if (limit < Long.MIN_VALUE) {
                return null;
            }
            if (limit >= Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            return (long) Math.floor(limit);
        }
        if (limit > Long.MAX_VALUE) {
            return null;
        }
        if (limit <= Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return (long) Math.ceil(limit);
    }

    RuntimeException createException(String which, Object value){
        return LuaException.runtimeError("'for' " + which + " must be a number", this);
    }
}

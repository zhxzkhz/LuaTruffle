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

    /** 负责将表达式结果强制转换为数字的节点 */
    @Child private LuaCoerceToNumberNode coerceNode;

    /**
     * Truffle 框架提供的循环包装器。
     * 它是实现 OSR (On-Stack Replacement) 编译优化的关键。
     */
    @Child
    private LoopNode loopNode;

    public LuaNumericForNode(int loopVariableIndex,
                             LuaExpressionNode startNode,
                             LuaExpressionNode limitNode,
                             LuaExpressionNode stepNode,
                             LuaStatementNode bodyNode) {
        this.loopVariableIndex = loopVariableIndex;
        this.startNode = startNode;
        this.limitNode = limitNode;
        this.stepNode = stepNode;
        // 创建转换节点，用于处理 Lua 的弱类型特性（如字符串 "10" 转数字 10）
        this.coerceNode = LuaCoerceToNumberNode.create();
        // 创建 LoopNode，包裹具体的重复执行逻辑 (RepeatingNode)
        this.loopNode = Truffle.getRuntime().createLoopNode(new LuaNumericForRepeatingNode(this.loopVariableIndex, bodyNode));
    }


    @Override
    public void executeVoid(VirtualFrame frame) {
        // 1. 【参数求值阶段】
        // Lua 语义规定：start, limit, step 只在循环开始前计算一次。
        // 这里我们获取结果并尝试转换为 long 类型。
        long startValue = expectLong("for loop 'initial' value", startNode.executeGeneric(frame), coerceNode);
        long limitValue = expectLong("for loop 'limit' value", limitNode.executeGeneric(frame), coerceNode);

        long stepValue = 1; // Lua 默认步长为 1
        if (stepNode != null) {
            stepValue = expectLong("for loop 'step' value", stepNode.executeGeneric(frame), coerceNode);
        }

        // 防御性检查：步长不能为 0，否则会造成死循环
        if (stepValue == 0) {
            throw LuaException.create("for loop step is zero", this);
        }

        // 2. 【初始化循环变量】
        // 显式设置 Slot 类型为 Long。
        // 这对 Graal 编译器非常重要，它能避免后续读取时的装箱/拆箱开销。
        frame.getFrameDescriptor().setSlotKind(loopVariableIndex, FrameSlotKind.Long);
        frame.setLong(loopVariableIndex, startValue);

        // 3. 【状态注入】
        // 将计算好的 limit 和 step 注入到 RepeatingNode 中。
        // ⚠️警告：这种直接修改 Node 字段的做法在多线程共享 AST 场景下是不安全的（Not Thread-Safe）。(待优化)
        // 在生产级实现中，建议通过 Frame 传递这些临时状态，或使用 Context 对象。
        if (this.loopNode.getRepeatingNode() instanceof LuaNumericForRepeatingNode luaNumericForRepeatingNode){
            luaNumericForRepeatingNode.setLimit(limitValue);
            luaNumericForRepeatingNode.setStep(stepValue);
        }

        // 4. 【启动循环】
        // LoopNode 会接管控制权，不断调用 RepeatingNode.executeRepeating
        loopNode.execute(frame);
    }

    // 辅助方法：类型转换与检查
    private long expectLong(String context, Object value, LuaCoerceToNumberNode coerce) {
        // 尝试转换值
        Object coercedValue = coerce.execute(value);

        if (coercedValue instanceof Long) {
            return (long) coercedValue;
        }

        // 【慢速路径 / 错误处理】
        // 如果转换失败（不是数字），转入解释器模式并抛出异常。
        // 这里的 invalidate() 告诉编译器：不要把抛出异常的代码编译进去，保持主路径的高效。
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw createException(context, value);
    }

    RuntimeException createException(String context, Object value){
        return LuaException.create("bad " + context + " (number expected, got " + value.getClass().getSimpleName() + ")", this);
    }
}
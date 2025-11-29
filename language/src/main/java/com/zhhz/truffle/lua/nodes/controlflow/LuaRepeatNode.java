package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * Lua 语言中 "repeat ... until" 循环的 AST 节点实现。
 * <p>
 * 语法结构: {@code repeat block until condition}
 * <p>
 * <b>语义特征：</b>
 * 1. <b>后测试循环 (Post-test Loop)</b>: 循环体至少会被执行一次。
 * 2. <b>作用域规则</b>: 在 Lua 中，{@code until} 的条件表达式可以访问循环体内定义的局部变量。
 *    (这一特性通常由 Parser 和 FrameDescriptor 布局决定，但节点执行逻辑必须支持这种顺序)。
 */
@NodeInfo(shortName = "repeat", description = "The node implementing the repeat-until loop")
public class LuaRepeatNode extends LuaStatementNode {

    /**
     * Truffle 框架提供的循环管理器。
     * <p>
     * 我们不直接在 Java 中写 while 循环，而是创建一个 {@link LoopNode}。
     * <b>Truffle 优化机制：</b>
     * LoopNode 会自动统计循环迭代次数。当达到特定阈值时，Graal 编译器会触发
     * <b>OSR (On-Stack Replacement)</b>，将当前正在解释执行的循环替换为高效的机器码，
     * 从而实现高性能的循环执行。
     */
    @Child private LoopNode loopNode;

    /**
     * 构造函数。
     *
     * @param bodyNode      循环体语句节点
     * @param conditionNode 终止条件表达式节点 (evaluate 为 true 时终止循环)
     */
    public LuaRepeatNode(LuaStatementNode bodyNode, LuaExpressionNode conditionNode) {
        // 1. 创建重复执行逻辑 (RepeatingNode)
        // LuaRepeatRepeatingNode 负责定义 "先执行 body，再检查 condition" 的具体逻辑。
        // 这里的 conditionNode 通常返回 boolean。

        // 2. 包装进 LoopNode
        // Truffle.getRuntime().createLoopNode(...) 是标准范式，用于启用编译器的循环优化。
        this.loopNode = Truffle.getRuntime().createLoopNode(
                new LuaRepeatRepeatingNode(bodyNode, conditionNode)
        );
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        // 将执行权委托给 LoopNode。
        // LoopNode 会不断调用内部 RepeatingNode.executeRepeating(frame)，
        // 直到 RepeatingNode 返回 false (表示循环应停止)。
        this.loopNode.execute(frame);
    }
}
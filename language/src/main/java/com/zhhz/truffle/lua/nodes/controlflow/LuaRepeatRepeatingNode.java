package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.util.LuaToBooleanNodeGen;
import com.zhhz.truffle.lua.nodes.util.LuaUnboxNodeGen;

/**
 * Lua "repeat ... until" 循环的单次迭代逻辑。
 * <p>
 * 该节点实现了 {@link RepeatingNode}，被包裹在 LoopNode 中运行。
 * 它的核心职责是：执行循环体 -> 检查中断 -> 检查条件 -> 决定是否继续。
 */
public class LuaRepeatRepeatingNode extends Node implements RepeatingNode {

    /** 循环体语句节点 */
    @Child
    private LuaStatementNode bodyNode;

    /**
     * 终止条件表达式节点。
     * 注意：Lua 的 repeat 作用域规则允许 condition 访问 body 中定义的局部变量。
     */
    @Child
    private LuaExpressionNode conditionNode;

    public LuaRepeatRepeatingNode(LuaStatementNode bodyNode, LuaExpressionNode conditionNode) {
        this.bodyNode = bodyNode;

        // 1. 节点包装 (Node Wrapping)与类型特化
        // 我们使用 LuaToBooleanNode(LuaUnboxNode(...)) 对原始条件节点进行两层包装。
        // 目的：确保 evaluateCondition 时无需进行昂贵的类型检查，直接返回 boolean。
        // 如果用户写了 `until "string"`, Unbox 会处理对象，ToBoolean 会按照 Lua 规则将其转为 true。
        this.conditionNode = LuaToBooleanNodeGen.create(LuaUnboxNodeGen.create(conditionNode));

        // 2. 调试信息迁移
        // 包装后的节点需要继承原始节点的源码位置，以便调试器高亮正确的位置。
        this.conditionNode.setSourceSection(conditionNode.getSourceCharIndex(), conditionNode.getSourceLength());
    }

    /**
     * 执行单次循环迭代。
     * LoopNode 会不断调用此方法，直到返回 false。
     *
     * @param frame 当前栈帧
     * @return {@code true} 表示循环应该继续；{@code false} 表示循环应该终止。
     */
    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        // 1. 【先执行循环体】(Post-test Loop)
        // 这是 repeat...until 与 while 的核心区别：无论条件如何，body 至少执行一次。
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaBreakException e) {
            // 2. 【处理 Break】
            // 捕获控制流异常：如果循环体内遇到了 break 语句，
            // 直接返回 false，通知 LoopNode 停止循环。
            return false;
        }
        // 注意：如果有 continue 逻辑，应该在 bodyNode 内部处理，
        // 或者在这里捕获 LuaContinueException 并直接 swallow (什么都不做)，以便执行下面的步骤。

        // 3. 【后判断条件】
        // 执行循环体之后，才计算条件表达式。
        // 这符合 Lua 语义，此时 frame 中可能已经有了 body 中定义的 local 变量。
        boolean conditionMet = evaluateCondition(frame);

        // 4. 【逻辑反转：Until vs While】
        // Lua 语义 (`until cond`): 当 cond 为 true 时 -> 停止。
        // Truffle 语义 (`return boolean`): 当 ret 为 true 时 -> 继续。
        //
        // 所以：
        // Condition True  (Lua: 终止) -> return False (Truffle: 停止)
        // Condition False (Lua: 继续) -> return True  (Truffle: 继续)
        return !conditionMet;
    }

    /**
     * 计算条件表达式的布尔值。
     */
    private boolean evaluateCondition(VirtualFrame frame) {
        try {
            // 由于构造函数中进行了类型特化包装，这里调用 executeBoolean 是安全的且高效的。
            return conditionNode.executeBoolean(frame);
        } catch (UnexpectedResultException ex) {
            // CompilerDirectives.shouldNotReachHere 会告诉 Graal 编译器：
            // 这一块代码是死代码（Dead Code），编译时可以直接剔除，减小机器码体积。
            // 因为 LuaToBooleanNode 保证了只会返回 boolean。
            throw CompilerDirectives.shouldNotReachHere(ex);
        }
    }
}
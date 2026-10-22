package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.util.LuaToBooleanNodeGen;

/**
 * Lua "repeat ... until" 循环的单次迭代逻辑。
 * <p>
 * 该节点实现了 {@link RepeatingNode}，被包裹在 LoopNode 中运行。
 * 它的核心职责是：执行循环体 -> 检查中断 -> 检查条件 -> 决定是否继续。
 */
public class LuaRepeatRepeatingNode extends Node implements RepeatingNode {

    @Child
    private LuaStatementNode bodyNode;

    @Child
    private LuaExpressionNode conditionNode;

    private final BranchProfile continueTaken = BranchProfile.create();
    private final BranchProfile breakTaken = BranchProfile.create();

    // debug 行钩子：until 行（每次条件检查触发；until 为真退出后不额外触发）
    private final int untilLine;

    public LuaRepeatRepeatingNode(LuaStatementNode bodyNode, LuaExpressionNode conditionNode) {
        this(bodyNode, conditionNode, 0);
    }

    public LuaRepeatRepeatingNode(LuaStatementNode bodyNode, LuaExpressionNode conditionNode, int untilLine) {
        this.bodyNode = bodyNode;
        this.conditionNode = LuaToBooleanNodeGen.create(conditionNode);
        this.conditionNode.setSourceSection(conditionNode.getSourceCharIndex(), conditionNode.getSourceLength());
        this.untilLine = untilLine;
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
        TruffleSafepoint.poll(this);
        com.zhhz.truffle.lua.runtime.LuaInterrupt.check(this);
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaContinueException e) {
            continueTaken.enter();
        } catch (LuaBreakException e) {
            breakTaken.enter();
            return false;
        }

        if (com.zhhz.truffle.lua.runtime.LuaLineHook.isAnyActiveContext()) {
            com.zhhz.truffle.lua.runtime.LuaLineHook.fireLine(this, untilLine, false);
        }
        boolean conditionMet = evaluateCondition(frame);
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
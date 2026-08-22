package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 一个专门的表达式节点，用于评估一个表达式列表 (explist)。
 * 它正确处理 Lua 的多返回值语义，即只有最后一个表达式可以扩展为多个值。
 * 这个节点本身可以向上层提供一个 Object[] 数组。
 */
@NodeInfo(description = "评估一系列表达式，处理最后一个表达式的多返回值。")
public final class LuaMultiValueNode extends LuaExpressionNode {

    /**
     * &#064;Children  注解是必须的。它告诉 Truffle 框架，这个数组里的所有节点都是当前节点的子节点。
     * Truffle 会负责管理这些子节点，例如在 AST 复制、节点替换等操作时。
     */
    @Children
    private final LuaExpressionNode[] expressionNodes;

    public LuaMultiValueNode(LuaExpressionNode[] expressionNodes) {
        this.expressionNodes = expressionNodes;
    }

    /**
     * 这是我们为这个节点定义的核心方法，专门用于需要多值的上下文。
     *
     *
     * @param frame 当前的执行帧 (VirtualFrame)
     * @return 一个包含所有表达式结果的 Object 数组。
     */
    @ExplodeLoop
    public Object executeMultiValue(VirtualFrame frame) {
        // 如果表达式列表为空，返回一个空数组。
        int argCount = this.expressionNodes.length;

        // 优化：无参调用直接处理
        if (argCount == 0) {
            return new Object[0];
        }

        final Object[] prefixArgs = new Object[argCount - 1];

        for (int i = 0; i < argCount - 1; i++) {
            prefixArgs[i] = LuaMultiValue.firstValue(this.expressionNodes[i].executeGeneric(frame));
        }


        // 步骤 B: 计算最后一个参数 (可能返回多值)
        Object lastResult = this.expressionNodes[argCount - 1].executeMultiValue(frame);

        return LuaMultiValue.collect(prefixArgs, lastResult);

    }

    /**
     * 实现从父类 LuaExpressionNode 继承的 executeGeneric 方法。
     * 这个方法用于那些不关心多返回值的上下文（例如 `print(f(), g())`）。
     * 根据 Lua 语义，当一个 explist 被用在只需要一个值的上下文中时，它只提供第一个值。
     *
     * @param frame 当前的执行帧 (VirtualFrame)
     * @return 表达式列表的第一个值，如果没有值则返回 null (代表 nil)。
     */
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        // 如果没有表达式，结果是 nil。
        if (expressionNodes.length == 0) {
            return LuaNil.SINGLETON;
        }

        // 只执行第一个表达式并返回其结果。
        // 注意：这里没有调用 executeMultiValue() 是为了优化。如果上层只需要一个值，
        // 我们就没必要计算 explist 中的所有表达式。
        Object firstResult = expressionNodes[0].executeGeneric(frame);
        return LuaMultiValue.firstValue(firstResult);
    }
}

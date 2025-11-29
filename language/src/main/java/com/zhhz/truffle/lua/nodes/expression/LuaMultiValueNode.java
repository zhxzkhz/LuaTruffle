package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
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
    public Object executeMultiValue(VirtualFrame frame) {
        // 如果表达式列表为空，返回一个空数组。
        int argCount = this.expressionNodes.length;

        // 优化：无参调用直接处理
        if (argCount == 0) {
            return LuaNil.SINGLETON;
        }

        final Object[] prefixArgs = new Object[argCount - 1];

        for (int i = 0; i < argCount - 1; i++) {
            // 在这里，我们仍然需要处理参数本身是多返回值的情况
            Object rawArg = this.expressionNodes[i].executeGeneric(frame);
            if (rawArg instanceof LuaMultiValue luaMultiValue) {
                //如果是多值则获取第一个
                if (luaMultiValue.isMultiValue()){
                    prefixArgs[i] = ((Object[]) luaMultiValue.values())[0];
                } else {
                    prefixArgs[i] = luaMultiValue.values();
                }
            } else {
                throw new RuntimeException("LuaReturnNode 返回值错误");
            }
        }


        // 步骤 B: 计算最后一个参数 (可能返回多值)
        Object lastResult = this.expressionNodes[argCount - 1].executeGeneric(frame);

        // 步骤 C: 根据最后一个参数的结果，创建最终数组
        Object[] finalArguments;

        if (lastResult instanceof LuaMultiValue luaMultiValue) {
            if (luaMultiValue.isMultiValue()){
                Object[] values = (Object[]) luaMultiValue.values();
                // --- 情况 1: 最后一个参数返回了多值 ---
                // 计算总长度
                int totalLength = (argCount - 1) + values.length;
                finalArguments = new Object[totalLength];

                // 只有这里发生了真正的数组复制
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                System.arraycopy(values, 0, finalArguments, prefixArgs.length, values.length);

            } else {
                // --- 情况 2: 最后一个参数返回单值 (最常见的情况) ---
                finalArguments = new Object[argCount];
                System.arraycopy(prefixArgs, 0, finalArguments, 0, prefixArgs.length);
                finalArguments[argCount - 1] = luaMultiValue.values();
            }

        } else {

            throw new RuntimeException("LuaReturnNode 返回值错误");
        }


        return finalArguments;

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
        // 如果第一个表达式的结果是多值，我们仍然只返回第一个。
        if (firstResult instanceof Object[] multiResult) {
            return multiResult.length > 0 ? multiResult[0] : null;
        } else {
            return firstResult;
        }
    }
}

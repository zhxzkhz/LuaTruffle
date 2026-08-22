package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.access.LuaWriteVariableNode;
import com.zhhz.truffle.lua.nodes.access.LuaWriteTableNode;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * 代表 Lua 的多重赋值语句 (varlist = explist)。
 * 它从右侧的 LuaMultiValueNode 获取值，然后根据 Lua 规则将它们分配给左侧的变量。
 */
@NodeInfo(description = "表示多赋值语句的节点")
public final class LuaMultiAssignmentNode extends LuaStatementNode {

    /**
     * &#064;Children:  左侧的目标变量列表。
     * 数组中的每个元素都是一个具体的写入节点，比如 LuaWriteLocalVariableNode 或 LuaWriteTableNode。
     */
    @Children
    private final LuaWriteVariableNode[] destinationNodes;

    /**
     * &#064;Child:  右侧的值来源。
     * 这是一个单一的 LuaMultiValueNode，它负责评估整个 explist。
     */
    @Children
    private final LuaExpressionNode[] sourceNodes;

    public LuaMultiAssignmentNode(LuaWriteVariableNode[] destinationNodes, LuaExpressionNode[] sourceValuesNode) {
        this.destinationNodes = destinationNodes;
        this.sourceNodes = sourceValuesNode;
    }

    /**
     * 执行赋值操作。
     * @param frame 当前的执行帧。
     */
    @Override
    @ExplodeLoop
    public void executeVoid(VirtualFrame frame) {
        final Object[] sourceValues;
        int srcLen = sourceNodes.length;
        // --- 1. 计算右侧值的数组 ---
        if (srcLen == 0){
            sourceValues = new Object[0];
        } else {

            final Object[] prefixArgs = new Object[srcLen - 1];
            for (int i = 0; i < srcLen - 1; i++) {
                Object rawArg = this.sourceNodes[i].executeGeneric(frame);
                if (rawArg == null) {
                    throw new RuntimeException("unexpected array or null return value");
                }
                prefixArgs[i] = LuaMultiValue.firstValue(rawArg);
            }


            // 步骤 B: 计算最后一个参数 (可能返回多值)
            Object lastResult = this.sourceNodes[srcLen - 1].executeMultiValue(frame);

            if (lastResult == null) {
                throw new IllegalStateException("unexpected array or null in multi-assignment");
            }
            sourceValues = LuaMultiValue.collect(prefixArgs, lastResult);

        }

        Object[] preparedTargets = new Object[destinationNodes.length];
        for (int i = 0; i < destinationNodes.length; i++) {
            if (destinationNodes[i] instanceof LuaWriteTableNode tableWrite) {
                preparedTargets[i] = tableWrite.prepareTarget(frame);
            }
        }

        // --- 2. 执行赋值 ---

        // 注意：这里不需要检查 destinationNodes.length，因为赋值应该尽可能填满左边
        for (int i = 0; i < destinationNodes.length; i++) {
            final Object valueToAssign;

            if (i < sourceValues.length) {
                valueToAssign = sourceValues[i];
            } else {
                valueToAssign = LuaNil.SINGLETON; // 补 nil
            }

            if (destinationNodes[i] instanceof LuaWriteTableNode tableWrite) {
                tableWrite.writePrepared(frame, preparedTargets[i], valueToAssign);
            } else {
                destinationNodes[i].execute(frame, valueToAssign);
            }
        }
    }
}

package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 实现 Lua 的一元按位非操作符 `~` (Bitwise NOT)。
 */
@NodeInfo(shortName = "~")
@NodeChild("operandNode")
public abstract class LuaBitwiseNotNode extends LuaExpressionNode {

    /** 操作数表达式节点（供运行时类型错误消息使用）。 */
    protected abstract LuaExpressionNode getOperandNode();

    // --- 快速路径 ---
    @Specialization
    protected long doLong(long operand) {
        // Java 的按位取反也是 ~
        return ~operand;
    }

    // --- 慢速/通用路径 ---
    @Fallback
    protected Object doGeneric(Object operand,
                               @Cached(value = "create()", neverDefault = true) LuaCoerceToIntegerNode coerceNode,
                               // 注意：这里需要一元元方法查找器
                               @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode) {

        // 1. 尝试转换为整数
        Long lOperand = coerceNode.execute(operand);
        if (lOperand != null) {
            return ~lOperand;
        }

        // 2. 尝试查找 __bnot 元方法
        Object metaResult = metatableNode.execute(operand, null, LuaMetatables.__BNOT);
        if (metaResult != null) {
            return com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(metaResult);
        }

        // 3. 报错
        boolean numeric = operand instanceof Long || operand instanceof Double
                || operand instanceof com.oracle.truffle.api.strings.TruffleString
                || operand instanceof String;
        if (numeric) {
            throw LuaException.runtimeError("number" + getOperandNode().getErrorSuffix()
                    + " has no integer representation", this);
        }
        throw LuaException.runtimeError("attempt to perform bitwise operation on a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(operand) + " value"
                + getOperandNode().getErrorSuffix(), this);
    }
}

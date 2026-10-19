package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 代表Lua的一元负号运算符 (-)。
 *
 * <p>这个节点有一个子节点，代表被取负的操作数。
 * 它实现了Lua的自动类型转换（字符串 -> 数字）。
 */
@NodeInfo(shortName = "-")
@NodeChild("operandNode")
public abstract class LuaUnaryMinusNode extends LuaExpressionNode {

    // 辅助方法，用于获取子节点，以便在抛出异常时提供给 Truffle
    protected abstract LuaExpressionNode getOperandNode();

    // --- 特化 1: 快速路径，操作数已经是数字 ---

    @Specialization
    protected long negateNumber(long value) {
        return -value;
    }

    @Specialization
    protected double negateDouble(double value) {
        return -value;
    }

    @Specialization(replaces = {"negateNumber", "negateDouble"})
    @TruffleBoundary
    protected Object negateGeneric(Object value,
                                   @Cached(value = "create()", neverDefault = true) LuaCoerceToNumberNode coerce,
                                   @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode) {

        Object object = coerce.executeArithmetic(value);

        if (object != null) {
            if (object instanceof Long objLong) {
                return -objLong;
            } else {
                return -((double) object);
            }
        }

        Object result = metatableNode.execute(value, null, LuaMetatables.__UNM);
        if (result != null) {
            return com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(result);
        }

        throw LuaException.runtimeError("attempt to perform arithmetic on a "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName(value) + " value"
                + getOperandNode().getErrorSuffix(), this);
    }


}

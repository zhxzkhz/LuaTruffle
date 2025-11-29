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
    protected Object negateNumber(long value) {
        // 使用 Math.negateExact 来处理 long 最小值溢出的情况
        try {
            return Math.negateExact(value);
        } catch (ArithmeticException e) {
            return -((double)value);
        }
    }

    @Specialization
    protected double negateNumber(double value) {
        return -value;
    }

    // --- 特化 2: 通用/回退路径，处理需要类型转换的情况 ---
    @Specialization(replaces = "negateNumber")
    @TruffleBoundary
    protected Object negateGeneric(Object value,
                                   @Cached("create()") LuaCoerceToNumberNode coerce,
                                   @Cached("create()") LuaMetatableNode metatableNode) {

        // 尝试将操作数转换为 double
        Object object = coerce.execute(value);

        if (object != null) {
            if (object instanceof Long objLong) {
                return negateNumber(objLong);
            } else {
                return -((double) object);
            }
        }

        Object result = metatableNode.execute(value, null, LuaMetatables.__UNM);
        if (result != null) {
            return result;
        }

        throw LuaException.create("attempt to perform arithmetic on a non-number value",this);
    }


}

package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;

/**
 * 实现了Lua的按位左移 (<<) 和右移 (>>) 运算符。
 * 具体的操作（左移或右移）由 isLeftShift 标志决定。
 */
@NodeInfo(shortName = "shift")
public abstract class LuaBitwiseShiftNode extends LuaBinaryNode { // 假设继承自我们设计的基类

    private final boolean isLeftShift;

    public LuaBitwiseShiftNode(boolean isLeftShift) {
        this.isLeftShift = isLeftShift;
    }

    // --- 特化 1: 快速路径，两个操作数都已经是数字 ---
    @Specialization
    protected long doLong(long left, long right) {
        // Lua 5.3 lvm.c: << 用 luaV_shiftl(x, y)，>> 用 luaV_shiftl(x, -y)
        return isLeftShift ? shiftLeft(left, right) : shiftLeft(left, -right);
    }

    /*
     * 标准 Lua 的 luaV_shiftl：位移量为正时左移，为负时逻辑右移（补零），
     * 位移量绝对值 >= 64 时结果为 0。右移使用无符号移位（intop 先转无符号）。
     */
    private static long shiftLeft(long value, long displacement) {
        if (displacement < 0) {
            if (displacement <= -64) {
                return 0;
            }
            return value >>> (-displacement);
        }
        if (displacement >= 64) {
            return 0;
        }
        return value << displacement;
    }

    @Fallback
    @CompilerDirectives.TruffleBoundary
    protected Object doGeneric(Object left, Object right,
                               @Cached(value = "create()", neverDefault = true) LuaCoerceToIntegerNode coerceLeft,
                               @Cached(value = "create()", neverDefault = true) LuaCoerceToIntegerNode coerceRight,
                               @Cached(value = "create()", neverDefault = true) LuaMetatableNode metatableNode) {

        boolean leftIsNum = isNumberOrString(left);
        boolean rightIsNum = isNumberOrString(right);

        if (leftIsNum && rightIsNum) {
            Long lLeft = coerceLeft.execute(left);
            Long lRight = coerceRight.execute(right);

            if (lLeft != null && lRight != null) {
                return isLeftShift ? shiftLeft(lLeft, lRight) : shiftLeft(lLeft, -lRight);
            } else {
                // 转换失败（例如 1.5 & 1），直接报错！
                LuaExpressionNode badNode = (lLeft == null) ? getLeftNode() : getRightNode();
                throw LuaException.runtimeError("number" + operandSuffix(badNode) + " has no integer representation", this);
            }
        }

        // 3. 只有当【至少有一个】操作数不是数字/字符串时，才查找元方法
        Object metaResult;
        if (isLeftShift) {
            metaResult = metatableNode.execute(left, right, LuaMetatables.__SHL);
        } else {
            metaResult = metatableNode.execute(left, right, LuaMetatables.__SHR);
        }
        if (metaResult != null) {
            return com.zhhz.truffle.lua.runtime.LuaMultiValue.firstValue(metaResult);
        }

        // 4. 元方法也没找到，抛出类型错误
        throw bitwiseError(left, right, leftIsNum, rightIsNum);


    }

}

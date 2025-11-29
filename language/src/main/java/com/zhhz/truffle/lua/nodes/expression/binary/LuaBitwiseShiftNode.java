package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
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
        if (isLeftShift) {
            // 【核心语义】使用 Java 的按位左移 <<
           return left << right;
        } else {
            // 【核心语义】使用 Java 的按位右移 >>
            // 注意：Lua 的 >> 是算术右移，Java的 >> 也是算术右移，所以是正确的。
            return left >> right;
        }
    }

    @Fallback
    @CompilerDirectives.TruffleBoundary
    protected Object doGeneric(Object left, Object right,
                               @Cached("create()") LuaCoerceToIntegerNode coerceLeft,
                               @Cached("create()") LuaCoerceToIntegerNode coerceRight,
                               @Cached("create()") LuaMetatableNode metatableNode) {

        boolean leftIsNum = isNumberOrString(left);
        boolean rightIsNum = isNumberOrString(right);

        if (leftIsNum && rightIsNum) {
            Long lLeft = coerceLeft.execute(left);
            Long lRight = coerceRight.execute(right);

            if (lLeft != null && lRight != null) {
                if (isLeftShift) {
                    return lLeft << lRight;
                } else {
                    return lLeft >> lRight;
                }
            } else {
                // 转换失败（例如 1.5 & 1），直接报错！
                throw LuaException.create("number has no integer representation",this);
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
            return metaResult;
        }

        // 4. 元方法也没找到，抛出类型错误
        throw LuaException.create("attempt to perform bitwise operation on a " + getTypeName(left) + " value",this);


    }

}

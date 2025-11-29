package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;

/**
 * 实现了Lua的整除运算符 (//)。
 */
@NodeInfo(shortName = "//")
public abstract class LuaFloorDivNode extends LuaBinaryNode {

    /**
     * 特化：两个整数相除，除数不为 0。
     * 使用 Math.floorDiv 保证向负无穷取整 (例如 -5 // 2 = -3，而不是 Java 默认的 -2)
     */
    @Specialization(guards = "right != 0")
    protected double doLong(long left, long right) {
        // 注意：Long.MIN_VALUE / -1 会溢出，Math.floorDiv 会自动处理或抛错，
        // 但在 Lua 中这通常应该返回 Long.MIN_VALUE (因为溢出了)。
        // Java 的 Math.floorDiv 在溢出时不会抛错而是返回正确值，除了 Integer.MIN_VALUE/-1。
        // 对于 long，Math.floorDiv 内部处理了溢出。
        return Math.floorDiv(left, right);
    }

    /**
     * 特化：整数除以 0。
     * Lua 规定整数除以 0 必须抛出错误。
     */
    @Specialization(guards = "right == 0")
    protected long doLongZero(long left, long right) {
        throw LuaException.create("attempt to divide by zero",this);
    }

    // --- 2. 浮点数快速路径 ---

    /**
     * 特化：浮点数整除。
     * 结果是 double，值为 floor(a / b)。
     */
    @Specialization
    protected double doDouble(double left, double right) {
        return Math.floor(left / right);
    }

    // --- 3. 混合类型快速路径 ---
    // 只要有一个是 double，结果就是 double

    @Specialization
    protected double doLongDouble(long left, double right) {
        return Math.floor(left / right);
    }

    @Specialization
    protected double doDoubleLong(double left, long right) {
        return Math.floor(left / right);
    }

    // --- 特化 2: 通用/回退路径，处理需要类型转换的情况 ---
    @Specialization()
    @TruffleBoundary
    protected Object floorDivGeneric(Object left, Object right,
                                     @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                                     @Cached("create()") LuaCoerceToNumberNode coerceRight,
                                     @Cached("create()") LuaMetatableNode metatableNode) {

        Object numLeft = coerceLeft.execute(left);
        Object numRight = coerceRight.execute(right);


        // 只有当两个操作数【都】成功转换为数字时，才进行算术运算
        if (numLeft != null && numRight != null) {

            // A. 浮点数路径 (只要有一个是 Double)
            if (numLeft instanceof Double || numRight instanceof Double) {
                // 类型提升：将 Long 转换为 Double
                double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
                double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();

                // 浮点数整除逻辑：
                // 1. 执行除法
                // 2. 向下取整 (Math.floor)
                // 3. 结果必须是 double (例如 5.0 // 2.0 = 2.0)
                // 注意：浮点数除以 0.0 会得到 Infinity，这是合法的，不需要抛错。
                return Math.floor(dLeft / dRight);

            } else {
                // B. 整数路径 (两个都是 Long)
                long lLeft = (Long) numLeft;
                long lRight = (Long) numRight;

                // 整数整除逻辑：

                // 1. 【关键检查】整数除以 0 必须报错
                if (lRight == 0) {
                    throw LuaException.create("attempt to divide by zero", this);
                }

                // 2. 使用 Math.floorDiv
                // 它保证向负无穷取整 (例如 -5 // 2 = -3)
                // 而普通的 '/' 是向零取整 (-5 / 2 = -2)
                return Math.floorDiv(lLeft, lRight);
            }
        }

        Object result = metatableNode.execute(left, right, LuaMetatables.__IDIV);

        if (result != null) {
            return result;
        }

        // 如果元方法也没找到，才报错
        throw LuaException.create("attempt to perform arithmetic on a " + getTypeName(left) + " value",this);

    }


}

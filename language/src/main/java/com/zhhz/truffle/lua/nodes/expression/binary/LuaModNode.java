package com.zhhz.truffle.lua.nodes.expression.binary;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 实现 Lua 的取模运算符 `%` (Modulo).
 * <p>
 * Lua 定义: a % b == a - math.floor(a/b)*b
 * <p>
 * 核心区别：
 * 1. Java 的 `%` 是取余 (Remainder)，对于负数结果符号同被除数。
 * 2. Lua 的 `%` 是取模 (Modulo)，对于负数结果符号同除数。
 *    例如：-10 % 3 -> Java: -1, Lua: 2
 */
@NodeInfo(shortName = "%")
public abstract class LuaModNode extends LuaBinaryNode {

    // --- 1. 整数快速路径 (long % long) ---

    /**
     * 特化：两个整数取模，且除数不为 0。
     * <p>
     * 优化：
     * 1. 使用 Math.floorMod 而不是 %，以符合 Lua 对负数取模的定义。
     * 2. 无需装箱/拆箱，全程在 CPU 寄存器中完成。
     */
    @Specialization(guards = "right != 0")
    protected long doLong(long left, long right) {
        return Math.floorMod(left, right);
    }

    /**
     * 特化：整数除以 0。
     * Lua 规则：整数对 0 取模会抛出错误 (attempt to perform 'n' modulo 0)。
     * 注意：浮点数对 0.0 取模结果是 NaN，不报错。
     */
    @Specialization(guards = "right == 0")
    protected long doLongZero(long left, long right) {
        throw LuaException.create("attempt to perform 'n' modulo 0", this);
    }

    // --- 2. 浮点数快速路径 (double % double) ---

    /**
     * 特化：浮点数取模。
     * Lua 规则：a % b == a - floor(a/b)*b
     */
    @Specialization
    protected double doDouble(double left, double right) {
        return left - Math.floor(left / right) * right;
    }

    // --- 3. 混合类型快速路径 (隐式转换) ---

    /**
     * 特化：long % double -> double
     */
    @Specialization
    protected double doLongDouble(long left, double right) {
        // 将 left 提升为 double
        double dLeft = (double) left;
        return dLeft - Math.floor(dLeft / right) * right;
    }

    /**
     * 特化：double % long -> double
     */
    @Specialization
    protected double doDoubleLong(double left, long right) {
        // 将 right 提升为 double
        double dRight = (double) right;
        return left - Math.floor(left / dRight) * dRight;
    }

    // --- 4. 通用慢速路径 (Fallback) ---

    /**
     * 回退：处理字符串转换、元方法等复杂情况。
     */
    @Fallback
    protected Object doGeneric(Object left, Object right,
                               // 1. 注入类型转换工具
                               @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                               @Cached("create()") LuaCoerceToNumberNode coerceRight,
                               // 2. 注入元方法查找工具
                               @Cached("create()") LuaMetatableNode metatableNode) {

        // --- 第一阶段：尝试转换为数字进行计算 ---

        Object numLeft = coerceLeft.execute(left);
        Object numRight = coerceRight.execute(right);

        if (numLeft != null && numRight != null) {
            // 只要有一个是 Double，结果就是 Double
            if (numLeft instanceof Double || numRight instanceof Double) {
                double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
                double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
                // 浮点取模逻辑
                return dLeft - Math.floor(dLeft / dRight) * dRight;
            } else {
                // 都是 Long
                long lLeft = (Long) numLeft;
                long lRight = (Long) numRight;

                if (lRight == 0) {
                    throw LuaException.create("attempt to perform 'n' modulo 0", this);
                }
                // 整数取模逻辑
                return Math.floorMod(lLeft, lRight);
            }
        }

        // --- 第二阶段：尝试查找 __mod 元方法 ---

        // 如果无法转为数字（例如 Table % Table），查找元方法
        Object metaResult = metatableNode.execute(left, right, LuaMetatables.__MOD);
        if (metaResult != null) {
            return metaResult;
        }

        // --- 第三阶段：报错 ---

        throw LuaException.typeError(this, "%", left, right);
    }
}

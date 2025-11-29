/*
 * Copyright (c) 2012, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
 * 实现 Lua 的乘法运算符 `*` (Multiplication)。
 */
@NodeInfo(shortName = "*")
public abstract class LuaMulNode extends LuaBinaryNode {

    // --- 快速路径 (Fast Paths): 原生类型特化 ---

    /**
     * 特化 1: 两个浮点数相乘。
     * 直接进行 double 运算，这是最高效的浮点路径。
     */
    @Specialization
    protected static double doDouble(double left, double right) {
        return left * right;
    }

    /**
     * 特化 2: 两个整数相乘。
     * <p>
     * 逻辑：
     * 1. 尝试使用 Math.multiplyExact 进行整数乘法，这会检测溢出。
     * 2. 如果发生溢出 (ArithmeticException)，捕获异常并转为 double 运算。
     *    这是 Lua 5.3+ 的标准行为：整数运算溢出后自动升级为浮点数。
     * <p>
     * 注意：由于返回类型是 Object (可能是 Long 或 Double)，这里会发生自动装箱。
     * 在极度追求性能的场景下，通常会拆分为两个特化：一个用 rewriteOn=ArithmeticException 返回 long，
     * 另一个作为 replaces 的特化返回 double。
     */
    @Specialization()
    public static Object doLong(long left, long right) {
        try {
            return Math.multiplyExact(left, right); // 尝试精确乘法
        } catch (ArithmeticException e) {
            // 溢出处理：转为浮点数
            return (double)left * (double)right;
        }
    }

    /**
     * 特化 3: 整数 * 浮点数。
     * Java 会自动将 long 提升为 double，结果为 double。
     */
    @Specialization
    protected static double doDouble(long left, double right) {
        return left * right;
    }

    /**
     * 特化 4: 浮点数 * 整数。
     * 同上，结果为 double。
     */
    @Specialization
    protected static double doDouble(double left, long right) {
        return left * right;
    }


    // --- 慢速路径 (Slow Path): 通用处理 ---

    /**
     * Fallback (回退): 处理所有未被上述特化捕获的情况。
     * 包括：字符串转换、混合对象类型、元方法查找等。
     */
    @Fallback
    protected Object doGeneric(Object left, Object right,
                               // 缓存：用于将字符串等转换为数字的辅助节点
                               @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                               @Cached("create()") LuaCoerceToNumberNode coerceRight,
                               // 缓存：用于查找元方法的辅助节点
                               @Cached("create()") LuaMetatableNode metatableNode) {

        // 开发阶段的断言，确保输入不为空
        assertNotNull(left, right, this);

        // --- 1. 尝试类型转换 (Coercion) ---
        // Lua 允许字符串参与算术运算，例如 "10" * 2
        Object numLeft = coerceLeft.execute(left);
        Object numRight = coerceRight.execute(right);


        // 如果两个操作数都能成功转换为数字
        if (numLeft != null && numRight != null) {

            // A. 浮点数路径
            // 只要有一个操作数是 Double，结果就是 Double
            if (numLeft instanceof Double || numRight instanceof Double) {
                // 安全地将两个数都转为 double 进行计算
                double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
                double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
                return dLeft * dRight;
            } else {
                // B. 整数路径
                // 两个都是 Long (例如两个整数字符串 "10" * "20")
                long lLeft = (Long) numLeft;
                long lRight = (Long) numRight;
                try {
                    // 同样需要处理溢出
                    return Math.multiplyExact(lLeft, lRight);
                } catch (ArithmeticException e) {
                    // 溢出后转为 double
                    return ((double)lLeft * (double)lRight);
                }
            }
        }

        // --- 2. 尝试元方法 (Metamethods) ---
        // 如果无法转换为数字（例如 table * table），查找 __mul 元方法
        Object result = metatableNode.execute(left, right, LuaMetatables.__MUL);
        if (result != null) {
            return result;
        }

        // --- 3. 报错 ---
        // 既不是数字，也没有元方法，抛出 Lua 类型错误
        throw LuaException.typeError(this, "*", left, right);
    }
}
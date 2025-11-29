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
 * 实现了Lua的减法运算符 (-)。
 *
 * <p>这个实现遵循Lua的语义：
 * <ol>
 *   <li>所有数字都按双精度浮点数 (double) 处理。</li>
 *   <li>在运算前，会自动尝试将字符串转换为数字。</li>
 * </ol>
 *
 * <p>它使用了“快速路径-慢速路径”的特化模式以获得最佳性能。
 */
@NodeInfo(shortName = "-")
public abstract class LuaSubNode extends LuaBinaryNode {

    /**
     * 【优化 2: 浮点数路径】
     */
    @Specialization
    protected double doDouble(double left, double right) {
        return left - right;
    }

    // 【关键】确保这个特化存在！
    @Specialization
    protected Object doLong(long left, long right) {
        try {
            return Math.subtractExact(left, right); // 使用 addExact 可以处理溢出
        } catch (ArithmeticException e) {
            // 溢出后，转为 double 计算
            return (double)left - (double)right;
        }
    }

    @Specialization
    protected double doDouble(long left, double right) {
        return left - right;
    }

    @Specialization
    protected double doDouble(double left, long right) {
        return left - right;
    }

    /**
     * Fallback 用于处理字符串转换和元方法。
     */
    @Fallback
    protected Object doGeneric(Object left, Object right,
                               @Cached("create()") LuaCoerceToNumberNode coerceLeft,
                               @Cached("create()") LuaCoerceToNumberNode coerceRight,
                               @Cached("create()") LuaMetatableNode metatableNode) {

        assertNotNull(left,right,this);

        // 1. 尝试将操作数转换为数字
        Object numLeft = coerceLeft.execute(left);
        Object numRight = coerceRight.execute(right);

        // 如果两个值都能被成功转换为数字
        if (numLeft != null && numRight != null) {

            // 只要有任何一个是 double，结果就是 double
            if (numLeft instanceof Double || numRight instanceof Double) {
                // 将两者都安全地转换为 double
                double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
                double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
                return dLeft - dRight;
            } else { // 两个都是 Long
                long lLeft = (Long) numLeft;
                long lRight = (Long) numRight;
                try {
                    return Math.subtractExact(lLeft,lRight);
                } catch (ArithmeticException e) {
                    // 溢出后，转为 double 计算
                    return ((double)lLeft - (double)lRight);
                }
            }
        }

        // 2. 尝试元方法 (逻辑不变)
        Object result = metatableNode.execute(left, right, LuaMetatables.__SUB);
        if (result != null) {
            return result;
        }
        throw LuaException.typeError(this, "-", left, right);
    }

}
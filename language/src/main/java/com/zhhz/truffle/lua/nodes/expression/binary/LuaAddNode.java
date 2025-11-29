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
 * 一个用于执行 “+” 操作的 Lua 节点。该操作可以对任意精度的数字执行加法，
 * 并且字符串自动转出数字
 * <p>
 * 对输入值进行类型特化对于性能至关重要。这是通过节点重写（node rewriting）实现的：
 * 特化过的子类仅处理单一类型，因此那个能够处理所有类型的泛型节点只会在遇到不同类型
 * 的情况下被使用。这些子类由 Truffle DSL 自动生成。此外，一个 {@link LuaAddNodeGen 工厂类}
 * 也会被生成，该类提供了例如 {@link LuaAddNodeGen#create 节点创建} 的功能。
 */
@NodeInfo(shortName = "+")
public abstract class LuaAddNode extends LuaBinaryNode {

    /**
     * 【优化 2: 浮点数路径】
     */
    @Specialization
    protected double doDouble(double left, double right) {
        return left + right;
    }

    // 【关键】确保这个特化存在！
    @Specialization
    protected Object doLong(long left, long right) {
        try {
            return Math.addExact(left, right); // 使用 addExact 可以处理溢出
        } catch (ArithmeticException e) {
            // 溢出后，转为 double 计算
            return  (double)left + (double)right;
        }
    }

    @Specialization
    protected double doDouble(long left, double right) {
        return left + right;
    }

    @Specialization
    protected double doDouble(double left, long right) {
        return left + right;
    }

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
                double dLeft = (numLeft instanceof Double) ? (Double) numLeft : ((Long) numLeft).doubleValue();
                double dRight = (numRight instanceof Double) ? (Double) numRight : ((Long) numRight).doubleValue();
                return dLeft + dRight;
            } else { // 两个都是 Long
                long lLeft = (Long) numLeft;
                long lRight = (Long) numRight;
                try {
                    return Math.addExact(lLeft,lRight); // 使用 addExact 可以处理溢出
                } catch (ArithmeticException e) {
                    // 溢出后，转为 double 计算
                    return ((double) lLeft + (double) lRight);
                }
            }
        }

        // 2. 尝试元方法 (逻辑不变)
        Object result = metatableNode.execute(left, right, LuaMetatables.__ADD);
        if (result != null) {
            return result;
        }

        throw LuaException.typeError(this, "+", left, right);
    }

}

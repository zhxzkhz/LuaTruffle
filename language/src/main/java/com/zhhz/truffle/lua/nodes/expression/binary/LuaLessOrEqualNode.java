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

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaMetatables;

/**
 * 实现 Lua 的 "<=" (小于等于) 操作符节点。
 * <p>
 * Lua 的比较规则：
 * 1. 如果两边都是数字，按数值比较。
 * 2. 如果两边都是字符串，按字典序比较。
 * 3. 否则，抛出类型错误（这与 == 不同，== 类型不同直接返回 false，而 <= 会报错）。
 */
@NodeInfo(shortName = "<=")
public abstract class LuaLessOrEqualNode extends LuaBinaryNode {

    // --- 1. 数字比较的快速路径 (Fast Paths) ---
    // Truffle DSL 会根据运行时参数的实际类型，选择最匹配的方法。

    /**
     * 特化：两个整数 (long) 比较。
     * 这是最高效的路径，直接对应 CPU 的整数比较指令。
     */
    @Specialization
    public static boolean doLong(long left, long right) {
        return left <= right;
    }

    /**
     * 特化：两个浮点数 (double) 比较。
     */
    @Specialization
    public static boolean doDouble(double left, double right) {
        return left <= right;
    }

    /**
     * 特化：左边是浮点数，右边是整数。
     * Java 会自动将 right (long) 提升为 double 进行比较，符合 Lua 语义。
     * (注：方法名 doLong 可能是复制粘贴遗留，但 Truffle 只看参数签名，不看方法名，所以逻辑是正确的)
     */
    @Specialization
    public static boolean doMixed1(double left, long right) {
        return left <= right;
    }

    /**
     * 特化：左边是整数，右边是浮点数。
     * Java 会自动将 left (long) 提升为 double 进行比较。
     */
    @Specialization
    public static boolean doMixed2(long left, double right) {
        return left <= right;
    }

    // --- 2. 字符串比较 ---

    /**
     * 特化：两个 TruffleString 比较。
     * Lua 允许字符串之间进行字典序比较。
     *
     * @param compareNode 这是一个被 @Cached 缓存的内部节点。
     *                    TruffleString 的比较操作比较复杂，缓存一个专门的节点可以提高性能，
     *                    避免每次比较都重新查找最佳策略。
     */
    @Specialization
    public static boolean lessThanOrEqualStrings(TruffleString left, TruffleString right,
                                                 @Cached TruffleString.CompareCharsUTF16Node compareNode) {
        // execute 返回值：负数表示 <，0 表示 ==，正数表示 >
        // 这里判断是否 <= 0
        return compareNode.execute(left, right) <= 0;
    }

    // --- 3. 慢速路径/错误处理 (Slow Path) ---

    /**
     * 回退 (Fallback)：处理所有未被上述特化捕获的情况。
     * <p>
     * 在 Lua 中，你不能比较 "1" <= 2 (类型不匹配)，也不能比较 nil <= nil。
     * 这些情况都会导致运行时错误。
     * <p>
     * &#064;Bind  Node node: 获取当前 AST 节点的位置信息，用于抛出带有行号的异常。
     */
    @Fallback
    public static Object typeError(Object left, Object right,
                                   @Cached("create()") LuaMetatableNode metatableNode,
                                   @Bind Node node) {

        Object result = metatableNode.execute(left, right, LuaMetatables.__LE);

        if (result != null) {
            return LuaTypesUtil.isTruthy(result);
        }
        // 抛出类似于 "attempt to compare string with number" 的错误
        throw LuaException.typeError(node, "<=", left, right);
    }
}
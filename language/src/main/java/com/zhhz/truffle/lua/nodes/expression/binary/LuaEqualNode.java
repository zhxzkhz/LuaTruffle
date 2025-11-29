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

import com.oracle.truffle.api.bytecode.OperationProxy;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaBinaryNode;
import com.zhhz.truffle.lua.nodes.util.LuaMetatableNode;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;

/**
 * 实现 Lua 的 `==` 操作符。
 */
@NodeInfo(shortName = "==")
@OperationProxy.Proxyable(allowUncached = true)
public abstract class LuaEqualNode extends LuaBinaryNode {

    // --- 1. 基础类型的快速路径 (无元方法) ---

    @Specialization
    protected static boolean doBoolean(boolean left, boolean right) {
        return left == right;
    }

    @Specialization
    protected static boolean doLong(long left, long right) {
        return left == right;
    }

    @Specialization
    protected static boolean doDouble(double left, double right) {
        return left == right;
    }

    // 混合数字比较 (例如 1 == 1.0)
    @Specialization
    protected static boolean doLongDouble(long left, double right) {
        return (double) left == right;
    }

    @Specialization
    protected static boolean doDoubleLong(double left, long right) {
        return left == (double) right;
    }

    @Specialization
    protected static boolean doNil(LuaNil left, LuaNil right) {
        return true; // nil == nil is true
    }

    // 字符串内容比较 (TruffleString 优化)
    @Specialization
    protected static boolean doTruffleString(TruffleString left, TruffleString right,
                                      @Cached TruffleString.EqualNode equalNode) {
        return equalNode.execute(left, right, TruffleString.Encoding.UTF_8);
    }

    @Specialization
    protected static boolean doFunction(LuaFunction left, LuaFunction right) {
        return left == right;
    }

    @Specialization
    protected static boolean doBoolean(LuaBoolean left, LuaBoolean right) {
        return left == right;
    }

    // --- 2. 引用类型和通用路径 (包含 __eq 处理) ---

    /**
     * 处理 Table 和其他对象的比较。
     * 逻辑：
     * 1. 引用相等 -> true
     * 2. 引用不等 -> 尝试查找 __eq 元方法
     * 3. 没找到元方法 -> false
     */
    @Specialization(replaces = {"doBoolean", "doLong", "doDouble", "doTruffleString", "doNil", "doFunction"}, limit = "3")
    protected static boolean doGeneric(Object left, Object right,
                                       @CachedLibrary("left") InteropLibrary leftInterop,
                                       @CachedLibrary("right") InteropLibrary rightInterop,
                                        @Cached("create()") LuaMetatableNode metatableNode) {


        // 1. 引用相等检查 (对于 Java 对象、LuaTable 等)
        if (left == right) {
            return true;
        }

        // 【互操作】检查跨语言对象的同一性 (Identity)
        // 如果 left 和 right 是同一个底层对象的不同包装器，这里会返回 true
        if (leftInterop.isIdentical(left, right, rightInterop)) {
            return true;
        }

        // Lua 规则：如果类型不同，直接返回 false (不查元表)
        // 注意：这里假设上面的 Specialization 已经处理了所有合法的数字混合情况
        if (left.getClass() != right.getClass()) {
            return false;
        }

        // 2. 尝试查找 __eq 元方法
        Object result = metatableNode.execute(left, right, LuaMetatables.__EQ);
        if (result != null) {
            return isTruthy(result);
        }

        // 4. 默认不相等
        return false;
    }


}
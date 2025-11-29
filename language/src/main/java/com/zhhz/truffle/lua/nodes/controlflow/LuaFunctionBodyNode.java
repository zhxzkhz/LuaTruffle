/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaNil;

/**
 * Lua 函数体的根节点。
 * <p>
 * 该节点包裹了函数内部所有的语句（通常是一个 BlockNode）。
 * 它的核心职责是建立一个<b>异常捕获边界</b>（Exception Boundary），
 * 用于捕获由 {@link LuaReturnNode} 抛出的 {@link LuaReturnException}。
 * <p>
 * 从语义上讲，函数体虽然内部执行的是语句（executeVoid），但函数调用本身是表达式（executeGeneric），
 * 因此该类继承自 {@link LuaExpressionNode}。
 */
@NodeInfo(shortName = "body")
public final class LuaFunctionBodyNode extends LuaExpressionNode {

    /**
     * 函数的实际执行体。
     * 使用 @Child 注解标记，表示这是 AST 的子节点，Truffle 框架会对齐进行部分求值（Partial Evaluation）。
     */
    @Child private LuaStatementNode bodyNode;

    /**
     * <b>性能分析工具 (Profiling)</b>
     * <p>
     * 这些 BranchProfile 用于在解释器阶段收集执行路径的概率信息。
     * Graal 编译器利用这些信息来生成更优化的机器码。
     * <p>
     * exceptionTaken: 记录函数是否通过显式的 return 语句返回。
     * nullTaken: 记录函数是否执行到了末尾（隐式返回 nil）。
     * <p>
     * 如果在运行时发现某个函数总是显式 return，编译器可能会完全优化掉返回 nil 的路径。
     */
    private final BranchProfile exceptionTaken = BranchProfile.create();
    private final BranchProfile nullTaken = BranchProfile.create();

    public LuaFunctionBodyNode(LuaStatementNode bodyNode) {
        this.bodyNode = bodyNode;
        addRootTag();
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        try {
            /*
             * 执行函数体内的语句序列。
             * 这里的 executeVoid 可能会递归执行很深（例如循环、嵌套块），
             * 任何地方的 return 都会抛出 LuaReturnException。
             */
            bodyNode.executeVoid(frame);

        } catch (LuaReturnException ex) {
            /*
             * 捕获到了显式的 return 信号。
             */

            // 1. 更新 Profile：告诉编译器这条路径（显式返回）被执行了。
            exceptionTaken.enter();

            // 2. 解包返回值：异常对象中携带了真正的计算结果。
            /* 异常传输实际返回值。 */
            return ex.getResult();
        }

        /*
         * 代码执行到了这里，说明 bodyNode 正常执行完毕，且没有遇到 return 语句。
         */

        // 1. 更新 Profile：告诉编译器这条路径（自然结束）被执行了。
        nullTaken.enter();

        // 2. Lua 语义规定：如果没有 return，函数默认返回 nil。
        return LuaNil.SINGLETON;
    }
}

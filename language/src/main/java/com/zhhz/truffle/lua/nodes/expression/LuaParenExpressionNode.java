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
package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

/**
 * 一个 {@link LuaExpressionNode}，表示带括号的表达式，例如 `(a + b)`。
 * <p>
 * 在运行时逻辑上，它仅仅返回被包含（子）表达式的值，不做任何修改。
 * <p>
 * <b>为什么需要这个节点？</b>
 * 它在 AST（抽象语法树）中作为独立节点存在，主要是为了<b>正确的源码归属 (Source Attribution)</b>。
 * 这保留了左右括号之间的词法范围，并允许工具（如 IDE 调试器、Profiler）将该“括号表达式”与其“内部内容”区分开来。
 * 例如，当你在调试器中单步执行时，它可以高亮显示整个 `(exp)` 范围，而不仅仅是内部的 `exp`。
 */
@NodeInfo(description = "A parenthesized expression")
public class LuaParenExpressionNode extends LuaExpressionNode {

    // @Child 注解告诉 Truffle 框架这是一个子节点，需要进行优化（如内联）和管理
    @Child private LuaExpressionNode expression;

    public LuaParenExpressionNode(LuaExpressionNode expression) {
        this.expression = expression;
    }

    public LuaExpressionNode getExpression() {
        return expression;
    }

    /**
     * 通用执行方法：直接委托（Delegate）给子节点执行。
     */
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return expression.executeGeneric(frame);
    }

    /**
     * 特化执行方法 (Long)：
     * 显式重写此方法是为了保持 Truffle 的<b>“快速路径” (Fast Path)</b>。
     * <p>
     * 如果内部表达式能返回原生 long (例如 `(1 + 2)` )，我们希望直接透传这个 long，
     * 而不是回退到 executeGeneric 导致发生自动装箱 (long -> Long 对象) 的性能开销。
     */
    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return expression.executeLong(frame);
    }

    /**
     * 特化执行方法 (Boolean)：同上，为了性能透传原生 boolean 类型。
     */
    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return expression.executeBoolean(frame);
    }
}
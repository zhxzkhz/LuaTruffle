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
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * Lua 语言中 "break" 语句的 AST 节点实现。
 * <p>
 * 在 Truffle 解释器实现中，"非本地控制流"（Non-local control flow，如 break, continue, return）
 * 通常通过抛出特定的 Java 异常（Control Flow Exceptions）来实现。
 * 这些异常会被上层的控制结构节点（如 WhileNode, RepeatNode）捕获，从而实现跳出循环的效果。
 */
@NodeInfo(shortName = "break", description = "The node implementing a break statement")
public final class LuaBreakNode extends LuaStatementNode {

    /**
     * 执行 break 语句。
     * <p>
     * 该方法不执行常规计算，而是立即抛出一个预定义的 {@link LuaBreakException}。
     * <p>
     * <b>Truffle 优化说明：</b>
     * 1. <b>Control Flow Exception</b>: 这个异常不是为了表示错误，而是为了改变执行路径。
     * 2. <b>编译优化</b>: Graal 编译器能够识别这种模式。在经过部分求值（Partial Evaluation）和编译后，
     *    这个 "抛出异常 + 外部捕获" 的动作会被优化为底层的 <b>GOTO 指令</b>（直接跳转），
     *    因此在编译代码中实现了零开销（Zero Overhead），完全没有传统 Java 异常的性能损耗。
     *
     * @param frame 当前栈帧（break 语句本身不需要访问栈帧数据）
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        // 抛出全局单例异常实例。
        // 注意：LuaBreakException 必须禁用栈追踪（fillInStackTrace），
        // 既为了性能（不需要生成堆栈信息），也因为这只是一个控制流信号。
        throw LuaBreakException.SINGLETON;
    }
}
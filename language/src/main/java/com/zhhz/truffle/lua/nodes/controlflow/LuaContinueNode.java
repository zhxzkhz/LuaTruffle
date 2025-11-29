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
 * Lua 语言中 "continue" 语句的 AST 节点实现。
 * <p>
 * 该节点不执行任何实际的计算逻辑，其唯一作用是触发控制流的变更。
 * 它通过抛出 {@link LuaContinueException} 来通知外层的循环节点（如 WhileNode, ForNode）
 * 跳过当前循环体中剩余的语句，并立即开始下一次迭代。
 */
@NodeInfo(shortName = "continue", description = "The node implementing a continue statement")
public final class LuaContinueNode extends LuaStatementNode {

    /**
     * 执行 continue 语句。
     * <p>
     * 该操作会立即中断当前 {@link LuaStatementNode} 序列的执行。
     *
     * @param frame 当前的栈帧对象（本节点无需读取或修改栈帧）
     * @throws LuaContinueException 抛出控制流异常，该异常应当被最近的封闭循环节点捕获。
     */
    @Override
    public void executeVoid(VirtualFrame frame) {
        // 抛出单例异常以通过堆栈展开（Stack Unwinding）退出当前语句块。
        // 在 Graal 编译后的机器码中，这会被转换为直接跳转（JMP/Branch）到循环头的指令，
        // 不会产生任何对象分配或异常处理的运行时开销。
        throw LuaContinueException.SINGLETON;
    }
}

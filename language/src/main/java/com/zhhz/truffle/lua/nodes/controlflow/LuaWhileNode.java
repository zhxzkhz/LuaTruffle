/*
 * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

/**
 * Lua 语言中 "while" 循环的顶层 AST 节点。
 * <p>
 * 语法结构: {@code while condition do block end}
 * <p>
 * <b>职责：</b>
 * 该节点本身不包含循环逻辑，而是作为一个容器，持有 Truffle 框架管理的 {@link LoopNode}。
 * 这种设计允许 Graal 编译器对循环进行深度优化（如循环展开、向量化、OSR 编译）。
 */
@NodeInfo(shortName = "while", description = "The node implementing a while loop")
public final class LuaWhileNode extends LuaStatementNode {

    /**
     * Truffle 的循环管理器节点。
     * 它负责不断调用 {@link LuaWhileRepeatingNode#executeRepeating}，
     * 并监控循环的回边计数（Back-edge count），以决定何时触发 JIT 编译。
     */
    @Child private LoopNode loopNode;

    public LuaWhileNode(LuaExpressionNode conditionNode, LuaStatementNode bodyNode) {
        // 创建 LoopNode，包裹具体的重复执行逻辑
        this.loopNode = Truffle.getRuntime().createLoopNode(new LuaWhileRepeatingNode(conditionNode, bodyNode));
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        // 将控制权完全交给 LoopNode
        loopNode.execute(frame);
    }
}

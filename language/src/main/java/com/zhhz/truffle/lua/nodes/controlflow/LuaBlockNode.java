/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.truffle.api.nodes.BlockNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 一个语句块的节点。
 * 它的唯一职责就是按顺序执行其所有的子语句节点。
 * 这个版本不使用高级的 com.oracle.truffle.api.nodes.BlockNode API，
 * 而是使用标准的 @Children + @ExplodeLoop 模式，这对于大多数语言来说
 * 已经足够高效且更容易理解。
 */
@NodeInfo(shortName = "block", description = "A block of statements")
public final class LuaBlockNode extends LuaStatementNode implements BlockNode.ElementExecutor<LuaStatementNode> {

    /**
     * 【核心】使用 @Child 注解来持有所有的子语句节点。
     * 这是一个标准的、健壮的设计。
     */
    @Child private BlockNode<LuaStatementNode> block;

    public LuaBlockNode(LuaStatementNode[] bodyNodes) {
        this.block = bodyNodes.length > 0 ? BlockNode.create(bodyNodes, this) : null;
    }


    @Override
    public void executeVoid(VirtualFrame frame) {
        if (this.block != null) {
            this.block.executeVoid(frame, BlockNode.NO_ARGUMENT);
        }
    }

    /**
     * 提供一个方法来访问（例如用于调试或工具）块内的语句。
     * @return An unmodifiable list of the statements in this block.
     */
    public List<LuaStatementNode> getBlock() {
        if (block == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(block.getElements()));
    }

    @Override
    public void executeVoid(VirtualFrame frame, LuaStatementNode node, int index, int argument) {
        node.executeVoid(frame);
    }
}

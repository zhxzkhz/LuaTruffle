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
package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import com.zhhz.truffle.lua.LuaLanguage;

import java.util.function.Supplier;

/**
 * 包装所有内置（用Java实现）函数的通用 RootNode。
 *
 * <p>它的职责是从 VirtualFrame 中提取参数，并将它们作为 Object[]
 * 传递给真正的内置函数逻辑节点。
 */
public class BuiltinRootNode extends RootNode {


    // 【关键】子节点现在是一个 *可以接收 Object[]* 的特殊节点
    @Child private LuaBuiltinNode builtinNode;

    public BuiltinRootNode(LuaLanguage language, Supplier<LuaBuiltinNode> builtinNode) {
        super(language);
        this.builtinNode = builtinNode.get();
    }

    public BuiltinRootNode(LuaLanguage language, LuaBuiltinNode builtinNode) {
        super(language);
        this.builtinNode = builtinNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        // 1. 获取完整的参数数组，它包含了父 Frame 在索引 0 的位置
        Object[] fullArguments = frame.getArguments();
        // 2. 【关键修复】创建一个新的、只包含用户参数的数组。
        //    我们跳过第一个元素（父 Frame）。
        //    (假设 LuaFunction.PARENT_FRAME_ARGUMENT_INDEX == 0)

        int userArgumentCount = fullArguments.length - 1;
        Object[] userArguments = new Object[0];
        if (userArgumentCount > 0) {
            userArguments = new Object[userArgumentCount];
            System.arraycopy(fullArguments, 1, userArguments, 0, userArgumentCount);
        }

        // 3. 将【纯净的】用户参数数组传递给业务逻辑节点
        return builtinNode.execute(userArguments);
    }


    @Override
    public SourceSection getSourceSection() {
        return super.getSourceSection();
    }
}


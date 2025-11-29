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
package com.zhhz.truffle.lua.nodes.expression.logical;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;

/**
 * 实现了Lua的 'and' 逻辑运算符。
 *
 * <p>这个节点是Truffle中实现短路求值的标准模式：
 * 手动实现 `executeGeneric` 方法，并在其中有条件地执行子节点。
 */
@NodeInfo(shortName = "and")
// 【修正1】不再继承自 LuaShortCircuitNode，直接继承 LuaExpressionNode
public class LuaLogicalAndNode extends LuaShortCircuitNode {

    public LuaLogicalAndNode(LuaExpressionNode left, LuaExpressionNode right) {
        super(left, right);
    }

    @Override
    protected Object execute(VirtualFrame frame, LuaExpressionNode left, LuaExpressionNode right) {
        // a. 首先，且只执行左边的操作数
        Object leftResult = left.executeGeneric(frame);

        // b. 检查左边的结果是否为“假”（falsy）
        if (evaluateRightProfile.profile( !isTruthy(leftResult))) {
            // c. 如果左边是“假”，短路！直接返回左边的结果。
            //    *不*执行右节点。
            return leftResult;
        } else {
            // d. 如果左边是“真”，则执行并返回右边的结果。
            return right.executeGeneric(frame);
        }
    }


}
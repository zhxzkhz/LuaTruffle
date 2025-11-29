/*
 * Copyright (c) 2016, 2024, Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.truffle.api.profiles.CountingConditionProfile;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

/**
 * Lua 中的逻辑运算使用短路求值：如果对左操作数的求值已经决定了整个运算的结果，
 * 那么右操作数就绝不能被执行。
 * 这一点是通过让 {@link LuaLogicalAndNode} 和 {@link LuaLogicalOrNode}
 * 使用这个基类来体现的。
 */
public abstract class LuaShortCircuitNode extends LuaExpressionNode {

    @Child private LuaExpressionNode left;
    @Child private LuaExpressionNode right;

    /**
     * 短路求值可以像条件语句一样使用，因此对其分支概率进行分析是有意义的。
     */
    public final CountingConditionProfile evaluateRightProfile = CountingConditionProfile.create();

    public LuaShortCircuitNode(LuaExpressionNode left, LuaExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public final Object executeGeneric(VirtualFrame frame) {
        return executeObject(frame);
    }

    @Override
    public final Object executeObject(VirtualFrame frame) {
        return execute(frame,left,right);
    }

    /**
     * 计算短路操作的结果。如果未评估正确的节点，则
     * 提供了<code>false</code>。
     */
    protected abstract Object execute(VirtualFrame frame,LuaExpressionNode leftValue, LuaExpressionNode rightValue);

}

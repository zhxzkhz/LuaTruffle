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
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;

/**
 * Logical disjunction node with short circuit evaluation.
 */
@NodeInfo(shortName = "or")
public final class LuaLogicalOrNode extends LuaShortCircuitNode {


    public LuaLogicalOrNode(LuaExpressionNode left, LuaExpressionNode right) {
        super(left, right);
    }

    /**
     * 手动实现 executeGeneric 以控制子节点的执行顺序（实现短路）。
     */
    @Override
    public Object execute(VirtualFrame frame,LuaExpressionNode left, LuaExpressionNode right) {
        // 1. 首先，且只执行左边的子节点
        Object leftResult;
        try {
            // 我们可以尝试特化地执行左边，比如期望一个boolean
            leftResult = left.executeBoolean(frame);
        } catch (UnexpectedResultException e) {
            // 如果左边返回的不是boolean，就用通用的方式获取
            leftResult = e.getResult();
        }

        // 2. 检查左边的结果是否为“真”（truthy）
        //    profile 方法需要一个 Node 参数，我们传入 `this`
        if (evaluateRightProfile.profile( isTruthy(leftResult))) {
            // 3. 如果左边是“真”，短路！直接返回左边的结果。
            return leftResult;
        } else {
            // 4. 如果左边是“假”，则执行并返回右边的结果。
            return right.executeGeneric(frame);
        }
    }

    // 如果需要，可以为布尔返回类型也提供一个优化的 execute
    @Override
    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        Object result = executeGeneric(frame);
        if (result instanceof Boolean) { // 或者 LuaBoolean
            return (boolean) result;
        }
        // 如果需要严格的类型检查，可以抛出异常
        // 或者根据Lua的规则，将非布尔值转换为布尔值
        throw new UnexpectedResultException(result);
    }



}

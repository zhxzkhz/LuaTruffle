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
package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.CountingConditionProfile;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.util.LuaToBooleanNodeGen;
import com.zhhz.truffle.lua.nodes.util.LuaUnboxNodeGen;

/**
 * Lua 语言中 "if-then-else" 语句的 AST 节点实现。
 * <p>
 * 该节点展示了 Truffle 中实现控制流的两个最佳实践：
 * 1. <b>Condition Profiling</b>: 收集执行时的概率数据供编译器优化。
 * 2. <b>Type Specialization</b>: 确保存储的条件节点总是返回 boolean 类型。
 */
@NodeInfo(shortName = "if", description = "The node implementing a condional statement")
public final class LuaIfNode extends LuaStatementNode {

    /**
     * 条件表达式节点。
     * <p>
     * 注意：这个节点在构造函数中已经被 {@link LuaToBooleanNode} 包装过。
     * 这意味着无论源代码写的是 `if 1 then` 还是 `if "str" then`，
     * 这个节点 executeBoolean 时一定会根据 Lua 规则返回 Java 的 true/false，
     * 从而避免在运行时重复进行类型检查。
     */
    @Child private LuaExpressionNode conditionNode;

    /**
     * 当条件为 true 时执行的语句块（Then 分支）。
     * 使用 @Child 标记以支持部分求值。
     */
    @Child private LuaStatementNode thenPartNode;

    /**
     * 当条件为 false 时执行的语句块（Else 分支）。
     * 这是一个可选子节点（可能为 null）。
     */
    @Child private LuaStatementNode elsePartNode;

    /**
     * <b>条件性能剖析 (Condition Profiling)</b>
     * <p>
     * 这是一个极其重要的字段。Truffle 解释器在运行时会通过这个 Profile 记录
     * 条件为 true 和 false 的频率。
     * <p>
     * <b>作用：</b>
     * 当 Graal 编译器编译此代码时，它会查看这个 Profile：
     * - 如果 true 的概率是 100%（比如调试开关），编译器会把 false 分支的代码彻底移除（Dead Code Elimination）。
     * - 如果 true 的概率是 90%，编译器会把 true 分支生成的机器码放在紧接着的位置（优化指令缓存），
     *   而把 else 分支放在较远的位置（Cold Path）。
     */
    private final CountingConditionProfile condition = CountingConditionProfile.create();

    public LuaIfNode(LuaExpressionNode conditionNode, LuaStatementNode thenPartNode, LuaStatementNode elsePartNode) {
        // 1. 类型强转链构造：
        // LuaUnboxNode: 负责处理可能的对象包装。
        // LuaToBooleanNode: 负责将任意 Lua 值转为 boolean (在 Lua 中只有 false 和 nil 是假，其他均为真)。
        // 这样做的目的是确保 this.conditionNode.executeBoolean() 永远可用且高效。
        this.conditionNode = LuaToBooleanNodeGen.create(LuaUnboxNodeGen.create(conditionNode));

        // 2. 调试信息迁移：
        // 因为我们用 Wrapper 节点包裹了原始节点，需要把原始源码位置信息复制到新节点上，
        // 否则调试器单步调试时可能无法正确定位。
        this.conditionNode.setSourceSection(conditionNode.getSourceCharIndex(), conditionNode.getSourceLength());

        this.thenPartNode = thenPartNode;
        this.elsePartNode = elsePartNode;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        /*
         * 1. 计算条件并进行 Profile 记录。
         * evaluateCondition(frame) 计算出 boolean 结果。
         * condition.profile(...) 将结果“注入”到 Profile 中并透传返回。
         */
        if (condition.profile(evaluateCondition(frame))) {
            /* 2. 执行 Then 分支 */
            thenPartNode.executeVoid(frame);
        } else {
            /* 3. 执行 Else 分支（如果存在） */
            if (elsePartNode != null) {
                elsePartNode.executeVoid(frame);
            }
        }
    }

    private boolean evaluateCondition(VirtualFrame frame) {
        try {
            /*
             * 这里的 executeBoolean 是特化（Specialized）版本的方法。
             * 因为我们在构造函数里已经包装了 LuaToBooleanNode，理论上它不可能抛出 UnexpectedResultException。
             */
            return conditionNode.executeBoolean(frame);
        } catch (UnexpectedResultException ex) {
            /*
             * 防御性编程：如果发生了 UnexpectedResultException，说明我们的 AST 构造逻辑有漏洞
             * (即 LuaToBooleanNode 返回了非 boolean 值)，这是编译器内部错误。
             */
            throw CompilerDirectives.shouldNotReachHere(ex);
        }
    }
}
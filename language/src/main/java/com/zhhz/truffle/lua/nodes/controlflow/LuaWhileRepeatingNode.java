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
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.util.LuaToBooleanNodeGen;
import com.zhhz.truffle.lua.nodes.util.LuaUnboxNodeGen;

/**
 * While 循环的单次迭代逻辑实现。
 * <p>
 * 该节点实现了 {@link RepeatingNode} 接口。
 * 它的核心逻辑遵循 "前测试循环" (Pre-test Loop) 模式：先检查条件，再执行循环体。
 */
public final class LuaWhileRepeatingNode extends Node implements RepeatingNode {

    /**
     * 循环条件表达式节点。
     * <p>
     * 注意：我们在构造函数中对这个节点进行了包装（Unbox + ToBoolean），
     * 确保它总是返回 boolean 类型，避免在热路径（Hot Path）中进行重复的类型检查。
     */
    @Child private LuaExpressionNode conditionNode;

    /**
     * 循环体语句节点（通常是一个 BlockNode）。
     * 当条件为 true 时执行。
     */
    @Child private LuaStatementNode bodyNode;

    /**
     * <b>分支剖析 (Branch Profiling)</b>
     * <p>
     * 这些 Profile 用于在解释器阶段收集控制流信息，指导编译器生成更优的机器码。
     * <p>
     * <b>continueTaken</b>:
     * 如果循环运行了 100 万次从未触发 continue，Profile 会告诉编译器。
     * 编译器将假设 continue 不存在，从而生成没有跳转检查的线性代码。
     * 一旦触发一次 continue，代码会去优化（Deoptimize）并重新编译包含跳转逻辑的代码。
     * <p>
     * <b>breakTaken</b>: 同理，用于优化 break 路径。
     */
    private final BranchProfile continueTaken = BranchProfile.create();
    private final BranchProfile breakTaken = BranchProfile.create();

    public LuaWhileRepeatingNode(LuaExpressionNode conditionNode, LuaStatementNode bodyNode) {
        // 1. 构造转换链：原始节点 -> 解包(Unbox) -> 转布尔(ToBoolean)
        // 这样 evaluateCondition 方法就能安全地调用 executeBoolean 了。
        this.conditionNode = LuaToBooleanNodeGen.create(LuaUnboxNodeGen.create(conditionNode));
        // 迁移源码位置信息用于调试
        this.conditionNode.setSourceSection(conditionNode.getSourceCharIndex(), conditionNode.getSourceLength());

        this.bodyNode = bodyNode;
    }

    /**
     * 执行单次迭代。
     *
     * @param frame 当前栈帧
     * @return {@code true} 表示继续下一次循环；{@code false} 表示退出循环。
     */
    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        // 1. 【前置条件检查】
        // 如果条件为假，直接退出循环 (return false)。
        if (!evaluateCondition(frame)) {
            return false;
        }

        try {
            // 2. 【执行循环体】
            /* Execute the loop body. */
            bodyNode.executeVoid(frame);

            // 3. 【正常继续】
            // 循环体执行完毕，没有异常，返回 true 继续下一轮
            /* Continue with next loop iteration. */
            return true;

        } catch (LuaContinueException ex) {
            // 4. 【处理 continue】
            /* In the interpreter, record profiling information that the loop uses continue. */
            // 记录 Profile：告诉编译器这个循环里确实用到了 continue
            continueTaken.enter();

            // 捕获异常后，返回 true，意味着跳过本轮剩余代码（本例中无剩余代码），
            // 重新开始下一轮（即再次调用 executeRepeating 并检查条件）。
            return true;

        } catch (LuaBreakException ex) {
            // 5. 【处理 break】
            // 记录 Profile：告诉编译器这个循环里确实用到了 break
            breakTaken.enter();

            // 捕获 break，返回 false，通知 LoopNode 彻底停止循环。
            return false;
        }
    }

    /**
     * 辅助方法：计算条件布尔值。
     */
    private boolean evaluateCondition(VirtualFrame frame) {
        try {
            /*
             * 调用特化的 boolean 执行方法。
             * 由于构造函数里的 LuaToBooleanNode 包装，这里只会返回 boolean。
             */
            return conditionNode.executeBoolean(frame);
        } catch (UnexpectedResultException ex) {
            // 这是一个 "不可能到达的代码" (Unreachable Code)。
            // 告诉编译器可以放心地移除这部分异常处理逻辑，减小代码体积。
            throw CompilerDirectives.shouldNotReachHere(ex);
        }
    }

    @Override
    public String toString() {
        return LuaStatementNode.formatSourceSection(this);
    }
}
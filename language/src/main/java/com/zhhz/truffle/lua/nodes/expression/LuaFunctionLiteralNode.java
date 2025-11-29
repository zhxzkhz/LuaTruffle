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
package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;

/**
 * 代表一个函数字面量 (e.g., "function() ... end") 的AST节点。
 * 这个节点在解析时被创建，并持有一个指向该函数代码的 RootCallTarget。
 * 当执行时，它会捕获当前作用域（用于闭包）并创建一个新的 LuaFunction 对象。
 */
@NodeInfo(shortName = "function")
public class LuaFunctionLiteralNode extends LuaExpressionNode {

    private final RootCallTarget callTarget;
    // (可选) 可以在这里存储一个函数名，用于调试
    private final TruffleString name;

    public LuaFunctionLiteralNode(RootCallTarget callTarget) {
        this.callTarget = callTarget;
        this.name = null;
    }

    public LuaFunctionLiteralNode(RootCallTarget callTarget,TruffleString name) {
        this.callTarget = callTarget;
        this.name = name;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        // 1. 【关键步骤：闭包捕获】
        //    frame 是当前正在执行的函数的栈帧。
        //    对于新定义的函数来说，这个 frame 就是它的“父作用域”。
        //    我们调用 materialize() 将其持久化，防止它随着当前函数返回而失效。
        MaterializedFrame parentFrame = frame.materialize();

        // 2. 创建函数对象
        //    我们将实际的对象创建逻辑放入慢路径，以避免 JIT 编译器
        //    在处理 Frame 逃逸分析时遇到困难。
        return createFunction(parentFrame);
    }

    /**
     * 辅助方法：创建 LuaFunction 对象。
     * 标记为 @TruffleBoundary 是为了“切断” JIT 对 Frame 对象的深度分析，
     * 防止出现 "FrameWithoutBoxing ... should not be materialized" 这样的 Bailout 异常。
     */
    @CompilerDirectives.TruffleBoundary
    private LuaFunction createFunction(MaterializedFrame parentFrame) {
        // 获取当前的语言上下文，以便获取对象的 Shape（如果你使用了 Shape 系统）
        LuaContext context = LuaContext.get(this);

        // 创建并返回运行时对象
        // 传入：Shape, 代码入口(CallTarget), 闭包环境(parentFrame)
        return new LuaFunction(context.getFunctionShape(), this.callTarget, parentFrame,name);
    }

}
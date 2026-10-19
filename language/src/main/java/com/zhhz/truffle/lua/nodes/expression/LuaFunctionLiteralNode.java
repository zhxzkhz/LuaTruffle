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
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

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

    // Lua 5.3.0 的闭包缓存：每个函数原型保留最近一次创建的闭包，
    // 若本次解析出的 upvalue cell 与缓存闭包完全一致，则复用同一闭包对象
    // (lvm.c 中的 getcached/pushclosure)。这样循环中不捕获新鲜变量的闭包
    // 会得到相同对象，从而 `a[3] == a[4]` 成立。
    private LuaFunction cachedFunction;

    public LuaFunctionLiteralNode(RootCallTarget callTarget) {
        this.callTarget = callTarget;
        this.name = null;
    }

    public LuaFunctionLiteralNode(RootCallTarget callTarget,TruffleString name) {
        this.callTarget = callTarget;
        this.name = name;
    }

    /** 该函数字面量对应的 CallTarget（其 RootNode 为 LuaRootNode）。 */
    public RootCallTarget getCallTarget() {
        return callTarget;
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
     * 辅助方法：创建 LuaFunction 对象，并在创建时把被捕获的 upvalue cell 快照进闭包。
     * 标记为 {@code @TruffleBoundary} 是为了“切断” JIT 对 Frame 对象的深度分析，
     * 防止出现 "FrameWithoutBoxing ... should not be materialized" 这样的 Bailout 异常。
     */
    @CompilerDirectives.TruffleBoundary
    private LuaFunction createFunction(MaterializedFrame parentFrame) {
        // 获取当前的语言上下文，以便获取对象的 Shape（如果你使用了 Shape 系统）
        LuaContext context = LuaContext.get(this);

        // 【关键】快照 upvalue cell：闭包创建后，其捕获的变量单元即固定，
        // 从而保证循环体每次迭代创建的闭包持有各自独立的 cell。
        Object[] cells = null;
        if (callTarget instanceof RootCallTarget rootCallTarget
                && rootCallTarget.getRootNode() instanceof LuaRootNode root) {
            LuaRootNode.UpvalueDescriptor[] descriptors = root.getUpvalueDescriptors();
            if (descriptors.length > 0) {
                cells = new Object[descriptors.length];
                for (int i = 0; i < descriptors.length; i++) {
                    cells[i] = resolveUpvalueCell(parentFrame, descriptors[i]);
                }
            }
        }

        // 复用一个已缓存的、upvalue cell 完全相同的闭包（Lua 5.3.0 语义）。
        LuaFunction cached = this.cachedFunction;
        if (cached != null && sameUpvalueCells(cached.getUpvalues(), cells)) {
            return cached;
        }

        // 创建并返回运行时对象
        LuaFunction function = new LuaFunction(context.getFunctionShape(), this.callTarget, parentFrame, name);
        if (cells != null) {
            function.setUpvalues(cells);
        }
        this.cachedFunction = function;
        return function;
    }

    /** 逐元素按对象身份比较两个 upvalue 数组（空/空视为相同）。 */
    private static boolean sameUpvalueCells(Object[] cached, Object[] current) {
        if (cached.length == 0 && current == null) {
            return true;
        }
        if (current == null || cached.length != current.length) {
            return false;
        }
        for (int i = 0; i < cached.length; i++) {
            if (cached[i] != current[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 沿静态链定位 upvalue 声明所在的帧，并取出该槽位中的 {@link LuaCell}。
     * 若槽位尚未以 cell 形式存放（理论上不应发生），则以当前值包装一个 cell。
     */
    private static Object resolveUpvalueCell(Frame frame, LuaRootNode.UpvalueDescriptor descriptor) {
        Frame declaringFrame = frame;
        for (int i = 1; i < descriptor.depth(); i++) {
            Object[] args = declaringFrame.getArguments();
            Object caller = args.length > 0 ? args[0] : null;
            if (!(caller instanceof LuaFunction parentFunction)) {
                return new LuaCell(LuaNil.SINGLETON);
            }
            MaterializedFrame parentFrame = parentFunction.getParentFrame();
            if (parentFrame == null) {
                return new LuaCell(LuaNil.SINGLETON);
            }
            declaringFrame = parentFrame;
        }
        int slot = descriptor.slot();
        if (slot < 0) {
            return new LuaCell(LuaNil.SINGLETON);
        }
        Object value;
        try {
            value = declaringFrame.getValue(slot);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            return new LuaCell(LuaNil.SINGLETON);
        }
        if (value instanceof LuaCell cell) {
            return cell;
        }
        return new LuaCell(value == null ? LuaNil.SINGLETON : value);
    }

}
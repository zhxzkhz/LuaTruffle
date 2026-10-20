/*
 * Copyright (c) 2022, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.controlflow.LuaReturnException;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaLineHook;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTailCallException;

public final class LuaAstRootNode extends LuaRootNode {

    @Child private LuaStatementNode bodyNode;

    private final TruffleString name;

    private final SourceSection sourceSection;

    /**
     * 是否为顶层入口（main chunk / dofile 等）。顶层 execute 自行处理尾调用循环，
     * 因为其外层没有 {@link LuaFunction.Execute} 的循环来捕获冒泡的
     * {@link LuaTailCallException}。非顶层函数（由 LuaFunction 调用）不在此捕获，
     * 让异常冒泡到 {@link LuaFunction.Execute} 的循环。
     */
    private final boolean topLevel;

    /** 顶层尾调用重试缓存。 */
    @Child private DirectCallNode tailCallNode;

    public LuaAstRootNode(LuaLanguage language, FrameDescriptor frameDescriptor, LuaStatementNode bodyNode,
                          SourceSection sourceSection, TruffleString name) {
        this(language, frameDescriptor, bodyNode, sourceSection, name, false);
    }

    public LuaAstRootNode(LuaLanguage language, FrameDescriptor frameDescriptor, LuaStatementNode bodyNode,
                          SourceSection sourceSection, TruffleString name, boolean topLevel) {
        super(language, frameDescriptor, name);
        this.bodyNode = bodyNode;
        this.sourceSection = sourceSection;
        this.name = name;
        this.topLevel = topLevel;
    }

    @Override
    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public TruffleString getTSName() {
        return name;
    }

    @Override
    protected boolean isInstrumentable() {
        return super.isInstrumentable();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        com.zhhz.truffle.lua.runtime.LuaContext context = com.zhhz.truffle.lua.runtime.LuaContext.get(this);
        if (context != null) {
            context.enterLuaCall(this);
            // 与参考实现一致：call 钩子在函数帧建立之后触发，
            // 因此钩子内 debug.getinfo(2) 看到的是被调用的函数（而不是调用点）。
            if (context.isAnyHookActive()) {
                LuaLineHook.fireCall(context);
            }
        }
        try {
            try {
                bodyNode.executeVoid(frame);
            } catch (LuaReturnException e) {
                return e.getResult();
            } catch (LuaTailCallException e) {
                if (topLevel) {
                    return doTailCall(e);
                }
                // 非顶层：让异常冒泡到调用本 CallTarget 的 LuaFunction.Execute 循环
                throw e;
            }
            // Lua 函数体正常结束且无 return：对 Lua 调用方意味着 0 个返回值；
            // 而 host 直接 eval 一个 chunk 时（调用参数为空），约定映射为 nil。
            if (topLevel && frame.getArguments().length == 0) {
                return LuaNil.SINGLETON;
            }
            return new LuaMultiValue(new Object[0]);
        } finally {
            if (context != null) {
                // return 钩子在帧销毁之前触发，level 2 仍是将返回的函数。
                if (context.isAnyHookActive()) {
                    LuaLineHook.fireReturn(context);
                }
                context.exitLuaCall();
            }
        }
    }

    /**
     * 顶层尾调用重试循环：在固定 Java 栈深度下反复执行尾递归目标，直到正常返回。
     */
    private Object doTailCall(LuaTailCallException initial) {
        LuaTailCallException e = initial;
        while (true) {
            LuaFunction target = e.getFunction();
            CallTarget callTarget = target.getCallTarget();
            Object[] args = e.getArguments();
            Object[] frameArguments = new Object[args.length + 1];
            frameArguments[0] = target;
            System.arraycopy(args, 0, frameArguments, 1, args.length);

            if (tailCallNode == null) {
                tailCallNode = DirectCallNode.create(callTarget);
            } else if (tailCallNode.getCallTarget() != callTarget) {
                tailCallNode = DirectCallNode.create(callTarget);
            }
            try {
                return tailCallNode.call(frameArguments);
            } catch (LuaTailCallException next) {
                e = next;
            }
        }
    }

}

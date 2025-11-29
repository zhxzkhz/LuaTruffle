/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.controlflow.LuaReturnException;
import com.zhhz.truffle.lua.runtime.LuaContext;


/**
 * 整个Lua脚本文件执行的根节点 (RootNode)。
 *
 * <p>这个节点是Truffle执行的入口点。它的职责非常简单：
 * 执行代表脚本顶层代码的主代码块 (a LuaBlockNode)。
 */
public final class LuaEvalRootNode extends RootNode {

    private static final TruffleString ROOT_EVAL = LuaContext.toTruffleString("root eval");

    /**
     * 子节点，代表了整个脚本的顶层代码。
     * 通常这是一个包含了所有顶层语句的 LuaBlockNode。
     */
    @Child private LuaStatementNode bodyNode;

    private final LuaLanguage language;

    public LuaEvalRootNode(LuaLanguage language, LuaStatementNode bodyNode) {
        super(language);
        this.language = language;
        this.bodyNode = bodyNode;
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    protected boolean isInstrumentable() {
        return false;
    }

    @Override
    public String getName() {
        return ROOT_EVAL.toJavaStringUncached();
    }

    public static TruffleString getTSName() {
        return ROOT_EVAL;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            // 直接执行脚本的主体部分
            bodyNode.executeVoid(frame);

        } catch (LuaReturnException e) {
            // 如果顶层代码块中有 'return' 语句，捕获它并返回其结果。
            return e.getResult();
        }

        // 如果脚本正常执行完毕，且没有顶层的 return 语句，
        // 则脚本的执行结果为空（在我们的模型中，是一个空的多值返回）。
        return new Object[0];
    }


}

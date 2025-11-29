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

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.controlflow.LuaReturnException;
import com.zhhz.truffle.lua.runtime.LuaNil;

public final class LuaAstRootNode extends LuaRootNode {

    @Child private LuaStatementNode bodyNode;

    private final TruffleString name;

    private final SourceSection sourceSection;

    public LuaAstRootNode(LuaLanguage language, FrameDescriptor frameDescriptor, LuaStatementNode bodyNode,
                          SourceSection sourceSection, TruffleString name) {
        super(language, frameDescriptor, name);
        this.bodyNode = bodyNode;
        this.sourceSection = sourceSection;
        this.name = name;
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

        // --- 2. 执行函数体 ---
        try {
            bodyNode.executeVoid(frame);
        } catch (LuaReturnException e) {
            var objects = e.getResult();
            if (e.getResult() instanceof Object[] values){
                if (values.length == 0) return LuaNil.SINGLETON; // nil
                if (values.length == 1) return values; // 【关键】返回单个值本身
            }
            return objects; // 【关键】返回 Object[]
        }
        // --- 3. 默认返回值 ---
        return LuaNil.SINGLETON;

    }

}

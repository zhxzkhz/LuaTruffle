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
package com.zhhz.truffle.lua.nodes;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * 所有Lua函数（包括顶层脚本）的执行根节点。
 *
 * <p>这个类是函数执行的入口点。它负责：
 * <ol>
 *   <li>处理函数参数的接收。</li>
 *   <li>执行函数体。</li>
 *   <li>处理返回值。</li>
 *   <li>(可选) 与Truffle调试器交互。</li>
 * </ol>
 */

@NodeInfo(language = "Lua", description = "所有Lua执行树的根")
public abstract class LuaRootNode extends RootNode {

    private final TruffleString name;

    /**
     * 固定参数个数（不含 {@code ...}）。-1 表示未设置（main chunk 等）。
     * 用于 {@code debug.getlocal(f, n)} 仅返回参数名而非局部变量名。
     */
    private int paramCount = -1;

    /** 是否为变长参数函数（含 {@code ...}）。用于 debug.getlocal 的负索引 vararg 语义。 */
    private boolean variadic;

    /**
     * 一个函数按首次引用顺序排列的 upvalue 描述符。
     *
     * @param name  upvalue 名
     * @param slot  槽位在祖先帧中的索引
     * @param depth 需要沿静态链向上跨越的函数边界数（1 表示直接父帧）
     */
    public record UpvalueDescriptor(TruffleString name, int slot, int depth) {}

    private UpvalueDescriptor[] upvalueDescriptors = new UpvalueDescriptor[0];

    /**
     * 该根节点是否为一个 chunk（main chunk 或 load/require 加载的代码块）。
     * chunk 的 {@code _ENV} 是特殊 upvalue，单独由 LuaFunction 的环境字段处理。
     */
    private boolean chunk;

    public UpvalueDescriptor[] getUpvalueDescriptors() {
        return upvalueDescriptors;
    }

    public void setUpvalueDescriptors(UpvalueDescriptor[] upvalueDescriptors) {
        this.upvalueDescriptors = upvalueDescriptors;
    }

    public boolean isChunk() {
        return chunk;
    }

    public void setChunk(boolean chunk) {
        this.chunk = chunk;
    }

    public LuaRootNode(LuaLanguage language, FrameDescriptor frameDescriptor, TruffleString name) {
        super(language, frameDescriptor);
        this.name = name;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    public boolean isVariadic() {
        return variadic;
    }

    public void setVariadic(boolean variadic) {
        this.variadic = variadic;
    }

    @Override
    public String getName() {
        return name != null ? name.toJavaStringUncached() : "anonymous";
    }

    public abstract TruffleString getTSName();

    @Override
    public String toString() {
        return getName();
    }

    // 我们甚至可以把 language 字段暴露给子类
    public final LuaLanguage getLuaLanguage() {
        return getLanguage(LuaLanguage.class);
    }


}

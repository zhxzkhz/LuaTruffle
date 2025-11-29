/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.zhhz.truffle.lua.runtime.LuaContext;


// @NodeInfo 用于调试工具显示节点信息
@NodeInfo(language = "Lua", description = "所有Lua语句的抽象基节点")
// @GenerateWrapper 告诉 Truffle DSL 生成一个包装类（LuaStatementNodeWrapper），用于支持调试器断点等工具
@GenerateWrapper
public abstract class LuaStatementNode extends Node implements InstrumentableNode {

    private static final int NO_SOURCE = -1;
    private static final int UNAVAILABLE_SOURCE = -2;

    // 使用 int 存储源码位置索引，比直接存储 SourceSection 对象更节省内存
    private int sourceCharIndex = NO_SOURCE;
    private int sourceLength;

    // 标记该节点是否属于某种特定的标签（用于调试器识别）
    private boolean hasStatementTag;
    private boolean hasRootTag;

    /*
     * SourceSection 的创建是“惰性”的（Lazy）。
     * 我们只存储索引，只有在真正需要源码信息时（例如报错或调试时）才查找 RootNode 并创建对象。
     * 这避免了在解析阶段创建大量的 SourceSection 对象，节省内存并加快启动速度。
     */
    @Override
    @TruffleBoundary // 标记为边界，因为创建对象和查找 Source 是慢速路径，不应被 JIT 编译内联
    public final SourceSection getSourceSection() {
        if (sourceCharIndex == NO_SOURCE) {
            // AST 节点没有关联源码
            return null;
        }
        RootNode rootNode = getRootNode();
        if (rootNode == null) {
            // 节点还没有被挂载到 RootNode 下
            return null;
        }
        SourceSection rootSourceSection = rootNode.getSourceSection();
        if (rootSourceSection == null) {
            return null;
        }
        Source source = rootSourceSection.getSource();
        if (sourceCharIndex == UNAVAILABLE_SOURCE) {
            if (hasRootTag && !rootSourceSection.isAvailable()) {
                return rootSourceSection;
            } else {
                return source.createUnavailableSection();
            }
        } else {
            // 根据索引和长度创建具体的源码片段对象
            return source.createSection(sourceCharIndex, sourceLength);
        }
    }

    // 判断当前节点是否有关联的源码位置
    public final boolean hasSource() {
        return sourceCharIndex != NO_SOURCE;
    }

    // InstrumentableNode 接口实现：只要有源码，就允许被工具（如调试器）通过 Wrapper 包装
    @Override
    public final boolean isInstrumentable() {
        return hasSource();
    }

    public final int getSourceCharIndex() {
        return sourceCharIndex;
    }

    public final int getSourceEndIndex() {
        return sourceCharIndex + sourceLength;
    }

    public final int getSourceLength() {
        return sourceLength;
    }

    // 由解析器（Parser）在构建 AST 时调用，设置源码位置
    public final void setSourceSection(int charIndex, int length) {
        assert sourceCharIndex == NO_SOURCE : "source must only be set once";
        if (charIndex < 0) {
            throw new IllegalArgumentException("charIndex < 0");
        } else if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        this.sourceCharIndex = charIndex;
        this.sourceLength = length;
    }

    // 标记源码不可用（例如由内部逻辑生成的代码）
    public final void setUnavailableSourceSection() {
        assert sourceCharIndex == NO_SOURCE : "source must only be set once";
        this.sourceCharIndex = UNAVAILABLE_SOURCE;
    }

    // InstrumentableNode 接口实现：判断节点是否匹配特定标签
    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.StatementTag.class) {
            return hasStatementTag;
        } else if (tag == StandardTags.RootTag.class || tag == StandardTags.RootBodyTag.class) {
            return hasRootTag;
        }
        return false;
    }

    // InstrumentableNode 接口实现：创建包装节点
    // 注意：LuaStatementNodeWrapper 类是编译时自动生成的
    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new LuaStatementNodeWrapper(this, probe);
    }

    /**
     * 执行此语句节点。
     * 语句通常只产生副作用（如赋值、打印），不需要返回值，因此是 void。
     */
    public abstract void executeVoid(VirtualFrame frame);

    /**
     * 标记此节点为一个“语句”（Statement）。
     * 调试器可以使用此标签来实现“单步跳过”（Step Over）功能。
     */
    public final void addStatementTag() {
        hasStatementTag = true;
    }

    /**
     * 标记此节点为函数体或根节点。
     * 调试器可以使用此标签来识别函数的入口和出口。
     */
    public final void addRootTag() {
        hasRootTag = true;
    }

    // 获取当前的语言上下文
    // 注意：在高性能路径（execute 方法内部）最好使用 @CachedContext 而不是这个方法
    public final LuaContext getContext() {
        return LuaContext.get(this);
    }

    @Override
    public String toString() {
        return formatSourceSection(this);
    }

    /**
     * 辅助方法：格式化源码位置信息，用于日志或调试显示。
     * 格式通常为 "文件名:行号"。
     */
    public static String formatSourceSection(Node node) {
        if (node == null) {
            return "<unknown>";
        }
        SourceSection section = node.getSourceSection();
        boolean estimated = false;
        // 如果当前节点没有位置信息，尝试向上查找父节点的位置
        if (section == null) {
            section = node.getEncapsulatingSourceSection();
            estimated = true;
        }

        if (section == null || section.getSource() == null) {
            return "<unknown source>";
        } else {
            String sourceName = section.getSource().getName();
            int startLine = section.getStartLine();
            return String.format("%s:%d%s", sourceName, startLine, estimated ? "~" : "");
        }
    }
}
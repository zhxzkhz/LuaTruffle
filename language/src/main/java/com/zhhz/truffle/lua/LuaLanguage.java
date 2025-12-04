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
package com.zhhz.truffle.lua;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.nodes.access.LuaReadLocalVariableNode;
import com.zhhz.truffle.lua.nodes.access.LuaWriteLocalVariableNode;
import com.zhhz.truffle.lua.nodes.controlflow.*;
import com.zhhz.truffle.lua.nodes.expression.LuaDoubleLiteralNode;
import com.zhhz.truffle.lua.nodes.expression.LuaFunctionLiteralNode;
import com.zhhz.truffle.lua.nodes.expression.LuaInvokeNode;
import com.zhhz.truffle.lua.nodes.expression.LuaStringLiteralNode;
import com.zhhz.truffle.lua.nodes.expression.binary.*;
import com.zhhz.truffle.lua.nodes.expression.logical.LuaLogicalAndNode;
import com.zhhz.truffle.lua.nodes.expression.logical.LuaLogicalOrNode;
import com.zhhz.truffle.lua.parser.LuaNodeParser;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaLanguageView;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  Lua 是一门简单的语言，用于演示和展示 Truffle 的特性。其实现尽可能地简洁清晰，
 *  以便于理解 Truffle 的思想和概念。该语言拥有头等函数，并且对象是键值存储。
 *  <p>
 *  Lua 是动态类型的，即，程序员无需指定类型名称。Lua 也是强类型的，即，类型之间不会进行自动转换。
 *  如果在运行时遇到的类型不支持某个操作，则会报告一个类型错误并停止执行。
 *  例如，{@code 4 - "2"} 会导致一个类型错误，因为减法操作仅为数字定义。
 *  <p>
 *  <b>类型 (Types):</b>
 *  <ul>
 *  <li>数字 (Number): 任意精度的整数。对于能够存放在 64 位范围内的数字，其实现使用 Java 的基本类型
 *  {@code long} 来表示；对于超出该范围的数字，则使用 {@link Double}。使用像 {@code long}
 *  这样的基本类型对于性能至关重要。
 *  <li>布尔值 (Boolean): 实现为 Java 的基本类型 {@code boolean}。
 *  <li>字符串 (String): 实现为 Java 的标准类型 {@link String}。
 *  <li>函数 (Function): 实现类型为 {@link LuaFunction}。
 *  <li>对象 (Object): 使用 Truffle 提供的对象模型进行高效实现。对象的实现类型是
 *  {@link DynamicObject} 的一个子类。
 *  <li>Null (只有一个值 {@code null}): 实现为单例 {@link LuaNil#SINGLETON}。
 *  </ul>
 *  类 {@link LuaTypes} 为 Truffle DSL 列出了这些类型，即，用于那些通过 Truffle DSL 注解指定的类型特化操作。
 *  <p>
 *  <b>语言概念 (Language concepts):</b>
 *  <ul>
 *  <li>字面量：用于 {@link LuaDoubleLiteralNode numbers} (数字)、{@link LuaStringLiteralNode strings} (字符串)
 *  和 {@link LuaFunctionLiteralNode functions} (函数)。
 *  <li>基本算术、逻辑和比较操作：{@link LuaAddNode +}、{@link LuaSubNode -}、
 *  {@link LuaMulNode *}、{@link LuaDivNode /}、{@link LuaLogicalAndNode logical and} (逻辑与)、
 *  {@link LuaLogicalOrNode logical or} (逻辑或)、{@link LuaEqualNode ==}、!=、{@link LuaLessThanNode <}、
 *  {@link LuaLessOrEqualNode ≤}、>、≥。
 *  <li>局部变量：局部变量在使用 (通过 {@link LuaReadLocalVariableNode read} 读取) 之前必须先被定义 (通过
 *  {@link LuaWriteLocalVariableNode write} 写入)。局部变量在其首次定义的代码块之外是不可见的。
 *  <li>基本控制流语句：{@link LuaBlockNode blocks} (代码块)、{@link LuaIfNode if} (if语句)、
 *  {@link LuaWhileNode while} (while循环) 与 {@link LuaBreakNode break} (break语句) 和
 *  {@link LuaContinueNode continue} (continue语句)，以及 {@link LuaReturnNode return} (return语句)。
 *  <li>调试控制：{@link LuaDebuggerNode debugger} (debugger语句) 使用
 *  {@link DebuggerTags.AlwaysHalt} 标签，以便在调试器下运行时暂停执行。
 *  <li>函数调用：{@link LuaInvokeNode invocations} (调用) 通过
 *  {@link LuaFunction polymorphic inline caches} (多态内联缓存) 得以高效实现。
 *  </ul>
 *  <p>
 *  <b>语法与解析 (Syntax and parsing):</b><br>
 *  其语法由一个 ANTLR 4 语法文件描述。
 *  {@link com.zhhz.truffle.lua.parser.LuaParser parser} (解析器) 和
 *  {@link com.zhhz.truffle.lua.parser.LuaLexer lexer} (词法分析器) 由 ANTLR 4 自动生成。
 *  Lua 使用一个 AST 访问者 (AST visitor) 将 AST 转换为一个 Truffle 解释器。在 Lua 源码中找到的所有函数都会被添加到
 *  <p>
 *  <b>内置函数 (Builtin functions):</b><br>
 *  那些无需预先定义即可在每个 Lua 源码中使用的库函数被称为内置函数。它们在 {@link LuaContext} 中注册
 */

@TruffleLanguage.Registration(id = LuaLanguage.ID, name = "Lua", defaultMimeType = LuaLanguage.MIME_TYPE, characterMimeTypes = LuaLanguage.MIME_TYPE, contextPolicy = ContextPolicy.SHARED, fileTypeDetectors = LuaFileDetector.class)
@ProvidedTags({StandardTags.CallTag.class, StandardTags.StatementTag.class, StandardTags.RootTag.class, StandardTags.RootBodyTag.class, StandardTags.ExpressionTag.class, DebuggerTags.AlwaysHalt.class,
                StandardTags.ReadVariableTag.class, StandardTags.WriteVariableTag.class})
@Bind.DefaultExpression("get($node)")
public final class LuaLanguage extends TruffleLanguage<LuaContext> {
    public static volatile int counter;

    public static final String ID = "lua";
    public static final String MIME_TYPE = "application/x-lua";
    private static final Source BUILTIN_SOURCE = Source.newBuilder(LuaLanguage.ID, "", "Lua builtin").build();

    public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_8;

    private final Assumption singleContext = Truffle.getRuntime().createAssumption("Single Lua context.");

    public LuaLanguage() {
        counter++;
    }

    public static int getCounter() {
        return counter;
    }

    @Override
    protected LuaContext createContext(Env env) {
        return new LuaContext(this, env);
    }

    @Override
    public boolean patchContext(LuaContext context, Env newEnv) {
        context.patchContext(newEnv);
        return true;
    }


    public static NodeInfo lookupNodeInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        NodeInfo info = clazz.getAnnotation(NodeInfo.class);
        if (info != null) {
            return info;
        } else {
            return lookupNodeInfo(clazz.getSuperclass());
        }
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        Source source = request.getSource();
        LuaContext context = LuaContext.get(null);
        if (!context.isPackagePathInitialized()) {
            context.initPackagePath(source);
            context.setPackagePathInitialized(true);
        }

        return LuaNodeParser.parseRootLua(this, source);
    }

    public static void printInstrumentationTree(PrintStream w, String indent, Node node) {
        ProvidedTags tags = LuaLanguage.class.getAnnotation(ProvidedTags.class);
        Class<?>[] tagClasses = tags.value();
        if (node instanceof LuaRootNode root) {
            w.println(root.getQualifiedName());
            w.println(root.getSourceSection().getCharacters());
        }
        if (node instanceof BytecodeNode bytecode) {
            w.println(bytecode.dump());
        }

        String newIndent = indent;
        List<Class<? extends Tag>> foundTags = getTags(node, tagClasses);
        if (!foundTags.isEmpty()) {
            int lineLength = 0;
            w.print(indent);
            lineLength += indent.length();
            w.print("(");
            lineLength += 1;
            String sep = "";
            for (Class<? extends Tag> tag : foundTags) {
                String identifier = Tag.getIdentifier(tag);
                if (identifier == null) {
                    identifier = tag.getSimpleName();
                }
                w.print(sep);
                lineLength += sep.length();
                w.print(identifier);
                lineLength += identifier.length();
                sep = ",";
            }
            w.print(")");
            lineLength += 1;
            SourceSection sourceSection = node.getSourceSection();

            int spaces = 60 - lineLength;
            for (int i = 0; i < spaces; i++) {
                w.print(" ");
            }

            String characters = sourceSection.getCharacters().toString();
            characters = characters.replaceAll("\\n", "");

            if (characters.length() > 60) {
                characters = characters.subSequence(0, 57) + "...";
            }

            w.printf("%s %3s:%-3s-%3s:%-3s | %3s:%-3s   %s%n", sourceSection.getSource().getName(),
                            sourceSection.getStartLine(),
                            sourceSection.getStartColumn(),
                            sourceSection.getEndLine(),
                            sourceSection.getEndColumn(),
                            sourceSection.getCharIndex(),
                            sourceSection.getCharLength(), characters);
            newIndent = newIndent + "  ";
        }

        for (Node child : node.getChildren()) {
            printInstrumentationTree(w, newIndent, child);
        }

    }

    @SuppressWarnings({"unchecked", "cast"})
    private static List<Class<? extends Tag>> getTags(Node node, Class<?>[] tags) {
        if (node instanceof InstrumentableNode instrumentableNode) {
            if (instrumentableNode.isInstrumentable()) {
                List<Class<? extends Tag>> foundTags = new ArrayList<>();
                for (Class<?> tag : tags) {
                    if (instrumentableNode.hasTag((Class<? extends Tag>) tag)) {
                        foundTags.add((Class<? extends Tag>) tag);
                    }
                }
                return foundTags;
            }
        }
        return List.of();
    }

    @Override
    protected void initializeMultipleContexts() {
        singleContext.invalidate();
    }

    @Override
    protected Object getLanguageView(LuaContext context, Object value) {
        return LuaLanguageView.create(value);
    }

    @Override
    protected boolean isVisible(LuaContext context, Object value) {
        return !InteropLibrary.getFactory().getUncached(value).isNull(value);
    }

    @Override
    protected Object getScope(LuaContext context) {
        return context.getGlobals();
    }

    private static final LanguageReference<LuaLanguage> REFERENCE = LanguageReference.create(LuaLanguage.class);

    public static LuaLanguage get(Node node) {
        return REFERENCE.get(node);
    }

    @Override
    protected void exitContext(LuaContext context, ExitMode exitMode, int exitCode) {

    }
}

/*
 * Copyright (c) 2014, 2024, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaLanguageView;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

/**
 * Lua不需要复杂的错误检查和报告机制，因此所有意外
 * 条件只是中止执行。当我们从终止时，使用此类中定义的异常
 */
@SuppressWarnings("serial")
public final class LuaException extends AbstractTruffleException {

    private static final InteropLibrary UNCACHED_LIB = InteropLibrary.getFactory().getUncached();
    private final Object errorObject;
    private boolean positioned;

    LuaException(String message, Node location) {
        this(message, message, location);
    }

    LuaException(String message, Object errorObject, Node location) {
        super(message, location);
        this.errorObject = errorObject;
    }

    @TruffleBoundary
    public static AbstractTruffleException create(String message, Node location) {
        return new LuaException(message, location);
    }

    /**
     * 创建运行时错误，并按其所在源码位置加上标准 Lua 的位置前缀
     * （{@code chunkid:line: message}，等价于 C 端的 {@code luaG_runerror} /
     * {@code luaG_addinfo}）。pcall 捕获后返回的字符串因此带位置信息，
     * 与标准 Lua 一致。
     */
    @TruffleBoundary
    public static AbstractTruffleException runtimeError(String message, Node location) {
        String full = withPosition(message, location);
        LuaException exception = new LuaException(full, full, location);
        exception.positioned = true;
        return exception;
    }

    /**
     * 创建已带位置的运行时错误：{@code errorObject} 作为 pcall 返回值，
     * {@code displayMessage} 作为显示消息。用于 VM 在调用边界为外层抛出的
     * 无位置错误补充 {@code chunkid:line:} 前缀（等价 C 端 {@code luaL_where}）。
     */
    @TruffleBoundary
    public static AbstractTruffleException positioned(Object errorObject, String displayMessage, Node location) {
        LuaException exception = new LuaException(displayMessage, errorObject, location);
        exception.positioned = true;
        return exception;
    }

    /** 该异常是否已包含源码位置前缀（避免 VM 重复补前缀）。 */
    public boolean isPositioned() {
        return positioned;
    }

    /** 若 location 有可用的源码位置，则为 message 加上 {@code chunkid:line: } 前缀。 */
    @TruffleBoundary
    public static String withPosition(String message, Node location) {
        if (location == null) {
            return message;
        }
        SourceSection section = location.getEncapsulatingSourceSection();
        if (section == null || !section.isAvailable()) {
            return message;
        }
        Source source = section.getSource();
        if (source == null) {
            return message;
        }
        // 优先使用词法器报告的源码行号：标准 Lua 中部分指令（如二元运算）
        // 的行号并非其源码片段的起始行。未设置时回退到片段的起始行。
        int line = section.getStartLine();
        if (location instanceof LuaStatementNode statementNode && statementNode.hasSourceLine()) {
            line = statementNode.getSourceLine();
        }
        return chunkId(source.getName()) + ":" + line + ": " + message;
    }

    /**
     * 复刻标准 Lua 的 {@code luaO_chunkid}：把源码名格式化为错误消息前缀。
     */
    @TruffleBoundary
    public static String chunkId(String source) {
        if (source == null) {
            // 对应 C 端 luaG_addinfo：无源码可用时直接用 "?"（而非 luaO_chunkid 的 [string ...]）
            return "?";
        }
        int bufflen = 60; // LUA_IDSIZE
        if (!source.isEmpty() && source.charAt(0) == '=') {
            String body = source.substring(1);
            return body.length() <= bufflen - 1 ? body : body.substring(0, bufflen - 1);
        }
        if (!source.isEmpty() && source.charAt(0) == '@') {
            String body = source.substring(1);
            if (body.length() <= bufflen - 1) {
                return body;
            }
            return "..." + body.substring(body.length() - (bufflen - 4));
        }
        StringBuilder out = new StringBuilder("[string \"");
        int l = source.length();
        int nl = source.indexOf('\n');
        if (l < bufflen - 11) {
            if (nl >= 0) {
                out.append(source, 0, nl).append("...");
            } else {
                out.append(source);
            }
        } else {
            out.append(source, 0, bufflen - 15).append("...");
        }
        out.append("\"]");
        return out.toString();
    }

    @TruffleBoundary
    public static AbstractTruffleException create(Object errorObject, Node location) {
        return new LuaException(String.valueOf(errorObject), errorObject, location);
    }

    /**
     * 创建错误对象与展示消息分离的异常：pcall 返回 {@code errorObject}，
     * 而 CLI/调试器显示 {@code displayMessage}（如经 __tostring 格式化的结果）。
     */
    @TruffleBoundary
    public static AbstractTruffleException create(Object errorObject, String displayMessage, Node location) {
        return new LuaException(displayMessage, errorObject, location);
    }

    public Object getErrorObject() {
        return errorObject;
    }

    @TruffleBoundary
    public static AbstractTruffleException typeError(Node operation, Object... values) {
        String operationName = null;
        if (operation != null) {
            NodeInfo nodeInfo = LuaLanguage.lookupNodeInfo(operation.getClass());
            if (nodeInfo != null) {
                operationName = nodeInfo.shortName();
            }
        }

        return typeError(operation, operationName, values);
    }

    /**
     * 为运行时类型错误提供用户可读的消息。
     */
    @TruffleBoundary
    @SuppressWarnings("deprecation")
    public static AbstractTruffleException typeError(Node location, String operationName, Object... values) {
        StringBuilder result = new StringBuilder();
        result.append("Type error");

        AbstractTruffleException ex = LuaException.create("", location);
        if (location != null) {
            SourceSection ss = ex.getEncapsulatingSourceSection();
            if (ss != null && ss.isAvailable()) {
                result.append(" at ").append(ss.getSource().getName()).append(" line ").append(ss.getStartLine()).append(" col ").append(ss.getStartColumn());
            }
        }

        result.append(": operation");
        if (location != null) {
            result.append(" \"").append(operationName).append("\"");
        }

        result.append(" not defined for");

        String sep = " ";
        for (Object o : values) {
            /*
             * 对于原始值或外来值，我们请求语言视图，以便打印值
             * 从简单语言而不是另一种语言的角度来看。因为这是一个
             * 很少调用异常方法，我们可以创建以下语言视图
             * 原始值，然后方便地请求元对象并显示字符串。
             * 对核心内置程序（如typeOf内置程序）使用语言视图可能不是一个好方法
             * 出于性能原因的想法。
             */
            Object value = LuaLanguageView.forValue(o);
            result.append(sep);
            sep = ", ";
            if (value == null) {
                result.append("ANY");
            } else {
                InteropLibrary valueLib = InteropLibrary.getFactory().getUncached(value);
                if (valueLib.hasMetaObject(value) && !valueLib.isNull(value)) {
                    String qualifiedName;
                    try {
                        qualifiedName = UNCACHED_LIB.asString(UNCACHED_LIB.getMetaQualifiedName(valueLib.getMetaObject(value)));
                    } catch (UnsupportedMessageException e) {
                        throw shouldNotReachHere(e);
                    }
                    result.append(qualifiedName);
                    result.append(" ");
                }
                if (valueLib.isString(value)) {
                    result.append("\"");
                }
                result.append(valueLib.toDisplayString(value));
                if (valueLib.isString(value)) {
                    result.append("\"");
                }
            }
        }
        return LuaException.create(result.toString(), location);
    }

    @TruffleBoundary
    public static AbstractTruffleException undefinedFunction(Node location, Object name) {
        throw create("Undefined function: " + name, location);
    }

    @TruffleBoundary
    public static AbstractTruffleException undefinedProperty(Node location, Object name) {
        throw create("Undefined property: " + name, location);
    }

}

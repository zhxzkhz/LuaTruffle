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
import com.oracle.truffle.api.source.SourceSection;
import com.zhhz.truffle.lua.runtime.LuaLanguageView;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

/**
 * Lua不需要复杂的错误检查和报告机制，因此所有意外
 * 条件只是中止执行。当我们从终止时，使用此类中定义的异常
 */
@SuppressWarnings("serial")
public final class LuaException extends AbstractTruffleException {

    private static final InteropLibrary UNCACHED_LIB = InteropLibrary.getFactory().getUncached();

    LuaException(String message, Node location) {
        super(message, location);
    }

    @TruffleBoundary
    public static AbstractTruffleException create(String message, Node location) {
        return new LuaException(message, location);
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

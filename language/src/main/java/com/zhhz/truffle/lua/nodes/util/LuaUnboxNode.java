/*
 * Copyright (c) 2015, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.bytecode.OperationProxy;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

/**
 * 将任何值标准化为Lua值的节点。这有助于减少值的数量
 * 表达式节点需要期待。
 */
@TypeSystemReference(LuaTypes.class)
@NodeChild
@OperationProxy.Proxyable(allowUncached = true)
public abstract class LuaUnboxNode extends LuaExpressionNode {

    public static final int LIMIT = 5;

    @Specialization
    public static TruffleString fromString(String value,
                    @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.execute(value, LuaLanguage.STRING_ENCODING);
    }

    @Specialization
    public static TruffleString fromTruffleString(TruffleString value) {
        return value;
    }

    @Specialization
    public static long fromLong(long value) {
        return value;
    }

    @Specialization
    public static double fromDouble(double value) {
        return value;
    }

    @Specialization
    public static LuaFunction fromFunction(LuaFunction value) {
        return value;
    }

    @Specialization
    public static LuaNil fromNil(LuaNil value) {
        return value;
    }

    /**
     * 【新增】专门处理 nil (null) 的特化。
     * 这个特化必须放在其他使用 @CachedLibrary("value") 的特化之前。
     */
    @Specialization(guards = "value == null")
    protected static Object unboxNil(Object value) {
        // 当 unbox 一个 nil 时，它仍然是 nil。
        return LuaNil.SINGLETON;
    }

    @Specialization(guards = "value != null",limit = "LIMIT")
    public static Object fromForeign(Object value, @CachedLibrary("value") InteropLibrary interop, @Bind Node node) {
        try {
            if (interop.fitsInLong(value)) {
                return interop.asLong(value);
            } else if (interop.fitsInDouble(value)) {
                return interop.asDouble(value);
            } else if (interop.fitsInBigInteger(value)) {
                return interop.asBigInteger(value);
            } else if (interop.isString(value)) {
                return interop.asTruffleString(value);
                //return new LuaString(context.getNumberShape(),interop.asTruffleString(value));
            } else if (interop.isBoolean(value)) {
                return interop.asBoolean(value);
            } else {
                return value;
            }
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

}

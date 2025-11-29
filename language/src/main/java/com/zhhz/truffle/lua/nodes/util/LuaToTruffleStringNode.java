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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaTypes;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaString;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;


@TypeSystemReference(LuaTypes.class)
@GenerateUncached
@GenerateInline
@GenerateCached(false)
public abstract class LuaToTruffleStringNode extends Node {

    static final int LIMIT = 5;

    private static final TruffleString TRUE = LuaContext.toTruffleString("true");
    private static final TruffleString FALSE = LuaContext.toTruffleString("false");
    private static final TruffleString FOREIGN_OBJECT = LuaContext.toTruffleString("[foreign object]");

    public abstract TruffleString execute(Node node, Object value);

    @Specialization
    public static TruffleString fromNull(@SuppressWarnings("unused") LuaNil value) {
        return LuaNil.NIL;
    }

    @Specialization
    protected static TruffleString fromString(String value,
                    // TruffleString nodes cannot be inlined yet
                    @Shared("fromJava") @Cached(inline = false) TruffleString.FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.execute(value, LuaLanguage.STRING_ENCODING);
    }

    @Specialization
    public static TruffleString fromTruffleString(TruffleString value) {
        return value;
    }

    @Specialization
    public static TruffleString fromTruffleString(LuaString value) {
        return value.getValue();
    }

    @Specialization
    public static TruffleString fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Specialization
    @TruffleBoundary
    protected static TruffleString fromLong(long value,
                    @Shared("fromLong") @Cached(inline = false) TruffleString.FromLongNode fromLongNode) {
        return fromLongNode.execute(value, LuaLanguage.STRING_ENCODING, true);
    }

    @Specialization
    public static TruffleString fromFunction(LuaFunction value) {
        return value.getName();
    }

    @Specialization(limit = "LIMIT")
    public static TruffleString fromInterop(Object value,
                    @CachedLibrary("value") InteropLibrary interop,
                    @Shared("fromLong") @Cached() TruffleString.FromLongNode fromLongNode,
                    @Shared("fromJava") @Cached() TruffleString.FromJavaStringNode fromJavaStringNode) {
        try {
            if (interop.fitsInLong(value)) {
                return fromLongNode.execute(interop.asLong(value), LuaLanguage.STRING_ENCODING, true);
            } else if (interop.isString(value)) {
                return fromJavaStringNode.execute(interop.asString(value), LuaLanguage.STRING_ENCODING);
            } else if (interop.isNumber(value)) {
                return fromJavaStringNode.execute(numberToString(value), LuaLanguage.STRING_ENCODING);
            } else if (interop.isNull(value)) {
                return LuaNil.NIL_CL;
            } else {
                return FOREIGN_OBJECT;
            }
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    @TruffleBoundary
    private static String numberToString(Object value) {
        return value.toString();
    }
}

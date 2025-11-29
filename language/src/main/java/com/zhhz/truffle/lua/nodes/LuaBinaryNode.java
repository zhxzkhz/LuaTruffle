/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;

/**
 * 接受两个参数的操作的实用基类
 */
@NodeChild("leftNode")
@NodeChild("rightNode")
public abstract class LuaBinaryNode extends LuaExpressionNode {

    @TruffleBoundary
    protected static double toDouble(Object value) {
        if (value instanceof LuaNumber) {
            return ((LuaNumber) value).getValue();
        }
        if (value instanceof Long){
            return ((Long) value).doubleValue();
        }
        if (value instanceof Double){
            return (Double) value;
        }
        if (value instanceof TruffleString) {
            try {
                return Double.parseDouble(((TruffleString) value).toJavaStringUncached());
            } catch (NumberFormatException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        throw new RuntimeException("attempt to perform arithmetic on a " + getTypeName(value) + " value");
    }


    protected boolean isNumberOrString(Object o) {
        return o instanceof Long || o instanceof Double || o instanceof TruffleString || o instanceof String; // 如果有 String 互操作
    }


    @TruffleBoundary
    protected static void assertNotNull(Object left, Object right, Node node){
        if (left == null || right == null || left instanceof LuaNil || right instanceof LuaNil){
            throw LuaException.create("attempt to perform arithmetic on a nil value",node);
        }
    }



}

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
package com.zhhz.truffle.lua.nodes.expression.logical;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaNil;


@NodeChild("operandNode")
@NodeInfo(shortName = "not")
public abstract class LuaLogicalNotNode extends LuaExpressionNode {

    /**
     * 特化：当操作数是 LuaBoolean.FALSE 时。
     */
    @Specialization
    protected LuaBoolean notFalse(LuaBoolean operand) {
        // not false is true, not nil is true
        // 为了简化，我们可以依赖下面的通用实现，
        // 但为常见情况提供特化是Truffle的风格。
        return LuaBoolean.valueOf(!operand.getValue());
    }

    /**
     * 特化：当操作数是 LuaNil 时。
     */
    @Specialization
    protected LuaBoolean notNil(LuaNil operand) {
        return LuaBoolean.TRUE;
    }

    /**
     * 通用/回退特化：处理所有其他“真”值。
     */
    @Specialization(guards = {"!isLuaFalse(operand)", "!isLuaNil(operand)"})
    protected LuaBoolean notTruthy(Object operand) {
        if (operand instanceof Boolean bool){
            return LuaBoolean.valueOf(!bool);
        }
        // 任何非 false、非 nil 的值，取 not 后都是 false
        return LuaBoolean.FALSE;
    }

    // 辅助的守卫方法
    protected static boolean isLuaFalse(Object value) {
        return value == LuaBoolean.FALSE;
    }

    protected static boolean isLuaNil(Object value) {
        return value == LuaNil.SINGLETON;
    }

    @Fallback
    public boolean typeError(Object value, @Bind Node node) {
        throw LuaException.typeError(node, "!", value);
    }

}

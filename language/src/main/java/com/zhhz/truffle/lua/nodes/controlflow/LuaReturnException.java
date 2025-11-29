/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;

import java.io.Serial;

/**
 * 用于实现 Lua "return" 语句的控制流异常。
 * <p>
 * <b>核心机制：</b>
 * 在 AST 解释器中，当执行到 return 语句时，我们抛出这个异常。
 * 这个异常会沿着调用栈向上冒泡，直到被 {@link LuaFunctionBodyNode} 捕获。
 * <p>
 * <b>数据传输：</b>
 * 与 Break/Continue 不同，Return 需要携带计算结果。这个异常充当了
 * "数据传输载体" (Data Carrier)，将返回值从语句层级传输回表达式层级。
 */
public class LuaReturnException extends ControlFlowException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实际的返回值负载 (Payload)。
     * <p>
     * 在 Lua 中，函数可以返回多值。因此这个 Object 既可能是一个单一的值（如 Long, TruffleString），
     * 也可能是一个 Object[] 数组（代表多重返回值）。
     * 声明为 final 以确保不可变性，利于编译器分析。
     */
    private final Object values;

    public LuaReturnException(Object values) {
        this.values = values;
    }

    /**
     * 工厂方法，用于抛出携带返回值的异常。
     * <p>
     * 这是一个语法糖，方便在节点中调用。
     *
     * @param result 包含多重返回值的对象 (Payload)
     * @return 一个准备被抛出的 LuaReturnException 实例。
     */
    public static LuaReturnException withResult(Object result) {
        return new LuaReturnException(result);
    }

    /**
     * 获取异常中携带的返回值。
     * <p>
     * 通常由 {@link LuaFunctionBodyNode} 在 catch 块中调用，
     * 将其解包并作为函数的最终执行结果返回。
     */
    public Object getResult() {
        return values;
    }
}

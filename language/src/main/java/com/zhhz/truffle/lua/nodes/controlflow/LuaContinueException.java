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
package com.zhhz.truffle.lua.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;

import java.io.Serial;

/**
 * 用于实现 Lua 中 "continue" 语句的控制流异常。
 * <p>
 * 在 Truffle 框架中，{@link ControlFlowException} 是一个特殊的异常基类。
 * 它用于表示非本地的控制流转移（如 continue, break, return）。
 * <p>
 * <b>性能关键点：</b>
 * Graal 编译器特别了解这个类。当编译器看到抛出这个异常并被当前编译单元内的循环捕获时，
 * 它不会生成常规的异常处理代码（即没有开销），而是将其转换为底层的跳转指令（GOTO 到循环开头）。
 */
public final class LuaContinueException extends ControlFlowException {

    /**
     * 全局单例实例。
     * <p>
     * 对于控制流异常，我们不需要携带任何状态（如错误信息）或堆栈轨迹。
     * 使用单例模式可以完全避免对象分配（Allocation Free），
     * 这对于像循环这样高频执行的代码块至关重要，能显著减少垃圾回收（GC）压力。
     */
    public static final LuaContinueException SINGLETON = new LuaContinueException();

    @Serial
    private static final long serialVersionUID = 5329687983726237188L;

    /*
     * 私有构造函数，防止外部实例化。
     * 强制所有使用者都必须使用 {@link #SINGLETON}。
     */
    private LuaContinueException() {
    }
}

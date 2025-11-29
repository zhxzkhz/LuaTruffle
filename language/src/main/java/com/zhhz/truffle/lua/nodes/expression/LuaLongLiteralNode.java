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
package com.zhhz.truffle.lua.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;

/**
 * 表示 Lua 语言中的整数（Long）字面量节点。
 * <p>
 * 例如：在 Lua 代码中写 `10`、`-5` 或 `0xFF` 时，解析器会生成这个节点。
 * 它是一个常量表达式，执行时永远返回固定的数值。
 */
// @NodeInfo 用于调试工具（如 IGV 或调试器），shortName 显示在 AST 树节点上
@NodeInfo(shortName = "const", description = "A constant integer (long) value")
public final class LuaLongLiteralNode extends LuaExpressionNode {

    // 存储该节点代表的实际数值，使用 final 确保不可变
    private final long value;

    public LuaLongLiteralNode(long value) {
        this.value = value;
    }

    /**
     * 特化执行方法：直接返回原生的 long 类型。
     * <p>
     * <b>Truffle 性能优化的关键点：</b>
     * 当父节点（例如加法节点 `LuaAddNode`）通过 `@Specialization` 特化为处理 long 类型时，
     * 它会直接调用这个方法。
     * <p>
     * 这样做的好处是<b>避免了自动装箱（Boxing）</b>，即避免了创建 `java.lang.Long` 对象，
     * 从而在热点代码路径上极大减少了内存分配和垃圾回收的压力。
     */
    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return value;
    }

    /**
     * 通用执行方法：返回 Object 类型。
     * <p>
     * 当父节点不知道或不关心具体类型（处于未特化状态）时，会调用此方法。
     * 在这里，原生的 `long` value 会被 Java 自动装箱为 `java.lang.Long` 对象返回。
     * 这通常是解释执行初期的“慢路径”。
     */
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return value;
    }
}

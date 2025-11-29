/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.utilities.TriState;

import java.lang.invoke.MethodHandles;

/**
 * Lua 语言中 "nil" 值的运行时实现。
 * <p>
 * 这是一个<b>单例 (Singleton)</b> 类。在整个 Lua 运行时的生命周期中，
 * 所有的 `nil` 值都指向 {@link #SINGLETON} 这同一个对象引用。
 * <p>
 * 该类通过 {@link ExportLibrary} 导出了 {@link InteropLibrary}，
 * 这意味着 GraalVM 的多语言环境（Polyglot）能够理解这个对象就是 Lua 版本的 "null"。
 */
@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public final class LuaNil extends LuaValue {

    // -------------------------------------------------------------------------
    // 对象布局 (Object Layout) 与单例模式
    // -------------------------------------------------------------------------

    /*
     * 即使是 Singleton，因为 LuaValue 继承自 DynamicObject (通常情况)，
     * Truffle 的对象模型要求所有对象必须关联一个 Shape（描述对象的字段布局）。
     * 对于没有任何字段的 nil，我们创建一个空的 Shape。
     */
    private static final Shape NIL_SHAPE = Shape.newBuilder().layout(LuaNil.class, MethodHandles.lookup()).build();

    public static final TruffleString NIL = LuaContext.toTruffleString("NIL");
    public static final TruffleString NIL_CL = LuaContext.toTruffleString("nil");

    /**
     * 全局唯一的 nil 实例。
     */
    public static final LuaNil SINGLETON = new LuaNil();

    /**
     * 预先计算并缓存 identityHashCode。
     * 这样做是为了在 isIdenticalOrUndefined 等互操作消息中提供极速访问，避免重复计算。
     */
    private static final int IDENTITY_HASH = System.identityHashCode(SINGLETON);

    /**
     * 私有构造函数。
     * 禁止外部通过 new LuaNil() 实例化，强制使用 SINGLETON。
     */
    private LuaNil() {
        super(NIL_SHAPE);
    }

    // -------------------------------------------------------------------------
    // Lua 语言内部行为
    // -------------------------------------------------------------------------

    @Override
    public String getTypeName() {
        return "nil";
    }

    /**
     * Java 层面的字符串表示。
     * <p>
     * 注意：这个方法可能会被隐式调用（例如在 Java 中进行字符串拼接 "val: " + nil）。
     * 修改它会影响宿主语言看到的字符串结果。
     */
    @Override
    public String toString() {
        return "nil";
    }

    // -------------------------------------------------------------------------
    // 互操作性 (InteropLibrary) 实现
    // 这里的 @ExportMessage 用于告诉外部世界（Java, JS, Python 等）如何对待 Lua 的 nil。
    // -------------------------------------------------------------------------

    /**
     * <b>核心互操作语义：</b>
     * 告诉其他语言："我就是空值"。
     * <p>
     * 例如：当 Java 代码调用 Lua 函数返回 nil 时，Java 端会判定 {@code value.isNull()} 为 true。
     */
    @ExportMessage
    boolean isNull() {
        return true;
    }

    /**
     * 声明该对象拥有元数据（类型信息）。
     */
    @ExportMessage
    boolean hasMetaObject() {
        return true;
    }

    /**
     * 获取该对象的类型描述。
     * 这允许外部工具（如调试器）显示该对象的类型为 "nil"。
     */
    @ExportMessage
    Object getMetaObject() {
        return LuaType.NIL;
    }

    /**
     * <b>身份一致性检查</b>
     * <p>
     * 判断两个对象是否是同一个实例（即 `===` 语义）。
     * 因为 nil 是单例，所以只要引用地址相同（LuaNil.SINGLETON == other），它们就是相同的。
     * 返回 {@link TriState} 是因为互操作协议支持 "是"、"否" 和 "未定义" 三种状态。
     */
    @ExportMessage
    static TriState isIdenticalOrUndefined(@SuppressWarnings("unused") LuaNil receiver, Object other) {
        return TriState.valueOf(LuaNil.SINGLETON == other);
    }

    /**
     * 导出哈希码。
     * 必须与 isIdenticalOrUndefined 的逻辑保持一致：相同的对象必须有相同的 HashCode。
     */
    @ExportMessage
    static int identityHashCode(@SuppressWarnings("unused") LuaNil receiver) {
        return IDENTITY_HASH;
    }

    /**
     * 供调试器 (Debugger) 或 IDE 显示用的字符串。
     * 当你在 VSCode 或 Chrome DevTools 中查看变量时，会显示 "nil"。
     */
    @ExportMessage
    public Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
        return "nil";
    }
}
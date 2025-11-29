/*
 * Copyright (c) 2020, 2024, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * Lua 类型系统定义。
 * <p>
 * 这个类实现了 {@link TruffleObject} 并导出了 {@link InteropLibrary}，
 * 这意味着这些“类型对象”本身可以被其他语言或工具访问。
 * 在 Truffle 的互操作协议中，这些对象被称为“元对象”（Meta-objects），类似于 Java 中的 {@code java.lang.Class}。
 */
@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public final class LuaType implements TruffleObject {

    /*
     * 这里定义了 Lua 语言的一组内置类型。
     * 我们使用 Lambda 表达式和 InteropLibrary 来定义如何判断一个值是否属于某种类型。
     *
     * 注意：InteropLibrary 的类型检查通常能很好地映射到简单语言的类型。
     * 但对于更复杂的情况（如大数检查），可能需要额外的逻辑。
     */

    // 数字：如果是 Long (FitsInLong) 或者是 Double 实例，则视为 Lua 的 Number
    public static final LuaType NUMBER = new LuaType("Number", (l, v) -> l.fitsInLong(v) || v instanceof Double);

    // Nil：对应互操作协议中的 Null
    public static final LuaType NIL = new LuaType("NIL", InteropLibrary::isNull);

    // 字符串
    public static final LuaType STRING = new LuaType("String", InteropLibrary::isString);

    // 布尔值
    public static final LuaType BOOLEAN = new LuaType("Boolean", InteropLibrary::isBoolean);

    // 对象：指代拥有成员（members）的东西，通常对应 Lua 的 Table
    public static final LuaType OBJECT = new LuaType("Object", InteropLibrary::hasMembers);

    // 函数：指代可执行的东西
    public static final LuaType FUNCTION = new LuaType("Function", InteropLibrary::isExecutable);

    // 多返回值结果：目前逻辑是总是返回 true，这可能是一个特殊的内部类型或兜底类型
    public static final LuaType LuaMultiValue = new LuaType("LuaMultiValue", (l, v) -> v instanceof LuaMultiValue);

    /*
     * 优先级列表。
     * 当我们需要确定一个未知值的类型时，会按此顺序进行检查。
     * 顺序很重要，因为某些特性可能重叠。例如，一个对象可能同时既是“可执行的”（Function）又有“成员”（Object）。
     * 在这里，我们将 Function 放在 Object 之前，意味着如果一个东西既能执行又有成员，我们优先视其为 Function。
     *
     * @CompilationFinal(dimensions = 1): 告诉编译器这个数组的内容在运行时是不变的，
     * 允许 JIT 编译器在部分求值时将其视为常量进行优化（例如循环展开）。
     */
    @CompilationFinal(dimensions = 1) public static final LuaType[] PRECEDENCE = new LuaType[]{NIL, NUMBER, STRING, BOOLEAN, FUNCTION, OBJECT, LuaMultiValue};

    private final String name;
    private final TypeCheck isInstance;

    /*
     * 私有构造函数。我们不允许动态创建 LuaType 实例，只使用预定义好的静态实例。
     * 在真实的复杂语言中，可能需要公开构造函数来支持用户自定义类型（如类定义）。
     */
    private LuaType(String name, TypeCheck isInstance) {
        this.name = name;
        this.isInstance = isInstance;
    }

    /**
     * 检查给定的值是否属于当前类型。
     * <p>
     * 性能优化关键点：
     * {@code CompilerAsserts.partialEvaluationConstant(this)} 断言在 JIT 编译的快速路径中，
     * 'this'（即当前的 LuaType 实例，如 LuaType.NUMBER）必须是一个编译时常量。
     * 这允许编译器内联 {@code isInstance} 接口的具体实现逻辑。
     */
    public boolean isInstance(Object value, InteropLibrary interop) {
        CompilerAsserts.partialEvaluationConstant(this);
        return isInstance.check(interop, value);
    }

    @ExportMessage
    boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    Class<? extends TruffleLanguage<?>> getLanguage() {
        return LuaLanguage.class;
    }

    /*
     * 声明所有的 LuaType 对象都是互操作协议中的“元对象”（Meta-objects）。
     * 元对象的其他例子包括 Java 的 Class 类，或者 JavaScript 的 prototype。
     */
    @ExportMessage
    boolean isMetaObject() {
        return true;
    }

    /*
     * 获取类型的限定名和简单名。
     * 由于我们的简单 Lua 实现没有包或命名空间的概念，两者返回相同的名字。
     * 这些名字通常用于调试显示或错误信息。
     */
    @ExportMessage(name = "getMetaQualifiedName")
    @ExportMessage(name = "getMetaSimpleName")
    public Object getName() {
        return name;
    }

    @ExportMessage(name = "toDisplayString")
    Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
        return name;
    }

    @Override
    public String toString() {
        return "LuaType[" + name + "]";
    }

    /*
     * 实现 isMetaInstance 消息。
     * 这个消息可以被其他语言调用，或者被内置的 `isInstance` 函数调用。
     * 它的作用是检查一个给定的值（可能是原生值、外部语言值或本语言值）是否属于当前的 LuaType。
     * 这允许其他语言对 Lua 的值执行 `instanceof` 检查。
     */
    @ExportMessage
    static class IsMetaInstance {

        /*
         * 缓存特化（Inline Cache）：
         * 我们假设在源代码的同一个位置，检查的类型通常是稳定的。
         *
         * limit = "3": 我们最多为 3 种不同的 LuaType 及其对应的 InteropLibrary 生成特化代码。
         * 如果超过 3 种，将回退到 doGeneric。
         *
         * @Cached("type"): 缓存当前的 LuaType 实例，使其在守卫（guards）中成为常量。
         * @CachedLibrary("value"): 为被检查的值缓存 InteropLibrary。
         */
        @Specialization(guards = "type == cachedType", limit = "3")
        static boolean doCached(@SuppressWarnings("unused") LuaType type, Object value,
                                @Cached("type") LuaType cachedType,
                                @CachedLibrary("value") InteropLibrary valueLib) {
            // 调用该类型特定的检查逻辑
            return cachedType.isInstance.check(valueLib, value);
        }

        /*
         * 慢路径（Slow Path）：
         * 当缓存失效或类型变化过于频繁时使用。
         * 使用 @TruffleBoundary 避免编译该方法，并使用非缓存的 InteropLibrary。
         */
        @TruffleBoundary
        @Specialization(replaces = "doCached")
        static boolean doGeneric(LuaType type, Object value) {
            return type.isInstance.check(InteropLibrary.getFactory().getUncached(), value);
        }
    }

    /*
     * 一个用于类型检查的函数式接口。
     * 也可以通过创建 LuaType 的子类来实现，但用接口更轻量。
     */
    @FunctionalInterface
    interface TypeCheck {
        boolean check(InteropLibrary lib, Object value);
    }

}
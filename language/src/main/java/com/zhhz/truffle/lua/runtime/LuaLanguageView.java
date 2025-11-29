/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.zhhz.truffle.lua.LuaLanguage;

/**
 * Lua 语言视图。
 * <p>
 * 这个类实现了一个包装器，用于将原始类型（如 Java 的 int, String）或其他语言的值，
 * 伪装成 Lua 语言的值。这主要用于工具支持（如调试器显示变量类型）和错误信息展示。
 * <p>
 * delegateTo = "delegate" 表示：除了在这个类中显式定义（ExportMessage）的消息外，
 * 其他所有 InteropLibrary 的消息（如读取成员、执行等）都直接转发给被包装的原始对象（delegate）。
 */
@ExportLibrary(value = InteropLibrary.class, delegateTo = "delegate")
@SuppressWarnings("static-method")
public final class LuaLanguageView implements TruffleObject {

    final Object delegate;

    LuaLanguageView(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * 声明此对象归属的语言。
     * 这告诉 Truffle 框架（以及调试器等工具），这个对象现在应该被视为 Lua 语言的一部分。
     */
    @ExportMessage
    boolean hasLanguage() {
        return true;
    }

    /**
     * 返回关联的语言类。
     * 语言视图必须始终与创建它们的语言相关联。这允许工具获取一个原生值或外部值，
     * 并将其视为本语言的值进行展示。
     */
    @ExportMessage
    Class<? extends TruffleLanguage<?>> getLanguage() {
        return LuaLanguage.class;
    }

    /**
     * 判断此对象是否有元对象（MetaObject，即类型信息）。
     * <p>
     * 我们通过遍历内置的 Lua 类型系统（LuaType）来检查原始值（delegate）是否符合某种 Lua 类型。
     * <p>
     * &#064;ExplodeLoop:  这是一个性能优化注解。它告诉 Graal 编译器在编译时将这个循环完全展开。
     * 因为 LuaType.PRECEDENCE 是一个固定的、数量很少的列表，展开循环可以消除迭代开销，
     * 并允许对具体的 isInstance 调用进行内联优化。
     */
    @ExportMessage
    @ExplodeLoop
    boolean hasMetaObject(@CachedLibrary("this.delegate") InteropLibrary interop) {
        /*
         * 我们使用 isInstance 方法来查明是否适用某种内置的 Lua 类型。
         * 如果适用，我们就可以在 getMetaObject 中提供相应的元对象。
         *
         * 注意：语言视图仅针对原始值（Primitives）和其他语言的值创建。
         * Lua 语言本身的值（如 LuaFunction）不需要视图，因为它们已经直接实现了 has/getMetaObject
         * 并关联了 LuaLanguage。
         */
        for (LuaType type : LuaType.PRECEDENCE) {
            if (type.isInstance(delegate, interop)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取元对象（即获取该值的 Lua 类型）。
     * 这里的逻辑与 hasMetaObject 相同，但这次我们返回匹配到的 LuaType。
     */
    @ExportMessage
    @ExplodeLoop
    Object getMetaObject(@CachedLibrary("this.delegate") InteropLibrary interop) throws UnsupportedMessageException {
        for (LuaType type : LuaType.PRECEDENCE) {
            if (type.isInstance(delegate, interop)) {
                return type;
            }
        }
        // 如果遍历完所有已知类型都无法识别，抛出异常
        throw UnsupportedMessageException.create();
    }

    /**
     * 获取用于显示的字符串表示形式。
     * 例如，在调试器变量视图中显示的值。
     */
    @ExportMessage
    @ExplodeLoop
    Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects, @CachedLibrary("this.delegate") InteropLibrary interop) {
        for (LuaType type : LuaType.PRECEDENCE) {
            if (type.isInstance(this.delegate, interop)) {
                try {
                    /*
                     * 由于使用了 @ExplodeLoop，这里的 'type' 在部分求值（Partial Evaluation）阶段
                     * 会变成一个常量。因此，这一连串的 if-else 在编译后的机器码中会被折叠，
                     * 只保留匹配到的那个分支的逻辑，非常高效。
                     */
                    if (type == LuaType.NUMBER) {
                        return longToString(interop.asLong(delegate));
                    } else if (type == LuaType.BOOLEAN) {
                        return Boolean.toString(interop.asBoolean(delegate));
                    } else if (type == LuaType.STRING) {
                        return interop.asString(delegate);
                    } else{
                        /* 对于其他类型，回退到使用类型的名称作为显示字符串 */
                        return type.getName();
                    }
                } catch (UnsupportedMessageException e) {
                    throw shouldNotReachHere(e);
                }
            }
        }
        return "Unsupported";
    }

    /*
     * Long.toString 方法内部逻辑复杂，不适合进行部分求值（Partial Evaluation）。
     * 因此需要使用 @TruffleBoundary 标记，告诉编译器不要尝试编译进核心路径，
     * 而是将其视为一个普通的 Java 方法调用（边界调用）。
     */
    @TruffleBoundary
    private static String longToString(long l) {
        return Long.toString(l);
    }

    /**
     * 静态工厂方法：创建一个新的视图。
     * 仅当值是原生类型或来自其他语言时才应调用此方法。
     */
    public static Object create(Object value) {
        assert isPrimitiveOrFromOtherLanguage(value);
        return new LuaLanguageView(value);
    }

    /*
     * 辅助方法：检查值是否需要视图。
     * 如果一个值没有关联语言，或者关联的语言不是 LuaLanguage，则它被视为原生值或外部语言值。
     */
    private static boolean isPrimitiveOrFromOtherLanguage(Object value) {
        InteropLibrary interop = InteropLibrary.getFactory().getUncached(value);
        try {
            return !interop.hasLanguage(value) || interop.getLanguage(value) != LuaLanguage.class;
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    /**
     * 为任何值返回一个语言视图。
     * <p>
     * 1. 如果该值已经源自 Lua 语言（例如 LuaFunction），则直接返回原值（不需要视图）。
     * 2. 如果是原生值（如 Java int）或外部语言值，则创建一个 LuaLanguageView 包装它。
     * <p>
     * 这通常用于“慢路径”（Slow Paths），例如在打印错误信息时，我们需要以 Lua 的方式显示一个值。
     */
    @TruffleBoundary
    public static Object forValue(Object value) {
        if (value == null) {
            return null;
        }
        InteropLibrary lib = InteropLibrary.getFactory().getUncached(value);
        try {
            // 检查该值是否已经属于 Lua 语言
            if (lib.hasLanguage(value) && lib.getLanguage(value) == LuaLanguage.class) {
                return value;
            } else {
                return create(value);
            }
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    // 辅助异常抛出方法（假设在类外部或父类中有定义，这里为了完整性补充注释）
    private static RuntimeException shouldNotReachHere(Throwable e) {
        throw new IllegalStateException(e);
    }
}
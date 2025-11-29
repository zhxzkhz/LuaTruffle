package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;

/**
 * 所有Lua运行时值的抽象基类 (Abstract Base Class)。
 *
 * <p>这个类做了几件重要的事情:
 * <ol>
 *   <li>它实现了 {@link TruffleObject}, 这使得所有Lua值都能与Truffle框架和其他语言互操作。</li>
 *   <li>它为所有值类型提供了附加元表 (metatable) 的能力。</li>
 *   <li>它导出了通用的 {@link InteropLibrary} 消息，这些消息对所有Lua值都有效，
 *       从而简化了子类的实现。</li>
 *   <li>它定义了一个抽象方法 {@link #getTypeName()}, 强制所有子类提供其Lua类型名。</li>
 * </ol>
 */
@ExportLibrary(InteropLibrary.class)
public abstract class LuaValue extends DynamicObject {


    protected LuaValue(Shape shape) {
        // 调用父类 DynamicObject 的构造函数
        super(shape);
    }

    @ExportMessage
    @CompilerDirectives.TruffleBoundary
    public String asString(){
        return toString();
    }

    @ExportMessage boolean isString() { return false; }

    /**
     * 强制所有子类实现，以返回它们在Lua中的类型名称字符串。
     * 这对于实现 `type()` 内置函数至关重要。
     * @return 类型名称 (e.g., "number", "string", "table").
     */
    public abstract String getTypeName();

    @Override
    @CompilerDirectives.TruffleBoundary
    public String toString() {
        return getTypeName() + ": 0x" + Integer.toHexString(hashCode());
    }

    public final void setMetatable(LuaTable metatable) {
        // 使用一个uncached的库来设置隐藏属性
        DynamicObjectLibrary.getUncached().put(this, LuaMetatables.__METATABLE, metatable);
    }

    public final LuaTable getMetatable() {
        // 使用uncached的库来读取隐藏属性
        Object mt = DynamicObjectLibrary.getUncached().getOrDefault(this, LuaMetatables.__METATABLE, null);
        return (mt instanceof LuaTable) ? (LuaTable) mt : null;
    }

    @ExportMessage
    public boolean hasLanguage() {
        return false;
    }

    @ExportMessage
    public Class<? extends com.oracle.truffle.api.TruffleLanguage<?>> getLanguage() throws UnsupportedMessageException {
        throw UnsupportedMessageException.create();
    }

    /**
     * 告诉外界我们值的“类型”或“类”的名称。
     * 这使得在调试器中可以显示更有意义的类型信息，而不是Java类名。
     */
    @ExportMessage
    @SuppressWarnings("unused")
    @CompilerDirectives.TruffleBoundary
    public Object toDisplayString(boolean allowSideEffects) {
        // 直接使用 asString() 的结果作为显示字符串
        return toString();
    }

}
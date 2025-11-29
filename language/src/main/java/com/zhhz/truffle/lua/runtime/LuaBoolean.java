package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

/**
 * 代表Lua的布尔类型 (true 和 false)，继承自LuaValue。
 *
 * <p>这个类使用了单例模式来表示 {@code true} 和 {@code false}，
 * 因为在整个程序中只需要这两个实例。这可以避免不必要的内存分配。
 *
 * <p>它完整地实现了 InteropLibrary 的布尔协议，允许其他语言安全地与Lua布尔值交互。
 */
@ExportLibrary(InteropLibrary.class)
public final class LuaBoolean extends LuaValue {

    // --- 单例实例 ---

    public static LuaBoolean TRUE;
    public static LuaBoolean FALSE;

    private final boolean value;

    /**
     * 私有构造函数，强制使用上面的公共单例实例。
     * @param value The boolean value.
     */
    private LuaBoolean(Shape shape, boolean value) {
        super(shape);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }



    /**
     * 【关键】静态工厂方法，用于获取单例实例。
     * 它在第一次被调用时，会从上下文中获取 Shape 并创建单例。
     *
     * @param context The current LuaContext.
     * @param value The desired boolean value.
     * @return The singleton instance for TRUE or FALSE.
     */
    public static LuaBoolean get(LuaContext context, boolean value) {
        if (value) {
            if (TRUE == null) {
                // 延迟初始化 (Lazy Initialization)
                TRUE = new LuaBoolean(context.getBooleanShape(), true);
            }
            return TRUE;
        } else {
            if (FALSE == null) {
                FALSE = new LuaBoolean(context.getBooleanShape(), false);
            }
            return FALSE;
        }
    }

    /**
     * 一个方便的静态工厂方法，用于从Java的boolean获取对应的LuaBoolean单例。
     * @param value a Java boolean
     * @return The corresponding {@link LuaBoolean#TRUE} or {@link LuaBoolean#FALSE} instance.
     */
    public static LuaBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    // --- 继承自 LuaValue 的方法 ---

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @ExportMessage
    public boolean isString() {
        return false;
    }

    // 提供一个比基类更具体的 asString/toString 实现
    @ExportMessage
    public String asString() {
        return Boolean.toString(this.value);
    }

    @Override
    public String toString() {
        return asString();
    }


    // =================================================================================
    // InteropLibrary 布尔“合同”的完整实现
    // =================================================================================

    /**
     * 宣称自己是一个布尔值。
     */
    @ExportMessage
    public boolean isBoolean() {
        return true;
    }

    /**
     * 将自己转换为Java的原始boolean类型。
     * 这个转换永远不会失败。
     */
    @ExportMessage
    public boolean asBoolean() {
        return this.value;
    }
}

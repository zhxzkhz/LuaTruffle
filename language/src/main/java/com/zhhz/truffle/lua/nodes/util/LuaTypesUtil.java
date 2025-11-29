package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaValue;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * 包含了与 Lua 类型系统相关的静态辅助方法的工具类。
 */
public final class LuaTypesUtil {

    // 私有构造函数，防止实例化
    private LuaTypesUtil() {}

    // --- 【优化】预先创建并缓存所有可能的返回值 ---
    public static final TruffleString T_NIL = toTruffleString("nil");
    public static final TruffleString T_NUMBER = toTruffleString("number");
    public static final TruffleString T_STRING = toTruffleString("string");
    public static final TruffleString T_BOOLEAN = toTruffleString("boolean");
    public static final TruffleString T_TABLE = toTruffleString("table");
    public static final TruffleString T_FUNCTION = toTruffleString("function");
    public static final TruffleString T_USERDATA = toTruffleString("userdata");

    /**
     * 【核心】实现 Lua 的“真值”判断规则。
     * 在 Lua 中，只有 false 和 nil 被认为是假 (falsy)。
     *
     * @param value 任何 Lua 值 (作为 Object)。
     * @return {@code true} 如果值是 truthy，{@code false} 如果值是 falsy。
     */
    public static boolean isTruthy(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof LuaBoolean luaBoolean) return luaBoolean.asBoolean();
        return value != null && value != LuaNil.SINGLETON;
    }

    public static String getTypeName(Object value) {
        if (value instanceof LuaValue) {
            return ((LuaValue) value).getTypeName();
        } else if (value instanceof TruffleString){
            return "string";
        } else if (value instanceof Long || value instanceof Double){
            return "number";
        }
        return "unknown";
    }


    // 你还可以把其他类似的工具方法放在这里，例如：
    // public static String getTypeName(Object value) { ... }
    // public static long coerceToLong(Object value) { ... }
}
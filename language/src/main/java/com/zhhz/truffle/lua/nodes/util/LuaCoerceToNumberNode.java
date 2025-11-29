package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaString;

/**
 * 一个辅助节点，负责将任何 Lua 值强制转换为一个数字 (long 或 double)。
 * 如果转换失败，它会返回 null 或一个特殊的哨兵对象，而不是抛出异常，
 * 以便调用者可以决定后续如何处理（例如，尝试元方法）。
 * <p>
 * &#064;GenerateUncached  允许我们在慢路径中创建一个这个节点的“非缓存”版本。
 */
@GenerateUncached
public abstract class LuaCoerceToNumberNode extends Node {

    // 定义一个静态方法来创建实例，方便使用
    public static LuaCoerceToNumberNode create() {
        return LuaCoerceToNumberNodeGen.create();
    }

    /**
     * 节点的执行入口。
     * @param value The value to be coerced.
     * @return A Long, a Double, or null if coercion fails.
     */
    public abstract Object execute(Object value);


    // --- 特化 (Specializations) ---

    // 快速路径 1: 值已经是 long
    @Specialization(guards = "isLong(value)")
    public long doLong(long value) {
        return value;
    }

    // 快速路径 2: 值已经是 double
    @Specialization(guards = "isDouble(value)")
    public double doDouble(double value) {
        return value;
    }

    // 快速路径 3: 对于我们自己的 LuaNumber 包装类 (如果还存在的话)
    // @Specialization
    // public Object doLuaNumber(LuaNumber value) {
    //     return value.isLong() ? value.getLongValue() : value.getDoubleValue();
    // }

    /**
     * 中速路径: 尝试将 TruffleString 转换为数字。
     * 这是一个慢路径操作，必须用 @TruffleBoundary 标记。
     */
    @Specialization
    @TruffleBoundary // 标记为边界，允许使用 Java String 操作
    public Object doString(TruffleString value) {

        // 1. 将 TruffleString 转换为 Java String
        String javaString = value.toJavaStringUncached();

        // 2. 使用 Java 的 trim()
        //    Java 的 trim() 会去除所有 ASCII 码 <= 32 的字符
        //    这完美覆盖了 Lua 定义的空格、换行、制表符等
        String trimmed = javaString.trim();

        // 3. 判空
        if (trimmed.isEmpty()) {
            return null;
        }
        // --- 1. 处理十六进制 (0x) ---
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            try {
                // 去掉 "0x"，指定基数为 16
                String hexBody = trimmed.substring(2);
                // 使用 parseUnsignedLong 以支持 full 64-bit range (如 0xFFFFFFFFFFFFFFFF)
                // 也就是 Lua 中的 -1
                return Long.parseUnsignedLong(hexBody, 16);
            } catch (NumberFormatException e) {
                // 如果整数解析失败，可能是十六进制浮点数 (Lua 5.2+ 支持)
                // Java 的 Double.parseDouble 支持 "0x1.8p1" 这种格式
                try {
                    return Double.parseDouble(trimmed);
                } catch (NumberFormatException e2) {
                    return null; // 彻底失败
                }
            }
        }

        // --- 2. 处理十进制 ---
        try {
            // 优先解析为 long
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e1) {
            try {
                // 失败则解析为 double (处理小数或科学记数法)
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException e2) {
                return null;
            }
        }
    }

    @Specialization
    @TruffleBoundary
    public Object doString(LuaString value) {
        return doString(value.getValue());
    }

    /**
     * 慢速路径: 处理所有其他无法转换的类型 (table, function, boolean, nil)。
     * 我们简单地返回 null。
     */
    @Specialization(guards = {"!isNumber(value)", "!isTruffleString(value)"})
    public Object doOther(Object value) {
        return null; // 表示转换失败
    }



    // --- 辅助的 Guard 方法 ---
    protected static boolean isLong(Object value) {
        return value instanceof Long;
    }
    protected static boolean isDouble(Object value) {
        return value instanceof Double;
    }
    protected static boolean isNumber(Object value) {
        return isLong(value) || isDouble(value);
    }
    protected static boolean isTruffleString(Object value) {
        return value instanceof TruffleString;
    }
}
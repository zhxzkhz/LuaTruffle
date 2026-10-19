package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * 辅助节点，负责将任何 Lua 值【强制】转换为一个整数 (Long)。
 * - 如果值是整数，直接返回。
 * - 如果值是无小数的浮点数，转换并返回。
 * - 如果值是可以解析为整数的字符串，转换并返回。
 * - 所有其他情况（包括带小数的浮点数），都返回 null 表示转换失败。
 */
@GenerateInline(false)
@GenerateUncached
public abstract class LuaCoerceToIntegerNode extends Node {

    public static LuaCoerceToIntegerNode create() {
        return LuaCoerceToIntegerNodeGen.create();
    }

    /**
     * 节点的执行入口。
     * @param value 要强制转换为整数的值
     * @return A 如果强制成功，则为Long，否则为null
     */
    public abstract Long execute(Object value);


    // --- 特化 (Specializations) ---

    /**
     * 快速路径 1: 值已经是 long 或 Long。
     */
    @Specialization
    public Long doLong(long value) {
        return value;
    }

    /**
     * 快速路径 2: 值是 double 或 Double。
     * 我们必须检查它是否能被无损地表示为 long。
     */
    @Specialization
    public Long doDouble(double value) {
        // 复刻标准 Lua 的 lua_numbertointeger：
        //   n >= LUA_MININTEGER && n < -LUA_MININTEGER
        // 即 [-2^63, 2^63)。上界必须为开区间：2^63 无法用有符号 64 位整数表示，
        // 若直接用 (long) 转换会饱和为 Long.MAX_VALUE 而错误地通过相等性检查。
        if (value >= (double) Long.MIN_VALUE && value < -((double) Long.MIN_VALUE)
                && value == (long) value) {
            return (long) value;
        }
        // 有小数部分或超出可表示范围，转换失败
        return null;
    }

    /**
     * 中速路径: 尝试将 TruffleString 转换为整数。
     * 先按标准 Lua 的数字语法解析（支持十六进制浮点等，如 "0xAA.0"），
     * 再检查其是否可无损表示为 long。
     */
    @Specialization
    @TruffleBoundary
    public Long doString(TruffleString value) {
        Object number = LuaCoerceToNumberNode.create().execute(value);
        if (number instanceof Long longValue) {
            return longValue;
        }
        if (number instanceof Double doubleValue) {
            return doDouble(doubleValue);
        }
        return null;
    }

    /**
     * 慢速路径: 处理所有其他无法转换的类型。
     * 包括 null, boolean, table, function 等。
     */
    @Specialization(guards = {
            "!isLong(value)",
            "!isDouble(value)",
            "!isTruffleString(value)"
    })
    public Long doOther(Object value) {
        return null; // 表示转换失败
    }

    // --- 辅助的 Guard 方法 ---
    protected static boolean isLong(Object value) {
        return value instanceof Long;
    }
    protected static boolean isDouble(Object value) {
        return value instanceof Double;
    }
    protected static boolean isTruffleString(Object value) {
        return value instanceof TruffleString;
    }
}

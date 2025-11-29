package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
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
        // 检查 double 值是否有小数部分
        if (value == (long) value) {
            return (long) value;
        }
        // 如果有小数，转换失败
        return null;
    }

    /**
     * 中速路径: 尝试将 TruffleString 转换为整数。
     * 这是一个慢路径操作。
     */
    @Specialization
    @TruffleBoundary
    public Long doString(TruffleString value) {
        try {
            // 优先尝试直接解析为 long
            return value.parseLongUncached();
        } catch (TruffleString.NumberFormatException e1) {
            // 如果直接解析 long 失败 (例如 "123.0")，
            // 我们需要尝试将其解析为 double，然后检查它是否有小数
            try {
                double doubleValue = value.parseDoubleUncached();
                // 复用 doDouble 的逻辑
                return doDouble(doubleValue);
            } catch (TruffleString.NumberFormatException e2) {
                // 如果连 double 都解析失败，转换彻底失败
                return null;
            }
        }
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

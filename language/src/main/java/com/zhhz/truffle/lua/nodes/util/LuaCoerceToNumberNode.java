package com.zhhz.truffle.lua.nodes.util;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.runtime.LuaString;

import java.math.BigInteger;

/**
 * 一个辅助节点，负责将任何 Lua 值强制转换为一个数字 (long 或 double)。
 * 如果转换失败，它会返回 null 或一个特殊的哨兵对象，而不是抛出异常，
 * 以便调用者可以决定后续如何处理（例如，尝试元方法）。
 * <p>
 * &#064;GenerateUncached  允许我们在慢路径中创建一个这个节点的“非缓存”版本。
 */
@GenerateInline(false)
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

    /**
     * Lua 5.3 arithmetic converts string operands to floats, even when the
     * string denotes an integer. Other conversion contexts preserve the
     * integer subtype returned by {@link #execute(Object)}.
     */
    public final Object executeArithmetic(Object value) {
        Object number = execute(value);
        if (number instanceof Long && (value instanceof TruffleString || value instanceof LuaString)) {
            return ((Long) number).doubleValue();
        }
        return number;
    }


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
        return parseLuaNumber(value.toJavaStringUncached());
    }

    @Specialization
    public Object doString(LuaString value) {
        return doString(value.getValue());
    }

    /**
     * 宿主互操作返回的装箱数字（Java 方法返回的 int/float/double 等会以
     * Integer/Float/Double 形式流入 guest 值域），必须能参与算术运算。
     */
    @Specialization
    @TruffleBoundary
    public Object doHostNumber(Number value) {
        if (value instanceof Long || value instanceof Integer
                || value instanceof Short || value instanceof Byte) {
            return value.longValue();
        }
        return value.doubleValue();
    }

    /**
     * 慢速路径: 处理所有其他无法转换的类型 (table, function, boolean, nil)。
     * 我们简单地返回 null。
     */
    @Fallback
    public Object doOther(Object value) {
        return null; // 表示转换失败
    }

    /**
     * 解析一个完整的 Lua 数字字符串（等价于 C 的 {@code lua_stringtonumber}）：
     * 成功返回 {@link Long} 或 {@link Double}，失败返回 {@code null}。
     */
    @TruffleBoundary
    public static Object parseLuaNumber(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isLuaWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isLuaWhitespace(value.charAt(end - 1))) {
            end--;
        }
        if (start == end) {
            return null;
        }

        String number = value.substring(start, end);
        int signIndex = number.charAt(0) == '+' || number.charAt(0) == '-' ? 1 : 0;
        boolean negative = number.charAt(0) == '-';
        boolean hex = number.length() > signIndex + 2
                && number.charAt(signIndex) == '0'
                && (number.charAt(signIndex + 1) == 'x' || number.charAt(signIndex + 1) == 'X');

        if (hex) {
            return parseHexNumber(number, signIndex, negative);
        }
        if (!isDecimalNumber(number, signIndex)) {
            return null;
        }

        boolean integer = number.indexOf('.', signIndex) < 0
                && number.indexOf('e', signIndex) < 0
                && number.indexOf('E', signIndex) < 0;
        try {
            if (integer) {
                return Long.parseLong(number);
            }
            return Double.parseDouble(number);
        } catch (NumberFormatException exception) {
            try {
                return Double.parseDouble(number);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private static Object parseHexNumber(String number, int signIndex, boolean negative) {
        int bodyStart = signIndex + 2;
        int exponentIndex = indexOfEither(number, 'p', 'P', bodyStart);
        int fractionIndex = number.indexOf('.', bodyStart);
        boolean floatingPoint = fractionIndex >= 0 || exponentIndex >= 0;

        if (!isHexNumber(number, bodyStart, fractionIndex, exponentIndex)) {
            return null;
        }
        if (floatingPoint) {
            String javaNumber = exponentIndex < 0 ? number + "p0" : number;
            try {
                return Double.parseDouble(javaNumber);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        BigInteger integer = new BigInteger(number.substring(bodyStart), 16);
        if (negative) {
            integer = integer.negate();
        }
        return integer.longValue();
    }

    private static boolean isDecimalNumber(String number, int start) {
        int index = start;
        int integerDigits = consumeDigits(number, index, 10);
        index += integerDigits;
        int fractionDigits = 0;
        if (index < number.length() && number.charAt(index) == '.') {
            index++;
            fractionDigits = consumeDigits(number, index, 10);
            index += fractionDigits;
        }
        if (integerDigits + fractionDigits == 0) {
            return false;
        }
        if (index < number.length() && (number.charAt(index) == 'e' || number.charAt(index) == 'E')) {
            index++;
            if (index < number.length() && (number.charAt(index) == '+' || number.charAt(index) == '-')) {
                index++;
            }
            int exponentDigits = consumeDigits(number, index, 10);
            if (exponentDigits == 0) {
                return false;
            }
            index += exponentDigits;
        }
        return index == number.length();
    }

    private static boolean isHexNumber(String number, int start, int fractionIndex, int exponentIndex) {
        int significandEnd = exponentIndex < 0 ? number.length() : exponentIndex;
        if (fractionIndex >= significandEnd) {
            return false;
        }
        int digits = 0;
        for (int i = start; i < significandEnd; i++) {
            char character = number.charAt(i);
            if (character == '.') {
                if (i != fractionIndex) {
                    return false;
                }
            } else if (digitValue(character, 16) >= 0) {
                digits++;
            } else {
                return false;
            }
        }
        if (digits == 0) {
            return false;
        }
        if (exponentIndex >= 0) {
            int index = exponentIndex + 1;
            if (index < number.length() && (number.charAt(index) == '+' || number.charAt(index) == '-')) {
                index++;
            }
            int exponentDigits = consumeDigits(number, index, 10);
            return exponentDigits > 0 && index + exponentDigits == number.length();
        }
        return true;
    }

    private static int consumeDigits(String value, int start, int radix) {
        int index = start;
        while (index < value.length() && digitValue(value.charAt(index), radix) >= 0) {
            index++;
        }
        return index - start;
    }

    private static int digitValue(char character, int radix) {
        int digit;
        if (character >= '0' && character <= '9') {
            digit = character - '0';
        } else if (character >= 'a' && character <= 'f') {
            digit = character - 'a' + 10;
        } else if (character >= 'A' && character <= 'F') {
            digit = character - 'A' + 10;
        } else {
            return -1;
        }
        return digit < radix ? digit : -1;
    }

    private static int indexOfEither(String value, char first, char second, int start) {
        for (int i = start; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == first || character == second) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isLuaWhitespace(char character) {
        return character == ' ' || character == '\f' || character == '\n'
                || character == '\r' || character == '\t' || character == 0x0b;
    }



    // --- 辅助的 Guard 方法 ---
    protected static boolean isLong(Object value) {
        return value instanceof Long;
    }
    protected static boolean isDouble(Object value) {
        return value instanceof Double;
    }
}

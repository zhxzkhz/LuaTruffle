package com.zhhz.truffle.lua.runtime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * C 风格的数值格式化，对应 Lua 的 {@code LUAI_NUMFFORMAT ("%.14g")}、{@code %a} 与 {@code %q}。
 *
 * <p>Java 的 {@code Double.toString}/{@code String.format("%g")} 与 C 的 printf 在舍入位数、
 * 指数写法以及 {@code inf}/{@code nan} 上都不一致，因此这里用 {@link BigDecimal} 做与 C 相同的
 * 正确舍入。
 */
public final class LuaNumberFormat {

    public static final int TOSTRING_PRECISION = 14;
    private static final long MANTISSA_MASK = (1L << 52) - 1;

    private LuaNumberFormat() {
    }

    /** Lua 的 {@code lua_number2str} + {@code tostringbuff} 的 {@code ".0"} 规则。 */
    public static String toString(double value) {
        if (Double.isNaN(value)) {
            return "nan";
        }
        if (value == Double.POSITIVE_INFINITY) {
            return "inf";
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return "-inf";
        }
        String text = formatG(value, TOSTRING_PRECISION, false, false);
        return isIntegerText(text) ? text + ".0" : text;
    }

    /** C 的 {@code %g}/{@code %G}；{@code precision <= 0} 视为 1。 */
    public static String formatG(double value, int precision, boolean alternate, boolean upper) {
        if (Double.isNaN(value)) {
            return upper ? "NAN" : "nan";
        }
        if (Double.isInfinite(value)) {
            String text = upper ? "INF" : "inf";
            return value < 0 ? "-" + text : text;
        }
        boolean negative = (Double.doubleToRawLongBits(value) & Long.MIN_VALUE) != 0;
        double absolute = Math.abs(value);
        int p = precision <= 0 ? 1 : precision;
        if (absolute == 0.0) {
            String body = alternate ? "0." + "0".repeat(p - 1) : "0";
            return negative ? "-" + body : body;
        }
        BigDecimal exact = new BigDecimal(absolute);
        BigDecimal rounded = exact.round(new MathContext(p, RoundingMode.HALF_EVEN));
        String digits = rounded.unscaledValue().toString();
        int exponent = digits.length() - 1 - rounded.scale();
        StringBuilder output = new StringBuilder();
        if (negative) {
            output.append('-');
        }
        if (exponent < -4 || exponent >= p) {
            StringBuilder significant = new StringBuilder(digits);
            while (significant.length() < p) {
                significant.append('0');
            }
            output.append(significant.charAt(0));
            String fraction = significant.substring(1);
            if (alternate) {
                output.append('.').append(fraction);
            } else {
                int end = fraction.length();
                while (end > 0 && fraction.charAt(end - 1) == '0') {
                    end--;
                }
                if (end > 0) {
                    output.append('.').append(fraction, 0, end);
                }
            }
            output.append(upper ? 'E' : 'e').append(exponent < 0 ? '-' : '+');
            int magnitude = Math.abs(exponent);
            if (magnitude < 10) {
                output.append('0');
            }
            output.append(magnitude);
        } else {
            int fractionDigits = p - 1 - exponent;
            BigDecimal fixed = rounded.setScale(Math.max(0, fractionDigits), RoundingMode.UNNECESSARY);
            String text = fixed.toPlainString();
            if (!alternate) {
                text = stripTrailingZeroes(text);
            } else if (text.indexOf('.') < 0) {
                text = text + ".";
            }
            output.append(text);
        }
        return output.toString();
    }

    /** C 的 {@code %a}/{@code %A}（Lua 不允许带修饰符）。 */
    public static String formatA(double value, boolean upper) {
        if (Double.isNaN(value)) {
            return upper ? "NAN" : "nan";
        }
        if (Double.isInfinite(value)) {
            String text = upper ? "INF" : "inf";
            return value < 0 ? "-" + text : text;
        }
        long bits = Double.doubleToRawLongBits(value);
        boolean negative = bits < 0;
        int biasedExponent = (int) ((bits >>> 52) & 0x7FF);
        long mantissa = bits & MANTISSA_MASK;
        int lead;
        int exponent;
        if (biasedExponent == 0) {
            if (mantissa == 0) {
                lead = 0;
                exponent = 0;
            } else {
                int highestBit = 63 - Long.numberOfLeadingZeros(mantissa);
                int shift = 52 - highestBit;
                mantissa = (mantissa << shift) & MANTISSA_MASK;
                exponent = -1022 - shift;
                lead = 1;
            }
        } else {
            lead = 1;
            exponent = biasedExponent - 1023;
        }
        int digits = 13;
        while (digits > 0 && (mantissa & 0xFL) == 0) {
            mantissa >>>= 4;
            digits--;
        }
        StringBuilder output = new StringBuilder();
        if (negative) {
            output.append('-');
        }
        output.append(upper ? "0X" : "0x").append(lead);
        if (digits > 0) {
            String hex = Long.toHexString(mantissa);
            while (hex.length() < digits) {
                hex = "0" + hex;
            }
            output.append('.').append(upper ? hex.toUpperCase(Locale.ROOT) : hex);
        }
        output.append(upper ? 'P' : 'p').append(exponent < 0 ? '-' : '+').append(Math.abs(exponent));
        return output.toString();
    }

    /** Lua 5.4 的 {@code %q} 浮点表示。 */
    public static String formatQ(double value) {
        if (Double.isNaN(value)) {
            return "(0/0)";
        }
        if (value == Double.POSITIVE_INFINITY) {
            return "1e9999";
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return "-1e9999";
        }
        return formatA(value, false);
    }

    private static boolean isIntegerText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '-' && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    private static String stripTrailingZeroes(String text) {
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && text.charAt(end - 1) == '.') {
            end--;
        }
        return text.substring(0, end);
    }
}

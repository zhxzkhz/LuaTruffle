package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaString;

import java.nio.charset.StandardCharsets;

final class LuaStringSupport {

    private LuaStringSupport() {
    }

    static byte[] ensureBytes(Object[] arguments, int index, String function) {
        if (index >= arguments.length) {
            throw argumentError(function, index + 1, "string expected, got no value");
        }
        return toBytes(arguments[index], index + 1, function);
    }

    static byte[] toBytes(Object value, int argument, String function) {
        if (value instanceof TruffleString string) {
            return TruffleString.CopyToByteArrayNode.getUncached().execute(string, LuaLanguage.STRING_ENCODING);
        }
        if (value instanceof LuaString string) {
            return TruffleString.CopyToByteArrayNode.getUncached().execute(string.getValue(), LuaLanguage.STRING_ENCODING);
        }
        if (value instanceof String string) {
            return utf8Bytes(string);
        }
        if (value instanceof Long number) {
            return asciiBytes(Long.toString(number));
        }
        if (value instanceof Double number) {
            return asciiBytes(doubleText(number));
        }
        throw stringExpectedError(function, argument, value);
    }

    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static RuntimeException stringExpectedError(String function, int argument, Object value) {
        return argumentError(function, argument, "string expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    /**
     * 宿主 {@code String} 转 UTF-8 字节。放在边界里，避免 {@code String.getBytes} 内部的
     * {@code CharsetEncoder}/{@code Throwable} 虚调用把 JDK 实现拉进运行期编译范围。
     */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static byte[] utf8Bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /** {@code Double.toString} 在 Native Image 中会牵出资源包/线程创建，放在边界里。 */
    @com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
    private static String doubleText(double number) {
        return number == (long) number ? Long.toString((long) number) : Double.toString(number);
    }

    /**
     * 数字转字符串后的字节化。数字只含 ASCII 字符，直接截断 char 即可，
     * 避免 {@code String.getBytes(US_ASCII)} 在 Native Image 中引入 CharsetEncoder
     * 的运行期编译依赖。
     */
    private static byte[] asciiBytes(String text) {
        byte[] bytes = new byte[text.length()];
        for (int i = 0; i < text.length(); i++) {
            bytes[i] = (byte) text.charAt(i);
        }
        return bytes;
    }

    static RuntimeException argumentError(String function, int arg, String message) {
        return com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.argumentError(function, arg, message);
    }

    static TruffleString fromBytes(byte[] bytes) {
        return TruffleString.fromByteArrayUncached(bytes, LuaLanguage.STRING_ENCODING, true);
    }

    static TruffleString fromBytes(byte[] bytes, int offset, int length) {
        return TruffleString.fromByteArrayUncached(bytes, offset, length, LuaLanguage.STRING_ENCODING, true);
    }

    static String toLatin1(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    static byte[] fromLatin1(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    static long ensureInteger(Object[] arguments, int index, String function) {
        if (index >= arguments.length) {
            throw argumentError(function, index + 1, "number expected, got no value");
        }
        Object value = arguments[index];
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Integer number) {
            return number.longValue();
        }
        if (value instanceof Double number) {
            if (Double.isFinite(number) && number == Math.rint(number)
                    && number >= Long.MIN_VALUE && number <= Long.MAX_VALUE) {
                return number.longValue();
            }
            // 数值存在但无整数表示（与 luaL_checkinteger 的 interror 一致）
            throw argumentError(function, index + 1, "number has no integer representation");
        }
        if (value instanceof TruffleString string) {
            String text = string.toJavaStringUncached();
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // 可解析为浮点则是「无整数表示」，否则是「类型不是数字」
                try {
                    Double.parseDouble(text.trim());
                    throw argumentError(function, index + 1, "number has no integer representation");
                } catch (NumberFormatException notNumber) {
                    throw argumentError(function, index + 1, "number expected, got string");
                }
            }
        }
        throw argumentError(function, index + 1, "number expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    static int relativePosition(long position, int length) {
        long normalized = position >= 0 ? position : (long) length + position + 1;
        if (normalized < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (normalized > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) normalized;
    }

    static boolean isNil(Object value) {
        return value == null || value == com.zhhz.truffle.lua.runtime.LuaNil.SINGLETON;
    }
}

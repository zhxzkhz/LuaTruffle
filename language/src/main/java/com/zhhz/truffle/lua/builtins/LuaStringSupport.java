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
            return string.getBytes(StandardCharsets.UTF_8);
        }
        if (value instanceof Long number) {
            return Long.toString(number).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Double number) {
            String text = number == number.longValue() ? Long.toString(number.longValue()) : Double.toString(number);
            return text.getBytes(StandardCharsets.US_ASCII);
        }
        throw argumentError(function, argument, "string expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
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

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
            throw LuaException.create("bad argument #" + (index + 1) + " to '" + function
                    + "' (string expected, got no value)", null);
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
        throw LuaException.create("bad argument #" + argument + " to '" + function + "' (string expected)", null);
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
            throw LuaException.create("bad argument #" + (index + 1) + " to '" + function
                    + "' (number expected, got no value)", null);
        }
        Object value = arguments[index];
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Integer number) {
            return number.longValue();
        }
        if (value instanceof Double number && Double.isFinite(number) && number == Math.rint(number)
                && number >= Long.MIN_VALUE && number <= Long.MAX_VALUE) {
            return number.longValue();
        }
        if (value instanceof TruffleString string) {
            try {
                return Long.parseLong(string.toJavaStringUncached());
            } catch (NumberFormatException ignored) {
                // Fall through to the argument error.
            }
        }
        throw LuaException.create("bad argument #" + (index + 1) + " to '" + function + "' (integer expected)", null);
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

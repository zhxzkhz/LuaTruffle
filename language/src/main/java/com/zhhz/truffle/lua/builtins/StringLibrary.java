package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.isTruthy;

/** Byte-oriented implementation of Lua's {@code string} standard library. */
public final class StringLibrary {

    private StringLibrary() {
    }

    @LuaBuiltin(name = "len")
    @TruffleBoundary
    public static long len(Object[] arguments) {
        return LuaStringSupport.ensureBytes(arguments, 0, "len").length;
    }

    @LuaBuiltin(name = "upper")
    @TruffleBoundary
    public static TruffleString upper(Object[] arguments) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "upper");
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            if (value >= 'a' && value <= 'z') {
                bytes[i] = (byte) (value - ('a' - 'A'));
            }
        }
        return LuaStringSupport.fromBytes(bytes);
    }

    @LuaBuiltin(name = "lower")
    @TruffleBoundary
    public static TruffleString lower(Object[] arguments) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "lower");
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            if (value >= 'A' && value <= 'Z') {
                bytes[i] = (byte) (value + ('a' - 'A'));
            }
        }
        return LuaStringSupport.fromBytes(bytes);
    }

    @LuaBuiltin(name = "reverse")
    @TruffleBoundary
    public static TruffleString reverse(Object[] arguments) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "reverse");
        for (int left = 0, right = bytes.length - 1; left < right; left++, right--) {
            byte value = bytes[left];
            bytes[left] = bytes[right];
            bytes[right] = value;
        }
        return LuaStringSupport.fromBytes(bytes);
    }

    @LuaBuiltin(name = "rep")
    @TruffleBoundary
    public static TruffleString rep(Object[] arguments) {
        byte[] value = LuaStringSupport.ensureBytes(arguments, 0, "rep");
        long repetitions = LuaStringSupport.ensureInteger(arguments, 1, "rep");
        byte[] separator = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.toBytes(arguments[2], 3, "rep") : new byte[0];
        if (repetitions <= 0) {
            return LuaStringSupport.fromBytes(new byte[0]);
        }
        long size;
        try {
            size = Math.addExact(Math.multiplyExact(repetitions, value.length),
                    Math.multiplyExact(repetitions - 1, separator.length));
        } catch (ArithmeticException exception) {
            throw LuaException.create("resulting string too large", null);
        }
        if (size > Integer.MAX_VALUE) {
            throw LuaException.create("resulting string too large", null);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream((int) size);
        for (long i = 0; i < repetitions; i++) {
            if (i > 0) {
                output.writeBytes(separator);
            }
            output.writeBytes(value);
        }
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @LuaBuiltin(name = "sub")
    @TruffleBoundary
    public static TruffleString sub(Object[] arguments) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "sub");
        int start = LuaStringSupport.relativePosition(LuaStringSupport.ensureInteger(arguments, 1, "sub"), bytes.length);
        int end = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.relativePosition(LuaStringSupport.ensureInteger(arguments, 2, "sub"), bytes.length)
                : bytes.length;
        start = Math.max(start, 1);
        end = Math.min(end, bytes.length);
        if (start > end) {
            return LuaStringSupport.fromBytes(new byte[0]);
        }
        return LuaStringSupport.fromBytes(bytes, start - 1, end - start + 1);
    }

    @LuaBuiltin(name = "char")
    @TruffleBoundary
    public static TruffleString charFunc(Object[] arguments) {
        byte[] bytes = new byte[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            long value = LuaStringSupport.ensureInteger(arguments, i, "char");
            if (value < 0 || value > 255) {
                throw LuaException.create("bad argument #" + (i + 1) + " to 'char' (value out of range)", null);
            }
            bytes[i] = (byte) value;
        }
        return LuaStringSupport.fromBytes(bytes);
    }

    @LuaBuiltin(name = "byte")
    @TruffleBoundary
    public static LuaMultiValue byteFunc(Object[] arguments) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, 0, "byte");
        int start = arguments.length > 1 && !LuaStringSupport.isNil(arguments[1])
                ? LuaStringSupport.relativePosition(LuaStringSupport.ensureInteger(arguments, 1, "byte"), bytes.length) : 1;
        int end = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.relativePosition(LuaStringSupport.ensureInteger(arguments, 2, "byte"), bytes.length) : start;
        start = Math.max(start, 1);
        end = Math.min(end, bytes.length);
        if (start > end) {
            return new LuaMultiValue(new Object[0]);
        }
        Object[] result = new Object[end - start + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = (long) (bytes[start - 1 + i] & 0xff);
        }
        return new LuaMultiValue(result);
    }

    @LuaBuiltin(name = "format")
    @TruffleBoundary
    public static TruffleString format(Object[] arguments) {
        byte[] format = LuaStringSupport.ensureBytes(arguments, 0, "format");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int argumentIndex = 1;
        for (int position = 0; position < format.length; position++) {
            int value = format[position] & 0xff;
            if (value != '%') {
                output.write(value);
                continue;
            }
            if (++position >= format.length) {
                throw LuaException.create("invalid conversion '%' to 'format'", null);
            }
            if ((format[position] & 0xff) == '%') {
                output.write('%');
                continue;
            }

            int directiveStart = position - 1;
            while (position < format.length && isFormatFlag(format[position] & 0xff)) {
                position++;
            }
            while (position < format.length && isDigit(format[position] & 0xff)) {
                position++;
            }
            if (position < format.length && (format[position] & 0xff) == '.') {
                position++;
                while (position < format.length && isDigit(format[position] & 0xff)) {
                    position++;
                }
            }
            if (position >= format.length) {
                throw LuaException.create("invalid format (ends with '%')", null);
            }
            int conversion = format[position] & 0xff;
            if (argumentIndex >= arguments.length) {
                throw LuaException.create("bad argument #" + (argumentIndex + 1) + " to 'format' (no value)", null);
            }
            Object argument = arguments[argumentIndex++];
            String directive = new String(format, directiveStart, position - directiveStart + 1, StandardCharsets.ISO_8859_1);
            byte[] formatted = formatValue(directive, conversion, argument, argumentIndex);
            output.writeBytes(formatted);
        }
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @LuaBuiltin(name = "find")
    @TruffleBoundary
    public static Object find(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "find");
        byte[] pattern = LuaStringSupport.ensureBytes(arguments, 1, "find");
        int initial = normalizeInitial(arguments, 2, source.length, "find");
        if (initial > source.length) {
            return LuaNil.SINGLETON;
        }
        boolean plain = arguments.length > 3 && isTruthy(arguments[3]);
        if (plain) {
            int found = indexOf(source, pattern, initial);
            return found < 0 ? LuaNil.SINGLETON
                    : new LuaMultiValue(new Object[]{(long) found + 1, (long) found + pattern.length});
        }
        LuaPatternMatcher.MatchResult match = new LuaPatternMatcher(source, pattern).find(initial);
        if (match == null) {
            return LuaNil.SINGLETON;
        }
        Object[] captures = match.captureCount() == 0 ? new Object[0] : match.capturesOrWholeMatch();
        Object[] result = new Object[captures.length + 2];
        result[0] = (long) match.start() + 1;
        result[1] = (long) match.end();
        System.arraycopy(captures, 0, result, 2, captures.length);
        return new LuaMultiValue(result);
    }

    @LuaBuiltin(name = "match")
    @TruffleBoundary
    public static Object match(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "match");
        byte[] pattern = LuaStringSupport.ensureBytes(arguments, 1, "match");
        int initial = normalizeInitial(arguments, 2, source.length, "match");
        if (initial > source.length) {
            return LuaNil.SINGLETON;
        }
        LuaPatternMatcher.MatchResult match = new LuaPatternMatcher(source, pattern).find(initial);
        return match == null ? LuaNil.SINGLETON : new LuaMultiValue(match.capturesOrWholeMatch());
    }

    @LuaBuiltin(name = "gsub")
    @TruffleBoundary
    public static LuaMultiValue gsub(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "gsub");
        byte[] pattern = LuaStringSupport.ensureBytes(arguments, 1, "gsub");
        if (arguments.length < 3) {
            throw LuaException.create("bad argument #3 to 'gsub' (string/function/table expected, got no value)", null);
        }
        Object replacement = arguments[2];
        long maximum = arguments.length > 3 && !LuaStringSupport.isNil(arguments[3])
                ? LuaStringSupport.ensureInteger(arguments, 3, "gsub") : source.length + 1L;
        if (maximum <= 0) {
            return new LuaMultiValue(new Object[]{LuaStringSupport.fromBytes(source), 0L});
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(source.length);
        int position = 0;
        long substitutions = 0;
        int lastMatchEnd = -1;
        boolean anchored = pattern.length > 0 && (pattern[0] & 0xff) == '^';
        LuaPatternMatcher matcher = new LuaPatternMatcher(source, pattern);
        while (substitutions < maximum) {
            LuaPatternMatcher.MatchResult match = matcher.find(position);
            if (match == null || match.start() < position) {
                break;
            }
            if (match.end() != lastMatchEnd) {
                output.write(source, position, match.start() - position);
                appendReplacement(output, replacement, match);
                substitutions++;
                position = match.end();
                lastMatchEnd = match.end();
            } else {
                if (match.start() < source.length) {
                    output.write(source, position, match.start() - position + 1);
                    position = match.start() + 1;
                } else {
                    break;
                }
            }
            if (anchored) {
                break;
            }
        }
        if (position < source.length) {
            output.write(source, position, source.length - position);
        }
        return new LuaMultiValue(new Object[]{LuaStringSupport.fromBytes(output.toByteArray()), substitutions});
    }

    @LuaBuiltin(name = "pack")
    public static TruffleString pack(Object[] arguments) {
        return StringBinaryLibrary.pack(arguments);
    }

    @LuaBuiltin(name = "packsize")
    public static long packsize(Object[] arguments) {
        return StringBinaryLibrary.packsize(arguments);
    }

    @LuaBuiltin(name = "unpack")
    public static LuaMultiValue unpack(Object[] arguments) {
        return StringBinaryLibrary.unpack(arguments);
    }

    @LuaBuiltin(name = "dump")
    public static Object dump(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaFunction)) {
            throw LuaException.create("bad argument #1 to 'dump' (function expected)", null);
        }
        throw LuaException.create("string.dump is not supported: binary chunk serialization is unavailable", null);
    }

    private static void appendReplacement(ByteArrayOutputStream output, Object replacement,
                                          LuaPatternMatcher.MatchResult match) {
        Object value;
        if (replacement instanceof LuaTable table) {
            Object[] captures = match.capturesOrWholeMatch();
            value = table.rawget(captures[0]);
        } else if (InteropLibrary.getUncached().isExecutable(replacement)) {
            try {
                value = InteropLibrary.getUncached().execute(replacement, match.capturesOrWholeMatch());
                value = LuaMultiValue.firstValue(value);
            } catch (LuaException exception) {
                throw exception;
            } catch (Exception exception) {
                throw LuaException.create("error in string.gsub replacement function\n" + exception.getMessage(), null);
            }
        } else {
            byte[] replacementBytes = LuaStringSupport.toBytes(replacement, 3, "gsub");
            appendReplacementTemplate(output, replacementBytes, match);
            return;
        }

        if (LuaStringSupport.isNil(value) || value instanceof Boolean booleanValue && !booleanValue
                || value instanceof LuaBoolean booleanValue && !booleanValue.getValue()) {
            output.writeBytes(match.wholeMatch());
            return;
        }
        output.writeBytes(LuaStringSupport.toBytes(value, 3, "gsub"));
    }

    private static void appendReplacementTemplate(ByteArrayOutputStream output, byte[] replacement,
                                                   LuaPatternMatcher.MatchResult match) {
        Object[] captures = match.capturesOrWholeMatch();
        for (int i = 0; i < replacement.length; i++) {
            int value = replacement[i] & 0xff;
            if (value != '%' || i + 1 >= replacement.length) {
                output.write(value);
                continue;
            }
            int escaped = replacement[++i] & 0xff;
            if (escaped == '%') {
                output.write('%');
            } else if (escaped == '0') {
                output.writeBytes(match.wholeMatch());
            } else if (escaped >= '1' && escaped <= '9') {
                int capture = escaped - '1';
                if (capture >= captures.length) {
                    throw LuaException.create("invalid capture index %" + (char) escaped, null);
                }
                output.writeBytes(LuaStringSupport.toBytes(captures[capture], 3, "gsub"));
            } else {
                throw LuaException.create("invalid use of '%' in replacement string", null);
            }
        }
    }

    private static int normalizeInitial(Object[] arguments, int index, int length, String function) {
        long initial = arguments.length > index && !LuaStringSupport.isNil(arguments[index])
                ? LuaStringSupport.ensureInteger(arguments, index, function) : 1;
        int normalized = LuaStringSupport.relativePosition(initial, length);
        return Math.max(0, normalized - 1);
    }

    private static int indexOf(byte[] source, byte[] pattern, int start) {
        if (pattern.length == 0) {
            return start;
        }
        outer:
        for (int i = start; i <= source.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (source[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static boolean isFormatFlag(int value) {
        return value == '-' || value == '+' || value == ' ' || value == '#' || value == '0';
    }

    private static boolean isDigit(int value) {
        return value >= '0' && value <= '9';
    }

    private static byte[] formatValue(String directive, int conversion, Object value, int argument) {
        if (conversion == 'q') {
            if (!directive.equals("%q")) {
                throw LuaException.create("specifier '%q' cannot have modifiers", null);
            }
            return quote(value);
        }
        try {
            String javaDirective = directive;
            Object javaValue;
            switch (conversion) {
                case 'c' -> javaValue = Math.toIntExact(toLong(value, argument, "format"));
                case 'd' -> javaValue = toLong(value, argument, "format");
                case 'i' -> {
                    javaDirective = directive.substring(0, directive.length() - 1) + 'd';
                    javaValue = toLong(value, argument, "format");
                }
                case 'o', 'x', 'X' -> javaValue = toLong(value, argument, "format");
                case 'u' -> {
                    String unsigned = Long.toUnsignedString(toLong(value, argument, "format"));
                    return applyStringWidth(directive, unsigned).getBytes(StandardCharsets.US_ASCII);
                }
                case 'a', 'A', 'e', 'E', 'f', 'g', 'G' -> javaValue = toDouble(value, argument, "format");
                case 's' -> javaValue = LuaStringSupport.toLatin1(LuaStringSupport.toBytes(value, argument, "format"));
                default -> throw LuaException.create("invalid conversion '%" + (char) conversion + "' to 'format'", null);
            }
            return String.format(Locale.ROOT, javaDirective, javaValue).getBytes(StandardCharsets.ISO_8859_1);
        } catch (LuaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw LuaException.create("invalid format: " + exception.getMessage(), null);
        }
    }

    private static String applyStringWidth(String directive, String value) {
        boolean left = directive.indexOf('-') >= 0;
        boolean zero = directive.indexOf('0') >= 0;
        int width = 0;
        for (int i = 1; i < directive.length() - 1; i++) {
            if (Character.isDigit(directive.charAt(i))) {
                width = width * 10 + directive.charAt(i) - '0';
            }
        }
        if (value.length() >= width) {
            return value;
        }
        String padding = String.valueOf(zero ? '0' : ' ').repeat(width - value.length());
        return left ? value + padding : padding + value;
    }

    private static byte[] quote(Object value) {
        if (LuaStringSupport.isNil(value)) {
            return "nil".getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Boolean booleanValue) {
            return Boolean.toString(booleanValue).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof LuaBoolean booleanValue) {
            return Boolean.toString(booleanValue.getValue()).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Long || value instanceof Double) {
            return LuaStringSupport.toBytes(value, 2, "format");
        }
        byte[] bytes = LuaStringSupport.toBytes(value, 2, "format");
        ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length + 2);
        output.write('"');
        for (int i = 0; i < bytes.length; i++) {
            int current = bytes[i] & 0xff;
            switch (current) {
                case '"', '\\' -> {
                    output.write('\\');
                    output.write(current);
                }
                case '\n' -> output.writeBytes("\\n".getBytes(StandardCharsets.US_ASCII));
                case '\r' -> output.writeBytes("\\r".getBytes(StandardCharsets.US_ASCII));
                case 0 -> output.writeBytes("\\0".getBytes(StandardCharsets.US_ASCII));
                default -> {
                    if (current < 32 || current == 127) {
                        output.writeBytes(String.format(Locale.ROOT, "\\%03d", current).getBytes(StandardCharsets.US_ASCII));
                    } else {
                        output.write(current);
                    }
                }
            }
        }
        output.write('"');
        return output.toByteArray();
    }

    private static long toLong(Object value, int argument, String function) {
        return LuaStringSupport.ensureInteger(new Object[]{value}, 0, function);
    }

    private static double toDouble(Object value, int argument, String function) {
        if (value instanceof Long number) {
            return number.doubleValue();
        }
        if (value instanceof Double number) {
            return number;
        }
        throw LuaException.create("bad argument #" + argument + " to '" + function + "' (number expected)", null);
    }
}

package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaNumberFormat;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (size > Integer.MAX_VALUE - 1) {
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
                if (argumentIndex >= arguments.length) {
                    throw LuaStringSupport.argumentError("format", argumentIndex + 1, "no value");
                }
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
                if (argumentIndex >= arguments.length) {
                    throw LuaStringSupport.argumentError("format", argumentIndex + 1, "no value");
                }
                throw LuaException.create("invalid conversion '%' to 'format'", null);
            }
            int conversion = format[position] & 0xff;
            if (argumentIndex >= arguments.length) {
                throw LuaStringSupport.argumentError("format", argumentIndex + 1, "no value");
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
    @TruffleBoundary
    public static Object dump(Object[] arguments) {
        if (arguments.length == 0 || !(arguments[0] instanceof LuaFunction function)) {
            throw LuaException.create("bad argument #1 to 'dump' (function expected)", null);
        }
        boolean strip = arguments.length > 1 && isTruthy(arguments[1]);
        com.zhhz.truffle.lua.bytecode.Proto proto = function.getProto();
        if (proto == null
                && function.getCallTarget() instanceof com.oracle.truffle.api.RootCallTarget rct
                && rct.getRootNode() instanceof com.zhhz.truffle.lua.nodes.LuaRootNode root) {
            proto = root.getProto();
        }
        if (proto == null) {
            throw LuaException.create("unable to dump given function", null);
        }
        byte[] data = com.zhhz.truffle.lua.bytecode.LuaDumper.dump(proto, strip);
        return TruffleString.fromByteArrayUncached(data, 0, data.length,
                com.zhhz.truffle.lua.LuaLanguage.STRING_ENCODING, false);
    }

    private static void appendReplacement(ByteArrayOutputStream output, Object replacement,
                                          LuaPatternMatcher.MatchResult match) {
        Object value;
        if (replacement instanceof LuaTable table) {
            Object[] captures = match.capturesOrWholeMatch();
            value = com.zhhz.truffle.lua.nodes.access.LuaReadTableNode.readUncached(null, table, captures[0]);
        } else if (InteropLibrary.getUncached().isExecutable(replacement)) {
            // 参考实现 lstrlib.c 通过 lua_call 调用替换函数：不可跨此 C 边界 yield。
            com.zhhz.truffle.lua.runtime.LuaContext context =
                    com.zhhz.truffle.lua.runtime.LuaContext.get(null);
            if (context != null) {
                context.enterNonYieldable();
            }
            try {
                value = InteropLibrary.getUncached().execute(replacement, match.capturesOrWholeMatch());
                value = LuaMultiValue.firstValue(value);
            } catch (LuaException exception) {
                throw exception;
            } catch (Exception exception) {
                throw LuaException.create("error in string.gsub replacement function\n" + LuaException.messageOf(exception), null);
            } finally {
                if (context != null) {
                    context.exitNonYieldable();
                }
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
        if (!isStringLike(value)) {
            String typeName = value instanceof com.zhhz.truffle.lua.runtime.LuaValue luaValue
                    ? luaValue.getTypeName() : "userdata";
            throw LuaException.create("invalid replacement value (a " + typeName + ")", null);
        }
        output.writeBytes(LuaStringSupport.toBytes(value, 3, "gsub"));
    }

    private static boolean isStringLike(Object value) {
        return value instanceof TruffleString || value instanceof com.zhhz.truffle.lua.runtime.LuaString
                || value instanceof String || value instanceof Long || value instanceof Double || value instanceof Integer;
    }

    private static void appendReplacementTemplate(ByteArrayOutputStream output, byte[] replacement,
                                                   LuaPatternMatcher.MatchResult match) {
        Object[] captures = null;
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
                if (captures == null) {
                    captures = match.capturesOrWholeMatch();
                }
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
            return quote(value, argument);
        }
        if (conversion == 'a' || conversion == 'A') {
            if (directive.length() != 2) {
                throw LuaException.create("modifiers for format '%a'/'%A' not implemented", null);
            }
            return LuaNumberFormat.formatA(toDouble(value, argument, "format"), conversion == 'A')
                    .getBytes(StandardCharsets.US_ASCII);
        }
        if (isSupportedConversion(conversion)) {
            validateFormat(directive, conversion);
        }
        try {
            if (directive.indexOf('.') >= 0 && isIntegerConversion(conversion)) {
                return formatIntegerPrecision(directive, conversion, value, argument);
            }
            if (conversion == 'p') {
                return formatPointer(directive, value).getBytes(StandardCharsets.US_ASCII);
            }
            if (conversion == 'e' || conversion == 'E' || conversion == 'f'
                    || conversion == 'g' || conversion == 'G') {
                return formatFloat(directive, conversion, value, argument);
            }
            String javaDirective = directive;
            Object javaValue;
            switch (conversion) {
                case 'c' -> {
                    int character = (int) (toLong(value, argument, "format") & 0xFF);
                    return String.format(Locale.ROOT, normalizeJavaDirective(directive), (char) character)
                            .getBytes(StandardCharsets.ISO_8859_1);
                }
                case 'd' -> javaValue = toLong(value, argument, "format");
                case 'i' -> {
                    javaDirective = directive.substring(0, directive.length() - 1) + 'd';
                    javaValue = toLong(value, argument, "format");
                }
                case 'o', 'x', 'X' -> {
                    long integer = toLong(value, argument, "format");
                    javaValue = integer;
                    if (integer == 0 && directive.indexOf('#') >= 0) {
                        javaDirective = directive.replace("#", "");
                    }
                }
                case 'u' -> {
                    String unsigned = Long.toUnsignedString(toLong(value, argument, "format"));
                    return applyStringWidth(directive, unsigned).getBytes(StandardCharsets.US_ASCII);
                }
                case 's' -> {
                    byte[] stringBytes = toLuaStringBytes(value);
                    if (directive.equals("%s")) {
                        return stringBytes;
                    }
                    for (byte stringByte : stringBytes) {
                        if (stringByte == 0) {
                            throw LuaStringSupport.argumentError("format", argument, "string contains zeros");
                        }
                    }
                    if (directive.indexOf('.') < 0 && stringBytes.length >= 100) {
                        return stringBytes;
                    }
                    javaValue = LuaStringSupport.toLatin1(stringBytes);
                }
                default -> throw LuaException.create("invalid conversion '" + directive + "' to 'format'", null);
            }
            return String.format(Locale.ROOT, normalizeJavaDirective(javaDirective), javaValue)
                    .getBytes(StandardCharsets.ISO_8859_1);
        } catch (LuaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw LuaException.create("invalid format: " + LuaException.messageOf(exception), null);
        }
    }

    /**
     * C 的 {@code %e/%f/%g} 语义。{@code %g} 与 {@code inf}/{@code nan} 需要与 C 完全一致的输出，
     * 有限值上的 {@code %e/%f} 直接交给 Java（其指数/填充规则与 C 一致）。
     */
    private static byte[] formatFloat(String directive, int conversion, Object value, int argument) {
        double number = toDouble(value, argument, "format");
        boolean upper = conversion == 'E' || conversion == 'G';
        int i = 1;
        boolean leftAlign = false;
        boolean plus = false;
        boolean space = false;
        boolean alternate = false;
        boolean zero = false;
        while (i < directive.length()) {
            char c = directive.charAt(i);
            if (c == '-') leftAlign = true;
            else if (c == '+') plus = true;
            else if (c == ' ') space = true;
            else if (c == '#') alternate = true;
            else if (c == '0') zero = true;
            else break;
            i++;
        }
        int width = 0;
        while (i < directive.length() && Character.isDigit(directive.charAt(i))) {
            width = width * 10 + (directive.charAt(i) - '0');
            i++;
        }
        int precision = -1;
        if (i < directive.length() && directive.charAt(i) == '.') {
            precision = 0;
            i++;
            while (i < directive.length() && Character.isDigit(directive.charAt(i))) {
                precision = precision * 10 + (directive.charAt(i) - '0');
                i++;
            }
        }

        boolean nonFinite = Double.isNaN(number) || Double.isInfinite(number);
        String body;
        if (conversion == 'g' || conversion == 'G') {
            body = LuaNumberFormat.formatG(number, precision < 0 ? 6 : precision, alternate, upper);
        } else if (nonFinite) {
            String text = Double.isNaN(number) ? (upper ? "NAN" : "nan") : (upper ? "INF" : "inf");
            body = number < 0 ? "-" + text : text;
        } else {
            body = formatFiniteDecimal(number, conversion, precision < 0 ? 6 : precision, alternate);
        }

        if (!body.startsWith("-")) {
            if (plus) {
                body = "+" + body;
            } else if (space) {
                body = " " + body;
            }
        }
        if (body.length() < width) {
            int padding = width - body.length();
            if (leftAlign) {
                body = body + " ".repeat(padding);
            } else if (zero && !nonFinite) {
                if (body.startsWith("-") || body.startsWith("+") || body.startsWith(" ")) {
                    body = body.charAt(0) + "0".repeat(padding) + body.substring(1);
                } else {
                    body = "0".repeat(padding) + body;
                }
            } else {
                body = " ".repeat(padding) + body;
            }
        }
        return body.getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * C {@code printf} 的 {@code %f/%e} 使用二进制 double 精确值并四舍五入到偶数；
     * Java 的 {@code Formatter} 使用 HALF_UP，且 {@code %e} 的零填充/进位规则不同。
     */
    private static String formatFiniteDecimal(double number, int conversion, int precision, boolean alternate) {
        boolean negative = number < 0 || (number == 0.0 && 1.0 / number < 0);
        BigDecimal absolute = new BigDecimal(Math.abs(number));
        String sign = negative ? "-" : "";
        if (conversion == 'f') {
            String digits = absolute.setScale(precision, RoundingMode.HALF_EVEN).toPlainString();
            if (alternate && precision == 0) {
                digits += ".";
            }
            return sign + digits;
        }

        int exponent;
        BigDecimal mantissa;
        if (absolute.signum() == 0) {
            exponent = 0;
            mantissa = absolute;
        } else {
            exponent = absolute.precision() - absolute.scale() - 1;
            mantissa = absolute.setScale(precision - exponent, RoundingMode.HALF_EVEN);
            if (mantissa.unscaledValue().abs().toString().length() > precision + 1) {
                exponent++;
                mantissa = absolute.setScale(precision - exponent, RoundingMode.HALF_EVEN);
            }
        }
        String digits = mantissa.unscaledValue().abs().toString();
        if (digits.length() < precision + 1) {
            digits = "0".repeat(precision + 1 - digits.length()) + digits;
        }
        StringBuilder result = new StringBuilder(sign);
        result.append(digits.charAt(0));
        if (precision > 0) {
            result.append('.').append(digits, 1, digits.length());
        } else if (alternate) {
            result.append('.');
        }
        result.append(conversion == 'E' ? 'E' : 'e');
        result.append(exponent < 0 ? '-' : '+');
        int absoluteExponent = Math.abs(exponent);
        if (absoluteExponent < 10) {
            result.append('0');
        }
        result.append(absoluteExponent);
        return result.toString();
    }

    /** C 的 {@code %p}：引用类型输出地址样式的十六进制，标量输出 {@code (null)}。 */
    private static String formatPointer(String directive, Object value) {
        String text;
        if (LuaStringSupport.isNil(value) || value instanceof Boolean || value instanceof LuaBoolean
                || value instanceof Long || value instanceof Double || value instanceof LuaNumber) {
            text = "(null)";
        } else {
            text = String.format(Locale.ROOT, "%016x", System.identityHashCode(value));
        }
        return applyStringWidth(directive, text);
    }

    /** Lua 的 {@code luaL_tolstring} 行为：{@code %s} 对 nil/boolean/数字也输出文本。 */
    private static byte[] toLuaStringBytes(Object value) {
        if (LuaStringSupport.isNil(value)) {
            return "nil".getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Boolean booleanValue) {
            return (booleanValue ? "true" : "false").getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof LuaBoolean booleanValue) {
            return (booleanValue.getValue() ? "true" : "false").getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof TruffleString || value instanceof LuaString || value instanceof String
                || value instanceof Long || value instanceof Double || value instanceof LuaNumber) {
            return LuaStringSupport.toBytes(value, 2, "format");
        }
        TruffleString text = com.zhhz.truffle.lua.nodes.util.LuaCoerceToStringNodeGen.create().execute(value);
        return LuaStringSupport.toBytes(text, 2, "format");
    }

    private static boolean isIntegerConversion(int conversion) {
        return conversion == 'd' || conversion == 'i' || conversion == 'o'
                || conversion == 'x' || conversion == 'X' || conversion == 'u';
    }

    /**
     * C 的 printf 允许对整数使用精度（最小位数），而 Java 的 {@link String.Format} 不支持。
     * 这里复刻 C 语义：精度对数字部分零填充（按位无符号解释 o/x/X/u），随后按宽度补空格；
     * 指定精度时忽略 {@code '0'} 标志。
     */
    private static byte[] formatIntegerPrecision(String directive, int conversion, Object value, int argument) {
        int i = 1;
        boolean leftAlign = false;
        boolean plus = false;
        boolean space = false;
        boolean alternate = false;
        while (i < directive.length()) {
            char c = directive.charAt(i);
            if (c == '-') leftAlign = true;
            else if (c == '+') plus = true;
            else if (c == ' ') space = true;
            else if (c == '#') alternate = true;
            else if (c == '0') { /* 精度存在时忽略 */ }
            else break;
            i++;
        }
        int width = 0;
        while (i < directive.length() && Character.isDigit(directive.charAt(i))) {
            width = width * 10 + (directive.charAt(i) - '0');
            i++;
        }
        int precision = 0;
        if (i < directive.length() && directive.charAt(i) == '.') {
            i++;
            while (i < directive.length() && Character.isDigit(directive.charAt(i))) {
                precision = precision * 10 + (directive.charAt(i) - '0');
                i++;
            }
        }

        long number = toLong(value, argument, "format");
        boolean negative = false;
        String digits;
        String prefix = "";
        switch (conversion) {
            case 'd', 'i' -> {
                String raw = Long.toString(number);
                if (raw.startsWith("-")) {
                    negative = true;
                    raw = raw.substring(1);
                }
                digits = raw;
            }
            case 'o' -> digits = Long.toOctalString(number);
            case 'x' -> digits = Long.toHexString(number);
            case 'X' -> digits = Long.toHexString(number).toUpperCase(Locale.ROOT);
            default -> digits = Long.toUnsignedString(number);
        }

        if (precision == 0 && digits.equals("0")) {
            digits = "";
        } else if (digits.length() < precision) {
            digits = "0".repeat(precision - digits.length()) + digits;
        }

        if (alternate) {
            switch (conversion) {
                case 'o' -> {
                    if (!digits.isEmpty() && !digits.startsWith("0")) {
                        prefix = "0";
                    }
                }
                case 'x' -> {
                    if (number != 0) {
                        prefix = "0x";
                    }
                }
                case 'X' -> {
                    if (number != 0) {
                        prefix = "0X";
                    }
                }
                default -> {
                }
            }
        }

        String sign = negative ? "-" : plus ? "+" : space ? " " : "";
        String body = sign + prefix + digits;
        if (body.length() < width) {
            String pad = " ".repeat(width - body.length());
            body = leftAlign ? body + pad : pad + body;
        }
        return body.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Java {@code Formatter} 拒绝重复 flags 以及 {@code '-'} 与 {@code '0'}、
     * {@code '+'} 与 {@code ' '} 的组合；C 允许并忽略冲突项。先规范化再交给 Java。
     */
    private static String normalizeJavaDirective(String directive) {
        int index = 1;
        boolean left = false;
        boolean plus = false;
        boolean space = false;
        boolean alternate = false;
        boolean zero = false;
        while (index < directive.length()) {
            char flag = directive.charAt(index);
            if (flag == '-') {
                left = true;
            } else if (flag == '+') {
                plus = true;
            } else if (flag == ' ') {
                space = true;
            } else if (flag == '#') {
                alternate = true;
            } else if (flag == '0') {
                zero = true;
            } else {
                break;
            }
            index++;
        }
        int width = 0;
        int widthIndex = index;
        while (widthIndex < directive.length() && isDigit(directive.charAt(widthIndex))) {
            width = width * 10 + directive.charAt(widthIndex) - '0';
            widthIndex++;
        }
        if (width == 0 || left) {
            zero = false;
        }
        if (width == 0) {
            left = false;
        }
        if (plus) {
            space = false;
        }
        StringBuilder result = new StringBuilder(directive.length());
        result.append('%');
        if (left) {
            result.append('-');
        }
        if (plus) {
            result.append('+');
        } else if (space) {
            result.append(' ');
        }
        if (alternate) {
            result.append('#');
        }
        if (zero) {
            result.append('0');
        }
        result.append(directive, index, directive.length());
        return result.toString();
    }

    private static String applyStringWidth(String directive, String value) {
        int index = 1;
        boolean left = false;
        boolean zero = false;
        while (index < directive.length() && isFormatFlag(directive.charAt(index))) {
            if (directive.charAt(index) == '-') {
                left = true;
            } else if (directive.charAt(index) == '0') {
                zero = true;
            }
            index++;
        }
        int width = 0;
        while (index < directive.length() && isDigit(directive.charAt(index))) {
            width = width * 10 + directive.charAt(index) - '0';
            index++;
        }
        if (value.length() >= width) {
            return value;
        }
        String padding = String.valueOf(zero ? '0' : ' ').repeat(width - value.length());
        return left ? value + padding : padding + value;
    }

    private static byte[] quote(Object value, int argument) {
        if (LuaStringSupport.isNil(value)) {
            return "nil".getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Boolean booleanValue) {
            return Boolean.toString(booleanValue).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof LuaBoolean booleanValue) {
            return Boolean.toString(booleanValue.getValue()).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Double number) {
            return LuaNumberFormat.formatQ(number).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof LuaNumber number) {
            return LuaNumberFormat.formatQ(number.getValue()).getBytes(StandardCharsets.US_ASCII);
        }
        if (value instanceof Long || value instanceof Integer) {
            return LuaStringSupport.toBytes(value, argument, "format");
        }
        if (!(value instanceof TruffleString || value instanceof LuaString || value instanceof String)) {
            throw LuaStringSupport.argumentError("format", argument, "value has no literal form");
        }
        byte[] bytes = LuaStringSupport.toBytes(value, argument, "format");
        ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length + 2);
        output.write('"');
        for (int i = 0; i < bytes.length; i++) {
            int current = bytes[i] & 0xff;
            switch (current) {
                // 标准 Lua addquoted：'"'、'\'、'\n' 输出 \+原字节（词法器 \<LF> 转义可还原）；
                // 其余控制字符（含 \0 与 \r）按"下一字节是否为数字"选择最短 \d 或
                // 3 位补零 \ddd 形式，避免转义数字与后续数字粘连产生解析歧义
                case '"', '\\', '\n' -> {
                    output.write('\\');
                    output.write(current);
                }
                default -> {
                    if (current < 32 || current == 127) {
                        boolean nextIsDigit = i + 1 < bytes.length
                                && (bytes[i + 1] & 0xff) >= '0' && (bytes[i + 1] & 0xff) <= '9';
                        String escape = nextIsDigit
                                ? String.format(Locale.ROOT, "\\%03d", current)
                                : String.format(Locale.ROOT, "\\%d", current);
                        output.writeBytes(escape.getBytes(StandardCharsets.US_ASCII));
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
        Object number = luaNumber(value);
        if (number instanceof Long integer) {
            return integer;
        }
        if (number instanceof Double floating) {
            if (Double.isFinite(floating) && floating == Math.rint(floating)
                    && floating >= -0x1p63 && floating < 0x1p63) {
                return floating.longValue();
            }
            throw LuaStringSupport.argumentError(function, argument, "number has no integer representation");
        }
        if (isString(value)) {
            throw LuaStringSupport.argumentError(function, argument, "number expected, got string");
        }
        throw LuaStringSupport.argumentError(function, argument, "number expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    private static double toDouble(Object value, int argument, String function) {
        Object number = luaNumber(value);
        if (number instanceof Number numeric) {
            return numeric.doubleValue();
        }
        if (isString(value)) {
            throw LuaStringSupport.argumentError(function, argument, "number expected, got string");
        }
        throw LuaStringSupport.argumentError(function, argument, "number expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    private static Object luaNumber(Object value) {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).longValue();
        }
        if (value instanceof Double || value instanceof Float) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof LuaNumber number) {
            return number.getValue();
        }
        if (value instanceof TruffleString string) {
            return com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode.parseLuaNumber(
                    string.toJavaStringUncached());
        }
        if (value instanceof LuaString string) {
            return com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode.parseLuaNumber(
                    string.getValue().toJavaStringUncached());
        }
        return null;
    }

    private static boolean isString(Object value) {
        return value instanceof TruffleString || value instanceof LuaString || value instanceof String;
    }

    private static boolean isSupportedConversion(int conversion) {
        return conversion == 'c' || conversion == 'd' || conversion == 'i' || conversion == 'o'
                || conversion == 'u' || conversion == 'x' || conversion == 'X' || conversion == 'e'
                || conversion == 'E' || conversion == 'f' || conversion == 'g' || conversion == 'G'
                || conversion == 'p' || conversion == 's';
    }

    /**
     * 复刻 lstrlib.c 的 {@code checkformat}：每种转换只接受固定 flags，宽度和精度最多两位数字，
     * 且精度只在该转换支持时才能出现。
     */
    private static void validateFormat(String directive, int conversion) {
        String flags;
        boolean precisionAllowed;
        switch (conversion) {
            case 'd', 'i' -> {
                flags = "-+0 ";
                precisionAllowed = true;
            }
            case 'u' -> {
                flags = "-0";
                precisionAllowed = true;
            }
            case 'o', 'x', 'X' -> {
                flags = "-#0";
                precisionAllowed = true;
            }
            case 'c', 'p' -> {
                flags = "-";
                precisionAllowed = false;
            }
            case 's' -> {
                if (directive.length() == 2) {
                    return;
                }
                flags = "-";
                precisionAllowed = true;
            }
            default -> {
                flags = "-+#0 ";
                precisionAllowed = true;
            }
        }
        int index = 1;
        while (index < directive.length() && flags.indexOf(directive.charAt(index)) >= 0) {
            index++;
        }
        if (index < directive.length() && directive.charAt(index) != '0') {
            int widthDigits = 0;
            while (index < directive.length() && isDigit(directive.charAt(index)) && widthDigits < 2) {
                index++;
                widthDigits++;
            }
            if (index < directive.length() && directive.charAt(index) == '.' && precisionAllowed) {
                index++;
                int precisionDigits = 0;
                while (index < directive.length() && isDigit(directive.charAt(index)) && precisionDigits < 2) {
                    index++;
                    precisionDigits++;
                }
            }
        }
        if (index >= directive.length() || !isAsciiLetter(directive.charAt(index))) {
            throw LuaException.create("invalid conversion specification: '" + directive + "'", null);
        }
    }

    private static boolean isAsciiLetter(char value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z';
    }
}

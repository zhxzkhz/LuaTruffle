package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary format implementation for string.pack/string.packsize/string.unpack,
 * 严格对照 Lua 5.3.0 的 lstrlib.c（str_pack/str_packsize/str_unpack）。
 */
final class StringBinaryLibrary {

    private StringBinaryLibrary() {
    }

    /** 二进制整数表示的最大字节数（标准 Lua 的 MAXINTSIZE） */
    private static final int MAXINTSIZE = 16;
    /** 默认最大对齐（标准 Lua 的 MAXALIGN，64 位平台为 8） */
    private static final int MAXALIGN = 8;
    /** 一个 Lua 整数（lua_Integer / lua_Number）的字节数（SZINT） */
    private static final int SZINT = 8;
    /** 字符串/表长度上限（标准 Lua 的 MAXSIZE，64 位平台取 INT_MAX） */
    private static final long MAXSIZE = Integer.MAX_VALUE;
    /** 原生端序 */
    private static final boolean NATIVE_LITTLE = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    @TruffleBoundary
    static TruffleString pack(Object[] arguments) {
        String format = format(arguments, 0, "pack");
        List<Item> items = parse(format, "pack");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        State state = new State();
        int valueIndex = 1;
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            int padding = alignment(output.size(), item, state, "pack");
            for (int i = 0; i < padding; i++) {
                output.write(0);
            }
            switch (item.kind) {
                case 'x' -> output.write(0);
                case 'X' -> { }
                case 'b', 'B', 'h', 'H', 'l', 'L', 'j', 'J', 'T', 'i', 'I' -> {
                    int arg = valueIndex + 1;
                    long value = LuaStringSupport.ensureInteger(arguments, valueIndex++, "pack");
                    boolean signed = isSigned(item.kind);
                    checkPackRange(value, item.size, signed, arg);
                    writeInteger(output, value, item.size, state.little, signed && value < 0);
                }
                case 'f', 'd', 'n' -> {
                    writeFloating(output, numberDouble(arguments, valueIndex++, "pack"), item.size, state.little);
                }
                case 'c' -> {
                    int arg = valueIndex + 1;
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    if (bytes.length != item.size) {
                        throw argError(arg, "pack", "wrong length");
                    }
                    output.writeBytes(bytes);
                }
                case 'z' -> {
                    int arg = valueIndex + 1;
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    for (byte value : bytes) {
                        if (value == 0) {
                            throw argError(arg, "pack", "string contains zeros");
                        }
                    }
                    output.writeBytes(bytes);
                    output.write(0);
                }
                case 's' -> {
                    int arg = valueIndex + 1;
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    if (item.size < SZINT && Long.compareUnsigned(bytes.length, 1L << (item.size * 8)) >= 0) {
                        throw argError(arg, "pack", "string length does not fit in given size");
                    }
                    writeInteger(output, bytes.length, item.size, state.little, false);
                    output.writeBytes(bytes);
                }
                default -> throw LuaException.create("invalid format option", null);
            }
        }
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @TruffleBoundary
    static long packsize(Object[] arguments) {
        List<Item> items = parse(format(arguments, 0, "packsize"), "packsize");
        State state = new State();
        long total = 0;
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            if (item.kind == 'z' || item.kind == 's') {
                throw argError(1, "packsize", "variable-length format");
            }
            long size = (item.kind == 'X' ? 0 : item.size) + alignment(total, item, state, "packsize");
            if (total > MAXSIZE - size) {
                throw argError(1, "packsize", "format result too large");
            }
            total += size;
        }
        return total;
    }

    @TruffleBoundary
    static LuaMultiValue unpack(Object[] arguments) {
        String format = format(arguments, 0, "unpack");
        byte[] source = LuaStringSupport.ensureBytes(arguments, 1, "unpack");
        long requested = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.ensureInteger(arguments, 2, "unpack") : 1;
        long relative = posrelat(requested, source.length);
        if (relative < 1 || relative - 1 > source.length) {
            throw argError(3, "unpack", "initial position out of string");
        }
        List<Item> items = parse(format, "unpack");
        State state = new State();
        int offset = (int) (relative - 1);
        List<Object> values = new ArrayList<>();
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            int padding = alignment(offset, item, state, "unpack");
            int dataSize = item.kind == 'X' ? 0 : item.size;
            if ((long) offset + padding + dataSize > source.length) {
                throw argError(2, "unpack", "data string too short");
            }
            offset += padding;
            switch (item.kind) {
                case 'x', 'X' -> { }
                case 'b', 'h', 'l', 'j', 'i' -> values.add(readInteger(source, offset, item.size, state.little, true));
                case 'B', 'H', 'L', 'J', 'T', 'I' -> values.add(readInteger(source, offset, item.size, state.little, false));
                case 'f', 'd', 'n' -> values.add(readFloating(source, offset, item.size, state.little));
                case 'c' -> values.add(LuaStringSupport.fromBytes(source, offset, item.size));
                case 's' -> {
                    long length = readInteger(source, offset, item.size, state.little, false);
                    int dataOffset = offset + item.size;
                    if (length < 0 || dataOffset + length > source.length) {
                        throw argError(2, "unpack", "data string too short");
                    }
                    values.add(LuaStringSupport.fromBytes(source, dataOffset, (int) length));
                    offset += (int) length;
                }
                case 'z' -> {
                    int length = zeroTerminatedLength(source, offset);
                    values.add(LuaStringSupport.fromBytes(source, offset, length));
                    offset += length + 1;
                }
                default -> throw LuaException.create("invalid format option", null);
            }
            offset += dataSize;
        }
        values.add((long) offset + 1);
        return new LuaMultiValue(values.toArray());
    }

    private static String format(Object[] arguments, int index, String function) {
        byte[] bytes = LuaStringSupport.ensureBytes(arguments, index, function);
        return bytes.length == 0 ? "" : new String(bytes, StandardCharsets.ISO_8859_1);
    }

    /** 解析格式串为选项列表；config 选项（'<','>','=','!'）也作为 Item 保留。 */
    private static List<Item> parse(String format, String function) {
        List<Item> items = new ArrayList<>();
        int position = 0;
        while (position < format.length()) {
            char option = format.charAt(position++);
            if (option == ' ') {
                continue;
            }
            switch (option) {
                case '<', '>', '=' -> items.add(new Item(option, 0));
                case '!' -> {
                    int[] result = numberSuffix(format, position, MAXALIGN);
                    position = result[1];
                    checkLimit(result[0], function);
                    items.add(new Item('!', result[0]));
                }
                case 'X' -> {
                    if (position >= format.length()) {
                        throw argError(1, function, "invalid next option for option 'X'");
                    }
                    char next = format.charAt(position);
                    int[] result = optionSize(next, format, position + 1, function);
                    position = result[1];
                    if (next == 'c' || result[0] == 0) {
                        throw argError(1, function, "invalid next option for option 'X'");
                    }
                    items.add(new Item('X', result[0]));
                }
                default -> {
                    int[] result = optionSize(option, format, position, function);
                    position = result[1];
                    items.add(new Item(option, result[0]));
                }
            }
        }
        return items;
    }

    /** 解析单个选项的尺寸，返回 {size, 新位置}。 */
    private static int[] optionSize(char option, String format, int position, String function) {
        switch (option) {
            case 'b', 'B' -> { return new int[]{1, position}; }
            case 'h', 'H' -> { return new int[]{2, position}; }
            case 'l', 'L', 'j', 'J', 'T' -> { return new int[]{8, position}; }
            case 'f' -> { return new int[]{4, position}; }
            case 'd', 'n' -> { return new int[]{8, position}; }
            case 'i', 'I' -> {
                int[] result = numberSuffix(format, position, 4);
                checkLimit(result[0], function);
                return result;
            }
            case 's' -> {
                int[] result = numberSuffix(format, position, 8);
                checkLimit(result[0], function);
                return result;
            }
            case 'c' -> {
                int[] result = numberSuffix(format, position, -1);
                if (result[0] < 0) {
                    throw argError(1, function, "missing size for format option 'c'");
                }
                return result;
            }
            case 'z' -> { return new int[]{0, position}; }
            case 'x' -> { return new int[]{1, position}; }
            case 'X', ' ' -> { return new int[]{0, position}; }
            case '<', '>', '=' -> { return new int[]{0, position}; }
            case '!' -> { return new int[]{0, numberSuffix(format, position, MAXALIGN)[1]}; }
            default -> throw argError(1, function, "invalid format option '" + option + "'");
        }
    }

    /**
     * 读取可选十进制数字，语义等价标准 Lua 的 getnum：一旦累计值超过
     * (MAXSIZE-9)/10 便停止（余下数字留给后续选项解析），默认值 df。
     */
    private static int[] numberSuffix(String format, int position, int defaultValue) {
        if (position >= format.length() || !isDigit(format.charAt(position))) {
            return new int[]{defaultValue, position};
        }
        long value = 0;
        do {
            value = value * 10 + (format.charAt(position++) - '0');
        } while (position < format.length() && isDigit(format.charAt(position))
                && value <= (MAXSIZE - 9) / 10);
        return new int[]{(int) value, position};
    }

    private static void checkLimit(int size, String function) {
        if (size > MAXINTSIZE || size <= 0) {
            throw argError(1, function, "integral size (" + size + ") out of limits [1," + MAXINTSIZE + "]");
        }
    }

    private static int alignment(long total, Item item, State state, String function) {
        if (item.kind == 'c') {
            return 0;
        }
        int align = item.size;
        if (align <= 1) {
            return 0;
        }
        if (align > state.maxAlign) {
            align = state.maxAlign;
        }
        if ((align & (align - 1)) != 0) {
            throw argError(1, function, "format asks for alignment not power of 2");
        }
        return (align - (int) (total & (align - 1))) & (align - 1);
    }

    private static boolean applyConfiguration(Item item, State state) {
        switch (item.kind) {
            case '<' -> { state.little = true; return true; }
            case '>' -> { state.little = false; return true; }
            case '=' -> { state.little = NATIVE_LITTLE; return true; }
            case '!' -> { state.maxAlign = item.size; return true; }
            default -> { return false; }
        }
    }

    private static double numberDouble(Object[] arguments, int index, String function) {
        if (index >= arguments.length) {
            throw LuaException.create("bad argument #" + (index + 1) + " to '" + function
                    + "' (number expected, got no value)", null);
        }
        Object value = arguments[index];
        if (value instanceof Long number) return number.doubleValue();
        if (value instanceof Double number) return number;
        throw LuaException.create("bad argument #" + (index + 1) + " to '" + function
                + "' (number expected)", null);
    }

    private static boolean isSigned(char kind) {
        return kind == 'b' || kind == 'h' || kind == 'l' || kind == 'j' || kind == 'i';
    }

    private static void checkPackRange(long value, int size, boolean signed, int arg) {
        if (size >= SZINT) {
            return;
        }
        if (signed) {
            long limit = 1L << (size * 8 - 1);
            if (!(-limit <= value && value < limit)) {
                throw argError(arg, "pack", "integer overflow");
            }
        } else if (Long.compareUnsigned(value, 1L << (size * 8)) >= 0) {
            throw argError(arg, "pack", "unsigned overflow");
        }
    }

    /**
     * 标准 Lua packint：按 islittle 写出 value 的低 size 字节；当 size 大于
     * 一个 Lua 整数（8 字节）且值为负时，多出的高位字节填 0xFF（符号扩展）。
     */
    private static void writeInteger(ByteArrayOutputStream output, long value, int size, boolean little, boolean neg) {
        byte[] buffer = new byte[size];
        for (int i = 0; i < size; i++) {
            long n = i < SZINT ? (value >>> (i * 8)) : (neg ? 0xFFL : 0L);
            buffer[little ? i : size - 1 - i] = (byte) (n & 0xff);
        }
        output.writeBytes(buffer);
    }

    /** 标准 Lua unpackint：读 size 字节；小于 8 字节按需符号扩展，大于 8 字节校验多余字节。 */
    private static long readInteger(byte[] source, int offset, int size, boolean little, boolean signed) {
        int limit = Math.min(size, SZINT);
        long result = 0;
        for (int i = limit - 1; i >= 0; i--) {
            result <<= 8;
            result |= source[offset + (little ? i : size - 1 - i)] & 0xffL;
        }
        if (size < SZINT) {
            if (signed) {
                long mask = 1L << (size * 8 - 1);
                result = (result ^ mask) - mask;
            }
        } else if (size > SZINT) {
            int mask = (!signed || result >= 0) ? 0 : 0xFF;
            for (int i = limit; i < size; i++) {
                if ((source[offset + (little ? i : size - 1 - i)] & 0xff) != mask) {
                    throw LuaException.create(size + "-byte integer does not fit into Lua Integer", null);
                }
            }
        }
        return result;
    }

    private static void writeFloating(ByteArrayOutputStream output, double value, int size, boolean little) {
        ByteBuffer buffer = ByteBuffer.allocate(size).order(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        if (size == 4) buffer.putFloat((float) value);
        else buffer.putDouble(value);
        output.writeBytes(buffer.array());
    }

    private static double readFloating(byte[] source, int offset, int size, boolean little) {
        ByteBuffer buffer = ByteBuffer.wrap(source, offset, size)
                .order(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return size == 4 ? buffer.getFloat() : buffer.getDouble();
    }

    private static int zeroTerminatedLength(byte[] source, int offset) {
        for (int i = offset; i < source.length; i++) {
            if (source[i] == 0) {
                return i - offset;
            }
        }
        // Lua 字符串内部以 '\0' 结尾，故隐式终止符位于数据末尾
        return source.length - offset;
    }

    /** 标准 Lua posrelat：负数位置从末尾计算，越界返回 0。 */
    private static long posrelat(long position, int length) {
        if (position >= 0) {
            return position;
        }
        if (position < -(long) length) {
            return 0;
        }
        return (long) length + position + 1;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static com.oracle.truffle.api.exception.AbstractTruffleException argError(
            int argument, String function, String message) {
        return LuaException.create("bad argument #" + argument + " to '" + function + "' (" + message + ")", null);
    }

    private record Item(char kind, int size) {
    }

    private static final class State {
        private boolean little = NATIVE_LITTLE;
        private int maxAlign = 1;
    }
}

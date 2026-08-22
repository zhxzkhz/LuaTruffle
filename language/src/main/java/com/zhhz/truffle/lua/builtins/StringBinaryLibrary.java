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

/** Binary format implementation for string.pack/string.packsize/string.unpack. */
final class StringBinaryLibrary {

    private StringBinaryLibrary() {
    }

    @LuaBuiltin(name = "pack")
    @TruffleBoundary
    static TruffleString pack(Object[] arguments) {
        String format = format(arguments, 0);
        List<Item> items = parse(format);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        State state = new State();
        int valueIndex = 1;
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            align(output.size(), item, state, output);
            switch (item.kind) {
                case 'x' -> output.write(0);
                case 'X' -> { }
                case 'b', 'B', 'h', 'H', 'l', 'L', 'j', 'J', 'T', 'i', 'I' -> {
                    long value = number(arguments, valueIndex++, "pack");
                    checkIntegerRange(value, item, item.kind == 'b' || item.kind == 'h' || item.kind == 'l'
                            || item.kind == 'j' || item.kind == 'i');
                    writeInteger(output, value, item.size, state.little);
                }
                case 'f', 'd', 'n' -> writeFloating(output, numberDouble(arguments, valueIndex++, "pack"), item, state.little);
                case 'c' -> {
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    if (bytes.length != item.size) {
                        throw LuaException.create("bad argument to 'pack' (string length does not match format)", null);
                    }
                    output.writeBytes(bytes);
                }
                case 'z' -> {
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    for (byte value : bytes) {
                        if (value == 0) {
                            throw LuaException.create("bad argument to 'pack' (string contains zeros)", null);
                        }
                    }
                    output.writeBytes(bytes);
                    output.write(0);
                }
                case 's' -> {
                    byte[] bytes = LuaStringSupport.ensureBytes(arguments, valueIndex++, "pack");
                    writeInteger(output, bytes.length, item.size, state.little);
                    output.writeBytes(bytes);
                }
                default -> throw LuaException.create("invalid format option", null);
            }
        }
        return LuaStringSupport.fromBytes(output.toByteArray());
    }

    @LuaBuiltin(name = "packsize")
    @TruffleBoundary
    static long packsize(Object[] arguments) {
        List<Item> items = parse(format(arguments, 0));
        State state = new State();
        int total = 0;
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            if (item.kind == 'z' || item.kind == 's') {
                throw LuaException.create("variable-length format in 'packsize'", null);
            }
            total += alignment(total, item, state);
            total += item.size;
        }
        return total;
    }

    @LuaBuiltin(name = "unpack")
    @TruffleBoundary
    static LuaMultiValue unpack(Object[] arguments) {
        String format = format(arguments, 0);
        byte[] source = LuaStringSupport.ensureBytes(arguments, 1, "unpack");
        long position = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.ensureInteger(arguments, 2, "unpack") : 1;
        if (position < 1 || position > (long) source.length + 1) {
            throw LuaException.create("initial position out of string", null);
        }
        List<Item> items = parse(format);
        State state = new State();
        int offset = (int) position - 1;
        List<Object> values = new ArrayList<>();
        for (Item item : items) {
            if (applyConfiguration(item, state)) {
                continue;
            }
            int padding = alignment(offset, item, state);
            offset += padding;
            int required = item.kind == 'X' ? 0 : item.kind == 'z' ? 1 : item.size;
            if (offset + required > source.length) {
                throw LuaException.create("data string too short", null);
            }
            switch (item.kind) {
                case 'x' -> offset += 1;
                case 'X' -> { }
                case 'b', 'h', 'l', 'j', 'i' -> {
                    values.add(readInteger(source, offset, item.size, state.little, true));
                    offset += item.size;
                }
                case 'B', 'H', 'L', 'J', 'T', 'I' -> {
                    values.add(readInteger(source, offset, item.size, state.little, false));
                    offset += item.size;
                }
                case 'f', 'd', 'n' -> {
                    values.add(readFloating(source, offset, item, state.little));
                    offset += item.size;
                }
                case 'c', 'z', 's' -> {
                    int length = item.kind == 'c' ? item.size : item.kind == 'z'
                            ? zeroTerminatedLength(source, offset) : checkedLength(source, offset, item.size, state.little);
                    int dataOffset = item.kind == 's' ? offset + item.size : offset;
                    if (dataOffset + length > source.length) {
                        throw LuaException.create("data string too short", null);
                    }
                    values.add(LuaStringSupport.fromBytes(source, dataOffset, length));
                    offset = item.kind == 'c' ? offset + item.size
                            : item.kind == 'z' ? dataOffset + length + 1 : dataOffset + length;
                }
                default -> throw LuaException.create("invalid format option", null);
            }
        }
        values.add((long) offset + 1);
        return new LuaMultiValue(values.toArray());
    }

    private static String format(Object[] arguments, int index) {
        return LuaStringSupport.ensureBytes(arguments, index, "pack").length == 0
                ? "" : new String(LuaStringSupport.ensureBytes(arguments, index, "pack"), StandardCharsets.ISO_8859_1);
    }

    private static List<Item> parse(String format) {
        List<Item> items = new ArrayList<>();
        for (int position = 0; position < format.length(); position++) {
            char option = format.charAt(position);
            if (option == ' ') {
                continue;
            }
            if (option == '<' || option == '>' || option == '=') {
                items.add(new Item(option, 0));
                continue;
            }
            if (option == '!') {
                int[] result = numberSuffix(format, position + 1, 1);
                int maxAlign = result[0];
                position = result[1] - 1;
                if (maxAlign <= 0 || (maxAlign & (maxAlign - 1)) != 0) {
                    throw LuaException.create("alignment not power of 2", null);
                }
                items.add(new Item('!', maxAlign));
                continue;
            }
            if (option == 'X') {
                if (position + 1 >= format.length()) {
                    throw LuaException.create("invalid next option for option 'X'", null);
                }
                char next = format.charAt(++position);
                int defaultSize = defaultSize(next);
                int[] result = numberSuffix(format, position + 1, defaultSize);
                position = result[1] - 1;
                if (defaultSize == 0 || next == 'c') {
                    throw LuaException.create("invalid next option for option 'X'", null);
                }
                items.add(new Item('X', result[0]));
                continue;
            }
            int defaultSize = defaultSize(option);
            if (defaultSize == 0 && option != 'c') {
                throw LuaException.create("invalid format option '" + option + "'", null);
            }
            int[] result = numberSuffix(format, position + 1, defaultSize);
            int size = result[0];
            position = result[1] - 1;
            if (option == 'c' && size <= 0) {
                throw LuaException.create("missing size for format option 'c'", null);
            }
            items.add(new Item(option, size));
        }
        return items;
    }

    private static int[] numberSuffix(String format, int position, int defaultValue) {
        if (position >= format.length() || !Character.isDigit(format.charAt(position))) {
            return new int[]{defaultValue, position};
        }
        int value = 0;
        while (position < format.length() && Character.isDigit(format.charAt(position))) {
            value = Math.addExact(Math.multiplyExact(value, 10), format.charAt(position++) - '0');
        }
        return new int[]{value, position};
    }

    private static int defaultSize(char option) {
        return switch (option) {
            case 'b', 'B' -> 1;
            case 'h', 'H' -> 2;
            case 'l', 'L', 'j', 'J', 'T', 'n', 'd' -> 8;
            case 'i', 'I' -> 4;
            case 'f' -> 4;
            case 's' -> 8;
            case 'z', 'x' -> 1;
            default -> 0;
        };
    }

    private static void align(int offset, Item item, State state, ByteArrayOutputStream output) {
        int padding = alignment(offset, item, state);
        for (int i = 0; i < padding; i++) output.write(0);
    }

    private static int alignment(int offset, Item item, State state) {
        if (item.kind == 'c' || item.kind == 'x' || item.size <= 1) return 0;
        int alignment = Math.min(item.size, state.maxAlign);
        if ((alignment & (alignment - 1)) != 0) {
            throw LuaException.create("format asks for alignment not power of 2", null);
        }
        return (alignment - (offset & (alignment - 1))) & (alignment - 1);
    }

    private static boolean applyConfiguration(Item item, State state) {
        switch (item.kind) {
            case '<' -> state.little = true;
            case '>' -> state.little = false;
            case '=' -> state.little = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
            case '!' -> state.maxAlign = item.size;
            default -> { return false; }
        }
        return true;
    }

    private static long number(Object[] arguments, int index, String function) {
        return LuaStringSupport.ensureInteger(arguments, index, function);
    }

    private static double numberDouble(Object[] arguments, int index, String function) {
        if (index >= arguments.length) {
            throw LuaException.create("bad argument to '" + function + "' (number expected)", null);
        }
        Object value = arguments[index];
        if (value instanceof Long number) return number.doubleValue();
        if (value instanceof Double number) return number;
        throw LuaException.create("bad argument to '" + function + "' (number expected)", null);
    }

    private static void checkIntegerRange(long value, Item item, boolean signed) {
        if (item.size >= 8) return;
        int bits = item.size * 8;
        long min = signed ? -(1L << (bits - 1)) : 0;
        long max = signed ? (1L << (bits - 1)) - 1 : (1L << bits) - 1;
        if (value < min || value > max) {
            throw LuaException.create("integer overflow", null);
        }
    }

    private static void writeInteger(ByteArrayOutputStream output, long value, int size, boolean little) {
        for (int i = 0; i < size; i++) {
            int shift = little ? i : size - i - 1;
            output.write((int) (value >>> (shift * 8)) & 0xff);
        }
    }

    private static long readInteger(byte[] source, int offset, int size, boolean little, boolean signed) {
        long value = 0;
        int count = Math.min(size, 8);
        for (int i = 0; i < count; i++) {
            int shift = little ? i : count - i - 1;
            value |= (long) (source[offset + i] & 0xff) << (shift * 8);
        }
        if (signed && size < 8 && (value & (1L << (size * 8 - 1))) != 0) {
            value |= -1L << (size * 8);
        }
        return value;
    }

    private static void writeFloating(ByteArrayOutputStream output, double value, Item item, boolean little) {
        ByteBuffer buffer = ByteBuffer.allocate(item.size).order(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        if (item.size == 4) buffer.putFloat((float) value);
        else buffer.putDouble(value);
        output.writeBytes(buffer.array());
    }

    private static double readFloating(byte[] source, int offset, Item item, boolean little) {
        ByteBuffer buffer = ByteBuffer.wrap(source, offset, item.size)
                .order(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return item.size == 4 ? buffer.getFloat() : buffer.getDouble();
    }

    private static int checkedLength(byte[] source, int offset, int size, boolean little) {
        long length = readInteger(source, offset, size, little, false);
        if (length > Integer.MAX_VALUE) throw LuaException.create("data string too short", null);
        return (int) length;
    }

    private static int zeroTerminatedLength(byte[] source, int offset) {
        for (int i = offset; i < source.length; i++) {
            if (source[i] == 0) return i - offset;
        }
        throw LuaException.create("unfinished string", null);
    }

    private record Item(char kind, int size) {
    }

    private static final class State {
        private boolean little = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
        private int maxAlign = 1;
    }
}

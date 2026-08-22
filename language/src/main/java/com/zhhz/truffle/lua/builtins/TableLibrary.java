package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaNumber;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import static com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getTypeName;
import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

/**
 * Lua standard library {@code table} functions which only need table storage
 * and do not need an executing AST node or the current Lua context.
 */
public final class TableLibrary {

    private TableLibrary() {
    }

    @LuaBuiltin(name = "concat")
    @TruffleBoundary
    public static TruffleString concat(Object[] args) {
        LuaTable table = ensureTable(args, 0, "concat");
        String separator = args.length > 1 && !isNil(args[1]) ? ensureString(args[1], 2, "concat") : "";
        long start = args.length > 2 && !isNil(args[2]) ? toInteger(args[2], 3, "concat") : 1;
        long end = args.length > 3 && !isNil(args[3]) ? toInteger(args[3], 4, "concat") : table.getArraySize();

        if (start > end) {
            return toTruffleString("");
        }
        long count;
        try {
            count = Math.addExact(Math.subtractExact(end, start), 1);
        } catch (ArithmeticException exception) {
            throw LuaException.create("too many elements to concatenate", null);
        }
        if (count > Integer.MAX_VALUE) {
            throw LuaException.create("too many elements to concatenate", null);
        }

        StringBuilder result = new StringBuilder();
        for (long offset = 0; offset < count; offset++) {
            long index = start + offset;
            if (offset > 0) {
                result.append(separator);
            }
            Object value = table.rawget(index);
            if (value == LuaNil.SINGLETON) {
                throw LuaException.create("invalid value (nil) at index " + index + " in table.concat", null);
            }
            result.append(stringifyConcatValue(value, index));
        }
        return toTruffleString(result.toString());
    }

    @LuaBuiltin(name = "insert")
    @TruffleBoundary
    public static LuaMultiValue insert(Object[] args) {
        LuaTable table = ensureTable(args, 0, "insert");
        if (args.length != 2 && args.length != 3) {
            throw LuaException.create("wrong number of arguments to 'insert'", null);
        }

        int length = table.getArraySize();
        int position;
        Object value;
        if (args.length == 2) {
            position = checkedPosition(length + 1L, 2, "insert");
            value = args[1];
        } else {
            position = toIndex(args[1], 2, "insert");
            if (position < 1 || position > length + 1) {
                throw LuaException.create("bad argument #2 to 'insert' (position out of bounds)", null);
            }
            value = args[2];
        }

        for (int index = length; index >= position; index--) {
            table.arrayRawSet(index + 1, table.arrayRawGet(index));
        }
        table.arrayRawSet(position, value);
        return new LuaMultiValue(new Object[0]);
    }

    @LuaBuiltin(name = "move")
    @TruffleBoundary
    public static LuaTable move(Object[] args) {
        LuaTable source = ensureTable(args, 0, "move");
        if (args.length < 4 || args.length > 5) {
            throw LuaException.create("wrong number of arguments to 'move'", null);
        }

        long from = toInteger(args[1], 2, "move");
        long end = toInteger(args[2], 3, "move");
        long target = toInteger(args[3], 4, "move");
        LuaTable destination = args.length == 5 && !isNil(args[4]) ? ensureTable(args, 4, "move") : source;

        if (end < from) {
            return destination;
        }

        long count;
        try {
            count = Math.addExact(Math.subtractExact(end, from), 1);
            Math.addExact(target, count - 1);
        } catch (ArithmeticException exception) {
            throw LuaException.create("index out of range in table.move", null);
        }
        if (count > Integer.MAX_VALUE || count < 1) {
            throw LuaException.create("index out of range in table.move", null);
        }

        if (source == destination && target > from && target <= end) {
            for (int offset = (int) count - 1; offset >= 0; offset--) {
                destination.rawset(target + offset, source.rawget(from + offset));
            }
        } else {
            for (int offset = 0; offset < (int) count; offset++) {
                destination.rawset(target + offset, source.rawget(from + offset));
            }
        }
        return destination;
    }

    @LuaBuiltin(name = "remove")
    @TruffleBoundary
    public static Object remove(Object[] args) {
        LuaTable table = ensureTable(args, 0, "remove");
        if (args.length > 2) {
            throw LuaException.create("wrong number of arguments to 'remove'", null);
        }

        int length = table.getArraySize();
        long position = args.length == 1 || isNil(args[1]) ? length : toInteger(args[1], 2, "remove");
        if (position != length && (position < 1 || position > (long) length + 1)) {
            throw LuaException.create("bad argument #2 to 'remove' (position out of bounds)", null);
        }

        Object removed = table.rawget(position);
        for (long index = position; index < length; index++) {
            table.rawset(index, table.rawget(index + 1));
        }
        table.rawset(position < length ? length : position, LuaNil.SINGLETON);
        return removed;
    }

    @LuaBuiltin(name = "unpack")
    @TruffleBoundary
    public static LuaMultiValue unpack(Object[] args) {
        LuaTable table = ensureTable(args, 0, "unpack");
        long start = args.length > 1 && !isNil(args[1]) ? toInteger(args[1], 2, "unpack") : 1;
        long end = args.length > 2 && !isNil(args[2]) ? toInteger(args[2], 3, "unpack") : table.getArraySize();
        if (start > end) {
            return new LuaMultiValue(new Object[0]);
        }

        long count;
        try {
            count = Math.addExact(Math.subtractExact(end, start), 1);
        } catch (ArithmeticException exception) {
            throw LuaException.create("too many results to unpack", null);
        }
        if (count > Integer.MAX_VALUE) {
            throw LuaException.create("too many results to unpack", null);
        }
        Object[] values = new Object[(int) count];
        for (int offset = 0; offset < values.length; offset++) {
            values[offset] = table.rawget(start + offset);
        }
        return new LuaMultiValue(values);
    }

    static LuaTable ensureTable(Object[] args, int index, String function) {
        if (index >= args.length || !(args[index] instanceof LuaTable table)) {
            throw LuaException.create("bad argument #" + (index + 1) + " to '" + function + "' (table expected, got "
                    + (index >= args.length ? "no value" : getTypeName(args[index])) + ")", null);
        }
        return table;
    }

    static int toIndex(Object value, int argument, String function) {
        long integer = toInteger(value, argument, function);
        return checkedPosition(integer, argument, function);
    }

    static long toInteger(Object value, int argument, String function) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof Double doubleValue && Double.isFinite(doubleValue) && doubleValue == Math.rint(doubleValue)) {
            if (doubleValue >= Long.MIN_VALUE && doubleValue <= Long.MAX_VALUE) {
                return doubleValue.longValue();
            }
        }
        if (value instanceof TruffleString stringValue) {
            try {
                return Long.parseLong(stringValue.toJavaStringUncached());
            } catch (NumberFormatException ignored) {
                // Fall through to the standard argument error.
            }
        }
        throw LuaException.create("bad argument #" + argument + " to '" + function + "' (integer expected, got "
                + getTypeName(value) + ")", null);
    }

    private static int checkedPosition(long value, int argument, String function) {
        if (value < 1 || value > Integer.MAX_VALUE) {
            throw LuaException.create("bad argument #" + argument + " to '" + function + "' (position out of bounds)", null);
        }
        return (int) value;
    }

    private static boolean isNil(Object value) {
        return value == null || value == LuaNil.SINGLETON;
    }

    private static String ensureString(Object value, int argument, String function) {
        if (value instanceof TruffleString stringValue) {
            return stringValue.toJavaStringUncached();
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Long longValue) {
            return Long.toString(longValue);
        }
        if (value instanceof Double doubleValue) {
            return doubleValue == doubleValue.longValue() ? Long.toString(doubleValue.longValue()) : Double.toString(doubleValue);
        }
        throw LuaException.create("bad argument #" + argument + " to '" + function + "' (string expected, got "
                + getTypeName(value) + ")", null);
    }

    private static String stringifyConcatValue(Object value, long index) {
        if (value instanceof TruffleString stringValue) {
            return stringValue.toJavaStringUncached();
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof LuaString luaString) {
            return luaString.getValue().toJavaStringUncached();
        }
        if (value instanceof Long longValue) {
            return Long.toString(longValue);
        }
        if (value instanceof Integer integerValue) {
            return Integer.toString(integerValue);
        }
        if (value instanceof Double doubleValue) {
            return doubleValue == doubleValue.longValue() ? Long.toString(doubleValue.longValue()) : Double.toString(doubleValue);
        }
        if (value instanceof LuaNumber luaNumber) {
            return luaNumber.asString();
        }
        throw LuaException.create("invalid value (" + getTypeName(value) + ") at index " + index + " in table.concat", null);
    }
}

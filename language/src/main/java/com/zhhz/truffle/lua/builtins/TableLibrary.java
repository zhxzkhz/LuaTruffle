package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.access.LuaReadTableNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.nodes.util.WriteHelperNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
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
        boolean rawGet = !hasMeta(table, LuaMetatables.__INDEX);
        long end = args.length > 3 && !isNil(args[3]) ? toInteger(args[3], 4, "concat") : lengthOf(table);

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
            Object value = tableGet(table, index, rawGet);
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

        // 标准 checktab：在调用 __len 之前先探测 __index/__newindex 是否存在，决定访问模式。
        boolean rawGet = !hasMeta(table, LuaMetatables.__INDEX);
        boolean rawSet = !hasMeta(table, LuaMetatables.__NEWINDEX);
        long length = lengthOf(table);
        long position;
        Object value;
        if (args.length == 2) {
            position = length + 1;
            value = args[1];
        } else {
            position = toInteger(args[1], 2, "insert");
            if (position < 1 || position > length + 1) {
                throw LuaException.create("bad argument #2 to 'insert' (position out of bounds)", null);
            }
            value = args[2];
        }

        for (long index = length; index >= position; index--) {
            tableSet(table, index + 1, tableGet(table, index, rawGet), rawSet);
        }
        tableSet(table, position, value, rawSet);
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
        // 标准 tmove：起始位置必须为正，以避免若干溢出问题
        if (from <= 0) {
            throw LuaTypesUtil.argumentError("move", 2, "initial position must be positive");
        }
        LuaTable destination = args.length == 5 && !isNil(args[4]) ? ensureTable(args, 4, "move") : source;

        if (end < from) {
            return destination;
        }

        // 访问模式取决于源/目标是否带有 __index / __newindex（等价于 luaL_getmetafield）
        boolean rawGet = !hasMeta(source, LuaMetatables.__INDEX);
        boolean rawSet = !hasMeta(destination, LuaMetatables.__NEWINDEX);

        long count = end - from + 1;
        if (target > from) {
            for (long offset = count - 1; offset >= 0; offset--) {
                tableSet(destination, target + offset, tableGet(source, from + offset, rawGet), rawSet);
            }
        } else {
            for (long offset = 0; offset < count; offset++) {
                tableSet(destination, target + offset, tableGet(source, from + offset, rawGet), rawSet);
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

        boolean rawGet = !hasMeta(table, LuaMetatables.__INDEX);
        boolean rawSet = !hasMeta(table, LuaMetatables.__NEWINDEX);
        long length = lengthOf(table);
        long position = args.length == 1 || isNil(args[1]) ? length : toInteger(args[1], 2, "remove");
        if (position != length && (position < 1 || position > length + 1)) {
            throw LuaException.create("bad argument #2 to 'remove' (position out of bounds)", null);
        }

        Object removed = tableGet(table, position, rawGet);
        for (long index = position; index < length; index++) {
            tableSet(table, index, tableGet(table, index + 1, rawGet), rawSet);
        }
        tableSet(table, position < length ? length : position, LuaNil.SINGLETON, rawSet);
        return removed;
    }

    @LuaBuiltin(name = "unpack")
    @TruffleBoundary
    public static LuaMultiValue unpack(Object[] args) {
        LuaTable table = ensureTable(args, 0, "unpack");
        long start = args.length > 1 && !isNil(args[1]) ? toInteger(args[1], 2, "unpack") : 1;
        boolean rawGet = !hasMeta(table, LuaMetatables.__INDEX);
        long end = args.length > 2 && !isNil(args[2]) ? toInteger(args[2], 3, "unpack") : lengthOf(table);
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
        // 复刻标准 Lua 的栈空间检查（luaL_checkstack "too many results to unpack"）：
        // 结果数逼近 LUAI_MAXSTACK 时无法压栈。
        if (count >= 999000) {
            throw LuaException.create("too many results to unpack", null);
        }
        Object[] values = new Object[(int) count];
        for (int offset = 0; offset < values.length; offset++) {
            values[offset] = tableGet(table, start + offset, rawGet);
        }
        return new LuaMultiValue(values);
    }

    /** 遵循 __index 的读取（标准 lua_geti 语义）。 */
    static Object getIndex(Object table, long index) {
        return LuaReadTableNode.readUncached(null, table, index);
    }

    /** 遵循 __newindex 的写入（标准 lua_seti 语义）。 */
    static void setIndex(Object table, long index, Object value) {
        WriteHelperNode.writeUncached(table, index, value);
    }

    /** 元表中是否存在（非 nil 的）指定事件，等价于标准 C 库的 luaL_getmetafield 判断。 */
    static boolean hasMeta(LuaTable table, TruffleString event) {
        LuaTable metatable = LuaContext.get(null).getEffectiveMetatable(table);
        if (metatable == null) {
            return false;
        }
        Object value = metatable.rawget(event, DynamicObjectLibrary.getUncached());
        return value != null && value != LuaNil.SINGLETON;
    }

    /** 按访问模式读取：raw 模式走原始访问，否则遵循 __index。 */
    static Object tableGet(LuaTable table, long index, boolean raw) {
        return raw ? table.rawget(index) : getIndex(table, index);
    }

    /** 按访问模式写入：raw 模式走原始访问，否则遵循 __newindex。 */
    static void tableSet(LuaTable table, long index, Object value, boolean raw) {
        if (raw) {
            table.rawset(index, value);
        } else {
            setIndex(table, index, value);
        }
    }

    /** 遵循 __len 的长度（标准 luaL_len / luaV_objlen 语义）。 */
    static long lengthOf(LuaTable table) {
        LuaTable metatable = LuaContext.get(null).getEffectiveMetatable(table);
        if (metatable != null) {
            Object lenMeta = metatable.rawget(LuaMetatables.__LEN, DynamicObjectLibrary.getUncached());
            if (lenMeta != null && lenMeta != LuaNil.SINGLETON) {
                Object result;
                try {
                    result = InteropLibrary.getUncached().execute(lenMeta, table, table);
                } catch (Exception e) {
                    throw LuaException.create("error in metamethod '__len': " + e.getMessage(), null);
                }
                Object value = LuaMultiValue.firstValue(result);
                if (value instanceof Long longValue) {
                    return longValue;
                }
                if (value instanceof Integer integerValue) {
                    return integerValue.longValue();
                }
                if (value instanceof Double doubleValue && doubleValue == Math.rint(doubleValue)) {
                    return doubleValue.longValue();
                }
                throw LuaException.create("object length is not an integer", null);
            }
        }
        return table.getArraySize();
    }

    static LuaTable ensureTable(Object[] args, int index, String function) {
        if (index >= args.length || !(args[index] instanceof LuaTable table)) {
            throw LuaTypesUtil.argumentError(function, index + 1, "table expected, got "
                    + (index >= args.length ? "no value"
                    : LuaTypesUtil.getArgumentTypeName(args[index])));
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
        throw LuaTypesUtil.argumentError(function, argument, "integer expected, got "
                + LuaTypesUtil.getArgumentTypeName(value));
    }

    private static int checkedPosition(long value, int argument, String function) {
        if (value < 1 || value > Integer.MAX_VALUE) {
            throw LuaTypesUtil.argumentError(function, argument, "position out of bounds");
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

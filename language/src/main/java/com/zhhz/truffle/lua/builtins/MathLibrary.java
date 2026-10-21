package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;

import java.util.Random;

/** Implementation of Lua 5.3's math standard library. */
public final class MathLibrary {
    private static final double LONG_UPPER_BOUND = 0x1.0p63;
    private static Random random;

    private MathLibrary() {
    }

    @TruffleBoundary
    private static Random random() {
        Random value = random;
        if (value == null) {
            value = new Random();
            random = value;
        }
        return value;
    }

    @LuaBuiltin(name = "abs")
    public static Object abs(Object[] args) {
        Object value = number(args, 0, "abs");
        return value instanceof Long integer ? (integer == Long.MIN_VALUE ? integer : Math.abs(integer))
                : Math.abs((Double) value);
    }

    @LuaBuiltin(name = "acos")
    public static double acos(Object[] args) { return Math.acos(doubleNumber(args, 0, "acos")); }

    @LuaBuiltin(name = "asin")
    public static double asin(Object[] args) { return Math.asin(doubleNumber(args, 0, "asin")); }

    @LuaBuiltin(name = "atan")
    public static double atan(Object[] args) {
        double y = doubleNumber(args, 0, "atan");
        return args.length > 1 ? Math.atan2(y, doubleNumber(args, 1, "atan")) : Math.atan(y);
    }

    @LuaBuiltin(name = "ceil")
    public static Object ceil(Object[] args) { return round(args, "ceil", true); }

    @LuaBuiltin(name = "cos")
    public static double cos(Object[] args) { return Math.cos(doubleNumber(args, 0, "cos")); }

    @LuaBuiltin(name = "deg")
    public static double deg(Object[] args) { return Math.toDegrees(doubleNumber(args, 0, "deg")); }

    @LuaBuiltin(name = "exp")
    public static double exp(Object[] args) { return Math.exp(doubleNumber(args, 0, "exp")); }

    @LuaBuiltin(name = "floor")
    public static Object floor(Object[] args) { return round(args, "floor", false); }

    @LuaBuiltin(name = "fmod")
    public static Object fmod(Object[] args) {
        Object left = number(args, 0, "fmod");
        Object right = number(args, 1, "fmod");
        if (left instanceof Long a && right instanceof Long b) {
            if (b == 0) {
                throw argumentError(2, "fmod", "zero");
            }
            return b == -1 ? 0L : a % b;
        }
        return doubleValue(left) % doubleValue(right);
    }

    @LuaBuiltin(name = "log")
    public static double log(Object[] args) {
        double value = doubleNumber(args, 0, "log");
        if (args.length < 2 || args[1] == LuaNil.SINGLETON || args[1] == null) {
            return Math.log(value);
        }
        return Math.log(value) / Math.log(doubleNumber(args, 1, "log"));
    }

    @LuaBuiltin(name = "max")
    public static Object max(Object[] args) { return extremum(args, true); }

    @LuaBuiltin(name = "min")
    public static Object min(Object[] args) { return extremum(args, false); }

    @LuaBuiltin(name = "modf")
    public static LuaMultiValue modf(Object[] args) {
        Object value = number(args, 0, "modf");
        if (value instanceof Long integer) {
            return new LuaMultiValue(new Object[]{integer, 0.0});
        }
        double number = (Double) value;
        if (!Double.isFinite(number)) {
            return new LuaMultiValue(new Object[]{number, Double.isNaN(number) ? number : 0.0});
        }
        double integral = number >= 0 ? Math.floor(number) : Math.ceil(number);
        return new LuaMultiValue(new Object[]{integral, number - integral});
    }

    @LuaBuiltin(name = "rad")
    public static double rad(Object[] args) { return Math.toRadians(doubleNumber(args, 0, "rad")); }

    @LuaBuiltin(name = "random")
    @TruffleBoundary
    public static Object random(Object[] args) {
        if (args.length > 2) {
            throw LuaException.create("wrong number of arguments", null);
        }
        if (args.length == 0) {
            return random().nextDouble();
        }
        long lower = 1;
        long upper = integer(args, 0, "random");
        if (args.length > 1) {
            lower = upper;
            upper = integer(args, 1, "random");
        }
        if (lower > upper) {
            throw argumentError(2, "random", "interval is empty");
        }
        if (!(lower >= 0 || upper <= Long.MAX_VALUE + lower)) {
            throw argumentError(1, "random", "interval too large");
        }
        if (lower == Long.MIN_VALUE && upper == Long.MAX_VALUE) {
            return random().nextLong();
        }
        long range = upper - lower + 1;
        if (range > 0) {
            return lower + random().nextLong(range);
        }
        long result;
        do {
            result = random().nextLong();
        } while (result < lower || result > upper);
        return result;
    }

    @LuaBuiltin(name = "randomseed")
    @TruffleBoundary
    public static Object randomseed(Object[] args) {
        random().setSeed(integer(args, 0, "randomseed"));
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "sin")
    public static double sin(Object[] args) { return Math.sin(doubleNumber(args, 0, "sin")); }

    @LuaBuiltin(name = "sqrt")
    public static double sqrt(Object[] args) { return Math.sqrt(doubleNumber(args, 0, "sqrt")); }

    @LuaBuiltin(name = "tan")
    public static double tan(Object[] args) { return Math.tan(doubleNumber(args, 0, "tan")); }

    @LuaBuiltin(name = "tointeger")
    public static Object toInteger(Object[] args) {
        if (args.length == 0) {
            throw argumentError(1, "tointeger", "number expected, got no value");
        }
        Object value = args[0];
        if (value instanceof TruffleString || value instanceof com.zhhz.truffle.lua.runtime.LuaString) {
            // 与标准 Lua 的 lua_tointegerx 一致：数字字符串也可转换
            value = com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode.create().execute(value);
            if (value == null) return LuaNil.SINGLETON;
        }
        if (value instanceof Long) return value;
        if (value instanceof Double number && Double.isFinite(number) && number == Math.rint(number)
                && number >= -LONG_UPPER_BOUND && number < LONG_UPPER_BOUND) {
            return number.longValue();
        }
        return LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "type")
    public static Object type(Object[] args) {
        if (args.length == 0 || args[0] == LuaNil.SINGLETON || args[0] == null) return LuaNil.SINGLETON;
        return args[0] instanceof Long ? text("integer") : args[0] instanceof Double ? text("float") : LuaNil.SINGLETON;
    }

    @LuaBuiltin(name = "ult")
    public static boolean unsignedLessThan(Object[] args) {
        return Long.compareUnsigned(integer(args, 0, "ult"), integer(args, 1, "ult")) < 0;
    }

    private static Object round(Object[] args, String function, boolean ceiling) {
        Object value = number(args, 0, function);
        if (value instanceof Long) return value;
        double result = ceiling ? Math.ceil((Double) value) : Math.floor((Double) value);
        return integralResult(result);
    }

    private static Object extremum(Object[] args, boolean maximum) {
        if (args.length == 0) throw argumentError(1, maximum ? "max" : "min", "value expected");
        Object result = number(args, 0, maximum ? "max" : "min");
        for (int i = 1; i < args.length; i++) {
            Object candidate = number(args, i, maximum ? "max" : "min");
            int comparison = compareNumbers(candidate, result);
            if ((maximum && comparison > 0) || (!maximum && comparison < 0)) result = candidate;
        }
        return result;
    }

    private static Object number(Object[] args, int index, String function) {
        if (index >= args.length) throw argumentError(index + 1, function, "number expected, got no value");
        Object value = args[index];
        if (value instanceof Long || value instanceof Double) return value;
        throw argumentError(index + 1, function, "number expected, got "
                + com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.getArgumentTypeName(value));
    }

    private static long integer(Object[] args, int index, String function) {
        Object value = number(args, index, function);
        if (value instanceof Long) return (Long) value;
        double number = (Double) value;
        if (Double.isFinite(number) && number == Math.rint(number)
                && number >= -LONG_UPPER_BOUND && number < LONG_UPPER_BOUND) {
            return (long) number;
        }
        throw argumentError(index + 1, function, "integer expected");
    }

    private static double doubleNumber(Object[] args, int index, String function) {
        return doubleValue(number(args, index, function));
    }

    private static double doubleValue(Object value) { return value instanceof Long ? ((Long) value).doubleValue() : (Double) value; }

    private static int compareNumbers(Object left, Object right) {
        if (left instanceof Long leftInteger) {
            return right instanceof Long rightInteger
                    ? Long.compare(leftInteger, rightInteger)
                    : compareIntegerToDouble(leftInteger, (Double) right);
        }
        if (right instanceof Long rightInteger) {
            return -compareIntegerToDouble(rightInteger, (Double) left);
        }
        double leftDouble = (Double) left;
        double rightDouble = (Double) right;
        return leftDouble > rightDouble ? 1 : leftDouble < rightDouble ? -1 : 0;
    }

    private static int compareIntegerToDouble(long integer, double number) {
        if (Double.isNaN(number)) return 0;
        if (number >= LONG_UPPER_BOUND) return -1;
        if (number < -LONG_UPPER_BOUND) return 1;
        long truncated = (long) number;
        int comparison = Long.compare(integer, truncated);
        if (comparison != 0 || number == truncated) return comparison;
        return number > truncated ? -1 : 1;
    }

    private static Object integralResult(double value) {
        // 注意：不能写成 `cond ? (long) value : value` —— 三元运算符的数值提升
        // 会把 long 分支提升为 double，导致结果总是被装箱成 Double。
        if (Double.isFinite(value) && value >= -LONG_UPPER_BOUND && value < LONG_UPPER_BOUND) {
            return (long) value;
        }
        return value;
    }

    private static TruffleString text(String value) {
        return TruffleString.fromJavaStringUncached(value, com.zhhz.truffle.lua.LuaLanguage.STRING_ENCODING);
    }

    private static RuntimeException argumentError(int index, String function, String message) {
        return com.zhhz.truffle.lua.nodes.util.LuaTypesUtil.argumentError(function, index, message);
    }

}

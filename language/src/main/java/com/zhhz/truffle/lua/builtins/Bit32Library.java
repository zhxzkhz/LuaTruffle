package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNodeGen;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaString;

/**
 * Implementation of Lua 5.3's (deprecated) {@code bit32} library,
 * a faithful port of {@code lbitlib.c} (LUA_COMPAT_BITLIB).
 */
public final class Bit32Library {
    private static final int NBITS = 32;
    private static final long ALLONES = 0xFFFFFFFFL;

    private Bit32Library() {
    }

    /** 只保留低 32 位。 */
    private static long trim(long x) {
        return x & ALLONES;
    }

    /** 构造一个低 n 位为 1 的掩码（1 <= n <= NBITS）。 */
    private static long mask(int n) {
        return ~((ALLONES << 1) << (n - 1));
    }

    @LuaBuiltin(name = "band")
    @TruffleBoundary
    public static Object band(Object[] args) {
        long r = -1L; // ~0
        for (int i = 0; i < args.length; i++) {
            r &= unsigned(args, i, "band");
        }
        return trim(r);
    }

    @LuaBuiltin(name = "btest")
    @TruffleBoundary
    public static boolean btest(Object[] args) {
        long r = -1L;
        for (int i = 0; i < args.length; i++) {
            r &= unsigned(args, i, "btest");
        }
        return trim(r) != 0;
    }

    @LuaBuiltin(name = "bor")
    @TruffleBoundary
    public static Object bor(Object[] args) {
        long r = 0L;
        for (int i = 0; i < args.length; i++) {
            r |= unsigned(args, i, "bor");
        }
        return trim(r);
    }

    @LuaBuiltin(name = "bxor")
    @TruffleBoundary
    public static Object bxor(Object[] args) {
        long r = 0L;
        for (int i = 0; i < args.length; i++) {
            r ^= unsigned(args, i, "bxor");
        }
        return trim(r);
    }

    @LuaBuiltin(name = "bnot")
    @TruffleBoundary
    public static Object bnot(Object[] args) {
        return trim(~unsigned(args, 0, "bnot"));
    }

    @LuaBuiltin(name = "lshift")
    @TruffleBoundary
    public static Object lshift(Object[] args) {
        return shift(unsigned(args, 0, "lshift"), integer(args, 1, "lshift"));
    }

    @LuaBuiltin(name = "rshift")
    @TruffleBoundary
    public static Object rshift(Object[] args) {
        return shift(unsigned(args, 0, "rshift"), -integer(args, 1, "rshift"));
    }

    @LuaBuiltin(name = "arshift")
    @TruffleBoundary
    public static Object arshift(Object[] args) {
        long r = unsigned(args, 0, "arshift");
        long i = integer(args, 1, "arshift");
        if (i < 0 || (r & (1L << (NBITS - 1))) == 0) {
            return shift(r, -i);
        }
        if (i >= NBITS) {
            r = ALLONES;
        } else {
            r = trim((r >>> i) | ~(trim(~0L) >>> i)); // 补上符号位
        }
        return r;
    }

    @LuaBuiltin(name = "lrotate")
    @TruffleBoundary
    public static Object lrotate(Object[] args) {
        return rotate(unsigned(args, 0, "lrotate"), integer(args, 1, "lrotate"));
    }

    @LuaBuiltin(name = "rrotate")
    @TruffleBoundary
    public static Object rrotate(Object[] args) {
        return rotate(unsigned(args, 0, "rrotate"), -integer(args, 1, "rrotate"));
    }

    @LuaBuiltin(name = "extract")
    @TruffleBoundary
    public static Object extract(Object[] args) {
        long r = trim(unsigned(args, 0, "extract"));
        int[] fw = fieldArgs(args, 1, "extract");
        return (r >>> fw[0]) & mask(fw[1]);
    }

    @LuaBuiltin(name = "replace")
    @TruffleBoundary
    public static Object replace(Object[] args) {
        long r = trim(unsigned(args, 0, "replace"));
        long v = unsigned(args, 1, "replace");
        int[] fw = fieldArgs(args, 2, "replace");
        int f = fw[0];
        // 标准 C 实现中掩码被存入 int（32 位），随后 m<<f 与 ~ 均按 32 位运算，
        // 再符号扩展参与 64 位与运算。必须复刻这一行为。
        int m = (int) mask(fw[1]);
        v &= m; // 抹去给定宽度之外的位
        long maskOut = ~(m << f);
        return (r & maskOut) | (v << f);
    }

    // --- 辅助方法 ---

    private static long shift(long r, long i) {
        if (i < 0) { // 右移？
            i = -i;
            r = trim(r);
            if (i >= NBITS) {
                return 0;
            }
            return r >>> i;
        }
        if (i >= NBITS) {
            return 0;
        }
        r <<= i;
        return trim(r);
    }

    private static long rotate(long r, long d) {
        int i = (int) (d & (NBITS - 1)); // i = d % NBITS
        return Integer.toUnsignedLong(Integer.rotateLeft((int) trim(r), i));
    }

    private static int[] fieldArgs(Object[] args, int fIndex, String function) {
        int f = (int) integer(args, fIndex, function);
        long w = (fIndex + 1 < args.length && !isNil(args[fIndex + 1]))
                ? integer(args, fIndex + 1, function) : 1;
        if (f < 0) {
            throw LuaTypesUtil.argumentError(function, fIndex + 1, "field cannot be negative");
        }
        if (w <= 0) {
            throw LuaTypesUtil.argumentError(function, fIndex + 2, "width must be positive");
        }
        if (f + w > NBITS) {
            throw LuaException.create("trying to access non-existent bits", null);
        }
        return new int[]{f, (int) w};
    }

    private static long unsigned(Object[] args, int index, String function) {
        return integer(args, index, function);
    }

    /** 等价于 luaL_checkinteger：接受整数、可表示为整数的浮点数以及数字字符串。 */
    private static long integer(Object[] args, int index, String function) {
        if (index >= args.length) {
            throw LuaTypesUtil.argumentError(function, index + 1, "number expected, got no value");
        }
        Object value = args[index];
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Double doubleValue) {
            return toInteger(doubleValue, index, function);
        }
        // 宿主 interop 的装箱数字（Integer/Float 等），与算术运算保持一致。
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).longValue();
        }
        if (value instanceof Float floatValue) {
            return toInteger(floatValue.doubleValue(), index, function);
        }
        if (value instanceof TruffleString || value instanceof LuaString) {
            Object number = LuaCoerceToNumberNodeGen.getUncached().execute(value);
            if (number instanceof Long longValue) {
                return longValue;
            }
            if (number instanceof Double doubleValue) {
                return toInteger(doubleValue, index, function);
            }
            throw LuaTypesUtil.argumentError(function, index + 1, "number has no integer representation");
        }
        throw LuaTypesUtil.argumentError(function, index + 1,
                "number expected, got " + LuaTypesUtil.getArgumentTypeName(value));
    }

    private static long toInteger(double value, int index, String function) {
        if (Double.isFinite(value) && value == (long) value
                && value >= (double) Long.MIN_VALUE && value < -((double) Long.MIN_VALUE)) {
            return (long) value;
        }
        throw LuaTypesUtil.argumentError(function, index + 1, "number has no integer representation");
    }

    private static boolean isNil(Object value) {
        return value == null || value == LuaNil.SINGLETON;
    }
}

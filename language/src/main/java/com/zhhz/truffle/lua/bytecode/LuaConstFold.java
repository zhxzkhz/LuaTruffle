package com.zhhz.truffle.lua.bytecode;

/**
 * 编译期常量折叠所需的原始算术 (复刻 lobject.c 的 {@code luaO_arith} 以及
 * lvm.c 的 {@code luaV_mod}/{@code luaV_div}/{@code luaV_shiftl})。
 *
 * <p>op 采用 lua.h 的 {@code LUA_OP*} 编号 (0..13)，即 {@code OpCode - OP_ADD}。
 * 返回 null 表示无法折叠 (与 C 中 validop 判定不可折叠等价)。
 */
public final class LuaConstFold {

    private LuaConstFold() {
    }

    private static final int NBITS = 64;

    private static final int LUA_OPADD = 0;
    private static final int LUA_OPSUB = 1;
    private static final int LUA_OPMUL = 2;
    private static final int LUA_OPMOD = 3;
    private static final int LUA_OPPOW = 4;
    private static final int LUA_OPDIV = 5;
    private static final int LUA_OPIDIV = 6;
    private static final int LUA_OPBAND = 7;
    private static final int LUA_OPBOR = 8;
    private static final int LUA_OPBXOR = 9;
    private static final int LUA_OPSHL = 10;
    private static final int LUA_OPSHR = 11;
    private static final int LUA_OPUNM = 12;
    private static final int LUA_OPBNOT = 13;

    /** const -> 精确整数；非整数返回 null。 */
    public static Long toInteger(LuaConstant c) {
        if (c instanceof LuaConstant.Int i) {
            return i.value();
        }
        if (c instanceof LuaConstant.Flt f) {
            double v = f.value();
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                return null;
            }
            long l = (long) v;
            if ((double) l != v) {
                return null;
            }
            return l;
        }
        return null;
    }

    /** const -> 数值；非数值返回 null。 */
    public static Double toNumber(LuaConstant c) {
        if (c instanceof LuaConstant.Int i) {
            return (double) i.value();
        }
        if (c instanceof LuaConstant.Flt f) {
            return f.value();
        }
        return null;
    }

    private static long shiftl(long x, long y) {
        if (y < 0) {
            if (y <= -NBITS) {
                return 0;
            }
            // Lua 5.3 的 >> 是逻辑右移 (intop 先转 unsigned)，故用无符号 >>> 。
            return x >>> (-y);
        } else {
            if (y >= NBITS) {
                return 0;
            }
            return x << y;
        }
    }

    private static long luaV_mod(long m, long n) {
        if (n == -1) {
            return 0;
        }
        long r = m % n;
        if (r != 0 && (r ^ n) < 0) {
            r += n;
        }
        return r;
    }

    private static long luaV_div(long m, long n) {
        if (n == -1) {
            return 0L - m;
        }
        long q = m / n;
        if ((m ^ n) < 0 && m % n != 0) {
            q -= 1;
        }
        return q;
    }

    private static long intarith(int op, long v1, long v2) {
        return switch (op) {
            case LUA_OPADD -> v1 + v2;
            case LUA_OPSUB -> v1 - v2;
            case LUA_OPMUL -> v1 * v2;
            case LUA_OPMOD -> luaV_mod(v1, v2);
            case LUA_OPIDIV -> luaV_div(v1, v2);
            case LUA_OPBAND -> v1 & v2;
            case LUA_OPBOR -> v1 | v2;
            case LUA_OPBXOR -> v1 ^ v2;
            case LUA_OPSHL -> shiftl(v1, v2);
            case LUA_OPSHR -> shiftl(v1, -v2);
            case LUA_OPUNM -> 0L - v1;
            case LUA_OPBNOT -> ~v1;
            default -> throw new LuaCompileException("invalid integer op " + op);
        };
    }

    private static double luaV_modf(double a, double b) {
        double m = a % b;
        if (m > 0 ? b < 0 : (m < 0 && b > 0)) {
            m += b;
        }
        return m;
    }

    private static double numarith(int op, double v1, double v2) {
        return switch (op) {
            case LUA_OPADD -> v1 + v2;
            case LUA_OPSUB -> v1 - v2;
            case LUA_OPMUL -> v1 * v2;
            case LUA_OPDIV -> v1 / v2;
            case LUA_OPPOW -> Math.pow(v1, v2);
            case LUA_OPIDIV -> Math.floor(v1 / v2);
            case LUA_OPUNM -> -v1;
            case LUA_OPMOD -> luaV_modf(v1, v2);
            default -> throw new LuaCompileException("invalid float op " + op);
        };
    }

    /**
     * 复刻 lcode.c 的 {@code validop}：判定该运算是否"可安全折叠"。
     * 位运算要求两操作数都能精确转成整数；DIV/IDIV/MOD 要求除数非 0。
     */
    public static boolean validop(int op, LuaConstant v1, LuaConstant v2) {
        switch (op) {
            case LUA_OPBAND:
            case LUA_OPBOR:
            case LUA_OPBXOR:
            case LUA_OPSHL:
            case LUA_OPSHR:
            case LUA_OPBNOT:
                return toInteger(v1) != null && toInteger(v2) != null;
            case LUA_OPDIV:
            case LUA_OPIDIV:
            case LUA_OPMOD: {
                Double n2 = toNumber(v2);
                return n2 != null && n2 != 0.0;
            }
            default:
                return true;
        }
    }

    /**
     * 尝试对两个常量做算术；成功返回结果常量，否则返回 null。
     */
    public static LuaConstant arith(int op, LuaConstant p1, LuaConstant p2) {
        if (op == LUA_OPBAND || op == LUA_OPBOR || op == LUA_OPBXOR
                || op == LUA_OPSHL || op == LUA_OPSHR || op == LUA_OPBNOT) {
            Long i1 = toInteger(p1);
            Long i2 = toInteger(p2);
            if (i1 == null || i2 == null) {
                return null;
            }
            return new LuaConstant.Int(intarith(op, i1, i2));
        }
        if (op == LUA_OPDIV || op == LUA_OPPOW) {
            Double n1 = toNumber(p1);
            Double n2 = toNumber(p2);
            if (n1 == null || n2 == null) {
                return null;
            }
            return new LuaConstant.Flt(numarith(op, n1, n2));
        }
        // 其余运算：优先整数，否则浮点
        if (p1 instanceof LuaConstant.Int && p2 instanceof LuaConstant.Int) {
            return new LuaConstant.Int(intarith(op, ((LuaConstant.Int) p1).value(),
                    ((LuaConstant.Int) p2).value()));
        }
        Double n1 = toNumber(p1);
        Double n2 = toNumber(p2);
        if (n1 == null || n2 == null) {
            return null;
        }
        if ((op == LUA_OPMOD || op == LUA_OPIDIV) && n2 == 0.0) {
            return null;
        }
        if (op == LUA_OPDIV && n2 == 0.0) {
            return null;
        }
        return new LuaConstant.Flt(numarith(op, n1, n2));
    }
}

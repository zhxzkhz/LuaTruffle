package com.zhhz.truffle.lua.bytecode;

/**
 * Lua 5.3 单条指令的 32 位编码 (lopcodes.h)。
 *
 * <p>位布局:
 * <pre>
 *   POS_OP=0  SIZE_OP=6
 *   POS_A =6  SIZE_A =8
 *   POS_C =14 SIZE_C =9
 *   POS_B =23 SIZE_B =9
 *   POS_Bx=14 SIZE_Bx=18
 *   POS_Ax=6  SIZE_Ax=26
 * </pre>
 * 所有指令以无符号数处理，但存放在 Java {@code int} 中 (Java int 亦为 32 位)。
 */
public final class Instruction {

    private Instruction() {
    }

    public static final int SIZE_C = 9;
    public static final int SIZE_B = 9;
    public static final int SIZE_Bx = SIZE_C + SIZE_B;
    public static final int SIZE_A = 8;
    public static final int SIZE_Ax = SIZE_C + SIZE_B + SIZE_A;
    public static final int SIZE_OP = 6;

    public static final int POS_OP = 0;
    public static final int POS_A = POS_OP + SIZE_OP;
    public static final int POS_C = POS_A + SIZE_A;
    public static final int POS_B = POS_C + SIZE_C;
    public static final int POS_Bx = POS_C;
    public static final int POS_Ax = POS_A;

    public static final int MAXARG_Bx = (1 << SIZE_Bx) - 1;   // 262143
    public static final int MAXARG_sBx = MAXARG_Bx >> 1;      // 131071
    public static final int MAXARG_Ax = (1 << SIZE_Ax) - 1;   // 67108863

    public static final int MAXARG_A = (1 << SIZE_A) - 1;     // 255
    public static final int MAXARG_B = (1 << SIZE_B) - 1;     // 511
    public static final int MAXARG_C = (1 << SIZE_C) - 1;     // 511

    /** RK 编码中表示常量的标志位。 */
    public static final int BITRK = 1 << (SIZE_B - 1);        // 256
    public static final int MAXINDEXRK = BITRK - 1;           // 255

    /** 无效寄存器 (8 位内可表示)。 */
    public static final int NO_REG = MAXARG_A;

    private static int mask1(int n, int p) {
        return (~((~0) << n)) << p;
    }

    private static int mask0(int n, int p) {
        return ~mask1(n, p);
    }

    private static int getArg(int i, int pos, int size) {
        return (i >>> pos) & mask1(size, 0);
    }

    private static int setArg(int i, int v, int pos, int size) {
        return (i & mask0(size, pos)) | ((v << pos) & mask1(size, pos));
    }

    public static OpCode getOpcode(int i) {
        return OpCode.fromInt((i >>> POS_OP) & mask1(SIZE_OP, 0));
    }

    public static int getA(int i) {
        return getArg(i, POS_A, SIZE_A);
    }

    public static int getB(int i) {
        return getArg(i, POS_B, SIZE_B);
    }

    public static int getC(int i) {
        return getArg(i, POS_C, SIZE_C);
    }

    public static int getBx(int i) {
        return getArg(i, POS_Bx, SIZE_Bx);
    }

    public static int getAx(int i) {
        return getArg(i, POS_Ax, SIZE_Ax);
    }

    public static int getsBx(int i) {
        return getBx(i) - MAXARG_sBx;
    }

    public static int withOpcode(int i, OpCode o) {
        return setArg(i, o.num(), POS_OP, SIZE_OP);
    }

    public static int withA(int i, int a) {
        return setArg(i, a, POS_A, SIZE_A);
    }

    public static int withB(int i, int b) {
        return setArg(i, b, POS_B, SIZE_B);
    }

    public static int withC(int i, int c) {
        return setArg(i, c, POS_C, SIZE_C);
    }

    public static int withBx(int i, int bx) {
        return setArg(i, bx, POS_Bx, SIZE_Bx);
    }

    public static int withSBx(int i, int sbx) {
        return setArg(i, sbx + MAXARG_sBx, POS_Bx, SIZE_Bx);
    }

    public static int withAx(int i, int ax) {
        return setArg(i, ax, POS_Ax, SIZE_Ax);
    }

    // --- RK 辅助 ---

    public static boolean isK(int x) {
        return (x & BITRK) != 0;
    }

    public static int indexK(int r) {
        return r & ~BITRK;
    }

    public static int rkAsK(int x) {
        return x | BITRK;
    }

    // --- 构造 ---

    public static int createABC(OpCode o, int a, int b, int c) {
        return (o.num() << POS_OP) | (a << POS_A) | (b << POS_B) | (c << POS_C);
    }

    public static int createABx(OpCode o, int a, int bc) {
        return (o.num() << POS_OP) | (a << POS_A) | (bc << POS_Bx);
    }

    public static int createAsBx(OpCode o, int a, int sbx) {
        return createABx(o, a, sbx + MAXARG_sBx);
    }

    public static int createAx(OpCode o, int a) {
        return (o.num() << POS_OP) | (a << POS_Ax);
    }
}

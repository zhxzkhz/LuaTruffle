package com.zhhz.truffle.lua.bytecode;

/**
 * Lua 5.3 虚拟机全部操作码 (lopcodes.h: enum OpCode)。
 *
 * <p>顺序必须与标准 Lua 5.3.0 完全一致，因为 opcode 数值会被编码进指令并影响
 * 常量表、跳转等语义。每个操作码携带其参数模式，对应 {@code luaP_opmodes[]} 表。
 */
public enum OpCode {

    // name,          test, setA, bMode,        cMode,        opMode
    MOVE        (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
    LOADK       (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgN, OpMode.iABx),
    LOADKX      (false, true,  OpArgMask.OpArgN, OpArgMask.OpArgN, OpMode.iABx),
    LOADBOOL    (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
    LOADNIL     (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),
    GETUPVAL    (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),

    GETTABUP    (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgK, OpMode.iABC),
    GETTABLE    (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgK, OpMode.iABC),

    SETTABUP    (false, false, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    SETUPVAL    (false, false, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),
    SETTABLE    (false, false, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),

    NEWTABLE    (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),

    SELF        (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgK, OpMode.iABC),

    ADD         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    SUB         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    MUL         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    MOD         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    POW         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    DIV         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    IDIV        (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    BAND        (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    BOR         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    BXOR        (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    SHL         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    SHR         (false, true,  OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    UNM         (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
    BNOT        (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
    NOT         (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
    LEN         (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),

    CONCAT      (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgR, OpMode.iABC),

    JMP         (false, false, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),
    EQ          (true,  false, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    LT          (true,  false, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
    LE          (true,  false, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),

    TEST        (true,  false, OpArgMask.OpArgN, OpArgMask.OpArgU, OpMode.iABC),
    TESTSET     (true,  true,  OpArgMask.OpArgR, OpArgMask.OpArgU, OpMode.iABC),

    CALL        (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
    TAILCALL    (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
    RETURN      (false, false, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),

    FORLOOP     (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),
    FORPREP     (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),

    TFORCALL    (false, false, OpArgMask.OpArgN, OpArgMask.OpArgU, OpMode.iABC),
    TFORLOOP    (false, true,  OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),

    SETLIST     (false, false, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),

    CLOSURE     (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABx),

    VARARG      (false, true,  OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),

    EXTRAARG    (false, false, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iAx);

    /** 值 == opcode 编号，即数组下标。 */
    public static final OpCode[] VALUES = values();
    public static final int NUM_OPCODES = VALUES.length;

    private final boolean test;
    private final boolean setA;
    private final OpArgMask bMode;
    private final OpArgMask cMode;
    private final OpMode opMode;

    OpCode(boolean test, boolean setA, OpArgMask bMode, OpArgMask cMode, OpMode opMode) {
        this.test = test;
        this.setA = setA;
        this.bMode = bMode;
        this.cMode = cMode;
        this.opMode = opMode;
    }

    /** 指令编号 (0..NUM_OPCODES-1)。 */
    public int num() {
        return ordinal();
    }

    /** 是否为测试指令 (其后必须跟随一条 JMP)。 */
    public boolean isTest() {
        return test;
    }

    /** 该指令是否写寄存器 A。 */
    public boolean setsA() {
        return setA;
    }

    public OpArgMask bMode() {
        return bMode;
    }

    public OpArgMask cMode() {
        return cMode;
    }

    public OpMode opMode() {
        return opMode;
    }

    public static OpCode fromInt(int op) {
        return VALUES[op];
    }
}

package com.zhhz.truffle.lua.bytecode;

/**
 * Lua 5.3 指令的基本格式模式 (lopcodes.h: enum OpMode)。
 */
public enum OpMode {
    /** A, B, C 三个参数。 */
    iABC,
    /** A, Bx 两个参数 (Bx = B:C 18 位无符号)。 */
    iABx,
    /** A, sBx 两个参数 (sBx 为有符号跳转偏移)。 */
    iAsBx,
    /** Ax 一个参数 (Ax = A:B:C 26 位无符号，仅 EXTRAARG 使用)。 */
    iAx
}

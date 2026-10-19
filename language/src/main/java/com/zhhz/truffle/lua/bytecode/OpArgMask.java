package com.zhhz.truffle.lua.bytecode;

/**
 * 指令参数的使用方式 (lopcodes.h: enum OpArgMask)。
 */
public enum OpArgMask {
    /** 该参数未使用。 */
    OpArgN,
    /** 该参数被使用 (原始数值，如常量表大小、寄存器区间宽度等)。 */
    OpArgU,
    /** 该参数是一个寄存器或跳转偏移。 */
    OpArgR,
    /** 该参数是常量或寄存器 (RK 编码)。 */
    OpArgK
}

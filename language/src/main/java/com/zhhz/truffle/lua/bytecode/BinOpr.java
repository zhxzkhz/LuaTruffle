package com.zhhz.truffle.lua.bytecode;

/**
 * 二元运算符 (lcode.h: enum BinOpr)。顺序必须与 C 一致，因为多处依赖
 * {@code op - OPR_ADD} / {@code op - OPR_EQ} 的数值映射到 OpCode。
 */
public enum BinOpr {
    ADD, SUB, MUL, MOD, POW,
    DIV,
    IDIV,
    BAND, BOR, BXOR,
    SHL, SHR,
    CONCAT,
    EQ, LT, LE,
    NE, GT, GE,
    AND, OR,
    NOBINOPR
}

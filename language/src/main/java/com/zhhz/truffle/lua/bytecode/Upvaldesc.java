package com.zhhz.truffle.lua.bytecode;

/**
 * 一个 upvalue 的描述 (lobject.h: Upvaldesc)。
 *
 * <p>{@code instack} 为真表示该 upvalue 捕获的是父函数的栈寄存器 {@code idx}；
 * 否则捕获的是父函数第 {@code idx} 个 upvalue。
 */
public final class Upvaldesc {

    public String name;
    public boolean instack;
    public int idx;

    public Upvaldesc() {
    }

    public Upvaldesc(String name, boolean instack, int idx) {
        this.name = name;
        this.instack = instack;
        this.idx = idx;
    }
}

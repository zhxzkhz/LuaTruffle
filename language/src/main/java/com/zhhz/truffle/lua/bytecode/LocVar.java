package com.zhhz.truffle.lua.bytecode;

/**
 * 调试信息中的局部变量 (lobject.h: LocVar)。
 */
public final class LocVar {

    public String varname;
    public int startpc;
    public int endpc;

    public LocVar() {
    }

    public LocVar(String varname, int startpc, int endpc) {
        this.varname = varname;
        this.startpc = startpc;
        this.endpc = endpc;
    }
}

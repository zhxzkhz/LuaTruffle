package com.zhhz.truffle.lua.bytecode;

/**
 * 一个函数原型的编译产物 (lobject.h: Proto)。
 *
 * <p>这是 Lua 5.3 虚拟机的核心数据结构：一段指令 ({@link #code})、一张常量表
 * ({@link #k})、若干嵌套原型 ({@link #p})，以及 upvalue 描述和调试信息。
 */
public final class Proto {

    /** 嵌套子原型。 */
    public Proto[] p = new Proto[0];
    /** 指令流，每项为一个 32 位编码指令。 */
    public int[] code = new int[0];
    /** 常量表。 */
    public LuaConstant[] k = new LuaConstant[0];
    /** 每条指令对应的源代码行 (调试信息)。 */
    public int[] lineinfo = new int[0];
    /** 局部变量调试信息。 */
    public LocVar[] locvars = new LocVar[0];
    /** upvalue 描述。 */
    public Upvaldesc[] upvalues = new Upvaldesc[0];

    /** 源名 (如 "@file.lua")，可为 null。 */
    public String source;
    /** 函数定义起始行。 */
    public int linedefined;
    /** 函数定义结束行。 */
    public int lastlinedefined;
    /** 形参个数。 */
    public int numparams;
    /** vararg 标志位 (lparser.h 的 VARARG_ISVARARG 等掩码)。 */
    public int isVararg;
    /** 需要的最大栈寄存器数。 */
    public int maxstacksize;

    public int sizecode() {
        return code.length;
    }

    public int sizek() {
        return k.length;
    }

    public int sizep() {
        return p.length;
    }

    public int sizelineinfo() {
        return lineinfo.length;
    }

    public int sizelocvars() {
        return locvars.length;
    }

    public int sizeupvalues() {
        return upvalues.length;
    }
}

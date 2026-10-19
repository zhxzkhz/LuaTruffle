package com.zhhz.truffle.lua.bytecode;

/**
 * 表达式描述符 (lparser.h: expdesc / expkind)。
 *
 * <p>代码生成器用它跟踪一个表达式当前"落在哪里"以及它的真/假跳转链表。
 */
public final class ExpDesc {

    /** 表达式种类 (lparser.h: expkind)。顺序与 C 一致。 */
    public enum Kind {
        VVOID,      // 无值
        VNIL,
        VTRUE,
        VFALSE,
        VK,         // info = 常量表下标
        VKFLT,      // nval = 浮点数值
        VKINT,      // ival = 整数值
        VNONRELOC,  // info = 结果寄存器
        VLOCAL,     // info = 局部变量寄存器
        VUPVAL,     // info = upvalue 下标
        VINDEXED,   // indT/indIdx = 表/键
        VJMP,       // info = 指令 pc
        VRELOCABLE, // info = 指令 pc
        VCALL,      // info = 指令 pc
        VVARARG     // info = 指令 pc
    }

    public static final int NO_JUMP = -1;

    public Kind k = Kind.VVOID;
    public int info;
    public long ival;
    public double nval;

    // VINDEXED 专用
    public int indIdx;
    public int indT;
    public Kind indVt; // VLOCAL 或 VUPVAL

    /** 'exit when true' 跳转链表。 */
    public int t = NO_JUMP;
    /** 'exit when false' 跳转链表。 */
    public int f = NO_JUMP;

    public ExpDesc() {
    }

    public static ExpDesc vkint(long v) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VKINT;
        e.ival = v;
        return e;
    }

    public static ExpDesc vkflt(double v) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VKFLT;
        e.nval = v;
        return e;
    }

    public static ExpDesc vk(int idx) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VK;
        e.info = idx;
        return e;
    }

    public static ExpDesc vnil() {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VNIL;
        return e;
    }

    public static ExpDesc vtrue() {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VTRUE;
        return e;
    }

    public static ExpDesc vfalse() {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VFALSE;
        return e;
    }

    public static ExpDesc vnonreloc(int reg) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VNONRELOC;
        e.info = reg;
        return e;
    }

    public static ExpDesc vlocal(int reg) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VLOCAL;
        e.info = reg;
        return e;
    }

    public static ExpDesc vupval(int idx) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VUPVAL;
        e.info = idx;
        return e;
    }

    public static ExpDesc vindexed(int table, int idx, Kind vt) {
        ExpDesc e = new ExpDesc();
        e.k = Kind.VINDEXED;
        e.indT = table;
        e.indIdx = idx;
        e.indVt = vt;
        return e;
    }

    /** emulates '*' ({@code *e1 = *e2})。 */
    public void copyFrom(ExpDesc o) {
        this.k = o.k;
        this.info = o.info;
        this.ival = o.ival;
        this.nval = o.nval;
        this.indIdx = o.indIdx;
        this.indT = o.indT;
        this.indVt = o.indVt;
        this.t = o.t;
        this.f = o.f;
    }

    public boolean hasJumps() {
        return t != f;
    }

    public boolean isVar() {
        return k.ordinal() >= Kind.VLOCAL.ordinal() && k.ordinal() <= Kind.VINDEXED.ordinal();
    }

    public boolean isInReg() {
        return k == Kind.VNONRELOC || k == Kind.VLOCAL;
    }
}

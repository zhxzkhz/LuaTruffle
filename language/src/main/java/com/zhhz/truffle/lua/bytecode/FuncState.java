package com.zhhz.truffle.lua.bytecode;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成一个函数的字节码时所需的状态 (lparser.h: FuncState) 以及 lcode.c 中
 * 全部 {@code luaK_*} 代码生成函数。
 *
 * <p>忠实移植 Lua 5.3.0 的 lcode.c。常量表去重使用每函数独立的哈希表
 * (C 里借用 scanner table，仅为优化，不影响生成结果的正确性)。
 */
public final class FuncState {

    public static final int NO_JUMP = -1;
    public static final int LUA_MULTRET = -1;
    private static final int MAXREGS = 250;
    private static final int LUAI_MAXSHORTLEN = 40;

    public Proto f = new Proto();
    public final FuncState prev;
    public final CompilerContext ctx;

    // --- 动态代码数组 ---
    private int[] code = new int[16];
    private int[] lineinfo = new int[16];

    /** 当前活跃块链 (lparser.c: FuncState.bl)。 */
    public BlockCnt bl;

    /** 子原型动态数组 (对应 f->p)。 */
    public Proto[] p = new Proto[0];
    /** 局部变量调试信息动态数组 (对应 f->locvars)。 */
    public LocVar[] locvars = new LocVar[0];
    /** upvalue 描述动态数组 (对应 f->upvalues)。 */
    public Upvaldesc[] upvalues = new Upvaldesc[0];

    public int pc;
    public int lasttarget = NO_JUMP;
    public int jpc = NO_JUMP;
    public int nk;
    public int np;
    public int firstlocal;
    public int nlocvars;
    public int nactvar;
    public int nups;
    public int freereg;

    public FuncState(FuncState prev, CompilerContext ctx) {
        this.prev = prev;
        this.ctx = ctx;
        if (prev != null) {
            this.f.source = prev.f.source;
        }
    }

    // ---------------------------------------------------------------- code

    private void ensureCode(int n) {
        if (n > code.length) {
            int cap = Math.max(code.length * 2, n);
            code = java.util.Arrays.copyOf(code, cap);
            lineinfo = java.util.Arrays.copyOf(lineinfo, cap);
        }
    }

    public int getCode(int pcIndex) {
        return code[pcIndex];
    }

    public void setCode(int pcIndex, int instr) {
        code[pcIndex] = instr;
    }

    private int code(int instr) {
        dischargejpc();
        ensureCode(pc + 1);
        code[pc] = instr;
        lineinfo[pc] = ctx.lastline;
        return pc++;
    }

    public int codeABC(OpCode o, int a, int b, int c) {
        return code(Instruction.createABC(o, a, b, c));
    }

    public int codeABx(OpCode o, int a, int bc) {
        return code(Instruction.createABx(o, a, bc));
    }

    public int codeAsBx(OpCode o, int a, int sbx) {
        return code(Instruction.createAsBx(o, a, sbx));
    }

    private int codeextraarg(int a) {
        return code(Instruction.createAx(OpCode.EXTRAARG, a));
    }

    public int codek(int reg, int k) {
        if (k <= Instruction.MAXARG_Bx) {
            return codeABx(OpCode.LOADK, reg, k);
        } else {
            int p = codeABx(OpCode.LOADKX, reg, 0);
            codeextraarg(k);
            return p;
        }
    }

    public void checkstack(int n) {
        int newstack = freereg + n;
        if (newstack > f.maxstacksize) {
            if (newstack >= MAXREGS) {
                throw new LuaCompileException("function or expression too complex");
            }
            f.maxstacksize = newstack;
        }
    }

    public void reserveregs(int n) {
        checkstack(n);
        freereg += n;
    }

    private void freeReg(int reg) {
        if (!Instruction.isK(reg) && reg >= nactvar) {
            freereg--;
        }
    }

    private void freeexp(ExpDesc e) {
        if (e.k == ExpDesc.Kind.VNONRELOC) {
            freeReg(e.info);
        }
    }

    // ------------------------------------------------------------- 常量表

    private int addk(LuaConstant key, LuaConstant value) {
        // 复刻 lcode.c addk：索引扫描表 h 由整个编译单元共享 (fs->ls->h)，
        // 但其中保存的下标是"当时那个函数"的常量下标；换个函数后若 f->k[k]
        // 不匹配（类型不同或越界）就新建一项并覆盖 h[key]。这正是 C 中会出现
        // 重复常量的原因，必须同样处理才能逐字节一致。
        Integer old = ctx.kmap.get(key);
        if (old != null) {
            int k = old;
            if (k < nk && f.k[k].tag() == value.tag() && f.k[k].equals(value)) {
                return k;
            }
        }
        LuaConstant[] nkArr = java.util.Arrays.copyOf(f.k, nk + 1);
        nkArr[nk] = value;
        f.k = nkArr;
        ctx.kmap.put(key, nk);
        return nk++;
    }

    private static int stringTag(byte[] b) {
        return b.length <= LUAI_MAXSHORTLEN
                ? LuaConstant.LUA_TSHRSTR : LuaConstant.LUA_TLNGSTR;
    }

    public int stringK(String s) {
        return stringK(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    public int stringK(byte[] bytes) {
        LuaConstant.Str v = new LuaConstant.Str(bytes.clone(), stringTag(bytes));
        return addk(v, v);
    }

    public int intK(long n) {
        LuaConstant.Int v = new LuaConstant.Int(n);
        return addk(v, v);
    }

    private int numberK(double r) {
        LuaConstant.Flt v = new LuaConstant.Flt(r);
        return addk(v, v);
    }

    private int boolK(boolean b) {
        LuaConstant.Bool v = new LuaConstant.Bool(b);
        return addk(v, v);
    }

    private int nilK() {
        return addk(LuaConstant.Nil.INSTANCE, LuaConstant.Nil.INSTANCE);
    }

    // ----------------------------------------------------- 结构增长

    /** 登记一个局部变量调试项，返回其在 f.locvars 中的下标。 */
    public int registerLocalVar(String name) {
        LocVar[] n = java.util.Arrays.copyOf(locvars, nlocvars + 1);
        n[nlocvars] = new LocVar(name, 0, 0);
        locvars = n;
        return nlocvars++;
    }

    /** 新增一个子原型 (lparser.c: addprototype)。 */
    public Proto addPrototype() {
        Proto clp = new Proto();
        Proto[] n = java.util.Arrays.copyOf(p, np + 1);
        n[np] = clp;
        p = n;
        np++;
        return clp;
    }

    /** 新增一个 upvalue (lparser.c: newupvalue)。 */
    public int newUpvalue(String name, ExpDesc v) {
        Upvaldesc u = new Upvaldesc();
        u.instack = (v.k == ExpDesc.Kind.VLOCAL);
        u.idx = v.info;
        u.name = name;
        Upvaldesc[] n = java.util.Arrays.copyOf(upvalues, nups + 1);
        n[nups] = u;
        upvalues = n;
        return nups++;
    }

    // --------------------------------------------------------- 结果数量控制

    public void setreturns(ExpDesc e, int nresults) {
        if (e.k == ExpDesc.Kind.VCALL) {
            setCode(e.info, Instruction.withC(getCode(e.info), nresults + 1));
        } else if (e.k == ExpDesc.Kind.VVARARG) {
            setCode(e.info, Instruction.withB(getCode(e.info), nresults + 1));
            setCode(e.info, Instruction.withA(getCode(e.info), freereg));
            reserveregs(1);
        }
    }

    public void setoneret(ExpDesc e) {
        if (e.k == ExpDesc.Kind.VCALL) {
            e.k = ExpDesc.Kind.VNONRELOC;
            e.info = Instruction.getA(getCode(e.info));
        } else if (e.k == ExpDesc.Kind.VVARARG) {
            setCode(e.info, Instruction.withB(getCode(e.info), 2));
            e.k = ExpDesc.Kind.VRELOCABLE;
        }
    }

    public void dischargevars(ExpDesc e) {
        switch (e.k) {
            case VLOCAL -> e.k = ExpDesc.Kind.VNONRELOC;
            case VUPVAL -> {
                e.info = codeABC(OpCode.GETUPVAL, 0, e.info, 0);
                e.k = ExpDesc.Kind.VRELOCABLE;
            }
            case VINDEXED -> {
                OpCode op = OpCode.GETTABUP;
                freeReg(e.indIdx);
                if (e.indVt == ExpDesc.Kind.VLOCAL) {
                    freeReg(e.indT);
                    op = OpCode.GETTABLE;
                }
                e.info = codeABC(op, 0, e.indT, e.indIdx);
                e.k = ExpDesc.Kind.VRELOCABLE;
            }
            case VVARARG, VCALL -> setoneret(e);
            default -> {
            }
        }
    }

    private int codeLabel(int a, int b, int jump) {
        getlabel();
        return codeABC(OpCode.LOADBOOL, a, b, jump);
    }

    private void discharge2reg(ExpDesc e, int reg) {
        dischargevars(e);
        switch (e.k) {
            case VNIL -> luaK_nil(reg, 1);
            case VFALSE, VTRUE -> codeABC(OpCode.LOADBOOL, reg, e.k == ExpDesc.Kind.VTRUE ? 1 : 0, 0);
            case VK -> codek(reg, e.info);
            case VKFLT -> codek(reg, numberK(e.nval));
            case VKINT -> codek(reg, intK(e.ival));
            case VRELOCABLE -> setCode(e.info, Instruction.withA(getCode(e.info), reg));
            case VNONRELOC -> {
                if (reg != e.info) {
                    codeABC(OpCode.MOVE, reg, e.info, 0);
                }
            }
            default -> {
                return; // VVOID or VJMP: nothing to do
            }
        }
        e.info = reg;
        e.k = ExpDesc.Kind.VNONRELOC;
    }

    private void discharge2anyreg(ExpDesc e) {
        if (e.k != ExpDesc.Kind.VNONRELOC) {
            reserveregs(1);
            discharge2reg(e, freereg - 1);
        }
    }

    private void exp2reg(ExpDesc e, int reg) {
        discharge2reg(e, reg);
        if (e.k == ExpDesc.Kind.VJMP) {
            e.t = concatRef(e.t, e.info);
        }
        if (e.hasJumps()) {
            int p_f = NO_JUMP;
            int p_t = NO_JUMP;
            if (need_value(e.t) != 0 || need_value(e.f) != 0) {
                int fj = (e.k == ExpDesc.Kind.VJMP) ? NO_JUMP : jump();
                p_f = codeLabel(reg, 0, 1);
                p_t = codeLabel(reg, 1, 0);
                patchtohere(fj);
            }
            int fin = getlabel();
            patchlistaux(e.f, fin, reg, p_f);
            patchlistaux(e.t, fin, reg, p_t);
        }
        e.f = e.t = NO_JUMP;
        e.info = reg;
        e.k = ExpDesc.Kind.VNONRELOC;
    }

    public void exp2nextreg(ExpDesc e) {
        dischargevars(e);
        freeexp(e);
        reserveregs(1);
        exp2reg(e, freereg - 1);
    }

    public int exp2anyreg(ExpDesc e) {
        dischargevars(e);
        if (e.k == ExpDesc.Kind.VNONRELOC) {
            if (!e.hasJumps()) {
                return e.info;
            }
            if (e.info >= nactvar) {
                exp2reg(e, e.info);
                return e.info;
            }
        }
        exp2nextreg(e);
        return e.info;
    }

    public void exp2anyregup(ExpDesc e) {
        if (e.k != ExpDesc.Kind.VUPVAL || e.hasJumps()) {
            exp2anyreg(e);
        }
    }

    public void exp2val(ExpDesc e) {
        if (e.hasJumps()) {
            exp2anyreg(e);
        } else {
            dischargevars(e);
        }
    }

    public int exp2RK(ExpDesc e) {
        exp2val(e);
        switch (e.k) {
            case VTRUE, VFALSE, VNIL -> {
                if (nk <= Instruction.MAXINDEXRK) {
                    e.info = (e.k == ExpDesc.Kind.VNIL) ? nilK() : boolK(e.k == ExpDesc.Kind.VTRUE);
                    e.k = ExpDesc.Kind.VK;
                    return Instruction.rkAsK(e.info);
                }
            }
            case VKINT -> {
                e.info = intK(e.ival);
                e.k = ExpDesc.Kind.VK;
                if (e.info <= Instruction.MAXINDEXRK) {
                    return Instruction.rkAsK(e.info);
                }
            }
            case VKFLT -> {
                e.info = numberK(e.nval);
                e.k = ExpDesc.Kind.VK;
                if (e.info <= Instruction.MAXINDEXRK) {
                    return Instruction.rkAsK(e.info);
                }
            }
            case VK -> {
                if (e.info <= Instruction.MAXINDEXRK) {
                    return Instruction.rkAsK(e.info);
                }
            }
            default -> {
            }
        }
        return exp2anyreg(e);
    }

    public void storevar(ExpDesc var, ExpDesc ex) {
        switch (var.k) {
            case VLOCAL -> {
                freeexp(ex);
                exp2reg(ex, var.info);
                return;
            }
            case VUPVAL -> {
                int e = exp2anyreg(ex);
                codeABC(OpCode.SETUPVAL, e, var.info, 0);
            }
            case VINDEXED -> {
                OpCode op = (var.indVt == ExpDesc.Kind.VLOCAL) ? OpCode.SETTABLE : OpCode.SETTABUP;
                int e = exp2RK(ex);
                codeABC(op, var.indT, var.indIdx, e);
            }
            default -> throw new LuaCompileException("invalid var kind to store");
        }
        freeexp(ex);
    }

    public void self(ExpDesc e, ExpDesc key) {
        exp2anyreg(e);
        int ereg = e.info;
        freeexp(e);
        e.info = freereg;
        e.k = ExpDesc.Kind.VNONRELOC;
        reserveregs(2);
        codeABC(OpCode.SELF, e.info, ereg, exp2RK(key));
        freeexp(key);
    }

    // -------------------------------------------------------------- 跳转

    public int luaK_nil(int from, int n) {
        int l = from + n - 1;
        if (pc > lasttarget) {
            int previous = code[pc - 1];
            if (Instruction.getOpcode(previous) == OpCode.LOADNIL) {
                int pfrom = Instruction.getA(previous);
                int pl = pfrom + Instruction.getB(previous);
                if ((pfrom <= from && from <= pl + 1) || (from <= pfrom && pfrom <= l + 1)) {
                    if (pfrom < from) {
                        from = pfrom;
                    }
                    if (pl > l) {
                        l = pl;
                    }
                    setCode(pc - 1, Instruction.withA(previous, from));
                    setCode(pc - 1, Instruction.withB(code[pc - 1], l - from));
                    return 0;
                }
            }
        }
        return codeABC(OpCode.LOADNIL, from, n - 1, 0);
    }

    public int jump() {
        int saved = jpc;
        jpc = NO_JUMP;
        int j = codeAsBx(OpCode.JMP, 0, NO_JUMP);
        j = concatRef(j, saved);
        return j;
    }

    public void ret(int first, int nret) {
        codeABC(OpCode.RETURN, first, nret + 1, 0);
    }

    private int condjump(OpCode op, int a, int b, int c) {
        codeABC(op, a, b, c);
        return jump();
    }

    private void fixjump(int pcIndex, int dest) {
        int offset = dest - (pcIndex + 1);
        if (Math.abs(offset) > Instruction.MAXARG_sBx) {
            throw new LuaCompileException("control structure too long");
        }
        setCode(pcIndex, Instruction.withSBx(code[pcIndex], offset));
    }

    public int getlabel() {
        lasttarget = pc;
        return pc;
    }

    private int getjump(int pcIndex) {
        int offset = Instruction.getsBx(code[pcIndex]);
        if (offset == NO_JUMP) {
            return NO_JUMP;
        }
        return (pcIndex + 1) + offset;
    }

    private int getjumpcontrol(int pcIndex) {
        if (pcIndex >= 1 && Instruction.getOpcode(code[pcIndex - 1]).isTest()) {
            return pcIndex - 1;
        }
        return pcIndex;
    }

    private int need_value(int list) {
        for (; list != NO_JUMP; list = getjump(list)) {
            int i = code[getjumpcontrol(list)];
            if (Instruction.getOpcode(i) != OpCode.TESTSET) {
                return 1;
            }
        }
        return 0;
    }

    private boolean patchtestreg(int node, int reg) {
        int i = getjumpcontrol(node);
        if (Instruction.getOpcode(code[i]) != OpCode.TESTSET) {
            return false;
        }
        if (reg != Instruction.NO_REG && reg != Instruction.getB(code[i])) {
            setCode(i, Instruction.withA(code[i], reg));
        } else {
            setCode(i, Instruction.createABC(OpCode.TEST, Instruction.getB(code[i]), 0, Instruction.getC(code[i])));
        }
        return true;
    }

    private void removevalues(int list) {
        for (; list != NO_JUMP; list = getjump(list)) {
            patchtestreg(list, Instruction.NO_REG);
        }
    }

    private void patchlistaux(int list, int vtarget, int reg, int dtarget) {
        while (list != NO_JUMP) {
            int next = getjump(list);
            if (patchtestreg(list, reg)) {
                fixjump(list, vtarget);
            } else {
                fixjump(list, dtarget);
            }
            list = next;
        }
    }

    private void dischargejpc() {
        patchlistaux(jpc, pc, Instruction.NO_REG, pc);
        jpc = NO_JUMP;
    }

    public void patchlist(int list, int target) {
        if (target == pc) {
            patchtohere(list);
        } else {
            patchlistaux(list, target, Instruction.NO_REG, target);
        }
    }

    public void patchclose(int list, int level) {
        level++;
        while (list != NO_JUMP) {
            int next = getjump(list);
            setCode(list, Instruction.withA(code[list], level));
            list = next;
        }
    }

    public void patchtohere(int list) {
        getlabel();
        jpc = concatRef(jpc, list);
    }

    /** 把跳转链表 l2 追加到 l1 之后，返回链表头 (对应 luaK_concat 的按引用语义)。 */
    public int concatRef(int l1, int l2) {
        if (l2 == NO_JUMP) {
            return l1;
        } else if (l1 == NO_JUMP) {
            return l2;
        } else {
            int list = l1;
            int next;
            while ((next = getjump(list)) != NO_JUMP) {
                list = next;
            }
            fixjump(list, l2);
            return l1;
        }
    }

    public void fixline(int line) {
        lineinfo[pc - 1] = line;
    }

    public void setlist(int base, int nelems, int tostore) {
        int c = (nelems - 1) / LuaBytecodeConstants.LFIELDS_PER_FLUSH + 1;
        int b = (tostore == LUA_MULTRET) ? 0 : tostore;
        if (c <= Instruction.MAXARG_C) {
            codeABC(OpCode.SETLIST, base, b, c);
        } else if (c <= Instruction.MAXARG_Ax) {
            codeABC(OpCode.SETLIST, base, b, 0);
            codeextraarg(c);
        } else {
            throw new LuaCompileException("constructor too long");
        }
        freereg = base + 1;
    }

    // ---------------------------------------------------------- 条件跳转

    private void invertjump(ExpDesc e) {
        int i = getjumpcontrol(e.info);
        setCode(i, Instruction.withA(code[i], Instruction.getA(code[i]) == 0 ? 1 : 0));
    }

    private int jumponcond(ExpDesc e, int cond) {
        if (e.k == ExpDesc.Kind.VRELOCABLE) {
            int ie = getCode(e.info);
            if (Instruction.getOpcode(ie) == OpCode.NOT) {
                pc--; // remove previous OP_NOT
                return condjump(OpCode.TEST, Instruction.getB(ie), 0, cond == 0 ? 1 : 0);
            }
        }
        discharge2anyreg(e);
        freeexp(e);
        return condjump(OpCode.TESTSET, Instruction.NO_REG, e.info, cond);
    }

    public void goiftrue(ExpDesc e) {
        int pcJump;
        dischargevars(e);
        switch (e.k) {
            case VJMP -> {
                invertjump(e);
                pcJump = e.info;
            }
            case VK, VKFLT, VKINT, VTRUE -> pcJump = NO_JUMP;
            default -> pcJump = jumponcond(e, 0);
        }
        e.f = concatRef(e.f, pcJump);
        patchtohere(e.t);
        e.t = NO_JUMP;
    }

    public void goiffalse(ExpDesc e) {
        int pcJump;
        dischargevars(e);
        switch (e.k) {
            case VJMP -> pcJump = e.info;
            case VNIL, VFALSE -> pcJump = NO_JUMP;
            default -> pcJump = jumponcond(e, 1);
        }
        e.t = concatRef(e.t, pcJump);
        patchtohere(e.f);
        e.f = NO_JUMP;
    }

    private void codenot(ExpDesc e) {
        dischargevars(e);
        switch (e.k) {
            case VNIL, VFALSE -> e.k = ExpDesc.Kind.VTRUE;
            case VK, VKFLT, VKINT, VTRUE -> e.k = ExpDesc.Kind.VFALSE;
            case VJMP -> invertjump(e);
            case VRELOCABLE, VNONRELOC -> {
                discharge2anyreg(e);
                freeexp(e);
                e.info = codeABC(OpCode.NOT, 0, e.info, 0);
                e.k = ExpDesc.Kind.VRELOCABLE;
            }
            default -> throw new LuaCompileException("cannot apply 'not'");
        }
        int temp = e.f;
        e.f = e.t;
        e.t = temp;
        removevalues(e.f);
        removevalues(e.t);
    }

    public void indexed(ExpDesc t, ExpDesc k) {
        t.indT = t.info;
        t.indIdx = exp2RK(k);
        t.indVt = (t.k == ExpDesc.Kind.VUPVAL) ? ExpDesc.Kind.VUPVAL : ExpDesc.Kind.VLOCAL;
        t.k = ExpDesc.Kind.VINDEXED;
    }

    // ------------------------------------------------------- 运算符生成

    private static boolean tonumeral(ExpDesc e, LuaConstant[] out) {
        if (e.t != NO_JUMP || e.f != NO_JUMP) {
            return false;
        }
        switch (e.k) {
            case VKINT -> {
                if (out != null) {
                    out[0] = new LuaConstant.Int(e.ival);
                }
                return true;
            }
            case VKFLT -> {
                if (out != null) {
                    out[0] = new LuaConstant.Flt(e.nval);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean constfolding(int op, ExpDesc e1, ExpDesc e2) {
        LuaConstant[] v1 = new LuaConstant[1];
        LuaConstant[] v2 = new LuaConstant[1];
        if (!tonumeral(e1, v1) || !tonumeral(e2, v2) || !LuaConstFold.validop(op, v1[0], v2[0])) {
            return false;
        }
        LuaConstant res = LuaConstFold.arith(op, v1[0], v2[0]);
        if (res == null) {
            return false;
        }
        if (res instanceof LuaConstant.Int it) {
            e1.k = ExpDesc.Kind.VKINT;
            e1.ival = it.value();
        } else if (res instanceof LuaConstant.Flt fl) {
            double n = fl.value();
            if (Double.isNaN(n) || n == 0) {
                return false;
            }
            e1.k = ExpDesc.Kind.VKFLT;
            e1.nval = n;
        }
        return true;
    }

    private void codeexpval(OpCode op, ExpDesc e1, ExpDesc e2, int line) {
        if (op.num() <= OpCode.BNOT.num()
                && constfolding(op.num() - OpCode.ADD.num(), e1, e2)) {
            return;
        }
        int o1;
        int o2;
        if (op == OpCode.UNM || op == OpCode.BNOT || op == OpCode.LEN) {
            o2 = 0;
            o1 = exp2anyreg(e1);
        } else {
            o2 = exp2RK(e2);
            o1 = exp2RK(e1);
        }
        if (o1 > o2) {
            freeexp(e1);
            freeexp(e2);
        } else {
            freeexp(e2);
            freeexp(e1);
        }
        e1.info = codeABC(op, 0, o1, o2);
        e1.k = ExpDesc.Kind.VRELOCABLE;
        fixline(line);
    }

    private void codecomp(OpCode op, int cond, ExpDesc e1, ExpDesc e2) {
        int o1 = exp2RK(e1);
        int o2 = exp2RK(e2);
        freeexp(e2);
        freeexp(e1);
        if (cond == 0 && op != OpCode.EQ) {
            int temp = o1;
            o1 = o2;
            o2 = temp;
            cond = 1;
        }
        e1.info = condjump(op, cond, o1, o2);
        e1.k = ExpDesc.Kind.VJMP;
    }

    public void prefix(UnOpr op, ExpDesc e, int line) {
        ExpDesc e2 = ExpDesc.vkint(0);
        switch (op) {
            case MINUS, BNOT, LEN -> {
                OpCode code = OpCode.VALUES[OpCode.UNM.num() + (op.ordinal() - UnOpr.MINUS.ordinal())];
                codeexpval(code, e, e2, line);
            }
            case NOT -> codenot(e);
            default -> throw new LuaCompileException("invalid unary operator");
        }
    }

    public void infix(BinOpr op, ExpDesc v) {
        switch (op) {
            case AND -> goiftrue(v);
            case OR -> goiffalse(v);
            case CONCAT -> exp2nextreg(v);
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW, BAND, BOR, BXOR, SHL, SHR -> {
                if (!tonumeral(v, null)) {
                    exp2RK(v);
                }
            }
            default -> exp2RK(v);
        }
    }

    public void posfix(BinOpr op, ExpDesc e1, ExpDesc e2, int line) {
        switch (op) {
            case AND -> {
                dischargevars(e2);
                e2.f = concatRef(e2.f, e1.f);
                e1.copyFrom(e2);
            }
            case OR -> {
                dischargevars(e2);
                e2.t = concatRef(e2.t, e1.t);
                e1.copyFrom(e2);
            }
            case CONCAT -> {
                exp2val(e2);
                if (e2.k == ExpDesc.Kind.VRELOCABLE
                        && Instruction.getOpcode(getCode(e2.info)) == OpCode.CONCAT) {
                    freeexp(e1);
                    setCode(e2.info, Instruction.withB(getCode(e2.info), e1.info));
                    e1.k = ExpDesc.Kind.VRELOCABLE;
                    e1.info = e2.info;
                } else {
                    exp2nextreg(e2);
                    codeexpval(OpCode.CONCAT, e1, e2, line);
                }
            }
            case ADD, SUB, MUL, DIV, IDIV, MOD, POW, BAND, BOR, BXOR, SHL, SHR -> {
                OpCode code = OpCode.VALUES[OpCode.ADD.num() + (op.ordinal() - BinOpr.ADD.ordinal())];
                codeexpval(code, e1, e2, line);
            }
            case EQ, LT, LE -> {
                OpCode code = OpCode.VALUES[OpCode.EQ.num() + (op.ordinal() - BinOpr.EQ.ordinal())];
                codecomp(code, 1, e1, e2);
            }
            case NE, GT, GE -> {
                OpCode code = OpCode.VALUES[OpCode.EQ.num() + (op.ordinal() - BinOpr.NE.ordinal())];
                codecomp(code, 0, e1, e2);
            }
            default -> throw new LuaCompileException("invalid binary operator");
        }
    }

    // ------------------------------------------------------------- 收尾

    /** 把动态数组写入 Proto，返回最终产物。 */
    public Proto close() {
        f.code = java.util.Arrays.copyOf(code, pc);
        f.lineinfo = java.util.Arrays.copyOf(lineinfo, pc);
        f.k = java.util.Arrays.copyOf(f.k, nk);
        f.p = java.util.Arrays.copyOf(p, np);
        f.locvars = java.util.Arrays.copyOf(locvars, nlocvars);
        f.upvalues = java.util.Arrays.copyOf(upvalues, nups);
        return f;
    }

    public int[] codeSnapshot(int n) {
        return java.util.Arrays.copyOf(code, n);
    }
}

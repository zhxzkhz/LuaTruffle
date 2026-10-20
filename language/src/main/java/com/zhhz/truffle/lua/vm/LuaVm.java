package com.zhhz.truffle.lua.vm;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.bytecode.Instruction;
import com.zhhz.truffle.lua.bytecode.LuaConstant;
import com.zhhz.truffle.lua.bytecode.LocVar;
import com.zhhz.truffle.lua.bytecode.OpCode;
import com.zhhz.truffle.lua.bytecode.Proto;
import com.zhhz.truffle.lua.bytecode.Upvaldesc;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToNumberNode;
import com.zhhz.truffle.lua.nodes.util.LuaTypesUtil;
import com.zhhz.truffle.lua.runtime.LuaBoolean;
import com.zhhz.truffle.lua.runtime.LuaCell;
import com.zhhz.truffle.lua.runtime.LuaContext;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaLightUserdata;
import com.zhhz.truffle.lua.runtime.LuaLineHook;
import com.zhhz.truffle.lua.runtime.LuaMetatables;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
import com.zhhz.truffle.lua.runtime.LuaString;
import com.zhhz.truffle.lua.runtime.LuaTable;

import java.util.Arrays;

/**
 * 移植自 {@code lvm.c} 的 Lua 5.3 字节码解释器。
 *
 * <p>本 VM 只用于执行来自二进制 chunk（{@code load}/{@code loadfile}/{@code dofile}）的函数，
 * 以及由 {@code string.dump} 往返得到的函数。源码 chunk 仍由 AST 引擎执行；两条路径共享同一套
 * 运行时值表示与元表机制。
 *
 * <p>寄存器模型：{@link Frame#regs} 为 {@code Object[]}。当某个局部变量被内层闭包捕获为 upvalue 时，
 * 其寄存器槽存放一个 {@link LuaCell}；读写时自动解包/写回，从而与 AST 引擎的闭包语义一致。
 */
public final class LuaVm {

    private static final int LFIELDS_PER_FLUSH = 50;
    private static final int LUA_MINSTACK = 20;
    private static final int MAXTAGLOOP = 200;
    private static final Object[] NO_ARGS = new Object[0];

    private final LuaContext context;

    /** 当前正在解释的帧（用于错误位置信息）。进入 runFrame 时设置，退出时恢复。 */
    private Frame curFrame;

    public LuaVm(LuaContext context) {
        this.context = context;
    }

    // =================================================================================
    // 帧
    // =================================================================================

    private static final class Frame {
        final Proto p;
        final Object[] k;
        final Object[] upvals;
        Object[] regs;
        Object[] varargs = NO_ARGS;
        int top;
        int pc;
        /** 对应的调试帧句柄（压入 LuaContext.getVmFrames()）。 */
        VmFrame dbg;
        /**
         * 结果直写目标：非空时 RETURN 把返回值直接写入该调用者帧的寄存器，
         * 调用者无需再接收/复制结果数组。{@code sinkWanted == 0} 表示「全部结果」。
         */
        Frame sinkCaller;
        int sinkDst;
        int sinkWanted;

        Frame(Proto p, Object[] upvals, Object[] k) {
            this.p = p;
            this.upvals = upvals;
            this.k = k;
            // 额外预留 LUA_MINSTACK(20) 个寄存器，容纳变长返回值溢出（对应 C 版
            // Lua 栈在 maxstacksize 之外预留的 LUA_MINSTACK 余量）。
            // 新数组全为 null；getR / VmFrame 读取时统一把 null 视为 nil，故无需预填充。
            this.regs = new Object[p.maxstacksize + LUA_MINSTACK];
            this.top = p.maxstacksize;
        }
    }

    // =================================================================================
    // 入口
    // =================================================================================

    /** 执行一个 VM 闭包，返回其结果数组。 */
    @TruffleBoundary
    public Object[] execute(LuaFunction function, Object[] args) {
        return execute(function, args, null, null);
    }

    /** 执行一个 VM 闭包，并记录其调用点解析出的 name/namewhat（供 debug.getinfo 'n'）。 */
    @TruffleBoundary
    public Object[] execute(LuaFunction function, Object[] args, String name, String namewhat) {
        return executeInternal(function, args, 0, args.length, name, namewhat, null, 0, 0);
    }

    /**
     * 以调用者寄存器区间 {@code regs[base, base+nargs)} 作为实参执行 VM 闭包，
     * 避免每次 VM→VM 调用都分配并复制一份 {@code Object[]}。
     */
    private Object[] executeSlice(LuaFunction function, Object[] regs, int base, int nargs,
                    String name, String namewhat) {
        return executeInternal(function, regs, base, nargs, name, namewhat, null, 0, 0);
    }

    /**
     * 执行 VM 闭包并把返回值直接写入 {@code caller} 的寄存器（从 {@code dst} 开始）。
     * {@code wanted == 0} 表示接收全部结果，此时由被调帧更新 {@code caller.top}。
     */
    private void executeInto(LuaFunction function, Object[] regs, int base, int nargs,
                    String name, String namewhat, Frame caller, int dst, int wanted) {
        executeInternal(function, regs, base, nargs, name, namewhat, caller, dst, wanted);
    }

    @TruffleBoundary
    private Object[] executeInternal(LuaFunction function, Object[] args, int base, int nargs,
                    String name, String namewhat, Frame sinkCaller, int sinkDst, int sinkWanted) {
        try {
            context.enterLuaCall(null);
        } catch (LuaException e) {
            throw positionStackError(e);
        }
        try {
            Proto p = function.getProto();
            Object[] upvals = function.getUpvalues();
            Object[] k = materializeConstants(p);
            Frame f = new Frame(p, upvals, k);
            f.sinkCaller = sinkCaller;
            f.sinkDst = sinkDst;
            f.sinkWanted = sinkWanted;

            int numParams = p.numparams;
            for (int i = 0; i < numParams; i++) {
                f.regs[i] = i < nargs ? slot(args, base + i) : LuaNil.SINGLETON;
            }
            if (p.isVararg != 0 && nargs > numParams) {
                int n = nargs - numParams;
                Object[] varargs = new Object[n];
                for (int i = 0; i < n; i++) {
                    varargs[i] = slot(args, base + numParams + i);
                }
                f.varargs = varargs;
            }

            VmFrame dbg = new VmFrame(function, p, f.regs);
            dbg.varargs = f.varargs;
            dbg.name = name;
            dbg.namewhat = namewhat;
            f.dbg = dbg;

            java.util.List<VmFrame> stack = context.getVmFrames();
            stack.add(dbg);
            try {
                return runFrame(f);
            } finally {
                stack.remove(stack.size() - 1);
            }
        } finally {
            context.exitLuaCall();
        }
    }

    /**
     * 为 {@link LuaContext#enterLuaCall} 抛出的无位置 "stack overflow" 补充当前 VM
     * 指令的源码位置前缀（等价 C 端 {@code luaG_runerror}）。非栈溢出错误原样返回。
     */
    private RuntimeException positionStackError(LuaException e) {
        Object errorObject = e.getErrorObject();
        String text = null;
        if (errorObject instanceof String s) {
            text = s;
        } else if (errorObject instanceof TruffleString ts) {
            text = ts.toJavaStringUncached();
        } else if (errorObject instanceof LuaString ls) {
            text = ls.getValue().toJavaStringUncached();
        }
        if (text == null || !text.contains("stack overflow")) {
            return e;
        }
        String full = withPosition(text);
        if (full.equals(text)) {
            return e;
        }
        return LuaException.positioned(full, full, null);
    }

    /**
     * 由已反序列化的 {@link Proto} 创建一个 VM 闭包。
     * 第一个 upvalue 作为 {@code _ENV}（环境，缺省为全局表）；其余初始化为 nil。
     */
    public static LuaFunction newClosure(LuaContext context, Proto proto, Object env) {
        LuaFunction function = new LuaFunction(context.getFunctionShape(), context.getVmCallTarget());
        function.setProto(proto);
        function.setVmClosure(true);
        int nup = proto.upvalues.length;
        Object[] upvalues = new Object[nup];
        Object envValue = env != null ? env : context.getGlobals();
        for (int i = 0; i < nup; i++) {
            upvalues[i] = new LuaCell(i == 0 ? envValue : LuaNil.SINGLETON);
        }
        function.setUpvalues(upvalues);
        return function;
    }

    private Object[] materializeConstants(Proto p) {
        Object[] cached = p.materializedConstants;
        if (cached != null) {
            return cached;
        }
        Object[] k = new Object[p.k.length];
        for (int i = 0; i < k.length; i++) {
            k[i] = materialize(p.k[i]);
        }
        p.materializedConstants = k;
        return k;
    }

    private Object materialize(LuaConstant c) {
        if (c instanceof LuaConstant.Int cInt) {
            return cInt.value();
        }
        if (c instanceof LuaConstant.Flt cFlt) {
            return cFlt.value();
        }
        if (c instanceof LuaConstant.Bool cBool) {
            return cBool.value();
        }
        if (c instanceof LuaConstant.Str cStr) {
            return TruffleString.fromByteArrayUncached(cStr.bytes(), 0, cStr.bytes().length,
                    LuaLanguage.STRING_ENCODING, false);
        }
        return LuaNil.SINGLETON;
    }

    // =================================================================================
    // 解释循环
    // =================================================================================

    @TruffleBoundary
    private Object[] runFrame(Frame f) {
        Frame previous = curFrame;
        curFrame = f;
        try {
            return runLoop(f);
        } finally {
            curFrame = previous;
        }
    }

    private Object[] runLoop(Frame f) {
        int[] code = f.p.code;
        Object[] k = f.k;
        while (true) {
            int idx = f.pc;
            dispatchHooks(f, idx);
            int i = code[idx];
            f.pc = idx + 1;
            if (f.dbg != null) {
                f.dbg.pc = idx;
            }
            OpCode op = Instruction.getOpcode(i);
            int a = Instruction.getA(i);
            switch (op) {
                case MOVE:
                    setR(f, a, getR(f, Instruction.getB(i)));
                    break;
                case LOADK:
                    setR(f, a, k[Instruction.getBx(i)]);
                    break;
                case LOADKX: {
                    int ax = Instruction.getAx(code[f.pc]);
                    f.pc++;
                    setR(f, a, k[ax]);
                    break;
                }
                case LOADBOOL:
                    setR(f, a, Instruction.getB(i) != 0);
                    if (Instruction.getC(i) != 0) {
                        f.pc++;
                    }
                    break;
                case LOADNIL: {
                    int b = Instruction.getB(i);
                    for (int j = 0; j <= b; j++) {
                        setR(f, a + j, LuaNil.SINGLETON);
                    }
                    break;
                }
                case GETUPVAL:
                    setR(f, a, upval(f, Instruction.getB(i)));
                    break;
                case GETTABUP:
                    setR(f, a, gettable(upval(f, Instruction.getB(i)), RK(f, Instruction.getC(i))));
                    break;
                case GETTABLE:
                    setR(f, a, gettable(getR(f, Instruction.getB(i)), RK(f, Instruction.getC(i))));
                    break;
                case SETTABUP:
                    settable(upval(f, a), RK(f, Instruction.getB(i)), RK(f, Instruction.getC(i)));
                    break;
                case SETUPVAL:
                    setUpval(f, Instruction.getB(i), getR(f, a));
                    break;
                case SETTABLE:
                    settable(getR(f, a), RK(f, Instruction.getB(i)), RK(f, Instruction.getC(i)));
                    break;
                case NEWTABLE:
                    setR(f, a, new LuaTable(context.getTableShape()));
                    break;
                case SELF: {
                    Object rb = getR(f, Instruction.getB(i));
                    setR(f, a + 1, rb);
                    setR(f, a, gettable(rb, RK(f, Instruction.getC(i))));
                    break;
                }
                case ADD: binArith(f, a, i, '+'); break;
                case SUB: binArith(f, a, i, '-'); break;
                case MUL: binArith(f, a, i, '*'); break;
                case DIV: binArith(f, a, i, '/'); break;
                case POW: binArith(f, a, i, '^'); break;
                case IDIV: binArith(f, a, i, '\\'); break;
                case MOD: binArith(f, a, i, '%'); break;
                case BAND: binBitwise(f, a, i, '&'); break;
                case BOR: binBitwise(f, a, i, '|'); break;
                case BXOR: binBitwise(f, a, i, '~'); break;
                case SHL: binBitwise(f, a, i, '<'); break;
                case SHR: binBitwise(f, a, i, '>'); break;
                case UNM: {
                    Object rb = getR(f, Instruction.getB(i));
                    if (rb instanceof Long l) {
                        setR(f, a, -l);
                    } else {
                        Double n = toFloat(rb);
                        if (n != null) {
                            setR(f, a, -n);
                        } else {
                            setR(f, a, unaryTM(rb, LuaMetatables.__UNM));
                        }
                    }
                    break;
                }
                case BNOT: {
                    Object rb = getR(f, Instruction.getB(i));
                    Long ib = toIntegerExact(rb);
                    if (ib != null) {
                        setR(f, a, ~ib);
                    } else {
                        setR(f, a, unaryTM(rb, LuaMetatables.__BNOT));
                    }
                    break;
                }
                case NOT:
                    setR(f, a, !isTruthy(getR(f, Instruction.getB(i))));
                    break;
                case LEN:
                    setR(f, a, objLen(getR(f, Instruction.getB(i))));
                    break;
                case CONCAT:
                    setR(f, a, concat(f, Instruction.getB(i), Instruction.getC(i)));
                    break;
                case JMP:
                    doJump(f, a, Instruction.getsBx(i), 0);
                    break;
                case EQ:
                    if ((equalsVal(RK(f, Instruction.getB(i)), RK(f, Instruction.getC(i))) ? 1 : 0) != a) {
                        f.pc++;
                    } else {
                        doNextJump(f);
                    }
                    break;
                case LT:
                    if ((lessThan(RK(f, Instruction.getB(i)), RK(f, Instruction.getC(i))) ? 1 : 0) != a) {
                        f.pc++;
                    } else {
                        doNextJump(f);
                    }
                    break;
                case LE:
                    if ((lessEqual(RK(f, Instruction.getB(i)), RK(f, Instruction.getC(i))) ? 1 : 0) != a) {
                        f.pc++;
                    } else {
                        doNextJump(f);
                    }
                    break;
                case TEST: {
                    Object ra = getR(f, a);
                    boolean cond = Instruction.getC(i) != 0 ? !isTruthy(ra) : isTruthy(ra);
                    if (cond) {
                        f.pc++;
                    } else {
                        doNextJump(f);
                    }
                    break;
                }
                case TESTSET: {
                    Object rb = getR(f, Instruction.getB(i));
                    boolean cond = Instruction.getC(i) != 0 ? !isTruthy(rb) : isTruthy(rb);
                    if (cond) {
                        f.pc++;
                    } else {
                        setR(f, a, rb);
                        doNextJump(f);
                    }
                    break;
                }
                case CALL: {
                    int b = Instruction.getB(i);
                    int c = Instruction.getC(i);
                    int nargs = Math.max(b != 0 ? b - 1 : f.top - a - 1, 0);
                    Object function = getR(f, a);
                    String[] nw = callName(f.p, idx, a, function);
                    if (function instanceof LuaFunction fn && fn.isVmClosure()) {
                        executeInto(fn, f.regs, a + 1, nargs, nw[0], nw[1], f, a,
                                        c == 0 ? 0 : c - 1);
                    } else {
                        Object[] results = callFrom(function, f.regs, a + 1, nargs, nw);
                        if (c == 0) {
                            for (int j = 0; j < results.length; j++) {
                                setR(f, a + j, results[j]);
                            }
                            f.top = a + results.length;
                        } else {
                            int nresults = c - 1;
                            for (int j = 0; j < nresults; j++) {
                                setR(f, a + j, j < results.length ? results[j] : LuaNil.SINGLETON);
                            }
                        }
                    }
                    break;
                }
                case TAILCALL: {
                    int b = Instruction.getB(i);
                    int nargs = Math.max(b != 0 ? b - 1 : f.top - a - 1, 0);
                    Object function = getR(f, a);
                    String[] nw = callName(f.p, idx, a, function);
                    Object[] results = callFrom(function, f.regs, a + 1, nargs, nw);
                    for (int j = 0; j < results.length; j++) {
                        setR(f, a + j, results[j]);
                    }
                    f.top = a + results.length;
                    break;
                }
                case RETURN: {
                    int b = Instruction.getB(i);
                    int n = Math.max(b != 0 ? b - 1 : f.top - a, 0);
                    if (f.p.sizep() > 0) {
                        closeUpvals(f, 0);
                    }
                    Frame caller = f.sinkCaller;
                    if (caller != null) {
                        int wanted = f.sinkWanted;
                        if (wanted == 0) {
                            for (int j = 0; j < n; j++) {
                                setR(caller, f.sinkDst + j, getR(f, a + j));
                            }
                            caller.top = f.sinkDst + n;
                        } else {
                            for (int j = 0; j < wanted; j++) {
                                setR(caller, f.sinkDst + j,
                                                j < n ? getR(f, a + j) : LuaNil.SINGLETON);
                            }
                        }
                        return null;
                    }
                    Object[] results = new Object[n];
                    for (int j = 0; j < n; j++) {
                        results[j] = getR(f, a + j);
                    }
                    return results;
                }
                case FORPREP: {
                    forPrep(f, a);
                    f.pc += Instruction.getsBx(i);
                    break;
                }
                case FORLOOP: {
                    if (forLoop(f, a)) {
                        f.pc += Instruction.getsBx(i);
                    }
                    break;
                }
                case TFORCALL: {
                    int c = Instruction.getC(i);
                    Object function = getR(f, a);
                    if (function instanceof LuaFunction fn && fn.isVmClosure()) {
                        executeInto(fn, f.regs, a + 1, 2, null, null, f, a + 3, c);
                    } else {
                        Object[] results = callFrom(function, f.regs, a + 1, 2, NO_NAME);
                        for (int j = 0; j < c; j++) {
                            setR(f, a + 3 + j, j < results.length ? results[j] : LuaNil.SINGLETON);
                        }
                    }
                    break;
                }
                case TFORLOOP: {
                    Object ctrl = getR(f, a + 1);
                    if (!isNil(ctrl)) {
                        setR(f, a, ctrl);
                        f.pc += Instruction.getsBx(i);
                    }
                    break;
                }
                case SETLIST: {
                    int b = Instruction.getB(i);
                    int c = Instruction.getC(i);
                    if (b == 0) {
                        b = f.top - a - 1;
                    }
                    if (c == 0) {
                        c = Instruction.getAx(code[f.pc]);
                        f.pc++;
                    }
                    LuaTable table = (LuaTable) getR(f, a);
                    long base = (long) (c - 1) * LFIELDS_PER_FLUSH;
                    for (int j = 1; j <= b; j++) {
                        table.rawset(base + j, getR(f, a + j));
                    }
                    break;
                }
                case CLOSURE: {
                    Proto child = f.p.p[Instruction.getBx(i)];
                    setR(f, a, makeClosure(f, child));
                    break;
                }
                case VARARG: {
                    int b = Instruction.getB(i) - 1;
                    Object[] va = f.varargs;
                    if (b < 0) {
                        b = va.length;
                        f.top = a + b;
                    }
                    for (int j = 0; j < b; j++) {
                        setR(f, a + j, j < va.length ? va[j] : LuaNil.SINGLETON);
                    }
                    break;
                }
                case EXTRAARG:
                    throw error("unexpected EXTRAARG instruction");
                default:
                    throw error("unsupported opcode " + op);
            }
        }
    }

    // =================================================================================
    // 寄存器 / upvalue 访问
    // =================================================================================
    /**
     * VM 解释器循环的钩子分发：在每个指令执行前检查 line/count 钩子，
     * 复刻 C 版 {@code luaG_traceexec}（ldebug.c:612）的行为：
     * <ul>
     *   <li>函数首条指令（prevIdx<0）触发行钩子；</li>
     *   <li>回边（idx <= prevIdx）强制触发；</li>
     *   <li>与上一条指令的行不同时触发。</li>
     * </ul>
     * {@code lastHookPc} 每条指令都更新（对应 C 版每条指令 {@code L->oldpc = savedpc}），
     * 这样函数调用返回后，下一条指令自动与调用指令的行比较（对应 {@code luaD_poscall}
     * 把 {@code oldpc} 重置为调用者的 savedpc），从而抑制同行事件的重复触发。
     */
    private void dispatchHooks(Frame f, int idx) {
        if (context.isAnyHookActive()) {
            boolean active = !context.isInHook();
            boolean needLine = active && context.isHookMask('l');
            boolean needCount = active && context.getHookCount() > 0;
            if (needCount) {
                LuaLineHook.fireCount(context);
            }
            if (needLine && f.dbg != null) {
                int[] lineinfo = f.p.lineinfo;
                if (lineinfo != null && idx >= 0 && idx < lineinfo.length) {
                    int prevIdx = f.dbg.lastHookPc;
                    int line = lineinfo[idx];
                    int prevLine = (prevIdx >= 0 && prevIdx < lineinfo.length) ? lineinfo[prevIdx] : -1;
                    if (line > 0 && (prevIdx < 0 || idx <= prevIdx || line != prevLine)) {
                        LuaLineHook.fireLine(context, line, true);
                    }
                }
            }
        }
        if (f.dbg != null) {
            f.dbg.lastHookPc = idx;
        }
    }

    private static Object getR(Frame f, int r) {
        if (r < 0 || r >= f.regs.length) {
            return LuaNil.SINGLETON;
        }
        return slot(f.regs, r);
    }

    /** 读取寄存器/实参数组的第 {@code i} 个槽：解包 cell，null 一律视为 nil。 */
    private static Object slot(Object[] regs, int i) {
        Object v = regs[i];
        if (v instanceof LuaCell c) {
            v = c.value;
        }
        return v == null ? LuaNil.SINGLETON : v;
    }

    private static void setR(Frame f, int r, Object v) {
        if (v == null) {
            v = LuaNil.SINGLETON;
        }
        if (r >= f.regs.length) {
            ensureRegs(f, r + 1);
        }
        Object cur = f.regs[r];
        if (cur instanceof LuaCell c) {
            c.value = v;
        } else {
            f.regs[r] = v;
        }
    }

    /**
     * 确保寄存器数组至少有 {@code needed} 个槽位（对应 C 版 Lua 的栈增长）。
     * 增长后同步更新调试帧持有的寄存器引用，保证 debug.getlocal 读到最新值。
     */
    private static void ensureRegs(Frame f, int needed) {
        if (needed <= f.regs.length) {
            return;
        }
        int oldCap = f.regs.length;
        int newCap = Math.max(needed, oldCap + (oldCap >> 1));
        f.regs = Arrays.copyOf(f.regs, newCap);
        Arrays.fill(f.regs, oldCap, newCap, LuaNil.SINGLETON);
        if (f.dbg != null) {
            f.dbg.regs = f.regs;
        }
    }

    private static Object upval(Frame f, int idx) {
        LuaCell cell = (LuaCell) f.upvals[idx];
        return cell.value == null ? LuaNil.SINGLETON : cell.value;
    }

    private static void setUpval(Frame f, int idx, Object v) {
        if (v == null) {
            v = LuaNil.SINGLETON;
        }
        ((LuaCell) f.upvals[idx]).value = v;
    }

    private Object RK(Frame f, int x) {
        return Instruction.isK(x) ? f.k[Instruction.indexK(x)] : getR(f, x);
    }

    private void doJump(Frame f, int a, int sBx, int extra) {
        if (a > 0) {
            closeUpvals(f, a - 1);
        }
        f.pc += sBx + extra;
    }

    private void doNextJump(Frame f) {
        int ni = f.p.code[f.pc];
        doJump(f, Instruction.getA(ni), Instruction.getsBx(ni), 1);
    }

    private void closeUpvals(Frame f, int from) {
        for (int i = from; i < f.regs.length; i++) {
            if (f.regs[i] instanceof LuaCell cell) {
                f.regs[i] = new LuaCell(cell.value == null ? LuaNil.SINGLETON : cell.value);
            }
        }
    }

    private LuaFunction makeClosure(Frame f, Proto child) {
        Object[] ups = new Object[child.upvalues.length];
        for (int j = 0; j < ups.length; j++) {
            Upvaldesc ud = child.upvalues[j];
            if (ud.instack) {
                Object slot = f.regs[ud.idx];
                if (!(slot instanceof LuaCell)) {
                    LuaCell cell = new LuaCell(slot == null ? LuaNil.SINGLETON : slot);
                    f.regs[ud.idx] = cell;
                    slot = cell;
                }
                ups[j] = slot;
            } else {
                ups[j] = f.upvals[ud.idx];
            }
        }
        // 闭包缓存（对应 lvm.c 的 getcached/pushclosure）：若已缓存的闭包
        // upvalue 全部一致（cell 身份相同），则复用，保证无 upvalue 闭包的相等性。
        Object cachedObj = child.cachedClosure;
        if (cachedObj instanceof LuaFunction cached) {
            Object[] cups = cached.getUpvalues();
            if (cups != null && cups.length == ups.length) {
                boolean match = true;
                for (int j = 0; j < ups.length; j++) {
                    if (cups[j] != ups[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return cached;
                }
            }
        }
        LuaFunction closure = new LuaFunction(context.getFunctionShape(), context.getVmCallTarget());
        closure.setProto(child);
        closure.setVmClosure(true);
        closure.setUpvalues(ups);
        child.cachedClosure = closure;
        return closure;
    }

    // =================================================================================
    // 数值 for
    // =================================================================================

    private void forPrep(Frame f, int a) {
        Object init = getR(f, a);
        Object limit = getR(f, a + 1);
        Object step = getR(f, a + 2);
        if (init instanceof Long il && step instanceof Long sl) {
            long[] ilimit = new long[1];
            boolean[] stopnow = new boolean[1];
            if (forLimit(limit, ilimit, sl, stopnow)) {
                long initv = stopnow[0] ? 0 : il;
                setR(f, a + 1, ilimit[0]);
                setR(f, a, initv - sl);
                return;
            }
        }
        Double nlimit = toFloat(limit);
        if (nlimit == null) {
            throw error("'for' limit must be a number");
        }
        setR(f, a + 1, nlimit);
        Double nstep = toFloat(step);
        if (nstep == null) {
            throw error("'for' step must be a number");
        }
        setR(f, a + 2, nstep);
        Double ninit = toFloat(init);
        if (ninit == null) {
            throw error("'for' initial value must be a number");
        }
        setR(f, a, ninit - nstep);
    }

    private boolean forLoop(Frame f, int a) {
        Object ra = getR(f, a);
        if (ra instanceof Long) {
            long step = ((Number) getR(f, a + 2)).longValue();
            long idx = (Long) ra + step;
            long limit = (Long) getR(f, a + 1);
            if ((0 < step) ? idx <= limit : limit <= idx) {
                // 控制变量与循环变量是同一个值，共享一次装箱。
                Long boxed = idx;
                setR(f, a, boxed);
                setR(f, a + 3, boxed);
                return true;
            }
        } else {
            double step = ((Number) getR(f, a + 2)).doubleValue();
            double idx = (Double) ra + step;
            double limit = ((Number) getR(f, a + 1)).doubleValue();
            if ((0 < step) ? idx <= limit : limit <= idx) {
                Double boxed = idx;
                setR(f, a, boxed);
                setR(f, a + 3, boxed);
                return true;
            }
        }
        return false;
    }

    private boolean forLimit(Object limit, long[] out, long step, boolean[] stopnow) {
        stopnow[0] = false;
        Long i = toIntegerRounded(limit, step < 0 ? 2 : 1);
        if (i != null) {
            out[0] = i;
            return true;
        }
        Double n = toFloat(limit);
        if (n == null) {
            return false;
        }
        if (n > 0) {
            out[0] = Long.MAX_VALUE;
            if (step < 0) {
                stopnow[0] = true;
            }
        } else {
            out[0] = Long.MIN_VALUE;
            if (step >= 0) {
                stopnow[0] = true;
            }
        }
        return true;
    }

    // =================================================================================
    // 算术 / 位运算
    // =================================================================================

    private void binArith(Frame f, int a, int i, char op) {
        Object b = RK(f, Instruction.getB(i));
        Object c = RK(f, Instruction.getC(i));
        Object nb = toNumberForArith(b);
        Object nc = toNumberForArith(c);
        if (nb != null && nc != null) {
            if (nb instanceof Long lb && nc instanceof Long lc) {
                setR(f, a, intArith(op, lb, lc));
                return;
            }
            double db = ((Number) nb).doubleValue();
            double dc = ((Number) nc).doubleValue();
            setR(f, a, floatArith(op, db, dc));
            return;
        }
        setR(f, a, arithTM(op, b, c));
    }

    private Object intArith(char op, long x, long y) {
        switch (op) {
            case '+': return x + y;
            case '-': return x - y;
            case '*': return x * y;
            case '/': return (double) x / (double) y;
            case '^': return Math.pow((double) x, (double) y);
            case '%': return luaMod(x, y);
            case '\\': return luaDiv(x, y);
            default: throw new IllegalStateException();
        }
    }

    private Object floatArith(char op, double x, double y) {
        switch (op) {
            case '+': return x + y;
            case '-': return x - y;
            case '*': return x * y;
            case '/': return x / y;
            case '^': return Math.pow(x, y);
            case '%': {
                double m = x % y;
                if ((m > 0) ? y < 0 : (m < 0 && y > 0)) {
                    m += y;
                }
                return m;
            }
            case '\\': return Math.floor(x / y);
            default: throw new IllegalStateException();
        }
    }

    private Object arithTM(char op, Object b, Object c) {
        TruffleString event = switch (op) {
            case '+' -> LuaMetatables.__ADD;
            case '-' -> LuaMetatables.__SUB;
            case '*' -> LuaMetatables.__MUL;
            case '/' -> LuaMetatables.__DIV;
            case '%' -> LuaMetatables.__MOD;
            case '^' -> LuaMetatables.__POW;
            case '\\' -> LuaMetatables.__IDIV;
            default -> throw new IllegalStateException();
        };
        Object tm = binTM(b, c, event);
        if (tm != NONE) {
            return tm;
        }
        Object bad = isNumberVal(b) ? c : b;
        throw typeError(bad, "perform arithmetic on");
    }

    private void binBitwise(Frame f, int a, int i, char op) {
        Object b = RK(f, Instruction.getB(i));
        Object c = RK(f, Instruction.getC(i));
        Long ib = toIntegerExact(b);
        Long ic = toIntegerExact(c);
        if (ib != null && ic != null) {
            switch (op) {
                case '&': setR(f, a, ib & ic); return;
                case '|': setR(f, a, ib | ic); return;
                case '~': setR(f, a, ib ^ ic); return;
                case '<': setR(f, a, shiftl(ib, ic)); return;
                default: setR(f, a, shiftl(ib, -ic)); return;
            }
        }
        TruffleString event = switch (op) {
            case '&' -> LuaMetatables.__BAND;
            case '|' -> LuaMetatables.__BOR;
            case '~' -> LuaMetatables.__BXOR;
            case '<' -> LuaMetatables.__SHL;
            default -> LuaMetatables.__SHR;
        };
        Object tm = binTM(b, c, event);
        if (tm != NONE) {
            setR(f, a, tm);
            return;
        }
        if (isNumberVal(b) && isNumberVal(c)) {
            throw error("number has no integer representation");
        }
        Object bad = isNumberVal(b) ? c : b;
        throw typeError(bad, "perform bitwise operation on");
    }

    private long luaDiv(long m, long n) {
        if (n == 0) {
            throw error("attempt to divide by zero");
        }
        if (n == -1) {
            return -m;
        }
        long q = m / n;
        if ((m ^ n) < 0 && m % n != 0) {
            q -= 1;
        }
        return q;
    }

    private long luaMod(long m, long n) {
        if (n == 0) {
            throw error("attempt to perform 'n%0'");
        }
        if (n == -1) {
            return 0;
        }
        long r = m % n;
        if (r != 0 && (m ^ n) < 0) {
            r += n;
        }
        return r;
    }

    private static long shiftl(long x, long y) {
        if (y < 0) {
            if (y <= -64) {
                return 0;
            }
            return x >>> (-y);
        }
        if (y >= 64) {
            return 0;
        }
        return x << y;
    }

    // =================================================================================
    // 比较 / 相等
    // =================================================================================

    private boolean lessThan(Object l, Object r) {
        if (l instanceof Long ll && r instanceof Long rl) {
            return ll < rl;
        }
        Double dl = toFloat(l);
        Double dr = toFloat(r);
        if (dl != null && dr != null) {
            return dl < dr;
        }
        if (isStringVal(l) && isStringVal(r)) {
            return compareStrings(asStringVal(l), asStringVal(r)) < 0;
        }
        int res = callOrderTM(l, r, LuaMetatables.__LT);
        if (res < 0) {
            throw error(LuaTypesUtil.orderErrorMessage(l, r));
        }
        return res != 0;
    }

    private boolean lessEqual(Object l, Object r) {
        if (l instanceof Long ll && r instanceof Long rl) {
            return ll <= rl;
        }
        Double dl = toFloat(l);
        Double dr = toFloat(r);
        if (dl != null && dr != null) {
            return dl <= dr;
        }
        if (isStringVal(l) && isStringVal(r)) {
            return compareStrings(asStringVal(l), asStringVal(r)) <= 0;
        }
        int res = callOrderTM(l, r, LuaMetatables.__LE);
        if (res >= 0) {
            return res != 0;
        }
        res = callOrderTM(r, l, LuaMetatables.__LT);
        if (res < 0) {
            throw error(LuaTypesUtil.orderErrorMessage(l, r));
        }
        return res == 0;
    }

    /** 返回 1/0，或 -1 表示没有元方法。 */
    private int callOrderTM(Object p1, Object p2, TruffleString event) {
        Object tm = metafield(p1, event);
        if (tm == null) {
            tm = metafield(p2, event);
        }
        if (tm == null) {
            return -1;
        }
        Object r = firstValue(callValue(tm, new Object[]{p1, p2}));
        return isTruthy(r) ? 1 : 0;
    }

    private boolean equalsVal(Object a, Object b) {
        boolean na = isNumberVal(a);
        boolean nb = isNumberVal(b);
        if (na || nb) {
            if (!na || !nb) {
                return false;
            }
            if (a instanceof Long la && b instanceof Long lb) {
                return la.longValue() == lb.longValue();
            }
            if (a instanceof Long la) {
                Double db = toFloat(b);
                return db != null && (double) la.longValue() == db;
            }
            if (b instanceof Long lb) {
                Double da = toFloat(a);
                return da != null && (double) lb.longValue() == da;
            }
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        if (a == b) {
            return true;
        }
        if (isNil(a) || isNil(b)) {
            return isNil(a) && isNil(b);
        }
        if (a == null || b == null) {
            return a == b;
        }
        if (isStringVal(a) && isStringVal(b)) {
            return asStringVal(a).equals(asStringVal(b));
        }
        boolean ba = a instanceof Boolean || a instanceof LuaBoolean;
        boolean bb = b instanceof Boolean || b instanceof LuaBoolean;
        if (ba || bb) {
            if (!ba || !bb) {
                return false;
            }
            return boolOf(a) == boolOf(b);
        }
        if (a instanceof LuaTable && b instanceof LuaTable) {
            Object tm = metafield(a, LuaMetatables.__EQ);
            if (tm == null) {
                tm = metafield(b, LuaMetatables.__EQ);
            }
            if (tm == null) {
                return false;
            }
            return isTruthy(firstValue(callValue(tm, new Object[]{a, b})));
        }
        return false;
    }

    // =================================================================================
    // 索引 / 长度 / 连接
    // =================================================================================

    private Object gettable(Object t, Object key) {
        for (int loop = 0; loop < MAXTAGLOOP; loop++) {
            Object tm;
            if (t instanceof LuaTable table) {
                Object res = table.rawget(key);
                if (!isNil(res) || (tm = metafield(t, LuaMetatables.__INDEX)) == null) {
                    return res == null ? LuaNil.SINGLETON : res;
                }
            } else {
                tm = metafield(t, LuaMetatables.__INDEX);
                if (tm == null) {
                    throw typeError(t, "index");
                }
            }
            if (tm instanceof LuaFunction) {
                return firstValue(callValue(tm, new Object[]{t, key}));
            }
            t = tm;
        }
        throw error("'__index' chain too long; possible loop");
    }

    private void settable(Object t, Object key, Object value) {
        for (int loop = 0; loop < MAXTAGLOOP; loop++) {
            Object tm;
            if (t instanceof LuaTable table) {
                Object oldval = table.rawget(key);
                if (!isNil(oldval)) {
                    table.rawset(key, value);
                    return;
                }
                tm = metafield(t, LuaMetatables.__NEWINDEX);
                if (tm == null) {
                    table.rawset(key, value);
                    return;
                }
            } else {
                tm = metafield(t, LuaMetatables.__NEWINDEX);
                if (tm == null) {
                    throw typeError(t, "index");
                }
            }
            if (tm instanceof LuaFunction) {
                callValue(tm, new Object[]{t, key, value});
                return;
            }
            t = tm;
        }
        throw error("'__newindex' chain too long; possible loop");
    }

    private Object objLen(Object rb) {
        if (rb instanceof LuaTable table) {
            Object tm = metafield(rb, LuaMetatables.__LEN);
            if (tm == null) {
                return (long) table.getArraySize();
            }
            return firstValue(callValue(tm, new Object[]{rb, rb}));
        }
        if (rb instanceof TruffleString ts) {
            return (long) ts.byteLength(LuaLanguage.STRING_ENCODING);
        }
        if (rb instanceof LuaString ls) {
            return (long) ls.getValue().byteLength(LuaLanguage.STRING_ENCODING);
        }
        Object tm = metafield(rb, LuaMetatables.__LEN);
        if (tm == null) {
            throw typeError(rb, "get length of");
        }
        return firstValue(callValue(tm, new Object[]{rb, rb}));
    }

    private Object concat(Frame f, int b, int c) {
        Object acc = getR(f, c);
        for (int j = c - 1; j >= b; j--) {
            acc = concatTwo(getR(f, j), acc);
        }
        return acc;
    }

    private Object concatTwo(Object p1, Object p2) {
        String s1 = stringOrNumber(p1);
        String s2 = stringOrNumber(p2);
        if (s1 != null && s2 != null) {
            return TruffleString.fromJavaStringUncached(s1 + s2, LuaLanguage.STRING_ENCODING);
        }
        Object tm = binTM(p1, p2, LuaMetatables.__CONCAT);
        if (tm != NONE) {
            return tm;
        }
        Object bad = (isStringVal(p1) || isNumberVal(p1)) ? p2 : p1;
        throw typeError(bad, "concatenate");
    }

    // =================================================================================
    // 元方法
    // =================================================================================

    private static final Object NONE = new Object();

    private Object metafield(Object value, TruffleString event) {
        LuaTable mt = context.getEffectiveMetatable(value);
        if (mt == null) {
            return null;
        }
        Object v = mt.rawget(event);
        return isNil(v) ? null : v;
    }

    private Object binTM(Object p1, Object p2, TruffleString event) {
        Object tm = metafield(p1, event);
        if (tm == null) {
            tm = metafield(p2, event);
        }
        if (tm == null) {
            return NONE;
        }
        return firstValue(callValue(tm, new Object[]{p1, p2}));
    }

    private Object unaryTM(Object v, TruffleString event) {
        Object tm = metafield(v, event);
        if (tm == null) {
            throw typeError(v, "perform arithmetic on");
        }
        return firstValue(callValue(tm, new Object[]{v, v}));
    }

    private Object firstValue(Object[] results) {
        return results.length == 0 ? LuaNil.SINGLETON : results[0];
    }

    // =================================================================================
    // 调用
    // =================================================================================

    private Object[] callValue(Object function, Object[] args) {
        return callValue(function, args, null, null);
    }

    /**
     * 从调用者寄存器区间发起调用。VM 闭包直接以 {@code (regs, base, nargs)} 绑定形参，
     * 只有内置函数或带 {@code __call} 的值才需要实体化实参数组。
     */
    private Object[] callFrom(Object function, Object[] regs, int base, int nargs, String[] nw) {
        if (function instanceof LuaFunction fn && fn.isVmClosure()) {
            return executeSlice(fn, regs, base, nargs, nw[0], nw[1]);
        }
        Object[] args = new Object[nargs];
        for (int j = 0; j < nargs; j++) {
            args[j] = slot(regs, base + j);
        }
        return callValue(function, args, nw[0], nw[1]);
    }

    private Object[] callValue(Object function, Object[] args, String name, String namewhat) {
        if (!(function instanceof LuaFunction fn)) {
            Object tm = metafield(function, LuaMetatables.__CALL);
            if (tm instanceof LuaFunction) {
                Object[] withSelf = new Object[args.length + 1];
                withSelf[0] = function;
                System.arraycopy(args, 0, withSelf, 1, args.length);
                return callValue(tm, withSelf);
            }
            throw typeError(function, "call");
        }
        if (fn.isVmClosure()) {
            return execute(fn, args, name, namewhat);
        }
        Object result;
        try {
            result = InteropLibrary.getUncached().execute(fn, args);
        } catch (com.oracle.truffle.api.interop.InteropException e) {
            throw error(e.getMessage());
        } catch (LuaException e) {
            throw decorate(e);
        }
        return toArray(result);
    }

    private static Object[] toArray(Object result) {
        if (result instanceof LuaMultiValue mv) {
            return mv.values();
        }
        if (result == null) {
            return new Object[]{LuaNil.SINGLETON};
        }
        return new Object[]{result};
    }

    // =================================================================================
    // 调用点的函数名解析（复刻 ldebug.c 的 funcnamefromcode/getobjname）
    // =================================================================================

    private static final String[] NO_NAME = {null, null};

    /** 仅对 VM 闭包解析调用点 name/namewhat；其余返回 {null,null}。 */
    private static String[] callName(Proto p, int callPc, int funcReg, Object function) {
        if (!(function instanceof LuaFunction fn) || !fn.isVmClosure()) {
            return NO_NAME;
        }
        // 结果只依赖 (code, k, locvars) 与调用点 pc（同一 pc 的 CALL 指令寄存器固定），
        // 故可按 pc 缓存，避免每次调用都做 O(sizecode) 的 findSetReg 扫描。
        String[][] cache = p.cachedCallNames;
        if (cache != null && callPc >= 0 && callPc < cache.length) {
            String[] hit = cache[callPc];
            if (hit != null) {
                return hit;
            }
        }
        String local = localNameAt(p, funcReg + 1, callPc);
        String[] result;
        if (local != null) {
            result = new String[]{local, "local"};
        } else {
            result = decodeObjName(p, findSetReg(p, callPc, funcReg), funcReg);
        }
        if (cache == null) {
            cache = new String[p.code.length][];
            p.cachedCallNames = cache;
        }
        if (callPc >= 0 && callPc < cache.length) {
            cache[callPc] = result;
        }
        return result;
    }

    /** 复刻 luaF_getlocalname：第 n 个（1-based）活跃局部变量名，无则 null。 */
    private static String localNameAt(Proto p, int n, int pc) {
        if (n <= 0) {
            return null;
        }
        LocVar[] locvars = p.locvars;
        int remaining = n;
        for (int i = 0; i < locvars.length && locvars[i].startpc <= pc; i++) {
            if (pc < locvars[i].endpc) {
                remaining--;
                if (remaining == 0) {
                    return locvars[i].varname;
                }
            }
        }
        return null;
    }

    /** 逆向查找最后一个写入寄存器 {@code reg} 的指令；无则 -1（简化版 findsetreg）。 */
    private static int findSetReg(Proto p, int lastpc, int reg) {
        int setreg = -1;
        int[] code = p.code;
        int limit = Math.min(Math.max(lastpc, 0), code.length);
        for (int pc = 0; pc < limit; pc++) {
            int i = code[pc];
            if (Instruction.getA(i) == reg && writesA(Instruction.getOpcode(i))) {
                setreg = pc;
            }
        }
        return setreg;
    }

    /** 该 opcode 是否会写入其 A 寄存器（用于寄存器溯源）。 */
    private static boolean writesA(OpCode op) {
        return switch (op) {
            case MOVE, LOADK, LOADKX, LOADBOOL, LOADNIL, GETUPVAL, GETTABUP, GETTABLE,
                 NEWTABLE, SELF, ADD, SUB, MUL, DIV, MOD, POW, IDIV, BAND, BOR, BXOR,
                 SHL, SHR, UNM, BNOT, NOT, LEN, CONCAT, CLOSURE, VARARG, TFORLOOP,
                 TESTSET, FORLOOP -> true;
            default -> false;
        };
    }

    /** 复刻 getobjname：把「设置函数寄存器的指令」解码为 (name, namewhat)。 */
    private static String[] decodeObjName(Proto p, int pc, int reg) {
        if (pc < 0 || pc >= p.code.length) {
            return NO_NAME;
        }
        int i = p.code[pc];
        switch (Instruction.getOpcode(i)) {
            case MOVE: {
                int b = Instruction.getB(i);
                if (b < Instruction.getA(i)) {
                    String local = localNameAt(p, b + 1, pc);
                    if (local != null) {
                        return new String[]{local, "local"};
                    }
                    return decodeObjName(p, findSetReg(p, pc, b), b);
                }
                return NO_NAME;
            }
            case GETUPVAL:
                return new String[]{upvalName(p, Instruction.getBx(i)), "upvalue"};
            case GETTABUP: {
                int b = Instruction.getB(i);
                String table = upvalName(p, b);
                String key = rkString(p, Instruction.getC(i));
                return new String[]{key, "_ENV".equals(table) ? "global" : "field"};
            }
            case GETTABLE:
                return new String[]{rkString(p, Instruction.getC(i)), "field"};
            case SELF:
                return new String[]{rkString(p, Instruction.getC(i)), "method"};
            default:
                return NO_NAME;
        }
    }

    private static String upvalName(Proto p, int idx) {
        if (idx >= 0 && idx < p.upvalues.length) {
            return p.upvalues[idx].name;
        }
        return null;
    }

    /** 解析 RK(C) 操作数中的字符串常量（BITRK 位表示常量索引）；寄存器或非字符串返回 null。 */
    private static String rkString(Proto p, int rk) {
        if ((rk & 0x100) == 0) {
            return null; // 是寄存器，不是常量
        }
        int idx = rk & 0xFF;
        if (idx < p.k.length && p.k[idx] instanceof LuaConstant.Str s) {
            return s.asString();
        }
        return null;
    }

    // =================================================================================
    // 工具
    // =================================================================================

    private static boolean isNil(Object o) {
        return o == null || o == LuaNil.SINGLETON;
    }

    private static boolean isStringVal(Object o) {
        return o instanceof TruffleString || o instanceof LuaString || o instanceof String;
    }

    private static String asStringVal(Object o) {
        if (o instanceof TruffleString ts) {
            return ts.toJavaStringUncached();
        }
        if (o instanceof LuaString ls) {
            return ls.getValue().toJavaStringUncached();
        }
        return (String) o;
    }

    private static boolean isNumberVal(Object o) {
        return o instanceof Long || o instanceof Double || o instanceof Integer || o instanceof Float
                || o instanceof Short || o instanceof Byte;
    }

    private static boolean isTruthy(Object o) {
        return LuaTypesUtil.isTruthy(o);
    }

    private static boolean boolOf(Object o) {
        if (o instanceof Boolean b) {
            return b;
        }
        return ((LuaBoolean) o).asBoolean();
    }

    private static String stringOrNumber(Object o) {
        if (o instanceof TruffleString ts) {
            return ts.toJavaStringUncached();
        }
        if (o instanceof LuaString ls) {
            return ls.getValue().toJavaStringUncached();
        }
        if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
            return Long.toString(((Number) o).longValue());
        }
        if (o instanceof Double || o instanceof Float) {
            return String.valueOf(((Number) o).doubleValue());
        }
        return null;
    }

    private static int compareStrings(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            int ca = a.charAt(i) & 0xFF;
            int cb = b.charAt(i) & 0xFF;
            if (ca != cb) {
                return ca - cb;
            }
        }
        return a.length() - b.length();
    }

    private Object toNumberForArith(Object o) {
        if (o instanceof Long || o instanceof Double) {
            return o;
        }
        if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
            return ((Number) o).longValue();
        }
        if (o instanceof Float) {
            return ((Number) o).doubleValue();
        }
        if (isStringVal(o)) {
            Object n = LuaCoerceToNumberNode.parseLuaNumber(asStringVal(o));
            if (n instanceof Long l) {
                return l.doubleValue();
            }
            return n;
        }
        return null;
    }

    private Double toFloat(Object o) {
        if (o instanceof Double d) {
            return d;
        }
        if (o instanceof Long l) {
            return (double) l.longValue();
        }
        if (o instanceof Integer || o instanceof Short || o instanceof Byte || o instanceof Float) {
            return ((Number) o).doubleValue();
        }
        if (isStringVal(o)) {
            Object n = LuaCoerceToNumberNode.parseLuaNumber(asStringVal(o));
            if (n instanceof Long l) {
                return l.doubleValue();
            }
            if (n instanceof Double d) {
                return d;
            }
        }
        return null;
    }

    private Long toIntegerExact(Object o) {
        return toIntegerRounded(o, 0);
    }

    /** mode: 0 精确，1 向下取整，2 向上取整；无法转换返回 null。 */
    private Long toIntegerRounded(Object o, int mode) {
        if (o instanceof Long l) {
            return l;
        }
        if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
            return ((Number) o).longValue();
        }
        if (o instanceof Double || o instanceof Float) {
            double n = ((Number) o).doubleValue();
            double f = Math.floor(n);
            if (n != f) {
                if (mode == 0) {
                    return null;
                } else if (mode > 1) {
                    f += 1;
                }
            }
            if (f >= (double) Long.MIN_VALUE && f < 9.223372036854776E18 && !Double.isNaN(f)) {
                return (long) f;
            }
            return null;
        }
        if (isStringVal(o)) {
            Object n = LuaCoerceToNumberNode.parseLuaNumber(asStringVal(o));
            if (n != null) {
                return toIntegerRounded(n, mode);
            }
        }
        return null;
    }

    // =================================================================================
    // 错误
    // =================================================================================

    private RuntimeException error(String message) {
        String full = withPosition(message);
        return LuaException.positioned(full, full, null);
    }

    /**
     * 在调用边界为被调函数抛出的「无位置」字符串错误补上 VM 当前 {@code chunkid:line:} 前缀
     * （等价 C 端 {@code luaL_where(L,1)} / {@code luaG_addinfo}）。仅处理字符串错误对象，
     * 表/数字等原样透传；已带位置的异常不重复加前缀。
     */
    private RuntimeException decorate(LuaException exception) {
        if (exception.isPositioned()) {
            return exception;
        }
        Object errorObject = exception.getErrorObject();
        String text = null;
        if (errorObject instanceof String s) {
            text = s;
        } else if (errorObject instanceof TruffleString ts) {
            text = ts.toJavaStringUncached();
        } else if (errorObject instanceof LuaString ls) {
            text = ls.getValue().toJavaStringUncached();
        }
        if (text == null) {
            return exception;
        }
        String full = withPosition(text);
        if (full.equals(text)) {
            return exception;
        }
        return LuaException.positioned(full, full, null);
    }

    private RuntimeException typeError(Object o, String operation) {
        return error("attempt to " + operation + " a " + objTypeName(o) + " value");
    }

    private String objTypeName(Object o) {
        if (o != null) {
            LuaTable mt = context.getEffectiveMetatable(o);
            if (mt != null) {
                Object name = mt.rawget(LuaMetatables.__NAME);
                if (name instanceof TruffleString ts) {
                    return ts.toJavaStringUncached();
                }
            }
        }
        return LuaTypesUtil.getTypeName(o);
    }

    private String withPosition(String message) {
        Frame f = curFrame;
        if (f == null) {
            return message;
        }
        Proto p = f.p;
        int line = -1;
        int[] li = p.lineinfo;
        if (li != null && li.length > 0) {
            int pc = f.dbg != null ? f.dbg.pc : f.pc - 1;
            if (pc < 0 || pc >= li.length) {
                pc = li.length - 1;
            }
            line = li[pc];
        }
        String chunkId = LuaException.chunkId(p.source);
        return chunkId + ":" + line + ": " + message;
    }
}

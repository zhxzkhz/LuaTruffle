package com.zhhz.truffle.lua.vm;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaException;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.bytecode.Instruction;
import com.zhhz.truffle.lua.bytecode.LuaConstant;
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
    private static final int MAXTAGLOOP = 200;
    private static final Object[] NO_ARGS = new Object[0];

    private final LuaContext context;

    private Proto curProto;
    private int curPc;

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
        final Object[] regs;
        Object[] varargs = NO_ARGS;
        int top;
        int pc;

        Frame(Proto p, Object[] upvals, Object[] k) {
            this.p = p;
            this.upvals = upvals;
            this.k = k;
            this.regs = new Object[p.maxstacksize];
            Arrays.fill(this.regs, LuaNil.SINGLETON);
            this.top = p.maxstacksize;
        }
    }

    // =================================================================================
    // 入口
    // =================================================================================

    /** 执行一个 VM 闭包，返回其结果数组。 */
    @TruffleBoundary
    public Object[] execute(LuaFunction function, Object[] args) {
        Proto p = function.getProto();
        Object[] upvals = function.getUpvalues();
        Object[] k = materializeConstants(p);
        Frame f = new Frame(p, upvals, k);

        int numParams = p.numparams;
        for (int i = 0; i < numParams; i++) {
            Object v = i < args.length ? args[i] : LuaNil.SINGLETON;
            f.regs[i] = v == null ? LuaNil.SINGLETON : v;
        }
        if ((p.isVararg & 2) != 0 && args.length > numParams) {
            f.varargs = Arrays.copyOfRange(args, numParams, args.length);
        }

        curProto = p;
        curPc = 0;
        return runFrame(f);
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
        Object[] k = new Object[p.k.length];
        for (int i = 0; i < k.length; i++) {
            k[i] = materialize(p.k[i]);
        }
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
        int[] code = f.p.code;
        Object[] k = f.k;
        while (true) {
            int idx = f.pc;
            int i = code[idx];
            f.pc = idx + 1;
            curProto = f.p;
            curPc = idx;
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
                    int nargs = b != 0 ? b - 1 : f.top - a - 1;
                    Object[] callArgs = new Object[Math.max(nargs, 0)];
                    for (int j = 0; j < callArgs.length; j++) {
                        callArgs[j] = getR(f, a + 1 + j);
                    }
                    Object[] results = callValue(getR(f, a), callArgs);
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
                    break;
                }
                case TAILCALL: {
                    int b = Instruction.getB(i);
                    int nargs = b != 0 ? b - 1 : f.top - a - 1;
                    Object[] callArgs = new Object[Math.max(nargs, 0)];
                    for (int j = 0; j < callArgs.length; j++) {
                        callArgs[j] = getR(f, a + 1 + j);
                    }
                    Object[] results = callValue(getR(f, a), callArgs);
                    for (int j = 0; j < results.length; j++) {
                        setR(f, a + j, results[j]);
                    }
                    f.top = a + results.length;
                    break;
                }
                case RETURN: {
                    int b = Instruction.getB(i);
                    int n = b != 0 ? b - 1 : f.top - a;
                    Object[] results = new Object[Math.max(n, 0)];
                    for (int j = 0; j < results.length; j++) {
                        results[j] = getR(f, a + j);
                    }
                    if (f.p.sizep() > 0) {
                        closeUpvals(f, 0);
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
                    Object[] callArgs = {getR(f, a + 1), getR(f, a + 2)};
                    Object[] results = callValue(getR(f, a), callArgs);
                    for (int j = 0; j < c; j++) {
                        setR(f, a + 3 + j, j < results.length ? results[j] : LuaNil.SINGLETON);
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
                    long last = (long) (c - 1) * LFIELDS_PER_FLUSH + b;
                    for (int j = b; j > 0; j--) {
                        table.rawset(last--, getR(f, a + j));
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

    private static Object getR(Frame f, int r) {
        Object v = f.regs[r];
        if (v instanceof LuaCell c) {
            return c.value == null ? LuaNil.SINGLETON : c.value;
        }
        return v == null ? LuaNil.SINGLETON : v;
    }

    private static void setR(Frame f, int r, Object v) {
        if (v == null) {
            v = LuaNil.SINGLETON;
        }
        Object cur = f.regs[r];
        if (cur instanceof LuaCell c) {
            c.value = v;
        } else {
            f.regs[r] = v;
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
        LuaFunction closure = new LuaFunction(context.getFunctionShape(), context.getVmCallTarget());
        closure.setProto(child);
        closure.setVmClosure(true);
        closure.setUpvalues(ups);
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
                setR(f, a, idx);
                setR(f, a + 3, idx);
                return true;
            }
        } else {
            double step = ((Number) getR(f, a + 2)).doubleValue();
            double idx = (Double) ra + step;
            double limit = ((Number) getR(f, a + 1)).doubleValue();
            if ((0 < step) ? idx <= limit : limit <= idx) {
                setR(f, a, idx);
                setR(f, a + 3, idx);
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
            case '%': return luaMod(x, y);
            case '\\': return luaDiv(x, y);
            default: return Math.pow((double) x, (double) y);
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
            return x >> (-y);
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
            return execute(fn, args);
        }
        Object result;
        try {
            result = InteropLibrary.getUncached().execute(fn, args);
        } catch (com.oracle.truffle.api.interop.InteropException e) {
            throw error(e.getMessage());
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
        return LuaException.create(withPosition(message), null);
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
        if (curProto == null) {
            return message;
        }
        int line = -1;
        int[] li = curProto.lineinfo;
        if (li != null && li.length > 0) {
            int pc = curPc;
            if (pc < 0 || pc >= li.length) {
                pc = li.length - 1;
            }
            line = li[pc];
        }
        String chunkId = LuaException.chunkId(curProto.source);
        return chunkId + ":" + line + ": " + message;
    }
}

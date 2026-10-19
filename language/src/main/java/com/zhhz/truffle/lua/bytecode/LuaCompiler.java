package com.zhhz.truffle.lua.bytecode;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.zhhz.truffle.lua.parser.LuaLexer;
import com.zhhz.truffle.lua.parser.LuaParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Lua 5.3 单遍编译器 (lparser.c 的忠实移植)，输入为 ANTLR 生成的语法分析树 (CST)。
 *
 * <p>与 C 版本的区别只有一个：token 流由 ANTLR 预先解析成 CST，因此原本依赖
 * "读下一个 token" 的判断改为检查 CST 的结构。表达式优先级通过直接递归 CST
 * (语法已编码结合性) 复现，调用 {@link FuncState} 中 lcode.c 生成的中间代码。
 */
public final class LuaCompiler {

    private static final int MAXVARS = 200;
    private static final int LUAI_MAXCCALLS = 200;
    private static final String ENV_NAME = "_ENV";

    private final CompilerContext ctx = new CompilerContext();
    private FuncState fs;
    private final Dyndata dyd = new Dyndata();
    private int nCcalls = 0;

    private int loopDepth = 0;
    private int continueList = FuncState.NO_JUMP;

    private LuaCompiler(String sourceName) {
        ctx.source = sourceName;
    }

    public static Proto compile(LuaParser.LualanguageContext tree, String sourceName) {
        LuaCompiler c = new LuaCompiler(sourceName);
        c.ctx.text = textOf(tree);
        return c.doCompile(tree);
    }

    public static Proto compile(LuaParser.ChunkContext tree, String sourceName) {
        LuaCompiler c = new LuaCompiler(sourceName);
        c.ctx.text = textOf(tree);
        return c.mainfunc(tree.block());
    }

    /**
     * 便捷入口：从 Lua 源码文本编译出主 {@link Proto}。文本按“字节模型”处理
     * （见 {@code LuaBaseParser.toByteModelText}）：每个字符对应一个源字节。
     */
    public static Proto compile(String source, String sourceName) {
        LuaLexer lexer = new LuaLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LuaParser parser = new LuaParser(tokens);
        parser.removeErrorListeners();
        LuaCompiler c = new LuaCompiler(sourceName);
        c.ctx.text = source;
        return c.mainfunc(parser.lualanguage().chunk().block());
    }

    /** 从已解析的语法树取回源码文本（对应 ANTLR 的输入流）。取不到时返回 null。 */
    private static String textOf(ParserRuleContext tree) {
        try {
            Token t = tree.getStart();
            if (t != null && t.getTokenSource() != null && t.getTokenSource().getInputStream() != null) {
                return t.getTokenSource().getInputStream().toString();
            }
        } catch (RuntimeException ignore) {
            // 取不到源码时退回节点行号，不影响正确性
        }
        return null;
    }

    private Proto doCompile(LuaParser.LualanguageContext tree) {
        return mainfunc(tree.chunk().block());
    }

    // ================================================================ 辅助结构

    static final class Labeldesc {
        String name;
        int line;
        int nactvar;
        int pc;
    }

    static final class Labellist {
        Labeldesc[] arr = new Labeldesc[0];
        int n;

        void add(Labeldesc d) {
            if (n >= arr.length) {
                arr = java.util.Arrays.copyOf(arr, Math.max(4, arr.length * 2));
            }
            arr[n++] = d;
        }

        void remove(int i) {
            for (int j = i; j < n - 1; j++) {
                arr[j] = arr[j + 1];
            }
            n--;
        }
    }

    static final class Dyndata {
        int[] actvar = new int[0];
        int nactvar;
        final Labellist gt = new Labellist();
        final Labellist label = new Labellist();

        void addActvar(int idx) {
            if (nactvar >= actvar.length) {
                actvar = java.util.Arrays.copyOf(actvar, Math.max(8, actvar.length * 2));
            }
            actvar[nactvar++] = idx;
        }
    }

    static final class ConsControl {
        ExpDesc v = new ExpDesc();
        ExpDesc t;
        int nh;
        int na;
        int tostore;
    }

    // ================================================================ 入口

    private Proto mainfunc(LuaParser.BlockContext block) {
        FuncState root = new FuncState(null, ctx);
        BlockCnt bl = new BlockCnt();
        openFunc(root, bl);
        root.f.isVararg = 1;
        ExpDesc v = new ExpDesc();
        v.k = ExpDesc.Kind.VLOCAL;
        v.info = 0;
        root.newUpvalue(ENV_NAME, v);
        statlist(block, 0);
        closeFunc();
        return root.f;
    }

    // ============================================================ 作用域

    private void openFunc(FuncState nfs, BlockCnt bl) {
        nfs.pc = 0;
        nfs.lasttarget = 0;
        nfs.jpc = FuncState.NO_JUMP;
        nfs.freereg = 0;
        nfs.nk = 0;
        nfs.np = 0;
        nfs.nups = 0;
        nfs.nlocvars = 0;
        nfs.nactvar = 0;
        nfs.firstlocal = dyd.nactvar;
        nfs.bl = null;
        nfs.f.source = ctx.source;
        nfs.f.maxstacksize = 2;
        fs = nfs;
        enterblock(nfs, bl, false);
    }

    private void closeFunc() {
        fs.ret(0, 0);
        leaveblock();
        fs.close();
        fs = fs.prev;
    }

    private void enterblock(FuncState f, BlockCnt bl, boolean isloop) {
        bl.isloop = isloop;
        bl.nactvar = f.nactvar;
        bl.firstlabel = dyd.label.n;
        bl.firstgoto = dyd.gt.n;
        bl.upval = false;
        bl.previous = f.bl;
        f.bl = bl;
    }

    private void leaveblock() {
        BlockCnt bl = fs.bl;
        if (bl.previous != null && bl.upval) {
            int j = fs.jump();
            fs.patchclose(j, bl.nactvar);
            fs.patchtohere(j);
        }
        if (bl.isloop) {
            breaklabel();
        }
        fs.bl = bl.previous;
        removevars(bl.nactvar);
        fs.freereg = fs.nactvar;
        dyd.label.n = bl.firstlabel;
        if (bl.previous != null) {
            movegotosout(fs, bl);
        } else if (bl.firstgoto < dyd.gt.n) {
            undefgoto(dyd.gt.arr[bl.firstgoto]);
        }
    }

    private LocVar getlocvar(FuncState f, int i) {
        return f.locvars[dyd.actvar[f.firstlocal + i]];
    }

    private void newLocalVar(String name) {
        int reg = fs.registerLocalVar(name);
        checklimit(fs, dyd.nactvar + 1 - fs.firstlocal, MAXVARS, "local variables");
        dyd.addActvar(reg);
    }

    private void adjustlocalvars(int nvars) {
        fs.nactvar += nvars;
        for (; nvars > 0; nvars--) {
            getlocvar(fs, fs.nactvar - nvars).startpc = fs.pc;
        }
    }

    private void removevars(int tolevel) {
        dyd.nactvar -= (fs.nactvar - tolevel);
        while (fs.nactvar > tolevel) {
            getlocvar(fs, --fs.nactvar).endpc = fs.pc;
        }
    }

    private int searchvar(FuncState f, String n) {
        for (int i = f.nactvar - 1; i >= 0; i--) {
            if (n.equals(getlocvar(f, i).varname)) {
                return i;
            }
        }
        return -1;
    }

    private int searchupvalue(FuncState f, String n) {
        for (int i = 0; i < f.nups; i++) {
            if (n.equals(f.upvalues[i].name)) {
                return i;
            }
        }
        return -1;
    }

    private void markupval(FuncState f, int level) {
        BlockCnt bl = f.bl;
        while (bl.nactvar > level) {
            bl = bl.previous;
        }
        bl.upval = true;
    }

    private int singlevaraux(FuncState f, String n, ExpDesc var, boolean base) {
        if (f == null) {
            return ExpDesc.Kind.VVOID.ordinal();
        }
        int v = searchvar(f, n);
        if (v >= 0) {
            var.k = ExpDesc.Kind.VLOCAL;
            var.info = v;
            if (!base) {
                markupval(f, v);
            }
            return ExpDesc.Kind.VLOCAL.ordinal();
        }
        int idx = searchupvalue(f, n);
        if (idx < 0) {
            if (singlevaraux(f.prev, n, var, false) == ExpDesc.Kind.VVOID.ordinal()) {
                return ExpDesc.Kind.VVOID.ordinal();
            }
            idx = f.newUpvalue(n, var);
        }
        var.k = ExpDesc.Kind.VUPVAL;
        var.info = idx;
        return ExpDesc.Kind.VUPVAL.ordinal();
    }

    private void singlevar(String varname, ExpDesc var) {
        if (singlevaraux(fs, varname, var, true) == ExpDesc.Kind.VVOID.ordinal()) {
            ExpDesc key = new ExpDesc();
            singlevaraux(fs, ENV_NAME, var, true);
            codestring(key, varname);
            fs.indexed(var, key);
        }
    }

    // ============================================================ goto/label

    private void breaklabel() {
        int l = newlabelentry(dyd.label, "break", 0, fs.pc);
        findgotos(dyd.label.arr[l]);
    }

    private int newlabelentry(Labellist l, String name, int line, int pc) {
        Labeldesc e = new Labeldesc();
        e.name = name;
        e.line = line;
        e.nactvar = fs.nactvar;
        e.pc = pc;
        l.add(e);
        return l.n - 1;
    }

    private void findgotos(Labeldesc lb) {
        Labellist gl = dyd.gt;
        int i = fs.bl.firstgoto;
        while (i < gl.n) {
            if (gl.arr[i].name.equals(lb.name)) {
                closegoto(i, lb);
            } else {
                i++;
            }
        }
    }

    private void closegoto(int g, Labeldesc label) {
        Labellist gl = dyd.gt;
        Labeldesc gt = gl.arr[g];
        if (gt.nactvar < label.nactvar) {
            String vname = getlocvar(fs, gt.nactvar).varname;
            semerror("<goto " + gt.name + "> at line " + gt.line
                    + " jumps into the scope of local '" + vname + "'");
        }
        fs.patchlist(gt.pc, label.pc);
        gl.remove(g);
    }

    private boolean findlabel(int g) {
        BlockCnt bl = fs.bl;
        Labeldesc gt = dyd.gt.arr[g];
        for (int i = bl.firstlabel; i < dyd.label.n; i++) {
            Labeldesc lb = dyd.label.arr[i];
            if (lb.name.equals(gt.name)) {
                if (gt.nactvar > lb.nactvar && (bl.upval || dyd.label.n > bl.firstlabel)) {
                    fs.patchclose(gt.pc, lb.nactvar);
                }
                closegoto(g, lb);
                return true;
            }
        }
        return false;
    }

    private void movegotosout(FuncState f, BlockCnt bl) {
        int i = bl.firstgoto;
        Labellist gl = dyd.gt;
        while (i < gl.n) {
            Labeldesc gt = gl.arr[i];
            if (gt.nactvar > bl.nactvar) {
                if (bl.upval) {
                    f.patchclose(gt.pc, bl.nactvar);
                }
                gt.nactvar = bl.nactvar;
            }
            if (!findlabel(i)) {
                i++;
            }
        }
    }

    private void gotostat(int pc, LuaParser.StatGotoContext gotoNode, int line) {
        String label;
        if (gotoNode != null) {
            label = gotoNode.NAME().getSymbol().getText();
        } else {
            label = "break";
        }
        int g = newlabelentry(dyd.gt, label, line, pc);
        findlabel(g);
    }

    private void labelstat(String label, int line, boolean isLastNoop) {
        Labellist ll = dyd.label;
        checkrepeated(ll, label);
        int l = newlabelentry(ll, label, line, fs.pc);
        if (isLastNoop) {
            ll.arr[l].nactvar = fs.bl.nactvar;
        }
        findgotos(ll.arr[l]);
    }

    // ============================================================ 错误

    private void semerror(String msg) {
        throw new LuaCompileException(msg);
    }

    private void checklimit(FuncState f, int v, int l, String what) {
        if (v > l) {
            errorlimit(f, l, what);
        }
    }

    private void errorlimit(FuncState f, int limit, String what) {
        int line = f.f.linedefined;
        String where = (line == 0) ? "main function" : ("function at line " + line);
        semerror("too many " + what + " (limit is " + limit + ") in " + where);
    }

    private static boolean hasmultret(ExpDesc.Kind k) {
        return k == ExpDesc.Kind.VCALL || k == ExpDesc.Kind.VVARARG;
    }

    // ============================================================ 语句

    private void statlist(LuaParser.BlockContext blk, int from) {
        List<LuaParser.StatContext> stats = blk.stat();
        for (int i = from; i < stats.size(); i++) {
            compileStat(stats, i, blk.retstat());
        }
        if (blk.retstat() != null) {
            retstat(blk.retstat());
        }
    }

    private void compileStat(List<LuaParser.StatContext> stats, int i, LuaParser.RetstatContext ret) {
        LuaParser.StatContext s = stats.get(i);
        if (s instanceof LuaParser.StatLabelContext ll) {
            labelstat(ll.label().NAME().getSymbol().getText(), s.getStart().getLine(), isLastNoop(stats, i, ret));
            // 消费 '::' NAME '::' 后 lastline 指向闭合 '::' 所在行
            ctx.lastline = endLine(s);
        } else {
            statement(s);
        }
    }

    private static boolean isLastNoop(List<LuaParser.StatContext> stats, int i, LuaParser.RetstatContext ret) {
        if (ret != null) {
            return false;
        }
        for (int j = i + 1; j < stats.size(); j++) {
            LuaParser.StatContext s = stats.get(j);
            if (!(s instanceof LuaParser.StatEmptyContext || s instanceof LuaParser.StatLabelContext)) {
                return false;
            }
        }
        return true;
    }

    private void block(LuaParser.BlockContext blk) {
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, false);
        statlist(blk, 0);
        leaveblock();
    }

    private void statement(LuaParser.StatContext st) {
        if (st == null) {
            return;
        }
        int line = st.getStart().getLine();
        ctx.lastline = line;
        enterlevel();
        if (st instanceof LuaParser.StatEmptyContext) {
            // nothing
        } else if (st instanceof LuaParser.StatIfContext ifs) {
            ifstat(ifs, line);
        } else if (st instanceof LuaParser.StatWhileContext ws) {
            whilestat(ws, line);
        } else if (st instanceof LuaParser.StatDoContext ds) {
            block(ds.block());
        } else if (st instanceof LuaParser.StatForNumericContext fn) {
            forstatNumeric(fn, line);
        } else if (st instanceof LuaParser.StatForGenericContext fg) {
            forstatGeneric(fg, line);
        } else if (st instanceof LuaParser.StatRepeatContext rs) {
            repeatstat(rs, line);
        } else if (st instanceof LuaParser.StatFunctionDefContext fd) {
            funcstat(fd, line);
        } else if (st instanceof LuaParser.StatLocalFunctionDefContext lf) {
            localfunc(lf, line);
        } else if (st instanceof LuaParser.StatLocalVarAssignContext la) {
            localstat(la);
        } else if (st instanceof LuaParser.StatLabelContext) {
            // 标签统一由 compileStat 处理（需要块上下文来判断"最后一条空操作语句"）。
            throw new IllegalStateException("label statement must go through compileStat");
        } else if (st instanceof LuaParser.StatBreakContext) {
            // C 中 luaK_jump 在消费 goto/break 之前求值，lastline 仍是上一个 token 所在行
            ctx.lastline = prevTokenLine(st.getStart().getStartIndex());
            gotostat(fs.jump(), null, line);
        } else if (st instanceof LuaParser.StatGotoContext gs) {
            ctx.lastline = prevTokenLine(st.getStart().getStartIndex());
            gotostat(fs.jump(), gs, line);
        } else if (st instanceof LuaParser.StatContinueContext) {
            continuestat(line);
        } else if (st instanceof LuaParser.StatAssignContext as) {
            exprstatAssign(as, line);
        } else if (st instanceof LuaParser.StatFunctionCallContext fc) {
            exprstatCall(fc, line);
        }
        fs.freereg = fs.nactvar;
        // C 在语句结束时会消费其收尾 token (如 do/while/for/if/function 的 END)，
        // lastline 随之推进到语句末尾 token 所在行，后续发射的 JMP/FORLOOP 会用到。
        ctx.lastline = endLine(st);
        leavelevel();
    }

    private void continuestat(int line) {
        if (loopDepth == 0) {
            semerror("continue not inside a loop");
        }
        continueList = fs.concatRef(continueList, fs.jump());
    }

    // ------------------------------------------------------------- if

    private void ifstat(LuaParser.StatIfContext c, int line) {
        int[] escapelist = {FuncState.NO_JUMP};
        List<LuaParser.ExpContext> exps = c.exp();
        List<LuaParser.BlockContext> blocks = c.block();
        int nb = blocks.size();
        boolean hasElse = nb == exps.size() + 1;
        for (int i = 0; i < exps.size(); i++) {
            boolean followed = (i + 1 < exps.size()) || hasElse;
            testThenBlock(exps.get(i), blocks.get(i), escapelist, followed);
        }
        if (hasElse) {
            block(blocks.get(nb - 1));
        }
        fs.patchtohere(escapelist[0]);
    }

    private void testThenBlock(LuaParser.ExpContext cond, LuaParser.BlockContext thenBlock,
            int[] escapelist, boolean followed) {
        ExpDesc v = new ExpDesc();
        expr(cond, v);
        List<LuaParser.StatContext> stats = thenBlock.stat();
        boolean firstGoto = !stats.isEmpty()
                && (stats.get(0) instanceof LuaParser.StatGotoContext
                        || stats.get(0) instanceof LuaParser.StatBreakContext);
        if (firstGoto) {
            fs.goiffalse(v);
            BlockCnt bl = new BlockCnt();
            enterblock(fs, bl, false);
            LuaParser.StatContext s0 = stats.get(0);
            int line = s0.getStart().getLine();
            if (s0 instanceof LuaParser.StatGotoContext gs) {
                gotostat(v.t, gs, line);
            } else {
                gotostat(v.t, null, line);
            }
            int i = 1;
            while (i < stats.size() && (stats.get(i) instanceof LuaParser.StatEmptyContext
                    || stats.get(i) instanceof LuaParser.StatLabelContext)) {
                compileStat(stats, i, thenBlock.retstat());
                i++;
            }
            boolean blockEnds = (i >= stats.size()) && (thenBlock.retstat() == null);
            if (blockEnds) {
                leaveblock();
                return;
            }
            // gotostat 已消费 goto/NAME，lastline 推进到该语句末尾 token 所在行
            ctx.lastline = endLine(s0);
            int jf = fs.jump();
            statlist(thenBlock, i);
            leaveblock();
            if (followed) {
                escapelist[0] = fs.concatRef(escapelist[0], fs.jump());
            }
            fs.patchtohere(jf);
        } else {
            fs.goiftrue(v);
            BlockCnt bl = new BlockCnt();
            enterblock(fs, bl, false);
            int jf = v.f;
            statlist(thenBlock, 0);
            leaveblock();
            if (followed) {
                escapelist[0] = fs.concatRef(escapelist[0], fs.jump());
            }
            fs.patchtohere(jf);
        }
    }

    // ------------------------------------------------------------- while

    private void whilestat(LuaParser.StatWhileContext c, int line) {
        BlockCnt bl = new BlockCnt();
        int whileinit = fs.getlabel();
        int condexit = cond(c.exp());
        enterblock(fs, bl, true);
        int savedContinue = continueList;
        int savedDepth = loopDepth;
        continueList = FuncState.NO_JUMP;
        loopDepth++;
        block(c.block());
        if (bl.upval) {
            fs.patchclose(continueList, bl.nactvar);
        }
        fs.patchlist(continueList, whileinit);
        continueList = savedContinue;
        loopDepth = savedDepth;
        jumpto(whileinit);
        leaveblock();
        fs.patchtohere(condexit);
    }

    // ------------------------------------------------------------- repeat

    private void repeatstat(LuaParser.StatRepeatContext c, int line) {
        int repeatInit = fs.getlabel();
        BlockCnt bl1 = new BlockCnt();
        BlockCnt bl2 = new BlockCnt();
        enterblock(fs, bl1, true);
        enterblock(fs, bl2, false);
        int savedContinue = continueList;
        int savedDepth = loopDepth;
        continueList = FuncState.NO_JUMP;
        loopDepth++;
        statlist(c.block(), 0);
        if (bl2.upval) {
            fs.patchclose(continueList, bl2.nactvar);
        }
        fs.patchtohere(continueList);
        continueList = savedContinue;
        loopDepth = savedDepth;
        int condexit = cond(c.exp());
        if (bl2.upval) {
            fs.patchclose(condexit, bl2.nactvar);
        }
        leaveblock();
        fs.patchlist(condexit, repeatInit);
        leaveblock();
    }

    // ------------------------------------------------------------- for

    private void forstatNumeric(LuaParser.StatForNumericContext c, int line) {
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, true);
        String varname = c.NAME().getSymbol().getText();
        fornum(c, varname, line);
        leaveblock();
    }

    private void forstatGeneric(LuaParser.StatForGenericContext c, int line) {
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, true);
        List<TerminalNode> names = c.namelist().NAME();
        forlist(c, names, line);
        leaveblock();
    }

    private void fornum(LuaParser.StatForNumericContext c, String varname, int line) {
        int base = fs.freereg;
        newLocalVar("(for index)");
        newLocalVar("(for limit)");
        newLocalVar("(for step)");
        newLocalVar(varname);
        List<LuaParser.ExpContext> exps = c.exp();
        exp1(exps.get(0));
        exp1(exps.get(1));
        if (exps.size() > 2) {
            exp1(exps.get(2));
        } else {
            fs.codek(fs.freereg, fs.intK(1));
            fs.reserveregs(1);
        }
        forbody(c.block(), base, line, 1, true);
    }

    private void forlist(LuaParser.StatForGenericContext c, List<TerminalNode> names, int line) {
        int nvars = 4;
        int base = fs.freereg;
        newLocalVar("(for generator)");
        newLocalVar("(for state)");
        newLocalVar("(for control)");
        newLocalVar(names.get(0).getSymbol().getText());
        for (int i = 1; i < names.size(); i++) {
            newLocalVar(names.get(i).getSymbol().getText());
            nvars++;
        }
        ExpDesc e = new ExpDesc();
        adjustAssign(3, explist(c.explist(), e), e);
        fs.checkstack(3);
        forbody(c.block(), base, line, nvars - 3, false);
    }

    private void forbody(LuaParser.BlockContext body, int base, int line, int nvars, boolean isnum) {
        BlockCnt bl = new BlockCnt();
        adjustlocalvars(3);
        int prep = isnum ? fs.codeAsBx(OpCode.FORPREP, base, FuncState.NO_JUMP) : fs.jump();
        enterblock(fs, bl, false);
        adjustlocalvars(nvars);
        fs.reserveregs(nvars);
        int savedContinue = continueList;
        int savedDepth = loopDepth;
        continueList = FuncState.NO_JUMP;
        loopDepth++;
        block(body);
        if (bl.upval) {
            fs.patchclose(continueList, bl.nactvar);
        }
        fs.patchtohere(continueList);
        continueList = savedContinue;
        loopDepth = savedDepth;
        leaveblock();
        fs.patchtohere(prep);
        int endfor;
        if (isnum) {
            endfor = fs.codeAsBx(OpCode.FORLOOP, base, FuncState.NO_JUMP);
        } else {
            fs.codeABC(OpCode.TFORCALL, base, 0, nvars);
            fs.fixline(line);
            endfor = fs.codeAsBx(OpCode.TFORLOOP, base + 2, FuncState.NO_JUMP);
        }
        fs.patchlist(endfor, prep + 1);
        fs.fixline(line);
    }

    // ------------------------------------------------------------- 局部/函数

    private void localfunc(LuaParser.StatLocalFunctionDefContext c, int line) {
        newLocalVar(c.NAME().getSymbol().getText());
        adjustlocalvars(1);
        ExpDesc b = new ExpDesc();
        int bodyLine = c.funcbody().OP().getSymbol().getLine();
        body(c.funcbody(), b, false, bodyLine);
        getlocvar(fs, b.info).startpc = fs.pc;
    }

    private void localstat(LuaParser.StatLocalVarAssignContext c) {
        int nvars = 0;
        List<TerminalNode> names = c.attnamelist().NAME();
        for (int i = 0; i < names.size(); i++) {
            newLocalVar(names.get(i).getSymbol().getText());
            nvars++;
        }
        int nexps;
        ExpDesc e = new ExpDesc();
        if (c.explist() != null) {
            nexps = explist(c.explist(), e);
        } else {
            e.k = ExpDesc.Kind.VVOID;
            nexps = 0;
        }
        adjustAssign(nvars, nexps, e);
        adjustlocalvars(nvars);
    }

    private void funcstat(LuaParser.StatFunctionDefContext c, int line) {
        ExpDesc v = new ExpDesc();
        int ismethod = funcname(c.funcname(), v);
        ExpDesc b = new ExpDesc();
        int bodyLine = c.funcbody().OP().getSymbol().getLine();
        body(c.funcbody(), b, ismethod != 0, bodyLine);
        fs.storevar(v, b);
        fs.fixline(line);
    }

    private int funcname(LuaParser.FuncnameContext c, ExpDesc v) {
        int ismethod = 0;
        List<TerminalNode> names = c.NAME();
        singlevar(names.get(0).getSymbol().getText(), v);
        int idx = 1;
        for (; idx < names.size(); idx++) {
            if (c.COL() != null && idx == names.size() - 1) {
                ismethod = 1;
            }
            String key = names.get(idx).getSymbol().getText();
            fieldsel(key, v);
        }
        return ismethod;
    }

    private void body(LuaParser.FuncbodyContext fb, ExpDesc e, boolean ismethod, int line) {
        Proto clp = fs.addPrototype();
        FuncState nfs = new FuncState(fs, ctx);
        nfs.f = clp;
        clp.linedefined = line;
        BlockCnt bl = new BlockCnt();
        openFunc(nfs, bl);
        if (ismethod) {
            newLocalVar("self");
            adjustlocalvars(1);
        }
        parlist(fb.parlist());
        statlist(fb.block(), 0);
        clp.lastlinedefined = fb.END().getSymbol().getLine();
        // C 中 body() 在 check_match 消费掉 'end' 后才 codeclosure/close_func，
        // 此刻 ls->lastline 已是 'end' 所在行：CLOSURE 与函数末尾隐式 RETURN 都用它。
        ctx.lastline = clp.lastlinedefined;
        codeclosure(e);
        closeFunc();
    }

    private void parlist(LuaParser.ParlistContext pl) {
        fs.f.isVararg = 0;
        int nparams = 0;
        if (pl instanceof LuaParser.ParlistNamelistContext nl) {
            List<TerminalNode> names = nl.namelist().NAME();
            for (TerminalNode n : names) {
                newLocalVar(n.getSymbol().getText());
                nparams++;
            }
            if (nl.DDD() != null) {
                fs.f.isVararg = 1;
            }
        } else if (pl instanceof LuaParser.ParlistVarargContext) {
            fs.f.isVararg = 1;
        }
        adjustlocalvars(nparams);
        fs.f.numparams = fs.nactvar;
        fs.reserveregs(fs.nactvar);
    }

    private void codeclosure(ExpDesc v) {
        FuncState pfs = fs.prev;
        v.k = ExpDesc.Kind.VRELOCABLE;
        v.info = pfs.codeABx(OpCode.CLOSURE, 0, pfs.np - 1);
        pfs.exp2nextreg(v);
    }

    // ------------------------------------------------------------- 赋值/调用

    private void exprstatCall(LuaParser.StatFunctionCallContext c, int line) {
        ExpDesc v = new ExpDesc();
        suffixedexp(c.functioncall().prefixexp(), v, line);
        if (v.k != ExpDesc.Kind.VCALL) {
            semerror("syntax error");
        }
        fs.setCode(v.info, Instruction.withC(fs.getCode(v.info), 1));
    }

    private void exprstatAssign(LuaParser.StatAssignContext c, int line) {
        List<LuaParser.VarContext> vars = c.varlist().var();
        java.util.ArrayList<ExpDesc> es = new java.util.ArrayList<>();
        for (int i = 0; i < vars.size(); i++) {
            ExpDesc v = new ExpDesc();
            suffixedexp(vars.get(i).prefixexp(), v, line);
            if (!v.isVar()) {
                semerror("syntax error");
            }
            if (i > 0 && v.k != ExpDesc.Kind.VINDEXED) {
                checkConflict(es, v);
            }
            es.add(v);
        }
        assignment(c, es);
    }

    private void checkConflict(List<ExpDesc> prev, ExpDesc v) {
        int extra = fs.freereg;
        boolean conflict = false;
        for (ExpDesc lhv : prev) {
            if (lhv.k == ExpDesc.Kind.VINDEXED) {
                if (lhv.indVt == v.k && lhv.indT == v.info) {
                    conflict = true;
                    lhv.indVt = ExpDesc.Kind.VLOCAL;
                    lhv.indT = extra;
                }
                if (v.k == ExpDesc.Kind.VLOCAL && lhv.indIdx == v.info) {
                    conflict = true;
                    lhv.indIdx = extra;
                }
            }
        }
        if (conflict) {
            OpCode op = (v.k == ExpDesc.Kind.VLOCAL) ? OpCode.MOVE : OpCode.GETUPVAL;
            fs.codeABC(op, extra, v.info, 0);
            fs.reserveregs(1);
        }
    }

    private void assignment(LuaParser.StatAssignContext c, List<ExpDesc> vars) {
        int nvars = vars.size();
        if (!vars.get(0).isVar()) {
            semerror("syntax error");
        }
        ExpDesc e = new ExpDesc();
        int nexps = explist(c.explist(), e);
        if (nexps == nvars) {
            fs.setoneret(e);
            fs.storevar(vars.get(nvars - 1), e);
            for (int i = nvars - 2; i >= 0; i--) {
                fs.storevar(vars.get(i), ExpDesc.vnonreloc(fs.freereg - 1));
            }
        } else {
            adjustAssign(nvars, nexps, e);
            if (nexps > nvars) {
                fs.freereg -= (nexps - nvars);
            }
            for (int i = nvars - 1; i >= 0; i--) {
                fs.storevar(vars.get(i), ExpDesc.vnonreloc(fs.freereg - 1));
            }
        }
    }

    private void adjustAssign(int nvars, int nexps, ExpDesc e) {
        int extra = nvars - nexps;
        if (hasmultret(e.k)) {
            extra++;
            if (extra < 0) {
                extra = 0;
            }
            fs.setreturns(e, extra);
            if (extra > 1) {
                fs.reserveregs(extra - 1);
            }
        } else {
            if (e.k != ExpDesc.Kind.VVOID) {
                fs.exp2nextreg(e);
            }
            if (extra > 0) {
                int reg = fs.freereg;
                fs.reserveregs(extra);
                fs.luaK_nil(reg, extra);
            }
        }
    }

    // ------------------------------------------------------------- return

    private void retstat(LuaParser.RetstatContext c) {
        ctx.lastline = c.getStart().getLine();
        int first;
        int nret;
        if (c.explist() == null) {
            first = nret = 0;
        } else {
            ExpDesc e = new ExpDesc();
            nret = explist(c.explist(), e);
            if (hasmultret(e.k)) {
                fs.setreturns(e, FuncState.LUA_MULTRET);
                if (e.k == ExpDesc.Kind.VCALL && nret == 1) {
                    fs.setCode(e.info, Instruction.withOpcode(fs.getCode(e.info), OpCode.TAILCALL));
                }
                first = fs.nactvar;
                nret = FuncState.LUA_MULTRET;
            } else if (nret == 1) {
                first = fs.exp2anyreg(e);
            } else {
                fs.exp2nextreg(e);
                first = fs.nactvar;
            }
        }
        fs.ret(first, nret);
    }

    // ------------------------------------------------------------- 条件/循环辅助

    private int cond(LuaParser.ExpContext c) {
        ExpDesc v = new ExpDesc();
        expr(c, v);
        if (v.k == ExpDesc.Kind.VNIL) {
            v.k = ExpDesc.Kind.VFALSE;
        }
        fs.goiftrue(v);
        return v.f;
    }

    private int exp1(LuaParser.ExpContext c) {
        ExpDesc e = new ExpDesc();
        expr(c, e);
        fs.exp2nextreg(e);
        return e.info;
    }

    private void jumpto(int target) {
        fs.patchlist(fs.jump(), target);
    }

    // ============================================================ 表达式

    private int explist(LuaParser.ExplistContext list, ExpDesc v) {
        List<LuaParser.ExpContext> es = list.exp();
        int n = 1;
        expr(es.get(0), v);
        for (int i = 1; i < es.size(); i++) {
            fs.exp2nextreg(v);
            expr(es.get(i), v);
            n++;
        }
        return n;
    }

    private void expr(LuaParser.ExpContext c, ExpDesc v) {
        ctx.lastline = c.getStart().getLine();
        if (c instanceof LuaParser.ExpAtomContext a) {
            atom(a.atom(), v);
        } else if (c instanceof LuaParser.ExpUnopContext u) {
            int line = u.op.getLine();
            expr(u.exp(), v);
            fs.prefix(getunopr(u.op.getType()), v, line);
        } else if (c instanceof LuaParser.ExpPowerContext p) {
            binop(p.exp(0), p.exp(1), BinOpr.POW, p.CARET().getSymbol(), v);
        } else if (c instanceof LuaParser.ExpMulDivModContext m) {
            binop(m.exp(0), m.exp(1), getbinopr(m.op.getType()), m.op, v);
        } else if (c instanceof LuaParser.ExpAddSubContext a) {
            binop(a.exp(0), a.exp(1), getbinopr(a.op.getType()), a.op, v);
        } else if (c instanceof LuaParser.ExpConcatContext cc) {
            binop(cc.exp(0), cc.exp(1), BinOpr.CONCAT, cc.DD().getSymbol(), v);
        } else if (c instanceof LuaParser.ExpBitwiseShiftContext b) {
            binop(b.exp(0), b.exp(1), getbinopr(b.op.getType()), b.op, v);
        } else if (c instanceof LuaParser.ExpBitwiseAndContext b) {
            binop(b.exp(0), b.exp(1), BinOpr.BAND, b.op, v);
        } else if (c instanceof LuaParser.ExpBitwiseXorContext b) {
            binop(b.exp(0), b.exp(1), BinOpr.BXOR, b.op, v);
        } else if (c instanceof LuaParser.ExpBitwiseOrContext b) {
            binop(b.exp(0), b.exp(1), BinOpr.BOR, b.op, v);
        } else if (c instanceof LuaParser.ExpComparisonContext cmp) {
            binop(cmp.exp(0), cmp.exp(1), getbinopr(cmp.op.getType()), cmp.op, v);
        } else if (c instanceof LuaParser.ExpLogicalAndContext a) {
            binop(a.exp(0), a.exp(1), BinOpr.AND, a.op, v);
        } else if (c instanceof LuaParser.ExpLogicalOrContext o) {
            binop(o.exp(0), o.exp(1), BinOpr.OR, o.op, v);
        }
        // C 读完表达式后 ls->linenumber 落在其末尾 token 结束行；后续把该表达式
        // 落栈 (exp2nextreg 发 LOADK 等) 时用的正是这一行。
        ctx.lastline = endLine(c);
    }

    private void binop(LuaParser.ExpContext left, LuaParser.ExpContext right, BinOpr op,
            Token opToken, ExpDesc v) {
        int line = opToken.getLine();
        expr(left, v);
        // C: 跳过运算符的 luaX_next 已把 lastline 设为运算符所在行，luaK_infix
        // 在需要把左操作数落栈 (发 LOADBOOL 等) 时用的正是这一行。
        ctx.lastline = line;
        fs.infix(op, v);
        ExpDesc v2 = new ExpDesc();
        expr(right, v2);
        ctx.lastline = endLine(right);
        fs.posfix(op, v, v2, line);
    }

    private void atom(LuaParser.AtomContext a, ExpDesc v) {
        if (a instanceof LuaParser.ExpNilContext) {
            v.k = ExpDesc.Kind.VNIL;
        } else if (a instanceof LuaParser.ExpFalseContext) {
            v.k = ExpDesc.Kind.VFALSE;
        } else if (a instanceof LuaParser.ExpTrueContext) {
            v.k = ExpDesc.Kind.VTRUE;
        } else if (a instanceof LuaParser.ExpNumberContext num) {
            numberLiteral(num.number(), v);
        } else if (a instanceof LuaParser.ExpStringContext s) {
            codestring(v, decodeString(s.string()));
        } else if (a instanceof LuaParser.ExpVarargContext) {
            if (fs.f.isVararg == 0) {
                semerror("cannot use '...' outside a vararg function");
            }
            // C 在读到 '...' 后 lastline 仍指向上一个 token，VARARG 用的是该行
            ctx.lastline = prevTokenLine(a.getStart().getStartIndex());
            v.k = ExpDesc.Kind.VVARARG;
            v.info = fs.codeABC(OpCode.VARARG, 0, 1, 0);
        } else if (a instanceof LuaParser.ExpFunctionDefContext f) {
            int line = f.functiondef().funcbody().OP().getSymbol().getLine();
            body(f.functiondef().funcbody(), v, false, line);
        } else if (a instanceof LuaParser.ExpTableConstructorContext t) {
            constructor(t.tableconstructor(), v);
        } else if (a instanceof LuaParser.ExpPrefixExpAtomContext p) {
            suffixedexp(p.prefixexp(), v, p.prefixexp().getStart().getLine());
        }
    }

    // ------------------------------------------------------------- 构造函数

    private void constructor(LuaParser.TableconstructorContext tc, ExpDesc t) {
        // C 在 checknext('{') 之前就发射 NEWTABLE，lastline 尚是 '{' 前一个 token 的行
        ctx.lastline = prevTokenLine(tc.getStart().getStartIndex());
        int pc = fs.codeABC(OpCode.NEWTABLE, 0, 0, 0);
        ConsControl cc = new ConsControl();
        cc.t = t;
        t.k = ExpDesc.Kind.VRELOCABLE;
        t.info = pc;
        cc.v = new ExpDesc();
        fs.exp2nextreg(t);
        if (tc.fieldlist() != null) {
            for (LuaParser.FieldContext f : tc.fieldlist().field()) {
                closelistfield(cc);
                field(f, cc);
            }
        }
        // check_match('}') 之后 lastline = '}' 所在行，SETLIST 用它
        ctx.lastline = endLine(tc);
        lastlistfield(cc);
        fs.setCode(pc, Instruction.withB(fs.getCode(pc), int2fb(cc.na)));
        fs.setCode(pc, Instruction.withC(fs.getCode(pc), int2fb(cc.nh)));
    }

    private void field(LuaParser.FieldContext f, ConsControl cc) {
        if (f instanceof LuaParser.FieldValueContext fv) {
            listfield(fv.exp(), cc);
        } else if (f instanceof LuaParser.FieldKeyNameContext fn) {
            // recfield 在解析 key 之前就先记录 freereg (lparser.c:643)，
            // 因而必须在此处、编译 key 之前取快照。
            int reg = fs.freereg;
            ExpDesc key = new ExpDesc();
            codestring(key, fn.NAME().getSymbol().getText());
            recfield(key, fn.exp(), cc, reg);
        } else if (f instanceof LuaParser.FieldKeyExprContext fe) {
            int reg = fs.freereg;
            ExpDesc key = new ExpDesc();
            yindex(fe.exp(0), key);
            recfield(key, fe.exp(1), cc, reg);
        }
    }

    private void recfield(ExpDesc key, LuaParser.ExpContext val, ConsControl cc, int reg) {
        cc.nh++;
        int rkkey = fs.exp2RK(key);
        ExpDesc v = new ExpDesc();
        expr(val, v);
        fs.codeABC(OpCode.SETTABLE, cc.t.info, rkkey, fs.exp2RK(v));
        fs.freereg = reg;
    }

    private void listfield(LuaParser.ExpContext e, ConsControl cc) {
        expr(e, cc.v);
        cc.na++;
        cc.tostore++;
    }

    private void closelistfield(ConsControl cc) {
        if (cc.v.k == ExpDesc.Kind.VVOID) {
            return;
        }
        fs.exp2nextreg(cc.v);
        cc.v.k = ExpDesc.Kind.VVOID;
        if (cc.tostore == LuaBytecodeConstants.LFIELDS_PER_FLUSH) {
            fs.setlist(cc.t.info, cc.na, cc.tostore);
            cc.tostore = 0;
        }
    }

    private void lastlistfield(ConsControl cc) {
        if (cc.tostore == 0) {
            return;
        }
        if (hasmultret(cc.v.k)) {
            fs.setreturns(cc.v, FuncState.LUA_MULTRET);
            fs.setlist(cc.t.info, cc.na, FuncState.LUA_MULTRET);
            cc.na--;
        } else {
            if (cc.v.k != ExpDesc.Kind.VVOID) {
                fs.exp2nextreg(cc.v);
            }
            fs.setlist(cc.t.info, cc.na, cc.tostore);
        }
    }

    // ------------------------------------------------------------- 前缀表达式

    private void suffixedexp(LuaParser.PrefixexpContext c, ExpDesc v, int line) {
        ctx.lastline = c.getStart().getLine();
        if (c instanceof LuaParser.VarNameContext n) {
            singlevar(n.NAME().getSymbol().getText(), v);
        } else if (c instanceof LuaParser.PrefixParenContext p) {
            expr(p.exp(), v);
            fs.dischargevars(v);
        } else if (c instanceof LuaParser.VarMemberAccessContext m) {
            suffixedexp(m.prefixexp(), v, line);
            fieldsel(m.NAME().getSymbol().getText(), v);
        } else if (c instanceof LuaParser.VarIndexedContext ix) {
            suffixedexp(ix.prefixexp(), v, line);
            fs.exp2anyregup(v);
            ExpDesc key = new ExpDesc();
            yindex(ix.exp(), key);
            fs.indexed(v, key);
        } else if (c instanceof LuaParser.FuncCallContext fc) {
            suffixedexp(fc.prefixexp(), v, line);
            fs.exp2nextreg(v);
            funcargs(fc.args(), v, line);
        } else if (c instanceof LuaParser.MethodCallContext mc) {
            suffixedexp(mc.prefixexp(), v, line);
            ExpDesc key = new ExpDesc();
            codestring(key, mc.NAME().getSymbol().getText());
            fs.self(v, key);
            funcargs(mc.args(), v, line);
        }
    }

    private void fieldsel(String name, ExpDesc v) {
        fs.exp2anyregup(v);
        ExpDesc key = new ExpDesc();
        codestring(key, name);
        fs.indexed(v, key);
    }

    private void yindex(LuaParser.ExpContext e, ExpDesc v) {
        expr(e, v);
        fs.exp2val(v);
    }

    private void funcargs(LuaParser.ArgsContext args, ExpDesc f, int line) {
        ExpDesc a = new ExpDesc();
        if (args instanceof LuaParser.ArgsParenContext p) {
            if (p.explist() != null) {
                explist(p.explist(), a);
                fs.setreturns(a, FuncState.LUA_MULTRET);
            }
        } else if (args instanceof LuaParser.ArgsTableContext t) {
            constructor(t.tableconstructor(), a);
        } else if (args instanceof LuaParser.ArgsStringContext s) {
            codestring(a, decodeString(s.string()));
        }
        // C 在读入收尾 token (')' / 字符串 END) 之后才把最后一个实参落栈，
        // 故该 LOADK 用的是实参列表末尾 token 所在行。
        ctx.lastline = endLine(args);
        int base = f.info;
        int nparams;
        if (hasmultret(a.k)) {
            nparams = FuncState.LUA_MULTRET;
        } else {
            if (a.k != ExpDesc.Kind.VVOID) {
                fs.exp2nextreg(a);
            }
            nparams = fs.freereg - (base + 1);
        }
        f.k = ExpDesc.Kind.VCALL;
        f.info = fs.codeABC(OpCode.CALL, base, nparams + 1, 2);
        fs.fixline(line);
        fs.freereg = base + 1;
    }

    // ------------------------------------------------------------- 字面量

    private void codestring(ExpDesc e, String s) {
        codestring(e, s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    private void codestring(ExpDesc e, byte[] bytes) {
        e.k = ExpDesc.Kind.VK;
        e.info = fs.stringK(bytes);
    }

    private void numberLiteral(LuaParser.NumberContext n, ExpDesc v) {
        String text = n.getStart().getText();
        boolean isHex = text.length() > 2 && text.charAt(0) == '0'
                && (text.charAt(1) == 'x' || text.charAt(1) == 'X');
        boolean hasFractionOrExponent;
        if (isHex) {
            hasFractionOrExponent = text.indexOf('.', 2) >= 0
                    || text.indexOf('p', 2) >= 0 || text.indexOf('P', 2) >= 0;
        } else {
            hasFractionOrExponent = text.indexOf('.') >= 0
                    || text.indexOf('e') >= 0 || text.indexOf('E') >= 0;
        }
        if (hasFractionOrExponent) {
            double value;
            if (isHex) {
                String doubleText = text;
                if (doubleText.indexOf('p') < 0 && doubleText.indexOf('P') < 0) {
                    doubleText = doubleText + "p0";
                }
                value = Double.parseDouble(doubleText);
            } else {
                value = Double.parseDouble(text);
            }
            v.k = ExpDesc.Kind.VKFLT;
            v.nval = value;
        } else if (isHex) {
            long value = 0L;
            for (int i = 2; i < text.length(); i++) {
                value = value * 16L + Character.digit(text.charAt(i), 16);
            }
            v.k = ExpDesc.Kind.VKINT;
            v.ival = value;
        } else {
            try {
                v.k = ExpDesc.Kind.VKINT;
                v.ival = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                v.k = ExpDesc.Kind.VKFLT;
                v.nval = Double.parseDouble(text);
            }
        }
    }

    private byte[] decodeString(LuaParser.StringContext s) {
        if (s instanceof LuaParser.StringLongContext) {
            return toLuaSourceBytes(parseLongString(s.getStart().getText()));
        }
        String raw = s.getStart().getText();
        // 短字符串字面量带定界引号，去掉后再解转义（与 LuaNodeParser 的
        // asTruffleString(token, removeQuotes=true) 一致）
        return unescape(toLuaSourceBytes(raw.substring(1, raw.length() - 1)));
    }

    // ------------------------------------------------------------- 运算符映射

    private static BinOpr getbinopr(int t) {
        return switch (t) {
            case LuaParser.PLUS -> BinOpr.ADD;
            case LuaParser.MINUS -> BinOpr.SUB;
            case LuaParser.STAR -> BinOpr.MUL;
            case LuaParser.PER -> BinOpr.MOD;
            case LuaParser.CARET -> BinOpr.POW;
            case LuaParser.SLASH -> BinOpr.DIV;
            case LuaParser.SS -> BinOpr.IDIV;
            case LuaParser.AMP -> BinOpr.BAND;
            case LuaParser.PIPE -> BinOpr.BOR;
            case LuaParser.SQUIG -> BinOpr.BXOR;
            case LuaParser.LL -> BinOpr.SHL;
            case LuaParser.GG -> BinOpr.SHR;
            case LuaParser.DD -> BinOpr.CONCAT;
            case LuaParser.SQEQ -> BinOpr.NE;
            case LuaParser.EE -> BinOpr.EQ;
            case LuaParser.LT -> BinOpr.LT;
            case LuaParser.LE -> BinOpr.LE;
            case LuaParser.GT -> BinOpr.GT;
            case LuaParser.GE -> BinOpr.GE;
            case LuaParser.AND -> BinOpr.AND;
            case LuaParser.OR -> BinOpr.OR;
            default -> BinOpr.NOBINOPR;
        };
    }

    private static UnOpr getunopr(int t) {
        return switch (t) {
            case LuaParser.NOT -> UnOpr.NOT;
            case LuaParser.MINUS -> UnOpr.MINUS;
            case LuaParser.SQUIG -> UnOpr.BNOT;
            case LuaParser.POUND -> UnOpr.LEN;
            default -> UnOpr.NOUNOPR;
        };
    }

    // ------------------------------------------------------------- 其它

    private void enterlevel() {
        nCcalls++;
        checklimit(fs, nCcalls, LUAI_MAXCCALLS, "C levels");
    }

    private void leavelevel() {
        nCcalls--;
    }

    private void checkrepeated(Labellist ll, String label) {
        for (int i = fs.bl.firstlabel; i < ll.n; i++) {
            if (label.equals(ll.arr[i].name)) {
                semerror("label '" + label + "' already defined on line " + ll.arr[i].line);
            }
        }
    }

    private void undefgoto(Labeldesc gt) {
        String msg = isreserved(gt.name)
                ? "<" + gt.name + "> at line " + gt.line + " not inside a loop"
                : "no visible label '" + gt.name + "' for <goto> at line " + gt.line;
        semerror(msg);
    }

    private static boolean isreserved(String s) {
        return switch (s) {
            case "and", "break", "do", "else", "elseif", "end", "false", "for", "function",
                    "goto", "if", "in", "local", "nil", "not", "or", "repeat", "return",
                    "then", "true", "until", "while" -> true;
            default -> false;
        };
    }

    private static int int2fb(int x) {
        int e = 0;
        if (x < 8) {
            return x;
        }
        while (x >= (8 << 4)) {
            x = (x + 0xf) >> 4;
            e += 4;
        }
        while (x >= (8 << 1)) {
            x = (x + 1) >> 1;
            e++;
        }
        return ((e + 1) << 3) | (x - 8);
    }

    // ------------------------------------------------------------- 字符/数字辅助

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return -1;
    }

    private static boolean isLuaWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B';
    }

    /**
     * 回查 {@code charIndex} 之前最近一个非空白字符所在行号，近似复刻 C 中
     * {@code ls->lastline}（= 上一个被消费 token 的行）。用于 goto/break 这类
     * "在读入自身关键字前就先发射指令"的语句。取不到源码时退回当前行号。
     */
    private int[] lineStarts;

    private int[] lineStarts() {
        if (lineStarts == null) {
            String text = ctx.text == null ? "" : ctx.text;
            int[] tmp = new int[Math.max(2, text.length() / 16)];
            int n = 0;
            tmp[n++] = 0;
            for (int j = 0; j < text.length(); j++) {
                if (text.charAt(j) == '\n') {
                    if (n >= tmp.length) {
                        tmp = java.util.Arrays.copyOf(tmp, tmp.length * 2);
                    }
                    tmp[n++] = j + 1;
                }
            }
            lineStarts = java.util.Arrays.copyOf(tmp, n);
        }
        return lineStarts;
    }

    /** {@code idx} 处字符所在行号 (1 起)。 */
    private int lineAtChar(int idx) {
        if (ctx.text == null) {
            return ctx.lastline;
        }
        int[] ls = lineStarts();
        int lim = Math.min(idx, ctx.text.length() - 1);
        int lo = 0;
        int hi = ls.length - 1;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (ls[mid] <= lim) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo + 1;
    }

    /**
     * 节点末尾 token 结束处的行号，等价于 C 中读完该 token 后的 {@code ls->linenumber}
     * （长字符串/含换行 token 会落在闭合行）。
     */
    private int endLine(ParserRuleContext node) {
        Token stop = node == null ? null : node.getStop();
        if (stop == null) {
            return ctx.lastline;
        }
        return lineAtChar(stop.getStopIndex());
    }

    private int prevTokenLine(int charIndex) {
        String text = ctx.text;
        if (text == null || charIndex <= 0) {
            return ctx.lastline;
        }
        int i = Math.min(charIndex - 1, text.length() - 1);
        while (i >= 0 && isLuaWhitespace(text.charAt(i))) {
            i--;
        }
        if (i < 0) {
            return 1;
        }
        return lineAtChar(i);
    }

    private static byte[] toLuaSourceBytes(String content) {
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) > 0xFF) {
                return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private static String parseLongString(String raw) {
        int equalsCount = 0;
        while (1 + equalsCount < raw.length() && raw.charAt(1 + equalsCount) == '=') {
            equalsCount++;
        }
        int contentStart = equalsCount + 2;
        int contentEnd = raw.length() - equalsCount - 2;
        String content = raw.substring(contentStart, contentEnd);
        if (content.startsWith("\r\n") || content.startsWith("\n\r")) {
            content = content.substring(2);
        } else if (content.startsWith("\n") || content.startsWith("\r")) {
            content = content.substring(1);
        }
        if (content.indexOf('\r') >= 0) {
            StringBuilder sb = new StringBuilder(content.length());
            for (int i = 0; i < content.length(); i++) {
                char ch = content.charAt(i);
                if (ch == '\r') {
                    sb.append('\n');
                    if (i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else if (ch == '\n') {
                    sb.append('\n');
                    if (i + 1 < content.length() && content.charAt(i + 1) == '\r') {
                        i++;
                    }
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return content;
    }

    private static byte[] unescape(byte[] rawBytes) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(rawBytes.length);
        int i = 0;
        while (i < rawBytes.length) {
            byte b = rawBytes[i];
            if (b == (byte) '\\') {
                i++;
                if (i >= rawBytes.length) {
                    out.write('\\');
                    break;
                }
                byte next = rawBytes[i];
                switch (next) {
                    case 'a' -> {
                        out.write(0x07);
                        i++;
                    }
                    case 'b' -> {
                        out.write('\b');
                        i++;
                    }
                    case 'f' -> {
                        out.write('\f');
                        i++;
                    }
                    case 'n' -> {
                        out.write('\n');
                        i++;
                    }
                    case '\n' -> {
                        out.write('\n');
                        i++;
                        if (i < rawBytes.length && rawBytes[i] == '\r') {
                            i++;
                        }
                    }
                    case 'r' -> {
                        out.write('\r');
                        i++;
                    }
                    case '\r' -> {
                        out.write('\n');
                        i++;
                        if (i < rawBytes.length && rawBytes[i] == '\n') {
                            i++;
                        }
                    }
                    case 't' -> {
                        out.write('\t');
                        i++;
                    }
                    case 'v' -> {
                        out.write(0x0B);
                        i++;
                    }
                    case '\\' -> {
                        out.write('\\');
                        i++;
                    }
                    case '"' -> {
                        out.write('"');
                        i++;
                    }
                    case '\'' -> {
                        out.write('\'');
                        i++;
                    }
                    case 'z' -> {
                        do {
                            i++;
                        } while (i < rawBytes.length && isLuaWhitespace((char) rawBytes[i]));
                    }
                    case 'x' -> {
                        i++;
                        int hexVal = 0;
                        int digits = 0;
                        while (i < rawBytes.length && digits < 2) {
                            int d = hexDigit((char) rawBytes[i]);
                            if (d < 0) {
                                break;
                            }
                            hexVal = hexVal * 16 + d;
                            digits++;
                            i++;
                        }
                        out.write(hexVal & 0xFF);
                    }
                    case 'u' -> {
                        i++;
                        // 转义 'u': 'u' 之后必须先有 '{' (llex.c readutf8esc)
                        if (i < rawBytes.length && rawBytes[i] == '{') {
                            i++;
                        }
                        int uniVal = 0;
                        while (i < rawBytes.length && (char) rawBytes[i] != '}') {
                            int d = hexDigit((char) rawBytes[i]);
                            if (d < 0) {
                                break;
                            }
                            uniVal = uniVal * 16 + d;
                            i++;
                        }
                        if (i < rawBytes.length) {
                            i++;
                        }
                        byte[] u8 = new String(Character.toChars(uniVal))
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        out.write(u8, 0, u8.length);
                    }
                    default -> {
                        if (next >= '0' && next <= '9') {
                            int decVal = 0;
                            int decDigits = 0;
                            while (i < rawBytes.length && decDigits < 3) {
                                byte dc = rawBytes[i];
                                if (dc < '0' || dc > '9') {
                                    break;
                                }
                                decVal = decVal * 10 + (dc - '0');
                                decDigits++;
                                i++;
                            }
                            out.write(decVal & 0xFF);
                        } else {
                            out.write('\\');
                            out.write(next);
                            i++;
                        }
                    }
                }
            } else {
                out.write(b);
                i++;
            }
        }
        return out.toByteArray();
    }
}

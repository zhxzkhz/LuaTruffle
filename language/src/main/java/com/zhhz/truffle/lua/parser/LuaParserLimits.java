package com.zhhz.truffle.lua.parser;

import com.oracle.truffle.api.source.Source;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * 复刻标准 Lua 解析期的若干硬限制，用于产生与官方测试套件一致的错误消息：
 * <ul>
 *   <li>{@code LUAI_MAXCCALLS}（200）→ "too many C levels"：语句/表达式递归深度，
 *       对应 {@code lparser.c} 的 {@code enterlevel}（{@code subexpr} 与 {@code statement}）。</li>
 *   <li>{@code assignment} 中的 {@code checklimit(nvars + nCcalls, LUAI_MAXCCALLS)}。</li>
 *   <li>{@code MAXREGS}（250）→ "function or expression too complex"。</li>
 * </ul>
 * 深度计算刻意区分左结合与右结合：只有右操作数/一元操作数/子结构会新增一层
 * {@code subexpr}，左结合链共享同一层，从而与标准 Lua 的递归形态一致。
 */
final class LuaParserLimits {

    private static final int MAX_CCALLS = 200;
    private static final int MAX_ARGS = 249;

    private final Source source;
    private final String chunkId;

    LuaParserLimits(Source source, String chunkId) {
        this.source = source;
        this.chunkId = chunkId;
    }

    /** 校验顶层 block；超出限制时抛出 {@link LuaParseError}。 */
    void check(LuaParser.BlockContext root) {
        // 一般性的递归深度（C levels）已由 LuaParserBase 在解析期强制；此处只补足
        // Lua 的 assignment 目标数检查（varlist 元素不经过 exp，所以解析期不会计数）。
        blockMax(root, 1);
    }

    private int blockMax(LuaParser.BlockContext block, int lvl) {
        int m = lvl;
        for (LuaParser.StatContext stat : block.stat()) {
            m = Math.max(m, statMax(stat, lvl + 1));
        }
        if (block.retstat() != null && block.retstat().explist() != null) {
            m = Math.max(m, explistMax(block.retstat().explist(), lvl + 1));
        }
        return m;
    }

    private int statMax(LuaParser.StatContext stat, int lvl) {
        if (stat instanceof LuaParser.StatDoContext s) {
            return blockMax(s.block(), lvl);
        }
        if (stat instanceof LuaParser.StatWhileContext s) {
            return Math.max(expMax(s.exp(), lvl), blockMax(s.block(), lvl));
        }
        if (stat instanceof LuaParser.StatRepeatContext s) {
            return Math.max(blockMax(s.block(), lvl), expMax(s.exp(), lvl));
        }
        if (stat instanceof LuaParser.StatIfContext s) {
            int m = lvl;
            for (LuaParser.ExpContext e : s.exp()) {
                m = Math.max(m, expMax(e, lvl));
            }
            for (LuaParser.BlockContext b : s.block()) {
                m = Math.max(m, blockMax(b, lvl));
            }
            return m;
        }
        if (stat instanceof LuaParser.StatForNumericContext s) {
            int m = lvl;
            for (LuaParser.ExpContext e : s.exp()) {
                m = Math.max(m, expMax(e, lvl));
            }
            return Math.max(m, blockMax(s.block(), lvl));
        }
        if (stat instanceof LuaParser.StatForGenericContext s) {
            return Math.max(explistMax(s.explist(), lvl), blockMax(s.block(), lvl));
        }
        if (stat instanceof LuaParser.StatFunctionDefContext s) {
            return funcbodyMax(s.funcbody(), lvl);
        }
        if (stat instanceof LuaParser.StatLocalFunctionDefContext s) {
            return funcbodyMax(s.funcbody(), lvl);
        }
        if (stat instanceof LuaParser.StatLocalVarAssignContext s) {
            return s.explist() == null ? lvl : explistMax(s.explist(), lvl);
        }
        if (stat instanceof LuaParser.StatAssignContext s) {
            int nvars = s.varlist().var().size();
            if (nvars + lvl > MAX_CCALLS) {
                throw error(s, "too many C levels (limit is " + MAX_CCALLS + ") in main function");
            }
            int m = lvl;
            for (LuaParser.VarContext v : s.varlist().var()) {
                m = Math.max(m, prefixMax(v.prefixexp(), lvl));
            }
            return Math.max(m, explistMax(s.explist(), lvl));
        }
        if (stat instanceof LuaParser.StatFunctionCallContext s) {
            return prefixMax(s.functioncall().prefixexp(), lvl);
        }
        return lvl;
    }

    private int funcbodyMax(LuaParser.FuncbodyContext fb, int lvl) {
        return blockMax(fb.block(), lvl);
    }

    private int explistMax(LuaParser.ExplistContext explist, int lvl) {
        int m = lvl;
        for (LuaParser.ExpContext e : explist.exp()) {
            m = Math.max(m, expMax(e, lvl));
        }
        return m;
    }

    /**
     * 计算解析 {@code exp} 子树时 {@code nCcalls} 的峰值。
     * {@code p} 为进入该 exp 的 {@code subexpr} 调用之前的值。
     */
    private int expMax(LuaParser.ExpContext exp, int p) {
        int cur = p + 1;
        if (exp instanceof LuaParser.ExpAtomContext atom) {
            return Math.max(cur, atomMax(atom.atom(), cur));
        }
        LuaParser.ExpContext left = null;
        LuaParser.ExpContext right = null;
        for (int i = 0; i < exp.getChildCount(); i++) {
            if (exp.getChild(i) instanceof LuaParser.ExpContext child) {
                if (left == null) {
                    left = child;
                } else {
                    right = child;
                }
            }
        }
        int m = cur;
        if (left == null) {
            return m;
        }
        if (right == null) {
            // 一元运算：操作数是一次新的 subexpr 递归
            return Math.max(m, expMax(left, cur));
        }
        // 二元运算：左操作数是同一 subexpr 的延续，右操作数是新的递归调用
        m = Math.max(m, expMax(left, p));
        return Math.max(m, expMax(right, cur));
    }

    private int atomMax(LuaParser.AtomContext atom, int lvl) {
        if (atom instanceof LuaParser.ExpPrefixExpAtomContext x) {
            return prefixMax(x.prefixexp(), lvl);
        }
        if (atom instanceof LuaParser.ExpTableConstructorContext x) {
            return tableMax(x.tableconstructor(), lvl);
        }
        if (atom instanceof LuaParser.ExpFunctionDefContext x) {
            return funcbodyMax(x.functiondef().funcbody(), lvl);
        }
        return lvl;
    }

    private int prefixMax(LuaParser.PrefixexpContext prefix, int lvl) {
        if (prefix instanceof LuaParser.PrefixParenContext p) {
            return expMax(p.exp(), lvl);
        }
        if (prefix instanceof LuaParser.VarNameContext) {
            return lvl;
        }
        if (prefix instanceof LuaParser.VarIndexedContext v) {
            return Math.max(prefixMax(v.prefixexp(), lvl), expMax(v.exp(), lvl));
        }
        if (prefix instanceof LuaParser.VarMemberAccessContext v) {
            return prefixMax(v.prefixexp(), lvl);
        }
        if (prefix instanceof LuaParser.FuncCallContext v) {
            return Math.max(prefixMax(v.prefixexp(), lvl), argsMax(v.args(), lvl));
        }
        if (prefix instanceof LuaParser.MethodCallContext v) {
            return Math.max(prefixMax(v.prefixexp(), lvl), argsMax(v.args(), lvl));
        }
        return lvl;
    }

    private int argsMax(LuaParser.ArgsContext args, int lvl) {
        if (args instanceof LuaParser.ArgsParenContext a) {
            if (a.explist() == null) {
                return lvl;
            }
            if (a.explist().exp().size() >= MAX_ARGS) {
                throw error(a, "function or expression too complex");
            }
            return explistMax(a.explist(), lvl);
        }
        if (args instanceof LuaParser.ArgsTableContext a) {
            return tableMax(a.tableconstructor(), lvl);
        }
        return lvl;
    }

    private int tableMax(LuaParser.TableconstructorContext table, int lvl) {
        int m = lvl;
        LuaParser.FieldlistContext fields = table.fieldlist();
        if (fields != null) {
            for (LuaParser.FieldContext field : fields.field()) {
                m = Math.max(m, fieldMax(field, lvl));
            }
        }
        return m;
    }

    private int fieldMax(LuaParser.FieldContext field, int lvl) {
        if (field instanceof LuaParser.FieldValueContext f) {
            return expMax(f.exp(), lvl);
        }
        if (field instanceof LuaParser.FieldKeyNameContext f) {
            return expMax(f.exp(), lvl);
        }
        if (field instanceof LuaParser.FieldKeyExprContext f) {
            List<LuaParser.ExpContext> exps = f.exp();
            int m = lvl;
            for (LuaParser.ExpContext e : exps) {
                m = Math.max(m, expMax(e, lvl));
            }
            return m;
        }
        return lvl;
    }

    private LuaParseError error(ParserRuleContext ctx, String message) {
        Token start = ctx.getStart();
        int line = start != null ? start.getLine() : 1;
        int column = start != null ? start.getCharPositionInLine() : 0;
        int length = ctx.getStop() != null && start != null
                ? ctx.getStop().getStopIndex() - start.getStartIndex() + 1
                : 1;
        return new LuaParseError(source, line, column, length,
                chunkId + ":" + line + ": " + message);
    }
}

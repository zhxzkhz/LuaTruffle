package com.zhhz.truffle.lua.parser;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class LuaParserBase extends Parser
{
    /** LUAI_MAXCCALLS（llimits.h）。 */
    private static final int MAX_CCALLS = 200;

    /**
     * 解析期的 C 调用深度，复刻标准 Lua 的 {@code enterlevel}/{@code leavelevel}：
     * {@code statement} 与 {@code subexpr} 各计一层。ANTLR 对左递归规则（{@code exp}）
     * 用循环消除左结合链，因此只有在右操作数/一元操作数/嵌套子结构处才会再次进入
     * {@code exp}，与标准 Lua 的递归形态一致。初始为 1（对应 {@code luaY_parser} 的一次 enterlevel）。
     */
    private int cLevel = 1;

    /** 左递归规则进入时压栈的规则号，供 {@code unrollRecursionContexts} 配对出栈。 */
    private final Deque<Integer> recursionRules = new ArrayDeque<>();

    protected LuaParserBase(TokenStream input) {
        super(input);
    }

    /** 超过 LUAI_MAXCCALLS 时抛出，由 {@code parseLuaImpl} 转换为 LuaParseError。 */
    public static final class CLimitReached extends RuntimeException {
        public final transient Token token;

        CLimitReached(Token token) {
            this.token = token;
        }
    }

    @Override
    public void enterRule(ParserRuleContext localctx, int state, int ruleIndex) {
        super.enterRule(localctx, state, ruleIndex);
        if (ruleIndex == LuaParser.RULE_stat || ruleIndex == LuaParser.RULE_retstat) {
            enterLevel(localctx);
        }
    }

    @Override
    public void exitRule() {
        int ruleIndex = _ctx.getRuleIndex();
        super.exitRule();
        if (ruleIndex == LuaParser.RULE_stat || ruleIndex == LuaParser.RULE_retstat) {
            cLevel--;
        }
    }

    @Override
    public void enterRecursionRule(ParserRuleContext localctx, int state, int ruleIndex, int precedence) {
        super.enterRecursionRule(localctx, state, ruleIndex, precedence);
        recursionRules.push(ruleIndex);
        if (ruleIndex == LuaParser.RULE_exp) {
            enterLevel(localctx);
        }
    }

    @Override
    public void unrollRecursionContexts(ParserRuleContext parentctx) {
        Integer ruleIndex = recursionRules.isEmpty() ? null : recursionRules.pop();
        super.unrollRecursionContexts(parentctx);
        if (ruleIndex != null && ruleIndex == LuaParser.RULE_exp) {
            cLevel--;
        }
    }

    private void enterLevel(ParserRuleContext ctx) {
        if (++cLevel > MAX_CCALLS) {
            throw new CLimitReached(ctx.getStart());
        }
    }

    public boolean IsFunctionCall()
    {
        BufferedTokenStream stream = (BufferedTokenStream)_input;
        var la = stream.LT(1);
        if (la.getType() != LuaLexer.NAME) return false;
        la = stream.LT(2);
        return la.getType() != LuaLexer.OP;
    }
}

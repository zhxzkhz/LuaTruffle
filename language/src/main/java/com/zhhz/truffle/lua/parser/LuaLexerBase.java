package com.zhhz.truffle.lua.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class LuaLexerBase extends Lexer {

    private int start_line;
    private int start_col;

    protected LuaLexerBase(CharStream input) {
        super(input);
    }

    /**
     * 修正长字符串 token 之后的 lexer 行号。ANTLR 默认对 \r 与 \n 各计一次换行，
     * 且 \r\n / \n\r 会被计两次；标准 Lua 把 \r\n / \n\r 各计为一次换行，
     * 单独的 \r 或 \n 各计一次。本方法根据 token 文本按标准 Lua 语义重新
     * 计算逻辑换行数，并把 lexer 行号重置为 token 起始行 + 逻辑换行数。
     */
    public void AdjustLongStringLine()
    {
        String text = getText();
        int logicalNewlines = 0;
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '\r')
            {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n')
                {
                    logicalNewlines++;
                    i++;
                }
                else
                {
                    logicalNewlines++;
                }
            }
            else if (c == '\n')
            {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\r')
                {
                    logicalNewlines++;
                    i++;
                }
                else
                {
                    logicalNewlines++;
                }
            }
        }
        int newLine = _tokenStartLine + logicalNewlines;
        setLine(newLine);
        setCharPositionInLine(0);
    }

    /**
     * ANTLR 词法器只对 '\n' 递增行号（'\r\n' 经 '\n' 计一次），不统计孤立 '\r'。
     * 标准 Lua 把孤立 '\r' 视为一个换行：NL token 匹配到孤立 '\r' 时补计行号。
     * 但 '\n\r' 配对标准 Lua 只计一次（前一个 '\n' 已计行），不补计。
     */
    public void AdjustNewlineLine()
    {
        String text = getText();
        if (text.length() == 1 && text.charAt(0) == '\r')
        {
            if (_tokenStartCharIndex > 0)
            {
                String previous = _input.getText(
                        org.antlr.v4.runtime.misc.Interval.of(_tokenStartCharIndex - 1, _tokenStartCharIndex - 1));
                if ("\n".equals(previous))
                {
                    return;  // '\n\r' 配对：'\n' 已计行，不重复补计
                }
            }
            setLine(getLine() + 1);
        }
    }

    /**
     * 短字符串 token 内的原始 CR 字节（如反斜杠-CR 续行转义、原始 CR 内容）
     * 不被 ANTLR 计行。按标准 Lua 的逻辑换行语义（\r\n、\n\r、孤立 \r、孤立 \n
     * 各计一次）从 token 起始行重算行号，与 AdjustLongStringLine 同一模式。
     */
    public void AdjustShortStringLine()
    {
        String text = getText();
        int logicalNewlines = 0;
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '\r')
            {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n')
                {
                    i++;
                }
                logicalNewlines++;
            }
            else if (c == '\n')
            {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\r')
                {
                    i++;
                }
                logicalNewlines++;
            }
        }
        if (logicalNewlines > 0)
        {
            setLine(_tokenStartLine + logicalNewlines);
            setCharPositionInLine(0);
        }
    }

    protected void HandleComment()
    {
        start_line = this.getLine();
        start_col = this.getCharPositionInLine() - 2;
        var cs = _input;
        if (cs.LA(1) == '[')
        {
            int sep = skip_sep(cs);
            if (sep >= 2)
            {
                read_long_string(cs, sep);
                return;
            }
        }
        while (cs.LA(1) != '\n' && cs.LA(1) != '\r' && cs.LA(1) != -1)
        {
            consumeChar(cs);
        }
    }

    private void read_long_string(CharStream cs, int sep)
    {
        boolean done = false;
        consumeChar(cs);
        do {
            var c = cs.LA(1);
            switch (c) {
                case -1:
                    done = true;
                    ANTLRErrorListener listener = this.getErrorListenerDispatch();
                    listener.syntaxError(this, null, this.start_line, this.start_col, "unfinished long comment", null);
                    break;
                case ']':
                    if (skip_sep(cs) == sep) {
                        consumeChar(cs);
                        done = true;
                    }
                    break;
                default:
                    if (cs.LA(1) == -1) {
                        done = true;
                        break;
                    }
                    consumeChar(cs);
                    break;
            }
        } while (!done);
    }

    private int skip_sep(CharStream cs)
    {
        int count = 0;
        int s = cs.LA(1);
        consumeChar(cs);
        while (cs.LA(1) == '=')
        {
            consumeChar(cs);
            count++;
        }
        if (cs.LA(1) == s) count += 2;
        else if (count == 0) count = 1;
        else count = 0;
        return count;
    }

    /**
     * 消费一个字符并手动维护行号（\r\n / \n\r 计为一次换行，单独 \r 或 \n 各计一次）。
     * 用于注释等手动读取场景。直接推进 CharStream 而不经过 LexerATNSimulator，
     * 避免模拟器的行号计数与手动 setLine 重复累加（实测长注释内部每条换行计两次，
     * 后续 token/错误行号被高估；长字符串因有 AdjustLongStringLine 修正而无此问题）。
     */
    private void consumeChar(CharStream cs)
    {
        int c = cs.LA(1);
        cs.consume();
        if (c == '\r')
        {
            if (cs.LA(1) == '\n')
            {
                cs.consume();
            }
            this.setLine(this.getLine() + 1);
            this.setCharPositionInLine(0);
        }
        else if (c == '\n')
        {
            if (cs.LA(1) == '\r')
            {
                cs.consume();
            }
            this.setLine(this.getLine() + 1);
            this.setCharPositionInLine(0);
        }
    }

    public boolean IsLine1Col0()
    {
        CharStream cs = _input;
        // 文件型 Source 的首行 '#' 注释由 LuaBaseParser.toByteModelText 的
        // skipFileInitialComment 预处理（等价 luaL_loadfilex skipcomment）。
        // 此处仅保留 '#!' 形式的内联 shebang 处理：load(str) 中的 '#' 是长度
        // 运算符（literals.lua 变量名字节测试要求 load("#=1") 语法错误失败），
        // 故此谓词在匹配完 '#' 后求值，LA(1) 必须为 '!'。
        return cs.index() == 1 && cs.LA(1) == '!';
    }
}

package com.zhhz.truffle.lua.parser;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 基本解析器类，处理常见的Lua行为，如错误报告、作用域和文字解析。
 */
public abstract class LuaBaseParser extends LuaParserBaseVisitor<LuaStatementNode> {

    /** 当前解析所用的 token 流，供 visitor 在需要时查看错误点之后的 token。 */
    protected TokenStream tokenStream;

    /**
     * Base implementation of parsing, which handles lexer and parser setup, and error reporting.
     */
    protected static void parseLuaImpl(Source source, LuaBaseParser visitor) {
        String characters = toByteModelText(source);
        if (!characters.isEmpty() && characters.charAt(0) == '\uFEFF') {
            characters = ' ' + characters.substring(1);
        } else if (characters.startsWith("\u00EF\u00BB\u00BF")) {
            // Latin-1 读入的源文件中，UTF-8 BOM (EF BB BF) 呈现为三个 Latin-1 字符，
            // 与标准 lua 一样跳过；用等宽空格替换以保持源位置不变。
            characters = "   " + characters.substring(3);
        }
        LuaLexer lexer = new LuaLexer(CharStreams.fromString(characters));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LuaParser parser = new LuaParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        BailoutErrorListener listener = new BailoutErrorListener(source);
        lexer.addErrorListener(listener);
        parser.addErrorListener(listener);
        visitor.tokenStream = tokens;

        try {
            parser.lualanguage().accept(visitor);
        } catch (LuaParserBase.CLimitReached limit) {
            int line = limit.token != null ? limit.token.getLine() : 1;
            int column = limit.token != null ? limit.token.getCharPositionInLine() : 0;
            throw new LuaParseError(source, line, column, 1,
                    chunkId(source.getName()) + ":" + line
                            + ": too many C levels (limit is 200) in main function");
        }
    }

    /**
     * 将源码文本转为字节模型字符串：每个字符对应一个源字节。
     * 文件型 Source 按 Latin-1 读入，字符即字节，原样返回；含真实 Unicode 字符
     * （&gt;0xFF，来自 -e、REPL、load 等非文件输入）的文本按 UTF-8 编码后再按
     * Latin-1 解码，使字符与该文本的实际字节一一对应。
     * 词法器与 {@link #sourceString} 必须使用同一文本，保证 token 索引一致。
     */
    protected static String toByteModelText(Source source) {
        String text = source.getCharacters().toString();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 0xFF) {
                text = new String(text.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        java.nio.charset.StandardCharsets.ISO_8859_1);
                break;
            }
        }
        // 仅文件型 Source 执行 luaL_loadfilex 的 skipcomment（跳过 UTF-8 BOM 与
        // 首行 '#' 注释，保留换行以保持行号）。load(str)/load(reader) 不做此处理：
        // 官方 literals.lua 的变量名字节测试依赖 load("#=1") 按语法错误失败。
        if (source.getPath() != null) {
            text = skipFileInitialComment(text);
        }
        return text;
    }

    /**
     * 对应标准 Lua luaL_loadfilex 的 skipBOM + skipcomment：
     * 跳过可选的 UTF-8 BOM；若其后首字符为 '#'，删除首行注释内容但保留换行
     * （标准实现以 '\n' 替换首行以修正行号）。无换行的注释文件变为空块。
     */
    private static String skipFileInitialComment(String text) {
        int start = 0;
        if (text.startsWith("\uFEFF")) {
            start = 1;
        } else if (text.startsWith("\u00EF\u00BB\u00BF")) {
            start = 3;
        }
        if (start < text.length() && text.charAt(start) == '#') {
            int nl = text.indexOf('\n', start);
            return nl < 0 ? "" : text.substring(nl);
        }
        return start == 0 ? text : text.substring(start);
    }

    protected final LuaLanguage language;
    protected final Source source;
    protected final TruffleString sourceString;

    /**
     * 在解析期间，用于跟踪词法作用域的栈。
     * 每个 Map 代表一个作用域 (函数或块)，将变量名映射到其 FrameSlot 的 `int` 索引。
     */
    protected final Stack<Scope> scopeStack;

    protected final Stack<FrameDescriptor.Builder> fdBuilderStack;
    private int nextLocalDeclarationId;

    /** 最近一次 {@link #declareLocalVariable} 创建的 {@link LocalSlot}（供循环变量等声明处使用）。 */
    protected LocalSlot lastDeclaredLocalSlot;

    /** 隐式 {@code _ENV} 局部变量的名字。 */
    protected static final TruffleString ENV_NAME = TruffleString.fromJavaStringUncached("_ENV", LuaLanguage.STRING_ENCODING);

    /** 主块隐式 {@code _ENV} 变量所占用的（隐藏）槽位。 */
    protected int mainEnvSlot = -1;

    /** 主块隐式 {@code _ENV} 变量的可变句柄。 */
    protected LocalSlot mainEnvLocalSlot;

    protected LuaBaseParser(LuaLanguage language, Source source) {
        this.language = language;
        this.source = source;
        this.scopeStack = new Stack<>();
        this.fdBuilderStack = new Stack<>();
        this.nextLocalDeclarationId = 0;
        sourceString = LuaContext.toTruffleString(toByteModelText(source));
    }

    protected LuaBaseParser(LuaLanguage language, Source source,Stack<Scope> scopes,Stack<FrameDescriptor.Builder> stack) {
        this.language = language;
        this.source = source;
        this.scopeStack = scopes;
        this.fdBuilderStack = stack;
        this.nextLocalDeclarationId = 0;
        sourceString = LuaContext.toTruffleString(toByteModelText(source));
    }


    protected void semErr(Token token, String message) {
        assert token != null;
        throwParseError(source, token.getLine(), token.getCharPositionInLine(), token, message);
    }

    private static final class BailoutErrorListener extends BaseErrorListener {
        private final Source source;

        BailoutErrorListener(Source source) {
            this.source = source;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            // 标准 Lua 的 exprstat/assignment 在读到 ',' 后即逐项累计赋值目标数并检查
            // LUAI_MAXCCALLS（C levels）——即使后面缺少 '=' 也会先报 C levels。
            // 我们的文法要求 varlist '=' explist，缺 '=' 时解析失败；这里在错误点回溯
            // 已解析出的 varlist，若目标数超限则改报 "too many C levels"。
            if (recognizer instanceof Parser parser) {
                int nvars = countParsedVarlist(parser.getContext());
                if (nvars > 0 && nvars + 2 > 200) {
                    throwParseError(source, line, charPositionInLine, null,
                            "too many C levels (limit is 200) in main function");
                    return;
                }
                // 对深度畸形输入，ANTLR 的自适应预测可能在真正递归下降前就失败，
                // 从而绕过解析期的 C levels 计数。此处用 token 流做一个粗略的嵌套深度
                // 估计；仅在解析已失败时使用，因此估计偏大也不会影响合法程序。
                if (estimateCLimit(parser.getInputStream()) > 200) {
                    throwParseError(source, line, charPositionInLine, null,
                            "too many C levels (limit is 200) in main function");
                    return;
                }
                // 局部变量上限（MAXVARS=200）：解析失败时也可能因单个 local 声明过长而
                // 应先报 "too many local variables"（标准 Lua 在读到第 201 个变量时即报错）。
                int localLine = tooManyLocals(parser.getInputStream());
                if (localLine >= 0) {
                    throwParseError(source, line, charPositionInLine, null,
                            "too many local variables (limit is 200) in "
                                    + (localLine > 0 ? "function at line " + localLine : "main function"));
                    return;
                }
            }
            // ANTLR lexer error ("token recognition error at: '...'"): offendingSymbol is null,
            // msg contains the unrecognized lexeme in single quotes. Standard Lua reports
            // "... near '<lexeme>'". Reconstruct from msg to match the official test suite
            // (literals.lua lexerror checks via string.find(msg, "near .-" .. err), err has ' appended).
            if (offendingSymbol == null && msg != null && msg.startsWith("token recognition error at:")) {
                String nearToken = extractLexerToken(msg);
                if (nearToken != null) {
                    // 标准 Lua read_string：裸 \r/\n 出现在短字符串内 → "unfinished string"，
                    // near 为词法缓冲区内容（词素末尾的换行字符是词法失败点，不属缓冲区）
                    if (isQuoteInitiated(nearToken) && !endsWithMatchingQuote(nearToken)) {
                        char last = nearToken.charAt(nearToken.length() - 1);
                        if (last == '\n' || last == '\r') {
                            throwParseError(source, line, charPositionInLine, null,
                                    "unfinished string near '" + nearToken.substring(0, nearToken.length() - 1) + "'");
                            return;
                        }
                        // 字符串消费到输入末尾仍未闭合（且尾部不在未完成的转义序列上）
                        // → "unfinished string near '<eof>'"；若尾部停在未完成/非法转义
                        // （十六进制转义不足两位、u 转义缺花括号、十进制转义超限等）
                        // 则标准报转义错误（near 为部分内容），维持 malformed string 路径。
                        if (!endsWithIncompleteEscape(nearToken)) {
                            throwParseError(source, line, charPositionInLine, null,
                                    "unfinished string near <eof>");
                            return;
                        }
                    }
                    // 非字符串起始、词法无法识别的单个非打印字节（控制字符或 >=0x7F）：
                    // 标准 Lua 报 "unexpected symbol near '<\ddd>'"（\ddd 为十进制字节值）。
                    if (!isQuoteInitiated(nearToken) && nearToken.length() == 1) {
                        char c = nearToken.charAt(0);
                        if (c < 0x20 || c >= 0x7f) {
                            int code = c & 0xFF;
                            throwParseError(source, line, charPositionInLine, null,
                                    "unexpected symbol near '<\\" + code + ">'");
                            return;
                        }
                    }
                    throwParseError(source, line, charPositionInLine, null,
                            "malformed string near '" + nearToken + "'");
                    return;
                }
            }
            // 未闭合的长括号（[=[alo]] 等）无法匹配 LONGSTRING 规则，被 ANTLR 分解为
            // 独立 token（'[' '=' '[' ...），解析器在表达式位置的 '[' 处报错。
            // 检查该 '[' 起始的源码是否为缺少闭合符的长括号，是则按标准 Lua 报
            // "unfinished long string (starting at line N) near '<eof>'"。
            if (offendingSymbol instanceof Token token && "[".equals(token.getText())) {
                String unfinishedLong = detectUnfinishedLongBracket(source, token);
                if (unfinishedLong != null) {
                    throwParseError(source, line, charPositionInLine, null, unfinishedLong);
                    return;
                }
            }
            throwParseError(source, line, charPositionInLine, (Token) offendingSymbol,
                    luaSyntaxMessage(recognizer, offendingSymbol, e, msg));
        }
    }

    /**
     * 把 ANTLR 的解析错误消息改写为 Lua 风格："... expected near '&lt;token&gt;'"。
     * 标准 Lua 的语法错误文本形如 {@code '=' expected near 'do'}，官方测试
     * （constructs.lua/errors.lua）据此断言，因此这里尽量重建该措辞。
     */
    private static String luaSyntaxMessage(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                           RecognitionException e, String msg) {
        String near = nearTokenText(offendingSymbol, e);
        String expected = expectedTokenText(recognizer, e);
        if (expected != null) {
            return expected + " expected near " + quoteNear(near);
        }
        if (msg != null && msg.contains("expected")) {
            return msg;
        }
        return "syntax error near " + quoteNear(near);
    }

    /**
     * 从当前解析上下文向上/向下查找已解析的 {@code varlist}，返回其目标个数；
     * 找不到返回 0。用于在赋值缺少 '=' 的语法错误前，按标准 Lua 判定 C levels 超限。
     */
    private static int countParsedVarlist(ParserRuleContext ctx) {
        ParserRuleContext current = ctx;
        while (current != null) {
            if (current instanceof LuaParser.StatAssignContext assign) {
                return assign.varlist() != null ? assign.varlist().var().size() : 0;
            }
            if (current instanceof LuaParser.VarlistContext varlist) {
                return varlist.var().size();
            }
            for (int i = 0; i < current.getChildCount(); i++) {
                if (current.getChild(i) instanceof LuaParser.VarlistContext varlist) {
                    return varlist.var().size();
                }
            }
            current = current.getParent();
        }
        return 0;
    }

    /**
     * 粗略估计 token 流中的最大嵌套深度（C levels）。综合括号/块关键字嵌套与
     * 右结合运算符（{@code ..}/{@code ^}）链长，用于解析失败时判定是否应报 C levels。
     */
    private static int estimateCLimit(TokenStream stream) {
        if (stream instanceof CommonTokenStream common) {
            common.fill();
        }
        int depth = 1;
        int max = 1;
        int chain = 0;
        int n = stream.size();
        for (int i = 0; i < n; i++) {
            Token t = stream.get(i);
            if (t == null || t.getChannel() != Token.DEFAULT_CHANNEL) {
                continue;
            }
            int type = t.getType();
            if (type == Token.EOF) {
                break;
            }
            if (type == LuaLexer.OP || type == LuaLexer.OCU || type == LuaLexer.OB
                    || type == LuaLexer.DO || type == LuaLexer.THEN
                    || type == LuaLexer.REPEAT || type == LuaLexer.FUNCTION) {
                depth++;
                chain = 0;
            } else if (type == LuaLexer.CP || type == LuaLexer.CCU || type == LuaLexer.CB
                    || type == LuaLexer.END || type == LuaLexer.UNTIL) {
                if (depth > 1) {
                    depth--;
                }
                chain = 0;
            } else if (type == LuaLexer.DD || type == LuaLexer.CARET) {
                chain++;
                if (depth + chain > max) {
                    max = depth + chain;
                }
            } else if (type == LuaLexer.NAME || type == LuaLexer.INT || type == LuaLexer.HEX
                    || type == LuaLexer.FLOAT || type == LuaLexer.HEX_FLOAT
                    || type == LuaLexer.NIL || type == LuaLexer.FALSE || type == LuaLexer.TRUE
                    || type == LuaLexer.DDD) {
                // 操作数：右结合链继续
            } else {
                chain = 0;
            }
            if (depth > max) {
                max = depth;
            }
        }
        return max;
    }

    /**
     * 检测解析失败输入中的局部变量上限（MAXVARS=200）。标准 Lua 在读到第 201 个
     * 局部变量时立即报 "too many local variables"，而畸形输入（例如缺少 {@code end}）
     * 会让 ANTLR 先于 visitor 报语法错误，故此处扫描 token 流兜底。
     *
     * @return 违规 local 声明所在函数的定义行；未发现时返回 -1，函数为顶层时返回 0。
     */
    private static int tooManyLocals(TokenStream stream) {
        if (stream instanceof CommonTokenStream common) {
            common.fill();
        }
        int n = stream.size();
        int lastFuncLine = 0;
        for (int i = 0; i < n; i++) {
            Token t = stream.get(i);
            if (t == null || t.getChannel() != Token.DEFAULT_CHANNEL) {
                continue;
            }
            int type = t.getType();
            if (type == Token.EOF) {
                break;
            }
            if (type == LuaLexer.FUNCTION) {
                lastFuncLine = t.getLine();
                continue;
            }
            if (type != LuaLexer.LOCAL) {
                continue;
            }
            int j = nextDefaultChannel(stream, i + 1, n);
            if (j < 0) {
                continue;
            }
            if (stream.get(j).getType() == LuaLexer.FUNCTION) {
                continue; // local function foo
            }
            int count = 0;
            boolean expectName = true;
            int k = j;
            while (k >= 0 && k < n) {
                Token tk = stream.get(k);
                if (tk.getChannel() != Token.DEFAULT_CHANNEL) {
                    k++;
                    continue;
                }
                int tkType = tk.getType();
                if (expectName) {
                    if (tkType != LuaLexer.NAME) {
                        break;
                    }
                    count++;
                    expectName = false;
                    k++;
                } else if (tkType == LuaLexer.COMMA) {
                    expectName = true;
                    k++;
                } else if (tkType == LuaLexer.LT) {
                    // 跳过属性 <const>/<close>
                    int close = nextDefaultChannel(stream, k + 1, n);
                    close = close < 0 ? close : nextDefaultChannel(stream, close + 1, n);
                    if (close < 0) {
                        break;
                    }
                    k = close + 1;
                } else {
                    break;
                }
            }
            if (count > 200) {
                return lastFuncLine;
            }
        }
        return -1;
    }

    private static int nextDefaultChannel(TokenStream stream, int from, int n) {
        for (int i = from; i < n; i++) {
            Token t = stream.get(i);
            if (t != null && t.getChannel() == Token.DEFAULT_CHANNEL) {
                return i;
            }
        }
        return -1;
    }

    /** 标准 Lua 中 eof 记为 {@code <eof>}（不带引号），其它 token 用单引号包裹。 */
    private static String quoteNear(String near) {
        return "<eof>".equals(near) ? "<eof>" : "'" + near + "'";
    }

    /** 重建错误点附近的 token 文本（标准 Lua 用 {@code '<eof>'}）。 */
    private static String nearTokenText(Object offendingSymbol, RecognitionException e) {
        Token token = offendingSymbol instanceof Token tk ? tk : (e != null ? e.getOffendingToken() : null);
        if (token != null) {
            if (token.getType() == Token.EOF) {
                return "<eof>";
            }
            String text = token.getText();
            if (text != null) {
                return text;
            }
        }
        return "<eof>";
    }

    /** 根据 ANTLR 期望集生成 " 'x' "、" 'x' or 'y' " 形式的期望 token 描述。 */
    private static String expectedTokenText(Recognizer<?, ?> recognizer, RecognitionException e) {
        if (e == null) {
            return null;
        }
        IntervalSet expected = e.getExpectedTokens();
        if (expected == null || expected.isNil()) {
            return null;
        }
        int[] tokens = expected.toArray();
        if (tokens.length == 0) {
            return null;
        }
        Vocabulary vocabulary = recognizer != null ? recognizer.getVocabulary() : VocabularyImpl.EMPTY_VOCABULARY;
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(tokens.length, 4);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(" or ");
            }
            sb.append(vocabulary.getDisplayName(tokens[i]));
        }
        if (tokens.length > limit) {
            sb.append(" or ...");
        }
        return sb.toString();
    }

    /**
     * Extract the lexeme wrapped in single quotes from an ANTLR
     * "token recognition error at: '...'" message.
     * e.g. token recognition error at: '"abc\x"' -> "abc\x"
     */
    private static String extractLexerToken(String msg) {
        int firstQuote = msg.indexOf('\'');
        int lastQuote = msg.lastIndexOf('\'');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return msg.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }

    /** 词素是否以字符串定界符（" 或 '）开头。 */
    private static boolean isQuoteInitiated(String lexeme) {
        return !lexeme.isEmpty() && (lexeme.charAt(0) == '"' || lexeme.charAt(0) == '\'');
    }

    /** 词素末字符是否为与开引号匹配的闭引号（词法失败点在闭引号上 → 转义/内容错误）。 */
    private static boolean endsWithMatchingQuote(String lexeme) {
        return lexeme.length() >= 2 && lexeme.charAt(lexeme.length() - 1) == lexeme.charAt(0);
    }

    /**
     * 判定词素尾部是否停在未完成或非法的转义序列上（标准 Lua 此时报告
     * "hexadecimal digit expected" / "missing '{'" / "missing '}'" /
     * "decimal escape too large" / "invalid escape sequence" 等转义错误，
     * near 为部分内容；否则到输入末尾未闭合报 "unfinished string"）。
     * 注意：行尾裸反斜杠在标准 Lua 中经 EOZ 路径报 unfinished string，
     * 不算未完成转义。
     */
    private static boolean endsWithIncompleteEscape(String lexeme) {
        int length = lexeme.length();
        int i = 1;  // 跳过开引号
        while (i < length) {
            char c = lexeme.charAt(i);
            if (c != '\\') {
                i++;
                continue;
            }
            if (i + 1 >= length) {
                return false;  // 行尾裸反斜杠：标准 Lua 报 unfinished string
            }
            char escape = lexeme.charAt(i + 1);
            switch (escape) {
                case 'x' -> {
                    int digits = 0;
                    int k = i + 2;
                    while (k < length && isHexDigit(lexeme.charAt(k))) {
                        digits++;
                        k++;
                    }
                    if (digits < 2) {
                        return true;  // \xHH 不足两位
                    }
                    i = k;
                }
                case 'u' -> {
                    if (i + 2 >= length || lexeme.charAt(i + 2) != '{') {
                        return true;  // u 转义后缺 '{'
                    }
                    int k = i + 3;
                    int hexDigits = 0;
                    while (k < length && isHexDigit(lexeme.charAt(k))) {
                        hexDigits++;
                        k++;
                    }
                    if (k >= length || lexeme.charAt(k) != '}' || hexDigits == 0) {
                        return true;  // u{... 转义未闭合或无数字
                    }
                    i = k + 1;
                }
                case '\n' -> i += 2;  // \<LF> 续行转义
                case '\r' -> {  // \<CR> 或 \<CR><LF> 续行转义
                    i += 2;
                    if (i < length && lexeme.charAt(i) == '\n') {
                        i++;
                    }
                }
                default -> {
                    if (escape >= '0' && escape <= '9') {
                        int value = 0;
                        int digits = 0;
                        int k = i + 1;
                        while (k < length && digits < 3
                                && lexeme.charAt(k) >= '0' && lexeme.charAt(k) <= '9') {
                            value = value * 10 + (lexeme.charAt(k) - '0');
                            digits++;
                            k++;
                        }
                        if (value > 255) {
                            return true;  // \ddd 数值超限
                        }
                        i = k;
                    } else if ("abfnrtvz\"'|$#\\".indexOf(escape) >= 0) {
                        i += 2;  // 合法单字符转义
                    } else {
                        return true;  // 非法转义字符
                    }
                }
            }
        }
        return false;
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    /**
     * 检查从 token 起始位置开始的源码是否为缺少闭合符的长括号
     * （[=*[ ... 到输入末尾未找到 ]=*]）。是则返回标准 Lua 格式的错误消息
     * "unfinished long string (starting at line N) near '<eof>'"，
     * 非长括号起始或已闭合返回 null（保持原解析错误）。
     */
    private static String detectUnfinishedLongBracket(Source source, Token token) {
        CharSequence chars = source.getCharacters();
        int length = chars.length();
        int start = token.getStartIndex();
        if (start < 0 || start >= length || chars.charAt(start) != '[') {
            return null;
        }
        int pos = start + 1;
        int level = 0;
        while (pos < length && chars.charAt(pos) == '=') {
            level++;
            pos++;
        }
        if (pos >= length || chars.charAt(pos) != '[') {
            return null;  // 不是长括号起始（'[' 为普通索引括号等）
        }
        pos++;  // 跳过第二个 '['
        while (pos < length) {
            if (chars.charAt(pos) == ']') {
                int scan = pos + 1;
                int equals = 0;
                while (scan < length && chars.charAt(scan) == '=') {
                    equals++;
                    scan++;
                }
                if (equals == level && scan < length && chars.charAt(scan) == ']') {
                    return null;  // 存在闭合符 ]=*]，非未闭合长括号
                }
            }
            pos++;
        }
        return "unfinished long string (starting at line " + token.getLine() + ") near <eof>";
    }

    private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
        int col = charPositionInLine + 1;
        int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
        throw new LuaParseError(source, line, col, length,
                chunkId(source.getName()) + ":" + line + ": " + message);
    }

    /**
     * 复刻标准 Lua 的 {@code luaO_chunkid}：把 Source 名格式化为错误消息前缀。
     * <ul>
     *   <li>{@code =name} → 直接输出 {@code name}；</li>
     *   <li>{@code @file} → 文件名（过长时保留尾部并前置 {@code ...}）；</li>
     *   <li>其它 → {@code [string "content"]}（在首个换行处截断，超长时截断）。</li>
     * </ul>
     */
    protected static String chunkId(String source) {
        if (source == null) {
            source = "?";
        }
        int bufflen = 60; // LUA_IDSIZE
        if (!source.isEmpty() && source.charAt(0) == '=') {
            String body = source.substring(1);
            return body.length() <= bufflen - 1 ? body : body.substring(0, bufflen - 1);
        }
        if (!source.isEmpty() && source.charAt(0) == '@') {
            String body = source.substring(1);
            if (body.length() <= bufflen - 1) {
                return body;
            }
            return "..." + body.substring(body.length() - (bufflen - 1 - 3));
        }
        StringBuilder out = new StringBuilder("[string \"");
        int l = source.length();
        int nl = source.indexOf('\n');
        if (l < bufflen - 11) {
            if (nl >= 0) {
                out.append(source, 0, nl).append("...");
            } else {
                out.append(source);
            }
        } else {
            out.append(source, 0, bufflen - 15).append("...");
        }
        out.append("\"]");
        return out.toString();
    }

    protected TruffleString asTruffleString(Token literalToken) {
        return asTruffleString(literalToken,false);
    }

    protected TruffleString asTruffleString_UTF16(Token literalToken, boolean removeQuotes) {
        int fromIndex = literalToken.getStartIndex();
        int length = literalToken.getStopIndex() - literalToken.getStartIndex() + 1;
        if (removeQuotes) {
            /* Remove the trailing and ending " */
            assert literalToken.getText().length() >= 2 && literalToken.getText().startsWith("\"") && literalToken.getText().endsWith("\"");
            fromIndex += 1;
            length -= 2;
        }
        return sourceString.substringByteIndexUncached(fromIndex * 2, length * 2, LuaLanguage.STRING_ENCODING, true);
    }

    protected TruffleString asTruffleString(Token literalToken, boolean removeQuotes) {
        // ANTLR Token 返回的是基于字符的索引 (Character Index)，而不是字节索引
        int fromIndex = literalToken.getStartIndex();
        int length = literalToken.getStopIndex() - literalToken.getStartIndex() + 1;

        if (removeQuotes) {
            /* 移除首尾的引号 " */
            assert literalToken.getText().length() >= 2 && ((
                    literalToken.getText().startsWith("\"") &&
                            literalToken.getText().endsWith("\"")) || (literalToken.getText().startsWith("'") &&
                    literalToken.getText().endsWith("'")));

            // 逻辑字符位置向后移 1 位，长度减少 2 个字符
            fromIndex += 1;
            length -= 2;
        }

        return sourceString.substringUncached(
                fromIndex,
                length,
                LuaLanguage.STRING_ENCODING, // 确保此处常量已改为 UTF-8
                true
        );
    }


    protected final void enterMain() {
        // 为顶层脚本（main chunk）也创建一个作用域和 builder
        //scopeStack.clear();
        fdBuilderStack.clear();

        // 顶层脚本也像一个函数，有自己的 FrameDescriptor 和作用域
        fdBuilderStack.push(FrameDescriptor.newBuilder());
        scopeStack.push(new Scope(false));

        // Lua 5.3：每个 chunk 都有一个隐式的 _ENV 局部变量（在标准实现中是主块的
        // upvalue）。全局访问 x 会被编译为 _ENV.x。为了让嵌套函数也能经 upvalue
        // 捕获 _ENV，这里把它声明为主块作用域的局部变量。槽位名使用非 TruffleString
        // 标识符，使其对 debug.getlocal 等具名槽位枚举不可见（标准实现中它是 upvalue）。
        mainEnvSlot = declareEnvironmentLocal();
    }

    private int declareEnvironmentLocal() {
        int slotIndex = fdBuilderStack.peek().addSlot(FrameSlotKind.Illegal, "$_ENV", null);
        int declarationId = nextLocalDeclarationId++;
        LocalSlot localSlot = new LocalSlot(slotIndex, ENV_NAME);
        scopeStack.peek().variables.put(ENV_NAME, new LocalVariable(slotIndex, LocalAttribute.NONE, declarationId, localSlot));
        mainEnvLocalSlot = localSlot;
        return slotIndex;
    }

    protected final FrameDescriptor exitMain() {
        return fdBuilderStack.pop().build();
    }

    /**
     * 当开始解析一个新函数时调用。
     * @param parameterNames 函数的参数名列表。
     * @return 一个 int 数组，包含了每个参数对应的槽位索引。
     */
    protected int[] enterFunctionScope(List<TruffleString> parameterNames) {
        // 1. 为新函数创建一个新的 FrameDescriptor.Builder，并压入栈
        FrameDescriptor.Builder builder = FrameDescriptor.newBuilder();
        fdBuilderStack.push(builder);
        // 2. 在作用域栈上创建一个新的作用域 (Map)
        scopeStack.push(new Scope(true));

        // 3. 为所有参数声明槽位，并收集它们的索引
        int[] parameterSlots = new int[parameterNames.size()];
        for (TruffleString parameterName : parameterNames) {
            declareLocalVariable(parameterName);
        }
        return parameterSlots;
    }

    /**
     * 当函数解析完成时调用。
     * @return 构建好的 FrameDescriptor。
     */
    protected FrameDescriptor exitFunctionScope() {
        // 1. 销毁当前函数的最外层作用域
        //    这个 pop() 必须与 enterFunctionScope() 中的 push() 相对应
        scopeStack.pop();
        // 2. 从栈中获取当前函数的 FrameDescriptor.Builder
        FrameDescriptor.Builder builder = fdBuilderStack.pop();
        // 使用 builder 构建最终的、不可变的 FrameDescriptor
        return builder.build();
    }

    /**
     * 当进入一个新的块作用域 (do...end) 时调用。
     */
    protected void enterBlockScope() {
        scopeStack.push(new Scope(false));
    }

    /**
     * 当离开一个块作用域时调用。
     */
    protected void exitBlockScope() {
        scopeStack.pop();
    }


    // --- 变量声明与查找 ---

    /**
     * 在当前作用域中声明一个新的局部变量。
     * @param name 变量名。
     * @return 该变量对应的槽位索引。
     */
    protected int declareLocalVariable(TruffleString name) {
        return declareLocalVariable(name, LocalAttribute.NONE);
    }

    protected int declareLocalVariable(TruffleString name, LocalAttribute attribute) {
        int slotIndex = fdBuilderStack.peek().addSlot(FrameSlotKind.Illegal, name, null);
        int declarationId = nextLocalDeclarationId++;
        LocalSlot localSlot = new LocalSlot(slotIndex, name);
        scopeStack.peek().variables.put(name, new LocalVariable(slotIndex, attribute, declarationId, localSlot));
        onLocalVariableDeclared(name, attribute, slotIndex, declarationId);
        lastDeclaredLocalSlot = localSlot;
        return slotIndex;
    }

    /**
     * 返回当前函数作用域中某个已存在局部变量的 {@link LocalSlot}（用于循环变量等声明处）。
     */
    protected LocalSlot localSlotOf(TruffleString name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            LocalVariable v = scopeStack.get(i).variables.get(name);
            if (v != null) {
                return v.slot();
            }
        }
        return null;
    }

    protected void onLocalVariableDeclared(TruffleString name, LocalAttribute attribute, int slotIndex, int declarationId) {
    }

    /**
     * 当在当前函数内首次引用到一个 upvalue 时调用。
     * 用于为各函数建立“按首次引用顺序排列”的 upvalue 列表，供 debug.getupvalue 使用。
     * 默认空实现；upvalue 记录由具体解析器决定。
     */
    /**
     * 当在当前函数内首次引用到一个 upvalue 时调用。
     * 用于为各函数建立“按首次引用顺序排列”的 upvalue 列表，供 debug.getupvalue 使用。
     * 返回该 upvalue 在当前函数 upvalue 数组中的下标（-1 表示无注册表）。
     */
    protected int onUpvalueReferenced(TruffleString name, int slotIndex, int depth) {
        return -1;
    }

    protected int declareHiddenSlot(Object identifier) {
        return fdBuilderStack.peek().addSlot(FrameSlotKind.Illegal, identifier, null);
    }

    /**
     * 根据变量名，在所有可见的作用域中查找其槽位索引。
     * (这个方法将取代您的旧 `createRead` 的查找逻辑)
     * @param name 变量名。
     * @return 一个包含索引和深度的对象，如果找不到则返回null。
     */
    protected VariableResolutionResult findVariable(TruffleString name) {
        int functionBoundaryDepth = 0;
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            var scope = scopeStack.get(i);
            LocalVariable variable = scope.variables.get(name);
            if (variable != null) {
                int slotIndex = variable.slotIndex();
                //int depth = scopeStack.size() - 1 - i;
                boolean isUpvalue = (functionBoundaryDepth > 0);
                int resultDepth = isUpvalue ? functionBoundaryDepth : 0;
                int upvalueIndex = -1;
                if (isUpvalue) {
                    // 标记声明处的槽位为“被捕获”，使其在运行时以 cell 方式存储。
                    variable.slot().captured = true;
                    upvalueIndex = onUpvalueReferenced(name, slotIndex, resultDepth);
                }
                return new VariableResolutionResult(slotIndex, resultDepth, isUpvalue, variable.attribute(), variable.declarationId(), variable.slot(), upvalueIndex);
            }
            // 如果我们刚刚检查过的是一个函数边界，增加 upvalue 深度计数
            if (scope.isFunctionBoundary) {
                functionBoundaryDepth++;
            }
        }
        return null; // 在所有局部作用域都找不到
    }

    /**
     * @param depth 0 = local, >0 = upvalue
     */ // 辅助类，用于封装查找结果
    protected record VariableResolutionResult(int slotIndex, int depth, boolean isUpvalue, LocalAttribute attribute, int declarationId, LocalSlot localSlot, int upvalueIndex) {}

    protected enum LocalAttribute {
        NONE,
        CONST,
        CLOSE
    }

    protected record LocalVariable(int slotIndex, LocalAttribute attribute, int declarationId, LocalSlot slot) {}

    /**
     * 一个局部变量声明的可变句柄。运行时节点持有它，以便在解析后期发现该变量被内层函数
     * 捕获时（{@link #captured} 置为 true），切换到 cell 存储语义。解析完成后该标志不再变化。
     */
    public static final class LocalSlot {
        public final int slotIndex;
        public final TruffleString name;
        public boolean captured;

        public LocalSlot(int slotIndex, TruffleString name) {
            this.slotIndex = slotIndex;
            this.name = name;
        }
    }

    // 定义一个 Scope 内部类
    protected static class Scope {
        final Map<TruffleString, LocalVariable> variables = new HashMap<>();
        final boolean isFunctionBoundary; // 标记这是否是一个函数作用域的开始

        Scope(boolean isFunctionBoundary) {
            this.isFunctionBoundary = isFunctionBoundary;
        }
    }
}

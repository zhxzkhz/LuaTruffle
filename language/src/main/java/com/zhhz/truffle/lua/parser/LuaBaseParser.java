package com.zhhz.truffle.lua.parser;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.runtime.LuaContext;
import org.antlr.v4.runtime.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 基本解析器类，处理常见的Lua行为，如错误报告、作用域和文字解析。
 */
public abstract class LuaBaseParser extends LuaParserBaseVisitor<LuaStatementNode> {

    /**
     * Base implementation of parsing, which handles lexer and parser setup, and error reporting.
     */
    protected static void parseLuaImpl(Source source, LuaBaseParser visitor) {
        LuaLexer lexer = new LuaLexer(CharStreams.fromString(source.getCharacters().toString()));
        LuaParser parser = new LuaParser(new CommonTokenStream(lexer));
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        BailoutErrorListener listener = new BailoutErrorListener(source);
        lexer.addErrorListener(listener);
        parser.addErrorListener(listener);

        parser.lualanguage().accept(visitor);
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

    protected LuaBaseParser(LuaLanguage language, Source source) {
        this.language = language;
        this.source = source;
        this.scopeStack = new Stack<>();
        this.fdBuilderStack = new Stack<>();
        sourceString = LuaContext.toTruffleString(source.getCharacters().toString());
    }

    protected LuaBaseParser(LuaLanguage language, Source source,Stack<Scope> scopes,Stack<FrameDescriptor.Builder> stack) {
        this.language = language;
        this.source = source;
        this.scopeStack = scopes;
        this.fdBuilderStack = stack;
        sourceString = LuaContext.toTruffleString(source.getCharacters().toString());
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
            throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
        }
    }

    private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
        int col = charPositionInLine + 1;
        String location = "-- line " + line + " col " + col + ": ";
        int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
        throw new LuaParseError(source, line, col, length,String.format("Error(s) parsing script:%n%s%s", location, message));
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
        int slotIndex = fdBuilderStack.peek().addSlot(FrameSlotKind.Illegal, name, null);
        scopeStack.peek().variables.put(name, slotIndex);
        return slotIndex;
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
            if (scope.variables.containsKey(name)) {
                int slotIndex = scope.variables.get(name);
                //int depth = scopeStack.size() - 1 - i;
                boolean isUpvalue = (functionBoundaryDepth > 0);
                int resultDepth = isUpvalue ? functionBoundaryDepth : 0;
                return new VariableResolutionResult(slotIndex, resultDepth, isUpvalue);
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
    protected record VariableResolutionResult(int slotIndex, int depth, boolean isUpvalue) {}

    // 定义一个 Scope 内部类
    protected static class Scope {
        final Map<TruffleString, Integer> variables = new HashMap<>();
        final boolean isFunctionBoundary; // 标记这是否是一个函数作用域的开始

        Scope(boolean isFunctionBoundary) {
            this.isFunctionBoundary = isFunctionBoundary;
        }
    }
}
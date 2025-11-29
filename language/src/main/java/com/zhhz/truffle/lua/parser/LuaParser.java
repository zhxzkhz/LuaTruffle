// Generated from language/src/main/java/com/zhhz/truffle/lua/parser/LuaParser.g4 by ANTLR 4.13.2
package com.zhhz.truffle.lua.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class LuaParser extends LuaParserBase {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMI=1, EQ=2, BREAK=3, GOTO=4, DO=5, END=6, WHILE=7, REPEAT=8, UNTIL=9, 
		IF=10, THEN=11, ELSEIF=12, ELSE=13, FOR=14, COMMA=15, IN=16, FUNCTION=17, 
		LOCAL=18, LT=19, GT=20, RETURN=21, CONTINUE=22, CC=23, NIL=24, FALSE=25, 
		TRUE=26, DOT=27, SQUIG=28, MINUS=29, POUND=30, OP=31, CP=32, NOT=33, LL=34, 
		GG=35, AMP=36, SS=37, PER=38, COL=39, LE=40, GE=41, AND=42, OR=43, PLUS=44, 
		STAR=45, OCU=46, CCU=47, OB=48, CB=49, EE=50, DD=51, PIPE=52, CARET=53, 
		SLASH=54, DDD=55, SQEQ=56, NAME=57, NORMALSTRING=58, CHARSTRING=59, LONGSTRING=60, 
		INT=61, HEX=62, FLOAT=63, HEX_FLOAT=64, COMMENT=65, WS=66, NL=67, SHEBANG=68;
	public static final int
		RULE_lualanguage = 0, RULE_chunk = 1, RULE_block = 2, RULE_stat = 3, RULE_attnamelist = 4, 
		RULE_attrib = 5, RULE_retstat = 6, RULE_label = 7, RULE_funcname = 8, 
		RULE_varlist = 9, RULE_namelist = 10, RULE_explist = 11, RULE_exp = 12, 
		RULE_atom = 13, RULE_prefixexp = 14, RULE_var = 15, RULE_functioncall = 16, 
		RULE_args = 17, RULE_functiondef = 18, RULE_funcbody = 19, RULE_parlist = 20, 
		RULE_tableconstructor = 21, RULE_fieldlist = 22, RULE_field = 23, RULE_fieldsep = 24, 
		RULE_number = 25, RULE_string = 26;
	private static String[] makeRuleNames() {
		return new String[] {
			"lualanguage", "chunk", "block", "stat", "attnamelist", "attrib", "retstat", 
			"label", "funcname", "varlist", "namelist", "explist", "exp", "atom", 
			"prefixexp", "var", "functioncall", "args", "functiondef", "funcbody", 
			"parlist", "tableconstructor", "fieldlist", "field", "fieldsep", "number", 
			"string"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'='", "'break'", "'goto'", "'do'", "'end'", "'while'", 
			"'repeat'", "'until'", "'if'", "'then'", "'elseif'", "'else'", "'for'", 
			"','", "'in'", "'function'", "'local'", "'<'", "'>'", "'return'", "'continue'", 
			"'::'", "'nil'", "'false'", "'true'", "'.'", "'~'", "'-'", "'#'", "'('", 
			"')'", "'not'", "'<<'", "'>>'", "'&'", "'//'", "'%'", "':'", "'<='", 
			"'>='", "'and'", "'or'", "'+'", "'*'", "'{'", "'}'", "'['", "']'", "'=='", 
			"'..'", "'|'", "'^'", "'/'", "'...'", "'~='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEMI", "EQ", "BREAK", "GOTO", "DO", "END", "WHILE", "REPEAT", 
			"UNTIL", "IF", "THEN", "ELSEIF", "ELSE", "FOR", "COMMA", "IN", "FUNCTION", 
			"LOCAL", "LT", "GT", "RETURN", "CONTINUE", "CC", "NIL", "FALSE", "TRUE", 
			"DOT", "SQUIG", "MINUS", "POUND", "OP", "CP", "NOT", "LL", "GG", "AMP", 
			"SS", "PER", "COL", "LE", "GE", "AND", "OR", "PLUS", "STAR", "OCU", "CCU", 
			"OB", "CB", "EE", "DD", "PIPE", "CARET", "SLASH", "DDD", "SQEQ", "NAME", 
			"NORMALSTRING", "CHARSTRING", "LONGSTRING", "INT", "HEX", "FLOAT", "HEX_FLOAT", 
			"COMMENT", "WS", "NL", "SHEBANG"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "LuaParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LuaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LualanguageContext extends ParserRuleContext {
		public ChunkContext chunk() {
			return getRuleContext(ChunkContext.class,0);
		}
		public TerminalNode EOF() { return getToken(LuaParser.EOF, 0); }
		public LualanguageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lualanguage; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterLualanguage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitLualanguage(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitLualanguage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LualanguageContext lualanguage() throws RecognitionException {
		LualanguageContext _localctx = new LualanguageContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_lualanguage);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			chunk();
			setState(55);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ChunkContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ChunkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_chunk; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterChunk(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitChunk(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitChunk(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ChunkContext chunk() throws RecognitionException {
		ChunkContext _localctx = new ChunkContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_chunk);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public List<StatContext> stat() {
			return getRuleContexts(StatContext.class);
		}
		public StatContext stat(int i) {
			return getRuleContext(StatContext.class,i);
		}
		public RetstatContext retstat() {
			return getRuleContext(RetstatContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 144115190232139194L) != 0)) {
				{
				{
				setState(59);
				stat();
				}
				}
				setState(64);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURN) {
				{
				setState(65);
				retstat();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatContext extends ParserRuleContext {
		public StatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stat; }
	 
		public StatContext() { }
		public void copyFrom(StatContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatLocalVarAssignContext extends StatContext {
		public TerminalNode LOCAL() { return getToken(LuaParser.LOCAL, 0); }
		public AttnamelistContext attnamelist() {
			return getRuleContext(AttnamelistContext.class,0);
		}
		public TerminalNode EQ() { return getToken(LuaParser.EQ, 0); }
		public ExplistContext explist() {
			return getRuleContext(ExplistContext.class,0);
		}
		public StatLocalVarAssignContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatLocalVarAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatLocalVarAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatLocalVarAssign(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatLabelContext extends StatContext {
		public LabelContext label() {
			return getRuleContext(LabelContext.class,0);
		}
		public StatLabelContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatIfContext extends StatContext {
		public TerminalNode IF() { return getToken(LuaParser.IF, 0); }
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(LuaParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(LuaParser.THEN, i);
		}
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(LuaParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(LuaParser.ELSEIF, i);
		}
		public TerminalNode ELSE() { return getToken(LuaParser.ELSE, 0); }
		public StatIfContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatIf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatIf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatIf(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatForNumericContext extends StatContext {
		public TerminalNode FOR() { return getToken(LuaParser.FOR, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public TerminalNode EQ() { return getToken(LuaParser.EQ, 0); }
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(LuaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(LuaParser.COMMA, i);
		}
		public TerminalNode DO() { return getToken(LuaParser.DO, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public StatForNumericContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatForNumeric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatForNumeric(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatForNumeric(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatFunctionCallContext extends StatContext {
		public FunctioncallContext functioncall() {
			return getRuleContext(FunctioncallContext.class,0);
		}
		public StatFunctionCallContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatGotoContext extends StatContext {
		public TerminalNode GOTO() { return getToken(LuaParser.GOTO, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public StatGotoContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatGoto(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatGoto(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatGoto(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatRepeatContext extends StatContext {
		public TerminalNode REPEAT() { return getToken(LuaParser.REPEAT, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode UNTIL() { return getToken(LuaParser.UNTIL, 0); }
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public StatRepeatContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatRepeat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatRepeat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatRepeat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatLocalFunctionDefContext extends StatContext {
		public TerminalNode LOCAL() { return getToken(LuaParser.LOCAL, 0); }
		public TerminalNode FUNCTION() { return getToken(LuaParser.FUNCTION, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public FuncbodyContext funcbody() {
			return getRuleContext(FuncbodyContext.class,0);
		}
		public StatLocalFunctionDefContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatLocalFunctionDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatLocalFunctionDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatLocalFunctionDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatDoContext extends StatContext {
		public TerminalNode DO() { return getToken(LuaParser.DO, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public StatDoContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatDo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatDo(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatDo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatAssignContext extends StatContext {
		public VarlistContext varlist() {
			return getRuleContext(VarlistContext.class,0);
		}
		public TerminalNode EQ() { return getToken(LuaParser.EQ, 0); }
		public ExplistContext explist() {
			return getRuleContext(ExplistContext.class,0);
		}
		public StatAssignContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatAssign(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatForGenericContext extends StatContext {
		public TerminalNode FOR() { return getToken(LuaParser.FOR, 0); }
		public NamelistContext namelist() {
			return getRuleContext(NamelistContext.class,0);
		}
		public TerminalNode IN() { return getToken(LuaParser.IN, 0); }
		public ExplistContext explist() {
			return getRuleContext(ExplistContext.class,0);
		}
		public TerminalNode DO() { return getToken(LuaParser.DO, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public StatForGenericContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatForGeneric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatForGeneric(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatForGeneric(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatFunctionDefContext extends StatContext {
		public TerminalNode FUNCTION() { return getToken(LuaParser.FUNCTION, 0); }
		public FuncnameContext funcname() {
			return getRuleContext(FuncnameContext.class,0);
		}
		public FuncbodyContext funcbody() {
			return getRuleContext(FuncbodyContext.class,0);
		}
		public StatFunctionDefContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatFunctionDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatFunctionDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatFunctionDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatBreakContext extends StatContext {
		public TerminalNode BREAK() { return getToken(LuaParser.BREAK, 0); }
		public StatBreakContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatBreak(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatBreak(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatBreak(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatWhileContext extends StatContext {
		public TerminalNode WHILE() { return getToken(LuaParser.WHILE, 0); }
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public TerminalNode DO() { return getToken(LuaParser.DO, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public StatWhileContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatWhile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatWhile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatWhile(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatEmptyContext extends StatContext {
		public TerminalNode SEMI() { return getToken(LuaParser.SEMI, 0); }
		public StatEmptyContext(StatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStatEmpty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStatEmpty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStatEmpty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatContext stat() throws RecognitionException {
		StatContext _localctx = new StatContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_stat);
		int _la;
		try {
			setState(149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				_localctx = new StatEmptyContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(68);
				match(SEMI);
				}
				break;
			case 2:
				_localctx = new StatAssignContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(69);
				varlist();
				setState(70);
				match(EQ);
				setState(71);
				explist();
				}
				break;
			case 3:
				_localctx = new StatFunctionCallContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(73);
				functioncall();
				}
				break;
			case 4:
				_localctx = new StatLabelContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(74);
				label();
				}
				break;
			case 5:
				_localctx = new StatBreakContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(75);
				match(BREAK);
				}
				break;
			case 6:
				_localctx = new StatGotoContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(76);
				match(GOTO);
				setState(77);
				match(NAME);
				}
				break;
			case 7:
				_localctx = new StatDoContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(78);
				match(DO);
				setState(79);
				block();
				setState(80);
				match(END);
				}
				break;
			case 8:
				_localctx = new StatWhileContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(82);
				match(WHILE);
				setState(83);
				exp(0);
				setState(84);
				match(DO);
				setState(85);
				block();
				setState(86);
				match(END);
				}
				break;
			case 9:
				_localctx = new StatRepeatContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(88);
				match(REPEAT);
				setState(89);
				block();
				setState(90);
				match(UNTIL);
				setState(91);
				exp(0);
				}
				break;
			case 10:
				_localctx = new StatIfContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(93);
				match(IF);
				setState(94);
				exp(0);
				setState(95);
				match(THEN);
				setState(96);
				block();
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(97);
					match(ELSEIF);
					setState(98);
					exp(0);
					setState(99);
					match(THEN);
					setState(100);
					block();
					}
					}
					setState(106);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(109);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(107);
					match(ELSE);
					setState(108);
					block();
					}
				}

				setState(111);
				match(END);
				}
				break;
			case 11:
				_localctx = new StatForNumericContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(113);
				match(FOR);
				setState(114);
				match(NAME);
				setState(115);
				match(EQ);
				setState(116);
				exp(0);
				setState(117);
				match(COMMA);
				setState(118);
				exp(0);
				setState(121);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(119);
					match(COMMA);
					setState(120);
					exp(0);
					}
				}

				setState(123);
				match(DO);
				setState(124);
				block();
				setState(125);
				match(END);
				}
				break;
			case 12:
				_localctx = new StatForGenericContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(127);
				match(FOR);
				setState(128);
				namelist();
				setState(129);
				match(IN);
				setState(130);
				explist();
				setState(131);
				match(DO);
				setState(132);
				block();
				setState(133);
				match(END);
				}
				break;
			case 13:
				_localctx = new StatFunctionDefContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(135);
				match(FUNCTION);
				setState(136);
				funcname();
				setState(137);
				funcbody();
				}
				break;
			case 14:
				_localctx = new StatLocalFunctionDefContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(139);
				match(LOCAL);
				setState(140);
				match(FUNCTION);
				setState(141);
				match(NAME);
				setState(142);
				funcbody();
				}
				break;
			case 15:
				_localctx = new StatLocalVarAssignContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(143);
				match(LOCAL);
				setState(144);
				attnamelist();
				setState(147);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(145);
					match(EQ);
					setState(146);
					explist();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttnamelistContext extends ParserRuleContext {
		public List<TerminalNode> NAME() { return getTokens(LuaParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(LuaParser.NAME, i);
		}
		public List<AttribContext> attrib() {
			return getRuleContexts(AttribContext.class);
		}
		public AttribContext attrib(int i) {
			return getRuleContext(AttribContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(LuaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(LuaParser.COMMA, i);
		}
		public AttnamelistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attnamelist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterAttnamelist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitAttnamelist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitAttnamelist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttnamelistContext attnamelist() throws RecognitionException {
		AttnamelistContext _localctx = new AttnamelistContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_attnamelist);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(NAME);
			setState(152);
			attrib();
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(153);
				match(COMMA);
				setState(154);
				match(NAME);
				setState(155);
				attrib();
				}
				}
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttribContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(LuaParser.LT, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public TerminalNode GT() { return getToken(LuaParser.GT, 0); }
		public AttribContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attrib; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterAttrib(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitAttrib(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitAttrib(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttribContext attrib() throws RecognitionException {
		AttribContext _localctx = new AttribContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_attrib);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(161);
				match(LT);
				setState(162);
				match(NAME);
				setState(163);
				match(GT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RetstatContext extends ParserRuleContext {
		public TerminalNode RETURN() { return getToken(LuaParser.RETURN, 0); }
		public ExplistContext explist() {
			return getRuleContext(ExplistContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(LuaParser.SEMI, 0); }
		public RetstatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_retstat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterRetstat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitRetstat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitRetstat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RetstatContext retstat() throws RecognitionException {
		RetstatContext _localctx = new RetstatContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_retstat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			match(RETURN);
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 280650879957889L) != 0)) {
				{
				setState(167);
				explist();
				}
			}

			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(170);
				match(SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LabelContext extends ParserRuleContext {
		public List<TerminalNode> CC() { return getTokens(LuaParser.CC); }
		public TerminalNode CC(int i) {
			return getToken(LuaParser.CC, i);
		}
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(CC);
			setState(174);
			match(NAME);
			setState(175);
			match(CC);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuncnameContext extends ParserRuleContext {
		public List<TerminalNode> NAME() { return getTokens(LuaParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(LuaParser.NAME, i);
		}
		public List<TerminalNode> DOT() { return getTokens(LuaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(LuaParser.DOT, i);
		}
		public TerminalNode COL() { return getToken(LuaParser.COL, 0); }
		public FuncnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFuncname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFuncname(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFuncname(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncnameContext funcname() throws RecognitionException {
		FuncnameContext _localctx = new FuncnameContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_funcname);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			match(NAME);
			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(178);
				match(DOT);
				setState(179);
				match(NAME);
				}
				}
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COL) {
				{
				setState(185);
				match(COL);
				setState(186);
				match(NAME);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VarlistContext extends ParserRuleContext {
		public List<VarContext> var() {
			return getRuleContexts(VarContext.class);
		}
		public VarContext var(int i) {
			return getRuleContext(VarContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(LuaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(LuaParser.COMMA, i);
		}
		public VarlistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varlist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterVarlist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitVarlist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitVarlist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarlistContext varlist() throws RecognitionException {
		VarlistContext _localctx = new VarlistContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_varlist);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			var();
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(190);
				match(COMMA);
				setState(191);
				var();
				}
				}
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NamelistContext extends ParserRuleContext {
		public List<TerminalNode> NAME() { return getTokens(LuaParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(LuaParser.NAME, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(LuaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(LuaParser.COMMA, i);
		}
		public NamelistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namelist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterNamelist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitNamelist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitNamelist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamelistContext namelist() throws RecognitionException {
		NamelistContext _localctx = new NamelistContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_namelist);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(NAME);
			setState(202);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(198);
					match(COMMA);
					setState(199);
					match(NAME);
					}
					} 
				}
				setState(204);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExplistContext extends ParserRuleContext {
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(LuaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(LuaParser.COMMA, i);
		}
		public ExplistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExplist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExplist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExplist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplistContext explist() throws RecognitionException {
		ExplistContext _localctx = new ExplistContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_explist);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			exp(0);
			setState(210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(206);
				match(COMMA);
				setState(207);
				exp(0);
				}
				}
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpContext extends ParserRuleContext {
		public ExpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exp; }
	 
		public ExpContext() { }
		public void copyFrom(ExpContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpBitwiseAndContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode AMP() { return getToken(LuaParser.AMP, 0); }
		public ExpBitwiseAndContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpBitwiseAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpBitwiseAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpBitwiseAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpBitwiseOrContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode PIPE() { return getToken(LuaParser.PIPE, 0); }
		public ExpBitwiseOrContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpBitwiseOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpBitwiseOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpBitwiseOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpBitwiseXorContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode SQUIG() { return getToken(LuaParser.SQUIG, 0); }
		public ExpBitwiseXorContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpBitwiseXor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpBitwiseXor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpBitwiseXor(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpMulDivModContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode STAR() { return getToken(LuaParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(LuaParser.SLASH, 0); }
		public TerminalNode SS() { return getToken(LuaParser.SS, 0); }
		public TerminalNode PER() { return getToken(LuaParser.PER, 0); }
		public ExpMulDivModContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpMulDivMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpMulDivMod(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpMulDivMod(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpComparisonContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode LT() { return getToken(LuaParser.LT, 0); }
		public TerminalNode GT() { return getToken(LuaParser.GT, 0); }
		public TerminalNode LE() { return getToken(LuaParser.LE, 0); }
		public TerminalNode GE() { return getToken(LuaParser.GE, 0); }
		public TerminalNode SQEQ() { return getToken(LuaParser.SQEQ, 0); }
		public TerminalNode EE() { return getToken(LuaParser.EE, 0); }
		public ExpComparisonContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpConcatContext extends ExpContext {
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode DD() { return getToken(LuaParser.DD, 0); }
		public ExpConcatContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpConcat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpConcat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpConcat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpAddSubContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(LuaParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(LuaParser.MINUS, 0); }
		public ExpAddSubContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpAddSub(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpAddSub(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpAddSub(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpBitwiseShiftContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode LL() { return getToken(LuaParser.LL, 0); }
		public TerminalNode GG() { return getToken(LuaParser.GG, 0); }
		public ExpBitwiseShiftContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpBitwiseShift(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpBitwiseShift(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpBitwiseShift(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpUnopContext extends ExpContext {
		public Token op;
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(LuaParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(LuaParser.NOT, 0); }
		public TerminalNode POUND() { return getToken(LuaParser.POUND, 0); }
		public TerminalNode SQUIG() { return getToken(LuaParser.SQUIG, 0); }
		public ExpUnopContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpUnop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpUnop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpUnop(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpAtomContext extends ExpContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public ExpAtomContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpLogicalAndContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode AND() { return getToken(LuaParser.AND, 0); }
		public ExpLogicalAndContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpLogicalAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpLogicalAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpLogicalAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpLogicalOrContext extends ExpContext {
		public Token op;
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode OR() { return getToken(LuaParser.OR, 0); }
		public ExpLogicalOrContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpLogicalOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpLogicalOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpLogicalOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpPowerContext extends ExpContext {
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode CARET() { return getToken(LuaParser.CARET, 0); }
		public ExpPowerContext(ExpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpPower(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpPower(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpPower(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpContext exp() throws RecognitionException {
		return exp(0);
	}

	private ExpContext exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpContext _localctx = new ExpContext(_ctx, _parentState);
		ExpContext _prevctx = _localctx;
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_exp, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQUIG:
			case MINUS:
			case POUND:
			case NOT:
				{
				_localctx = new ExpUnopContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(214);
				((ExpUnopContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 10468982784L) != 0)) ) {
					((ExpUnopContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(215);
				exp(12);
				}
				break;
			case FUNCTION:
			case NIL:
			case FALSE:
			case TRUE:
			case OP:
			case OCU:
			case DDD:
			case NAME:
			case NORMALSTRING:
			case CHARSTRING:
			case LONGSTRING:
			case INT:
			case HEX:
			case FLOAT:
			case HEX_FLOAT:
				{
				_localctx = new ExpAtomContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(216);
				atom();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(254);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(252);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						_localctx = new ExpPowerContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(219);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(220);
						match(CARET);
						setState(221);
						exp(13);
						}
						break;
					case 2:
						{
						_localctx = new ExpMulDivModContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(222);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(223);
						((ExpMulDivModContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 18049995198431232L) != 0)) ) {
							((ExpMulDivModContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(224);
						exp(12);
						}
						break;
					case 3:
						{
						_localctx = new ExpAddSubContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(225);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(226);
						((ExpAddSubContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==MINUS || _la==PLUS) ) {
							((ExpAddSubContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(227);
						exp(11);
						}
						break;
					case 4:
						{
						_localctx = new ExpConcatContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(228);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(229);
						match(DD);
						setState(230);
						exp(9);
						}
						break;
					case 5:
						{
						_localctx = new ExpBitwiseShiftContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(231);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(232);
						((ExpBitwiseShiftContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==LL || _la==GG) ) {
							((ExpBitwiseShiftContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(233);
						exp(9);
						}
						break;
					case 6:
						{
						_localctx = new ExpBitwiseAndContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(234);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(235);
						((ExpBitwiseAndContext)_localctx).op = match(AMP);
						setState(236);
						exp(8);
						}
						break;
					case 7:
						{
						_localctx = new ExpBitwiseXorContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(237);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(238);
						((ExpBitwiseXorContext)_localctx).op = match(SQUIG);
						setState(239);
						exp(7);
						}
						break;
					case 8:
						{
						_localctx = new ExpBitwiseOrContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(240);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(241);
						((ExpBitwiseOrContext)_localctx).op = match(PIPE);
						setState(242);
						exp(6);
						}
						break;
					case 9:
						{
						_localctx = new ExpComparisonContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(243);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(244);
						((ExpComparisonContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 73186792481226752L) != 0)) ) {
							((ExpComparisonContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(245);
						exp(5);
						}
						break;
					case 10:
						{
						_localctx = new ExpLogicalAndContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(246);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(247);
						((ExpLogicalAndContext)_localctx).op = match(AND);
						setState(248);
						exp(4);
						}
						break;
					case 11:
						{
						_localctx = new ExpLogicalOrContext(new ExpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_exp);
						setState(249);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(250);
						((ExpLogicalOrContext)_localctx).op = match(OR);
						setState(251);
						exp(3);
						}
						break;
					}
					} 
				}
				setState(256);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomContext extends ParserRuleContext {
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
	 
		public AtomContext() { }
		public void copyFrom(AtomContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpNumberContext extends AtomContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public ExpNumberContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpNumber(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpFalseContext extends AtomContext {
		public TerminalNode FALSE() { return getToken(LuaParser.FALSE, 0); }
		public ExpFalseContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpFalse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpFalse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpFalse(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpPrefixExpAtomContext extends AtomContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public ExpPrefixExpAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpPrefixExpAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpPrefixExpAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpPrefixExpAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpNilContext extends AtomContext {
		public TerminalNode NIL() { return getToken(LuaParser.NIL, 0); }
		public ExpNilContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpNil(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpNil(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpNil(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpVarargContext extends AtomContext {
		public TerminalNode DDD() { return getToken(LuaParser.DDD, 0); }
		public ExpVarargContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpVararg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpVararg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpVararg(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpTableConstructorContext extends AtomContext {
		public TableconstructorContext tableconstructor() {
			return getRuleContext(TableconstructorContext.class,0);
		}
		public ExpTableConstructorContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpTableConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpTableConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpTableConstructor(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpStringContext extends AtomContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public ExpStringContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpString(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpTrueContext extends AtomContext {
		public TerminalNode TRUE() { return getToken(LuaParser.TRUE, 0); }
		public ExpTrueContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpTrue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpTrue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpTrue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpFunctionDefContext extends AtomContext {
		public FunctiondefContext functiondef() {
			return getRuleContext(FunctiondefContext.class,0);
		}
		public ExpFunctionDefContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterExpFunctionDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitExpFunctionDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitExpFunctionDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_atom);
		try {
			setState(266);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NIL:
				_localctx = new ExpNilContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(257);
				match(NIL);
				}
				break;
			case FALSE:
				_localctx = new ExpFalseContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(258);
				match(FALSE);
				}
				break;
			case TRUE:
				_localctx = new ExpTrueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(259);
				match(TRUE);
				}
				break;
			case INT:
			case HEX:
			case FLOAT:
			case HEX_FLOAT:
				_localctx = new ExpNumberContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(260);
				number();
				}
				break;
			case NORMALSTRING:
			case CHARSTRING:
			case LONGSTRING:
				_localctx = new ExpStringContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(261);
				string();
				}
				break;
			case DDD:
				_localctx = new ExpVarargContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(262);
				match(DDD);
				}
				break;
			case FUNCTION:
				_localctx = new ExpFunctionDefContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(263);
				functiondef();
				}
				break;
			case OCU:
				_localctx = new ExpTableConstructorContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(264);
				tableconstructor();
				}
				break;
			case OP:
			case NAME:
				_localctx = new ExpPrefixExpAtomContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(265);
				prefixexp(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrefixexpContext extends ParserRuleContext {
		public PrefixexpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixexp; }
	 
		public PrefixexpContext() { }
		public void copyFrom(PrefixexpContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncCallContext extends PrefixexpContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public FuncCallContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrefixParenContext extends PrefixexpContext {
		public TerminalNode OP() { return getToken(LuaParser.OP, 0); }
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public TerminalNode CP() { return getToken(LuaParser.CP, 0); }
		public PrefixParenContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterPrefixParen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitPrefixParen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitPrefixParen(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarIndexedContext extends PrefixexpContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public TerminalNode OB() { return getToken(LuaParser.OB, 0); }
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public TerminalNode CB() { return getToken(LuaParser.CB, 0); }
		public VarIndexedContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterVarIndexed(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitVarIndexed(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitVarIndexed(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarMemberAccessContext extends PrefixexpContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public TerminalNode DOT() { return getToken(LuaParser.DOT, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public VarMemberAccessContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterVarMemberAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitVarMemberAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitVarMemberAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarNameContext extends PrefixexpContext {
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public VarNameContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterVarName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitVarName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitVarName(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallContext extends PrefixexpContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public TerminalNode COL() { return getToken(LuaParser.COL, 0); }
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public MethodCallContext(PrefixexpContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixexpContext prefixexp() throws RecognitionException {
		return prefixexp(0);
	}

	private PrefixexpContext prefixexp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PrefixexpContext _localctx = new PrefixexpContext(_ctx, _parentState);
		PrefixexpContext _prevctx = _localctx;
		int _startState = 28;
		enterRecursionRule(_localctx, 28, RULE_prefixexp, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
				{
				_localctx = new VarNameContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(269);
				match(NAME);
				}
				break;
			case OP:
				{
				_localctx = new PrefixParenContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(270);
				match(OP);
				setState(271);
				exp(0);
				setState(272);
				match(CP);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(292);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(290);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
					case 1:
						{
						_localctx = new VarIndexedContext(new PrefixexpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_prefixexp);
						setState(276);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(277);
						match(OB);
						setState(278);
						exp(0);
						setState(279);
						match(CB);
						}
						break;
					case 2:
						{
						_localctx = new VarMemberAccessContext(new PrefixexpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_prefixexp);
						setState(281);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(282);
						match(DOT);
						setState(283);
						match(NAME);
						}
						break;
					case 3:
						{
						_localctx = new FuncCallContext(new PrefixexpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_prefixexp);
						setState(284);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(285);
						args();
						}
						break;
					case 4:
						{
						_localctx = new MethodCallContext(new PrefixexpContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_prefixexp);
						setState(286);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(287);
						match(COL);
						setState(288);
						match(NAME);
						setState(289);
						args();
						}
						break;
					}
					} 
				}
				setState(294);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VarContext extends ParserRuleContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public VarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarContext var() throws RecognitionException {
		VarContext _localctx = new VarContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			prefixexp(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctioncallContext extends ParserRuleContext {
		public PrefixexpContext prefixexp() {
			return getRuleContext(PrefixexpContext.class,0);
		}
		public FunctioncallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functioncall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFunctioncall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFunctioncall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFunctioncall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctioncallContext functioncall() throws RecognitionException {
		FunctioncallContext _localctx = new FunctioncallContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_functioncall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			prefixexp(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgsContext extends ParserRuleContext {
		public ArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_args; }
	 
		public ArgsContext() { }
		public void copyFrom(ArgsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArgsStringContext extends ArgsContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public ArgsStringContext(ArgsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterArgsString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitArgsString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitArgsString(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArgsTableContext extends ArgsContext {
		public TableconstructorContext tableconstructor() {
			return getRuleContext(TableconstructorContext.class,0);
		}
		public ArgsTableContext(ArgsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterArgsTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitArgsTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitArgsTable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArgsParenContext extends ArgsContext {
		public TerminalNode OP() { return getToken(LuaParser.OP, 0); }
		public TerminalNode CP() { return getToken(LuaParser.CP, 0); }
		public ExplistContext explist() {
			return getRuleContext(ExplistContext.class,0);
		}
		public ArgsParenContext(ArgsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterArgsParen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitArgsParen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitArgsParen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgsContext args() throws RecognitionException {
		ArgsContext _localctx = new ArgsContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_args);
		int _la;
		try {
			setState(306);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OP:
				_localctx = new ArgsParenContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(299);
				match(OP);
				setState(301);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 280650879957889L) != 0)) {
					{
					setState(300);
					explist();
					}
				}

				setState(303);
				match(CP);
				}
				break;
			case OCU:
				_localctx = new ArgsTableContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(304);
				tableconstructor();
				}
				break;
			case NORMALSTRING:
			case CHARSTRING:
			case LONGSTRING:
				_localctx = new ArgsStringContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(305);
				string();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctiondefContext extends ParserRuleContext {
		public TerminalNode FUNCTION() { return getToken(LuaParser.FUNCTION, 0); }
		public FuncbodyContext funcbody() {
			return getRuleContext(FuncbodyContext.class,0);
		}
		public FunctiondefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functiondef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFunctiondef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFunctiondef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFunctiondef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctiondefContext functiondef() throws RecognitionException {
		FunctiondefContext _localctx = new FunctiondefContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_functiondef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			match(FUNCTION);
			setState(309);
			funcbody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuncbodyContext extends ParserRuleContext {
		public TerminalNode OP() { return getToken(LuaParser.OP, 0); }
		public ParlistContext parlist() {
			return getRuleContext(ParlistContext.class,0);
		}
		public TerminalNode CP() { return getToken(LuaParser.CP, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode END() { return getToken(LuaParser.END, 0); }
		public FuncbodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcbody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFuncbody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFuncbody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFuncbody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncbodyContext funcbody() throws RecognitionException {
		FuncbodyContext _localctx = new FuncbodyContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_funcbody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			match(OP);
			setState(312);
			parlist();
			setState(313);
			match(CP);
			setState(314);
			block();
			setState(315);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParlistContext extends ParserRuleContext {
		public ParlistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parlist; }
	 
		public ParlistContext() { }
		public void copyFrom(ParlistContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParlistVarargContext extends ParlistContext {
		public TerminalNode DDD() { return getToken(LuaParser.DDD, 0); }
		public ParlistVarargContext(ParlistContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterParlistVararg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitParlistVararg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitParlistVararg(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParlistNamelistContext extends ParlistContext {
		public NamelistContext namelist() {
			return getRuleContext(NamelistContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(LuaParser.COMMA, 0); }
		public TerminalNode DDD() { return getToken(LuaParser.DDD, 0); }
		public ParlistNamelistContext(ParlistContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterParlistNamelist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitParlistNamelist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitParlistNamelist(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParlistEmptyContext extends ParlistContext {
		public ParlistEmptyContext(ParlistContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterParlistEmpty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitParlistEmpty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitParlistEmpty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParlistContext parlist() throws RecognitionException {
		ParlistContext _localctx = new ParlistContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_parlist);
		int _la;
		try {
			setState(324);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
				_localctx = new ParlistNamelistContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(317);
				namelist();
				setState(320);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(318);
					match(COMMA);
					setState(319);
					match(DDD);
					}
				}

				}
				break;
			case DDD:
				_localctx = new ParlistVarargContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(322);
				match(DDD);
				}
				break;
			case CP:
				_localctx = new ParlistEmptyContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableconstructorContext extends ParserRuleContext {
		public TerminalNode OCU() { return getToken(LuaParser.OCU, 0); }
		public TerminalNode CCU() { return getToken(LuaParser.CCU, 0); }
		public FieldlistContext fieldlist() {
			return getRuleContext(FieldlistContext.class,0);
		}
		public TableconstructorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableconstructor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterTableconstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitTableconstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitTableconstructor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableconstructorContext tableconstructor() throws RecognitionException {
		TableconstructorContext _localctx = new TableconstructorContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_tableconstructor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			match(OCU);
			setState(328);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 280653027441537L) != 0)) {
				{
				setState(327);
				fieldlist();
				}
			}

			setState(330);
			match(CCU);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldlistContext extends ParserRuleContext {
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public List<FieldsepContext> fieldsep() {
			return getRuleContexts(FieldsepContext.class);
		}
		public FieldsepContext fieldsep(int i) {
			return getRuleContext(FieldsepContext.class,i);
		}
		public FieldlistContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldlist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFieldlist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFieldlist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFieldlist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldlistContext fieldlist() throws RecognitionException {
		FieldlistContext _localctx = new FieldlistContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_fieldlist);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			field();
			setState(338);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(333);
					fieldsep();
					setState(334);
					field();
					}
					} 
				}
				setState(340);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			setState(342);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==COMMA) {
				{
				setState(341);
				fieldsep();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldContext extends ParserRuleContext {
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
	 
		public FieldContext() { }
		public void copyFrom(FieldContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldKeyNameContext extends FieldContext {
		public TerminalNode NAME() { return getToken(LuaParser.NAME, 0); }
		public TerminalNode EQ() { return getToken(LuaParser.EQ, 0); }
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public FieldKeyNameContext(FieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFieldKeyName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFieldKeyName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFieldKeyName(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldValueContext extends FieldContext {
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public FieldValueContext(FieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFieldValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFieldValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFieldValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldKeyExprContext extends FieldContext {
		public TerminalNode OB() { return getToken(LuaParser.OB, 0); }
		public List<ExpContext> exp() {
			return getRuleContexts(ExpContext.class);
		}
		public ExpContext exp(int i) {
			return getRuleContext(ExpContext.class,i);
		}
		public TerminalNode CB() { return getToken(LuaParser.CB, 0); }
		public TerminalNode EQ() { return getToken(LuaParser.EQ, 0); }
		public FieldKeyExprContext(FieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFieldKeyExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFieldKeyExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFieldKeyExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_field);
		try {
			setState(354);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new FieldKeyExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(344);
				match(OB);
				setState(345);
				exp(0);
				setState(346);
				match(CB);
				setState(347);
				match(EQ);
				setState(348);
				exp(0);
				}
				break;
			case 2:
				_localctx = new FieldKeyNameContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(350);
				match(NAME);
				setState(351);
				match(EQ);
				setState(352);
				exp(0);
				}
				break;
			case 3:
				_localctx = new FieldValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(353);
				exp(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldsepContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(LuaParser.COMMA, 0); }
		public TerminalNode SEMI() { return getToken(LuaParser.SEMI, 0); }
		public FieldsepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldsep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterFieldsep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitFieldsep(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitFieldsep(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldsepContext fieldsep() throws RecognitionException {
		FieldsepContext _localctx = new FieldsepContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_fieldsep);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(356);
			_la = _input.LA(1);
			if ( !(_la==SEMI || _la==COMMA) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumberContext extends ParserRuleContext {
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
	 
		public NumberContext() { }
		public void copyFrom(NumberContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberFloatContext extends NumberContext {
		public TerminalNode FLOAT() { return getToken(LuaParser.FLOAT, 0); }
		public NumberFloatContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterNumberFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitNumberFloat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitNumberFloat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberHexContext extends NumberContext {
		public TerminalNode HEX() { return getToken(LuaParser.HEX, 0); }
		public NumberHexContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterNumberHex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitNumberHex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitNumberHex(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberIntContext extends NumberContext {
		public TerminalNode INT() { return getToken(LuaParser.INT, 0); }
		public NumberIntContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterNumberInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitNumberInt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitNumberInt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberHexFloatContext extends NumberContext {
		public TerminalNode HEX_FLOAT() { return getToken(LuaParser.HEX_FLOAT, 0); }
		public NumberHexFloatContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterNumberHexFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitNumberHexFloat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitNumberHexFloat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_number);
		try {
			setState(362);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT:
				_localctx = new NumberIntContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(358);
				match(INT);
				}
				break;
			case HEX:
				_localctx = new NumberHexContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(359);
				match(HEX);
				}
				break;
			case FLOAT:
				_localctx = new NumberFloatContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(360);
				match(FLOAT);
				}
				break;
			case HEX_FLOAT:
				_localctx = new NumberHexFloatContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(361);
				match(HEX_FLOAT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringContext extends ParserRuleContext {
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
	 
		public StringContext() { }
		public void copyFrom(StringContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringCharContext extends StringContext {
		public TerminalNode CHARSTRING() { return getToken(LuaParser.CHARSTRING, 0); }
		public StringCharContext(StringContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStringChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStringChar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStringChar(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringNormalContext extends StringContext {
		public TerminalNode NORMALSTRING() { return getToken(LuaParser.NORMALSTRING, 0); }
		public StringNormalContext(StringContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStringNormal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStringNormal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStringNormal(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringLongContext extends StringContext {
		public TerminalNode LONGSTRING() { return getToken(LuaParser.LONGSTRING, 0); }
		public StringLongContext(StringContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).enterStringLong(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LuaParserListener ) ((LuaParserListener)listener).exitStringLong(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LuaParserVisitor ) return ((LuaParserVisitor<? extends T>)visitor).visitStringLong(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_string);
		try {
			setState(367);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NORMALSTRING:
				_localctx = new StringNormalContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(364);
				match(NORMALSTRING);
				}
				break;
			case CHARSTRING:
				_localctx = new StringCharContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(365);
				match(CHARSTRING);
				}
				break;
			case LONGSTRING:
				_localctx = new StringLongContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(366);
				match(LONGSTRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 12:
			return exp_sempred((ExpContext)_localctx, predIndex);
		case 14:
			return prefixexp_sempred((PrefixexpContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean exp_sempred(ExpContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 13);
		case 1:
			return precpred(_ctx, 11);
		case 2:
			return precpred(_ctx, 10);
		case 3:
			return precpred(_ctx, 9);
		case 4:
			return precpred(_ctx, 8);
		case 5:
			return precpred(_ctx, 7);
		case 6:
			return precpred(_ctx, 6);
		case 7:
			return precpred(_ctx, 5);
		case 8:
			return precpred(_ctx, 4);
		case 9:
			return precpred(_ctx, 3);
		case 10:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean prefixexp_sempred(PrefixexpContext _localctx, int predIndex) {
		switch (predIndex) {
		case 11:
			return precpred(_ctx, 4);
		case 12:
			return precpred(_ctx, 3);
		case 13:
			return precpred(_ctx, 2);
		case 14:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001D\u0172\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0005\u0002=\b\u0002"+
		"\n\u0002\f\u0002@\t\u0002\u0001\u0002\u0003\u0002C\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003g\b\u0003\n\u0003\f\u0003"+
		"j\t\u0003\u0001\u0003\u0001\u0003\u0003\u0003n\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003z\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0094"+
		"\b\u0003\u0003\u0003\u0096\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0005\u0004\u009d\b\u0004\n\u0004\f\u0004\u00a0"+
		"\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u00a5\b\u0005"+
		"\u0001\u0006\u0001\u0006\u0003\u0006\u00a9\b\u0006\u0001\u0006\u0003\u0006"+
		"\u00ac\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0005\b\u00b5\b\b\n\b\f\b\u00b8\t\b\u0001\b\u0001\b\u0003"+
		"\b\u00bc\b\b\u0001\t\u0001\t\u0001\t\u0005\t\u00c1\b\t\n\t\f\t\u00c4\t"+
		"\t\u0001\n\u0001\n\u0001\n\u0005\n\u00c9\b\n\n\n\f\n\u00cc\t\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000b\u00d1\b\u000b\n\u000b\f\u000b\u00d4"+
		"\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u00da\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00fd\b\f\n\f\f\f\u0100\t\f"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u010b\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0003\u000e\u0113\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005"+
		"\u000e\u0123\b\u000e\n\u000e\f\u000e\u0126\t\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0003\u0011\u012e\b\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0133\b\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0141\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0145\b\u0014\u0001"+
		"\u0015\u0001\u0015\u0003\u0015\u0149\b\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u0151\b\u0016\n"+
		"\u0016\f\u0016\u0154\t\u0016\u0001\u0016\u0003\u0016\u0157\b\u0016\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0163\b\u0017\u0001"+
		"\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003"+
		"\u0019\u016b\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0170"+
		"\b\u001a\u0001\u001a\u0000\u0002\u0018\u001c\u001b\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"024\u0000\u0006\u0002\u0000\u001c\u001e!!\u0003\u0000%&--66\u0002\u0000"+
		"\u001d\u001d,,\u0001\u0000\"#\u0004\u0000\u0013\u0014()2288\u0002\u0000"+
		"\u0001\u0001\u000f\u000f\u019c\u00006\u0001\u0000\u0000\u0000\u00029\u0001"+
		"\u0000\u0000\u0000\u0004>\u0001\u0000\u0000\u0000\u0006\u0095\u0001\u0000"+
		"\u0000\u0000\b\u0097\u0001\u0000\u0000\u0000\n\u00a4\u0001\u0000\u0000"+
		"\u0000\f\u00a6\u0001\u0000\u0000\u0000\u000e\u00ad\u0001\u0000\u0000\u0000"+
		"\u0010\u00b1\u0001\u0000\u0000\u0000\u0012\u00bd\u0001\u0000\u0000\u0000"+
		"\u0014\u00c5\u0001\u0000\u0000\u0000\u0016\u00cd\u0001\u0000\u0000\u0000"+
		"\u0018\u00d9\u0001\u0000\u0000\u0000\u001a\u010a\u0001\u0000\u0000\u0000"+
		"\u001c\u0112\u0001\u0000\u0000\u0000\u001e\u0127\u0001\u0000\u0000\u0000"+
		" \u0129\u0001\u0000\u0000\u0000\"\u0132\u0001\u0000\u0000\u0000$\u0134"+
		"\u0001\u0000\u0000\u0000&\u0137\u0001\u0000\u0000\u0000(\u0144\u0001\u0000"+
		"\u0000\u0000*\u0146\u0001\u0000\u0000\u0000,\u014c\u0001\u0000\u0000\u0000"+
		".\u0162\u0001\u0000\u0000\u00000\u0164\u0001\u0000\u0000\u00002\u016a"+
		"\u0001\u0000\u0000\u00004\u016f\u0001\u0000\u0000\u000067\u0003\u0002"+
		"\u0001\u000078\u0005\u0000\u0000\u00018\u0001\u0001\u0000\u0000\u0000"+
		"9:\u0003\u0004\u0002\u0000:\u0003\u0001\u0000\u0000\u0000;=\u0003\u0006"+
		"\u0003\u0000<;\u0001\u0000\u0000\u0000=@\u0001\u0000\u0000\u0000><\u0001"+
		"\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000?B\u0001\u0000\u0000\u0000"+
		"@>\u0001\u0000\u0000\u0000AC\u0003\f\u0006\u0000BA\u0001\u0000\u0000\u0000"+
		"BC\u0001\u0000\u0000\u0000C\u0005\u0001\u0000\u0000\u0000D\u0096\u0005"+
		"\u0001\u0000\u0000EF\u0003\u0012\t\u0000FG\u0005\u0002\u0000\u0000GH\u0003"+
		"\u0016\u000b\u0000H\u0096\u0001\u0000\u0000\u0000I\u0096\u0003 \u0010"+
		"\u0000J\u0096\u0003\u000e\u0007\u0000K\u0096\u0005\u0003\u0000\u0000L"+
		"M\u0005\u0004\u0000\u0000M\u0096\u00059\u0000\u0000NO\u0005\u0005\u0000"+
		"\u0000OP\u0003\u0004\u0002\u0000PQ\u0005\u0006\u0000\u0000Q\u0096\u0001"+
		"\u0000\u0000\u0000RS\u0005\u0007\u0000\u0000ST\u0003\u0018\f\u0000TU\u0005"+
		"\u0005\u0000\u0000UV\u0003\u0004\u0002\u0000VW\u0005\u0006\u0000\u0000"+
		"W\u0096\u0001\u0000\u0000\u0000XY\u0005\b\u0000\u0000YZ\u0003\u0004\u0002"+
		"\u0000Z[\u0005\t\u0000\u0000[\\\u0003\u0018\f\u0000\\\u0096\u0001\u0000"+
		"\u0000\u0000]^\u0005\n\u0000\u0000^_\u0003\u0018\f\u0000_`\u0005\u000b"+
		"\u0000\u0000`h\u0003\u0004\u0002\u0000ab\u0005\f\u0000\u0000bc\u0003\u0018"+
		"\f\u0000cd\u0005\u000b\u0000\u0000de\u0003\u0004\u0002\u0000eg\u0001\u0000"+
		"\u0000\u0000fa\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000\u0000hf\u0001"+
		"\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000im\u0001\u0000\u0000\u0000"+
		"jh\u0001\u0000\u0000\u0000kl\u0005\r\u0000\u0000ln\u0003\u0004\u0002\u0000"+
		"mk\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000no\u0001\u0000\u0000"+
		"\u0000op\u0005\u0006\u0000\u0000p\u0096\u0001\u0000\u0000\u0000qr\u0005"+
		"\u000e\u0000\u0000rs\u00059\u0000\u0000st\u0005\u0002\u0000\u0000tu\u0003"+
		"\u0018\f\u0000uv\u0005\u000f\u0000\u0000vy\u0003\u0018\f\u0000wx\u0005"+
		"\u000f\u0000\u0000xz\u0003\u0018\f\u0000yw\u0001\u0000\u0000\u0000yz\u0001"+
		"\u0000\u0000\u0000z{\u0001\u0000\u0000\u0000{|\u0005\u0005\u0000\u0000"+
		"|}\u0003\u0004\u0002\u0000}~\u0005\u0006\u0000\u0000~\u0096\u0001\u0000"+
		"\u0000\u0000\u007f\u0080\u0005\u000e\u0000\u0000\u0080\u0081\u0003\u0014"+
		"\n\u0000\u0081\u0082\u0005\u0010\u0000\u0000\u0082\u0083\u0003\u0016\u000b"+
		"\u0000\u0083\u0084\u0005\u0005\u0000\u0000\u0084\u0085\u0003\u0004\u0002"+
		"\u0000\u0085\u0086\u0005\u0006\u0000\u0000\u0086\u0096\u0001\u0000\u0000"+
		"\u0000\u0087\u0088\u0005\u0011\u0000\u0000\u0088\u0089\u0003\u0010\b\u0000"+
		"\u0089\u008a\u0003&\u0013\u0000\u008a\u0096\u0001\u0000\u0000\u0000\u008b"+
		"\u008c\u0005\u0012\u0000\u0000\u008c\u008d\u0005\u0011\u0000\u0000\u008d"+
		"\u008e\u00059\u0000\u0000\u008e\u0096\u0003&\u0013\u0000\u008f\u0090\u0005"+
		"\u0012\u0000\u0000\u0090\u0093\u0003\b\u0004\u0000\u0091\u0092\u0005\u0002"+
		"\u0000\u0000\u0092\u0094\u0003\u0016\u000b\u0000\u0093\u0091\u0001\u0000"+
		"\u0000\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0096\u0001\u0000"+
		"\u0000\u0000\u0095D\u0001\u0000\u0000\u0000\u0095E\u0001\u0000\u0000\u0000"+
		"\u0095I\u0001\u0000\u0000\u0000\u0095J\u0001\u0000\u0000\u0000\u0095K"+
		"\u0001\u0000\u0000\u0000\u0095L\u0001\u0000\u0000\u0000\u0095N\u0001\u0000"+
		"\u0000\u0000\u0095R\u0001\u0000\u0000\u0000\u0095X\u0001\u0000\u0000\u0000"+
		"\u0095]\u0001\u0000\u0000\u0000\u0095q\u0001\u0000\u0000\u0000\u0095\u007f"+
		"\u0001\u0000\u0000\u0000\u0095\u0087\u0001\u0000\u0000\u0000\u0095\u008b"+
		"\u0001\u0000\u0000\u0000\u0095\u008f\u0001\u0000\u0000\u0000\u0096\u0007"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u00059\u0000\u0000\u0098\u009e\u0003"+
		"\n\u0005\u0000\u0099\u009a\u0005\u000f\u0000\u0000\u009a\u009b\u00059"+
		"\u0000\u0000\u009b\u009d\u0003\n\u0005\u0000\u009c\u0099\u0001\u0000\u0000"+
		"\u0000\u009d\u00a0\u0001\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000"+
		"\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\t\u0001\u0000\u0000\u0000"+
		"\u00a0\u009e\u0001\u0000\u0000\u0000\u00a1\u00a2\u0005\u0013\u0000\u0000"+
		"\u00a2\u00a3\u00059\u0000\u0000\u00a3\u00a5\u0005\u0014\u0000\u0000\u00a4"+
		"\u00a1\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5"+
		"\u000b\u0001\u0000\u0000\u0000\u00a6\u00a8\u0005\u0015\u0000\u0000\u00a7"+
		"\u00a9\u0003\u0016\u000b\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a8"+
		"\u00a9\u0001\u0000\u0000\u0000\u00a9\u00ab\u0001\u0000\u0000\u0000\u00aa"+
		"\u00ac\u0005\u0001\u0000\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ab"+
		"\u00ac\u0001\u0000\u0000\u0000\u00ac\r\u0001\u0000\u0000\u0000\u00ad\u00ae"+
		"\u0005\u0017\u0000\u0000\u00ae\u00af\u00059\u0000\u0000\u00af\u00b0\u0005"+
		"\u0017\u0000\u0000\u00b0\u000f\u0001\u0000\u0000\u0000\u00b1\u00b6\u0005"+
		"9\u0000\u0000\u00b2\u00b3\u0005\u001b\u0000\u0000\u00b3\u00b5\u00059\u0000"+
		"\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b5\u00b8\u0001\u0000\u0000"+
		"\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b7\u00bb\u0001\u0000\u0000\u0000\u00b8\u00b6\u0001\u0000\u0000"+
		"\u0000\u00b9\u00ba\u0005\'\u0000\u0000\u00ba\u00bc\u00059\u0000\u0000"+
		"\u00bb\u00b9\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000"+
		"\u00bc\u0011\u0001\u0000\u0000\u0000\u00bd\u00c2\u0003\u001e\u000f\u0000"+
		"\u00be\u00bf\u0005\u000f\u0000\u0000\u00bf\u00c1\u0003\u001e\u000f\u0000"+
		"\u00c0\u00be\u0001\u0000\u0000\u0000\u00c1\u00c4\u0001\u0000\u0000\u0000"+
		"\u00c2\u00c0\u0001\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000"+
		"\u00c3\u0013\u0001\u0000\u0000\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000"+
		"\u00c5\u00ca\u00059\u0000\u0000\u00c6\u00c7\u0005\u000f\u0000\u0000\u00c7"+
		"\u00c9\u00059\u0000\u0000\u00c8\u00c6\u0001\u0000\u0000\u0000\u00c9\u00cc"+
		"\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cb"+
		"\u0001\u0000\u0000\u0000\u00cb\u0015\u0001\u0000\u0000\u0000\u00cc\u00ca"+
		"\u0001\u0000\u0000\u0000\u00cd\u00d2\u0003\u0018\f\u0000\u00ce\u00cf\u0005"+
		"\u000f\u0000\u0000\u00cf\u00d1\u0003\u0018\f\u0000\u00d0\u00ce\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u0017\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5\u00d6\u0006\f\uffff"+
		"\uffff\u0000\u00d6\u00d7\u0007\u0000\u0000\u0000\u00d7\u00da\u0003\u0018"+
		"\f\f\u00d8\u00da\u0003\u001a\r\u0000\u00d9\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d9\u00d8\u0001\u0000\u0000\u0000\u00da\u00fe\u0001\u0000\u0000\u0000"+
		"\u00db\u00dc\n\r\u0000\u0000\u00dc\u00dd\u00055\u0000\u0000\u00dd\u00fd"+
		"\u0003\u0018\f\r\u00de\u00df\n\u000b\u0000\u0000\u00df\u00e0\u0007\u0001"+
		"\u0000\u0000\u00e0\u00fd\u0003\u0018\f\f\u00e1\u00e2\n\n\u0000\u0000\u00e2"+
		"\u00e3\u0007\u0002\u0000\u0000\u00e3\u00fd\u0003\u0018\f\u000b\u00e4\u00e5"+
		"\n\t\u0000\u0000\u00e5\u00e6\u00053\u0000\u0000\u00e6\u00fd\u0003\u0018"+
		"\f\t\u00e7\u00e8\n\b\u0000\u0000\u00e8\u00e9\u0007\u0003\u0000\u0000\u00e9"+
		"\u00fd\u0003\u0018\f\t\u00ea\u00eb\n\u0007\u0000\u0000\u00eb\u00ec\u0005"+
		"$\u0000\u0000\u00ec\u00fd\u0003\u0018\f\b\u00ed\u00ee\n\u0006\u0000\u0000"+
		"\u00ee\u00ef\u0005\u001c\u0000\u0000\u00ef\u00fd\u0003\u0018\f\u0007\u00f0"+
		"\u00f1\n\u0005\u0000\u0000\u00f1\u00f2\u00054\u0000\u0000\u00f2\u00fd"+
		"\u0003\u0018\f\u0006\u00f3\u00f4\n\u0004\u0000\u0000\u00f4\u00f5\u0007"+
		"\u0004\u0000\u0000\u00f5\u00fd\u0003\u0018\f\u0005\u00f6\u00f7\n\u0003"+
		"\u0000\u0000\u00f7\u00f8\u0005*\u0000\u0000\u00f8\u00fd\u0003\u0018\f"+
		"\u0004\u00f9\u00fa\n\u0002\u0000\u0000\u00fa\u00fb\u0005+\u0000\u0000"+
		"\u00fb\u00fd\u0003\u0018\f\u0003\u00fc\u00db\u0001\u0000\u0000\u0000\u00fc"+
		"\u00de\u0001\u0000\u0000\u0000\u00fc\u00e1\u0001\u0000\u0000\u0000\u00fc"+
		"\u00e4\u0001\u0000\u0000\u0000\u00fc\u00e7\u0001\u0000\u0000\u0000\u00fc"+
		"\u00ea\u0001\u0000\u0000\u0000\u00fc\u00ed\u0001\u0000\u0000\u0000\u00fc"+
		"\u00f0\u0001\u0000\u0000\u0000\u00fc\u00f3\u0001\u0000\u0000\u0000\u00fc"+
		"\u00f6\u0001\u0000\u0000\u0000\u00fc\u00f9\u0001\u0000\u0000\u0000\u00fd"+
		"\u0100\u0001\u0000\u0000\u0000\u00fe\u00fc\u0001\u0000\u0000\u0000\u00fe"+
		"\u00ff\u0001\u0000\u0000\u0000\u00ff\u0019\u0001\u0000\u0000\u0000\u0100"+
		"\u00fe\u0001\u0000\u0000\u0000\u0101\u010b\u0005\u0018\u0000\u0000\u0102"+
		"\u010b\u0005\u0019\u0000\u0000\u0103\u010b\u0005\u001a\u0000\u0000\u0104"+
		"\u010b\u00032\u0019\u0000\u0105\u010b\u00034\u001a\u0000\u0106\u010b\u0005"+
		"7\u0000\u0000\u0107\u010b\u0003$\u0012\u0000\u0108\u010b\u0003*\u0015"+
		"\u0000\u0109\u010b\u0003\u001c\u000e\u0000\u010a\u0101\u0001\u0000\u0000"+
		"\u0000\u010a\u0102\u0001\u0000\u0000\u0000\u010a\u0103\u0001\u0000\u0000"+
		"\u0000\u010a\u0104\u0001\u0000\u0000\u0000\u010a\u0105\u0001\u0000\u0000"+
		"\u0000\u010a\u0106\u0001\u0000\u0000\u0000\u010a\u0107\u0001\u0000\u0000"+
		"\u0000\u010a\u0108\u0001\u0000\u0000\u0000\u010a\u0109\u0001\u0000\u0000"+
		"\u0000\u010b\u001b\u0001\u0000\u0000\u0000\u010c\u010d\u0006\u000e\uffff"+
		"\uffff\u0000\u010d\u0113\u00059\u0000\u0000\u010e\u010f\u0005\u001f\u0000"+
		"\u0000\u010f\u0110\u0003\u0018\f\u0000\u0110\u0111\u0005 \u0000\u0000"+
		"\u0111\u0113\u0001\u0000\u0000\u0000\u0112\u010c\u0001\u0000\u0000\u0000"+
		"\u0112\u010e\u0001\u0000\u0000\u0000\u0113\u0124\u0001\u0000\u0000\u0000"+
		"\u0114\u0115\n\u0004\u0000\u0000\u0115\u0116\u00050\u0000\u0000\u0116"+
		"\u0117\u0003\u0018\f\u0000\u0117\u0118\u00051\u0000\u0000\u0118\u0123"+
		"\u0001\u0000\u0000\u0000\u0119\u011a\n\u0003\u0000\u0000\u011a\u011b\u0005"+
		"\u001b\u0000\u0000\u011b\u0123\u00059\u0000\u0000\u011c\u011d\n\u0002"+
		"\u0000\u0000\u011d\u0123\u0003\"\u0011\u0000\u011e\u011f\n\u0001\u0000"+
		"\u0000\u011f\u0120\u0005\'\u0000\u0000\u0120\u0121\u00059\u0000\u0000"+
		"\u0121\u0123\u0003\"\u0011\u0000\u0122\u0114\u0001\u0000\u0000\u0000\u0122"+
		"\u0119\u0001\u0000\u0000\u0000\u0122\u011c\u0001\u0000\u0000\u0000\u0122"+
		"\u011e\u0001\u0000\u0000\u0000\u0123\u0126\u0001\u0000\u0000\u0000\u0124"+
		"\u0122\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000\u0000\u0000\u0125"+
		"\u001d\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0127"+
		"\u0128\u0003\u001c\u000e\u0000\u0128\u001f\u0001\u0000\u0000\u0000\u0129"+
		"\u012a\u0003\u001c\u000e\u0000\u012a!\u0001\u0000\u0000\u0000\u012b\u012d"+
		"\u0005\u001f\u0000\u0000\u012c\u012e\u0003\u0016\u000b\u0000\u012d\u012c"+
		"\u0001\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\u012f"+
		"\u0001\u0000\u0000\u0000\u012f\u0133\u0005 \u0000\u0000\u0130\u0133\u0003"+
		"*\u0015\u0000\u0131\u0133\u00034\u001a\u0000\u0132\u012b\u0001\u0000\u0000"+
		"\u0000\u0132\u0130\u0001\u0000\u0000\u0000\u0132\u0131\u0001\u0000\u0000"+
		"\u0000\u0133#\u0001\u0000\u0000\u0000\u0134\u0135\u0005\u0011\u0000\u0000"+
		"\u0135\u0136\u0003&\u0013\u0000\u0136%\u0001\u0000\u0000\u0000\u0137\u0138"+
		"\u0005\u001f\u0000\u0000\u0138\u0139\u0003(\u0014\u0000\u0139\u013a\u0005"+
		" \u0000\u0000\u013a\u013b\u0003\u0004\u0002\u0000\u013b\u013c\u0005\u0006"+
		"\u0000\u0000\u013c\'\u0001\u0000\u0000\u0000\u013d\u0140\u0003\u0014\n"+
		"\u0000\u013e\u013f\u0005\u000f\u0000\u0000\u013f\u0141\u00057\u0000\u0000"+
		"\u0140\u013e\u0001\u0000\u0000\u0000\u0140\u0141\u0001\u0000\u0000\u0000"+
		"\u0141\u0145\u0001\u0000\u0000\u0000\u0142\u0145\u00057\u0000\u0000\u0143"+
		"\u0145\u0001\u0000\u0000\u0000\u0144\u013d\u0001\u0000\u0000\u0000\u0144"+
		"\u0142\u0001\u0000\u0000\u0000\u0144\u0143\u0001\u0000\u0000\u0000\u0145"+
		")\u0001\u0000\u0000\u0000\u0146\u0148\u0005.\u0000\u0000\u0147\u0149\u0003"+
		",\u0016\u0000\u0148\u0147\u0001\u0000\u0000\u0000\u0148\u0149\u0001\u0000"+
		"\u0000\u0000\u0149\u014a\u0001\u0000\u0000\u0000\u014a\u014b\u0005/\u0000"+
		"\u0000\u014b+\u0001\u0000\u0000\u0000\u014c\u0152\u0003.\u0017\u0000\u014d"+
		"\u014e\u00030\u0018\u0000\u014e\u014f\u0003.\u0017\u0000\u014f\u0151\u0001"+
		"\u0000\u0000\u0000\u0150\u014d\u0001\u0000\u0000\u0000\u0151\u0154\u0001"+
		"\u0000\u0000\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0152\u0153\u0001"+
		"\u0000\u0000\u0000\u0153\u0156\u0001\u0000\u0000\u0000\u0154\u0152\u0001"+
		"\u0000\u0000\u0000\u0155\u0157\u00030\u0018\u0000\u0156\u0155\u0001\u0000"+
		"\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000\u0157-\u0001\u0000\u0000"+
		"\u0000\u0158\u0159\u00050\u0000\u0000\u0159\u015a\u0003\u0018\f\u0000"+
		"\u015a\u015b\u00051\u0000\u0000\u015b\u015c\u0005\u0002\u0000\u0000\u015c"+
		"\u015d\u0003\u0018\f\u0000\u015d\u0163\u0001\u0000\u0000\u0000\u015e\u015f"+
		"\u00059\u0000\u0000\u015f\u0160\u0005\u0002\u0000\u0000\u0160\u0163\u0003"+
		"\u0018\f\u0000\u0161\u0163\u0003\u0018\f\u0000\u0162\u0158\u0001\u0000"+
		"\u0000\u0000\u0162\u015e\u0001\u0000\u0000\u0000\u0162\u0161\u0001\u0000"+
		"\u0000\u0000\u0163/\u0001\u0000\u0000\u0000\u0164\u0165\u0007\u0005\u0000"+
		"\u0000\u01651\u0001\u0000\u0000\u0000\u0166\u016b\u0005=\u0000\u0000\u0167"+
		"\u016b\u0005>\u0000\u0000\u0168\u016b\u0005?\u0000\u0000\u0169\u016b\u0005"+
		"@\u0000\u0000\u016a\u0166\u0001\u0000\u0000\u0000\u016a\u0167\u0001\u0000"+
		"\u0000\u0000\u016a\u0168\u0001\u0000\u0000\u0000\u016a\u0169\u0001\u0000"+
		"\u0000\u0000\u016b3\u0001\u0000\u0000\u0000\u016c\u0170\u0005:\u0000\u0000"+
		"\u016d\u0170\u0005;\u0000\u0000\u016e\u0170\u0005<\u0000\u0000\u016f\u016c"+
		"\u0001\u0000\u0000\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u016f\u016e"+
		"\u0001\u0000\u0000\u0000\u01705\u0001\u0000\u0000\u0000!>Bhmy\u0093\u0095"+
		"\u009e\u00a4\u00a8\u00ab\u00b6\u00bb\u00c2\u00ca\u00d2\u00d9\u00fc\u00fe"+
		"\u010a\u0112\u0122\u0124\u012d\u0132\u0140\u0144\u0148\u0152\u0156\u0162"+
		"\u016a\u016f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
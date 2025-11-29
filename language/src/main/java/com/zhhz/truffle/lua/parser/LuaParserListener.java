// Generated from language/src/main/java/com/zhhz/truffle/lua/parser/LuaParser.g4 by ANTLR 4.13.2
package com.zhhz.truffle.lua.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LuaParser}.
 */
public interface LuaParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LuaParser#lualanguage}.
	 * @param ctx the parse tree
	 */
	void enterLualanguage(LuaParser.LualanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#lualanguage}.
	 * @param ctx the parse tree
	 */
	void exitLualanguage(LuaParser.LualanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#chunk}.
	 * @param ctx the parse tree
	 */
	void enterChunk(LuaParser.ChunkContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#chunk}.
	 * @param ctx the parse tree
	 */
	void exitChunk(LuaParser.ChunkContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(LuaParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(LuaParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatEmpty}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatEmpty(LuaParser.StatEmptyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatEmpty}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatEmpty(LuaParser.StatEmptyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatAssign(LuaParser.StatAssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatAssign(LuaParser.StatAssignContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatFunctionCall(LuaParser.StatFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatFunctionCall(LuaParser.StatFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatLabel}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatLabel(LuaParser.StatLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatLabel}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatLabel(LuaParser.StatLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatBreak}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatBreak(LuaParser.StatBreakContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatBreak}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatBreak(LuaParser.StatBreakContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatGoto}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatGoto(LuaParser.StatGotoContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatGoto}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatGoto(LuaParser.StatGotoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatDo}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatDo(LuaParser.StatDoContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatDo}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatDo(LuaParser.StatDoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatWhile}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatWhile(LuaParser.StatWhileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatWhile}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatWhile(LuaParser.StatWhileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatRepeat}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatRepeat(LuaParser.StatRepeatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatRepeat}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatRepeat(LuaParser.StatRepeatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatIf}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatIf(LuaParser.StatIfContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatIf}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatIf(LuaParser.StatIfContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatForNumeric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatForNumeric(LuaParser.StatForNumericContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatForNumeric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatForNumeric(LuaParser.StatForNumericContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatForGeneric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatForGeneric(LuaParser.StatForGenericContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatForGeneric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatForGeneric(LuaParser.StatForGenericContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatFunctionDef(LuaParser.StatFunctionDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatFunctionDef(LuaParser.StatFunctionDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatLocalFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatLocalFunctionDef(LuaParser.StatLocalFunctionDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatLocalFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatLocalFunctionDef(LuaParser.StatLocalFunctionDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatLocalVarAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatLocalVarAssign(LuaParser.StatLocalVarAssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatLocalVarAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatLocalVarAssign(LuaParser.StatLocalVarAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#attnamelist}.
	 * @param ctx the parse tree
	 */
	void enterAttnamelist(LuaParser.AttnamelistContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#attnamelist}.
	 * @param ctx the parse tree
	 */
	void exitAttnamelist(LuaParser.AttnamelistContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#attrib}.
	 * @param ctx the parse tree
	 */
	void enterAttrib(LuaParser.AttribContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#attrib}.
	 * @param ctx the parse tree
	 */
	void exitAttrib(LuaParser.AttribContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#retstat}.
	 * @param ctx the parse tree
	 */
	void enterRetstat(LuaParser.RetstatContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#retstat}.
	 * @param ctx the parse tree
	 */
	void exitRetstat(LuaParser.RetstatContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(LuaParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(LuaParser.LabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#funcname}.
	 * @param ctx the parse tree
	 */
	void enterFuncname(LuaParser.FuncnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#funcname}.
	 * @param ctx the parse tree
	 */
	void exitFuncname(LuaParser.FuncnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#varlist}.
	 * @param ctx the parse tree
	 */
	void enterVarlist(LuaParser.VarlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#varlist}.
	 * @param ctx the parse tree
	 */
	void exitVarlist(LuaParser.VarlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#namelist}.
	 * @param ctx the parse tree
	 */
	void enterNamelist(LuaParser.NamelistContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#namelist}.
	 * @param ctx the parse tree
	 */
	void exitNamelist(LuaParser.NamelistContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#explist}.
	 * @param ctx the parse tree
	 */
	void enterExplist(LuaParser.ExplistContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#explist}.
	 * @param ctx the parse tree
	 */
	void exitExplist(LuaParser.ExplistContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpBitwiseAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpBitwiseAnd(LuaParser.ExpBitwiseAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpBitwiseAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpBitwiseAnd(LuaParser.ExpBitwiseAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpBitwiseOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpBitwiseOr(LuaParser.ExpBitwiseOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpBitwiseOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpBitwiseOr(LuaParser.ExpBitwiseOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpBitwiseXor}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpBitwiseXor(LuaParser.ExpBitwiseXorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpBitwiseXor}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpBitwiseXor(LuaParser.ExpBitwiseXorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpMulDivMod}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpMulDivMod(LuaParser.ExpMulDivModContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpMulDivMod}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpMulDivMod(LuaParser.ExpMulDivModContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpComparison}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpComparison(LuaParser.ExpComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpComparison}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpComparison(LuaParser.ExpComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpConcat}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpConcat(LuaParser.ExpConcatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpConcat}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpConcat(LuaParser.ExpConcatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpAddSub}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpAddSub(LuaParser.ExpAddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpAddSub}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpAddSub(LuaParser.ExpAddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpBitwiseShift}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpBitwiseShift(LuaParser.ExpBitwiseShiftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpBitwiseShift}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpBitwiseShift(LuaParser.ExpBitwiseShiftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpUnop}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpUnop(LuaParser.ExpUnopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpUnop}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpUnop(LuaParser.ExpUnopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpAtom}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpAtom(LuaParser.ExpAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpAtom}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpAtom(LuaParser.ExpAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpLogicalAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpLogicalAnd(LuaParser.ExpLogicalAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpLogicalAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpLogicalAnd(LuaParser.ExpLogicalAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpLogicalOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpLogicalOr(LuaParser.ExpLogicalOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpLogicalOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpLogicalOr(LuaParser.ExpLogicalOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpPower}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExpPower(LuaParser.ExpPowerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpPower}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExpPower(LuaParser.ExpPowerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpNil}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpNil(LuaParser.ExpNilContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpNil}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpNil(LuaParser.ExpNilContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpFalse}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpFalse(LuaParser.ExpFalseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpFalse}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpFalse(LuaParser.ExpFalseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpTrue}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpTrue(LuaParser.ExpTrueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpTrue}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpTrue(LuaParser.ExpTrueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpNumber}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpNumber(LuaParser.ExpNumberContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpNumber}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpNumber(LuaParser.ExpNumberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpString}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpString(LuaParser.ExpStringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpString}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpString(LuaParser.ExpStringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpVararg}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpVararg(LuaParser.ExpVarargContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpVararg}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpVararg(LuaParser.ExpVarargContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpFunctionDef}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpFunctionDef(LuaParser.ExpFunctionDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpFunctionDef}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpFunctionDef(LuaParser.ExpFunctionDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpTableConstructor}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpTableConstructor(LuaParser.ExpTableConstructorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpTableConstructor}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpTableConstructor(LuaParser.ExpTableConstructorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpPrefixExpAtom}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterExpPrefixExpAtom(LuaParser.ExpPrefixExpAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpPrefixExpAtom}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitExpPrefixExpAtom(LuaParser.ExpPrefixExpAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterFuncCall(LuaParser.FuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitFuncCall(LuaParser.FuncCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PrefixParen}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterPrefixParen(LuaParser.PrefixParenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PrefixParen}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitPrefixParen(LuaParser.PrefixParenContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarIndexed}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterVarIndexed(LuaParser.VarIndexedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarIndexed}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitVarIndexed(LuaParser.VarIndexedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarMemberAccess}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterVarMemberAccess(LuaParser.VarMemberAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarMemberAccess}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitVarMemberAccess(LuaParser.VarMemberAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarName}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterVarName(LuaParser.VarNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarName}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitVarName(LuaParser.VarNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(LuaParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(LuaParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(LuaParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(LuaParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#functioncall}.
	 * @param ctx the parse tree
	 */
	void enterFunctioncall(LuaParser.FunctioncallContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#functioncall}.
	 * @param ctx the parse tree
	 */
	void exitFunctioncall(LuaParser.FunctioncallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArgsParen}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgsParen(LuaParser.ArgsParenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArgsParen}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgsParen(LuaParser.ArgsParenContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArgsTable}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgsTable(LuaParser.ArgsTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArgsTable}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgsTable(LuaParser.ArgsTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArgsString}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgsString(LuaParser.ArgsStringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArgsString}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgsString(LuaParser.ArgsStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#functiondef}.
	 * @param ctx the parse tree
	 */
	void enterFunctiondef(LuaParser.FunctiondefContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#functiondef}.
	 * @param ctx the parse tree
	 */
	void exitFunctiondef(LuaParser.FunctiondefContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#funcbody}.
	 * @param ctx the parse tree
	 */
	void enterFuncbody(LuaParser.FuncbodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#funcbody}.
	 * @param ctx the parse tree
	 */
	void exitFuncbody(LuaParser.FuncbodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParlistNamelist}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void enterParlistNamelist(LuaParser.ParlistNamelistContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParlistNamelist}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void exitParlistNamelist(LuaParser.ParlistNamelistContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParlistVararg}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void enterParlistVararg(LuaParser.ParlistVarargContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParlistVararg}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void exitParlistVararg(LuaParser.ParlistVarargContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParlistEmpty}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void enterParlistEmpty(LuaParser.ParlistEmptyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParlistEmpty}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 */
	void exitParlistEmpty(LuaParser.ParlistEmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#tableconstructor}.
	 * @param ctx the parse tree
	 */
	void enterTableconstructor(LuaParser.TableconstructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#tableconstructor}.
	 * @param ctx the parse tree
	 */
	void exitTableconstructor(LuaParser.TableconstructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void enterFieldlist(LuaParser.FieldlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void exitFieldlist(LuaParser.FieldlistContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldKeyExpr}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void enterFieldKeyExpr(LuaParser.FieldKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldKeyExpr}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void exitFieldKeyExpr(LuaParser.FieldKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldKeyName}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void enterFieldKeyName(LuaParser.FieldKeyNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldKeyName}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void exitFieldKeyName(LuaParser.FieldKeyNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void enterFieldValue(LuaParser.FieldValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 */
	void exitFieldValue(LuaParser.FieldValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link LuaParser#fieldsep}.
	 * @param ctx the parse tree
	 */
	void enterFieldsep(LuaParser.FieldsepContext ctx);
	/**
	 * Exit a parse tree produced by {@link LuaParser#fieldsep}.
	 * @param ctx the parse tree
	 */
	void exitFieldsep(LuaParser.FieldsepContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberInt}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumberInt(LuaParser.NumberIntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberInt}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumberInt(LuaParser.NumberIntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberHex}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumberHex(LuaParser.NumberHexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberHex}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumberHex(LuaParser.NumberHexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumberFloat(LuaParser.NumberFloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumberFloat(LuaParser.NumberFloatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberHexFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumberHexFloat(LuaParser.NumberHexFloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberHexFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumberHexFloat(LuaParser.NumberHexFloatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringNormal}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void enterStringNormal(LuaParser.StringNormalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringNormal}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void exitStringNormal(LuaParser.StringNormalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringChar}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void enterStringChar(LuaParser.StringCharContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringChar}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void exitStringChar(LuaParser.StringCharContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringLong}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void enterStringLong(LuaParser.StringLongContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringLong}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 */
	void exitStringLong(LuaParser.StringLongContext ctx);
}
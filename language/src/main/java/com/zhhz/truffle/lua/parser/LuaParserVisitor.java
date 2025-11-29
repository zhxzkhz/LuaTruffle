// Generated from language/src/main/java/com/zhhz/truffle/lua/parser/LuaParser.g4 by ANTLR 4.13.2
package com.zhhz.truffle.lua.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LuaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LuaParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LuaParser#lualanguage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLualanguage(LuaParser.LualanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#chunk}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChunk(LuaParser.ChunkContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(LuaParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatEmpty}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatEmpty(LuaParser.StatEmptyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatAssign(LuaParser.StatAssignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatFunctionCall(LuaParser.StatFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatLabel}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatLabel(LuaParser.StatLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatBreak}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatBreak(LuaParser.StatBreakContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatGoto}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatGoto(LuaParser.StatGotoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatDo}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatDo(LuaParser.StatDoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatWhile}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatWhile(LuaParser.StatWhileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatRepeat}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatRepeat(LuaParser.StatRepeatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatIf}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatIf(LuaParser.StatIfContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatForNumeric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatForNumeric(LuaParser.StatForNumericContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatForGeneric}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatForGeneric(LuaParser.StatForGenericContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatFunctionDef(LuaParser.StatFunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatLocalFunctionDef}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatLocalFunctionDef(LuaParser.StatLocalFunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatLocalVarAssign}
	 * labeled alternative in {@link LuaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatLocalVarAssign(LuaParser.StatLocalVarAssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#attnamelist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttnamelist(LuaParser.AttnamelistContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#attrib}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrib(LuaParser.AttribContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#retstat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetstat(LuaParser.RetstatContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(LuaParser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#funcname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncname(LuaParser.FuncnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#varlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarlist(LuaParser.VarlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#namelist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamelist(LuaParser.NamelistContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#explist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplist(LuaParser.ExplistContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpBitwiseAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBitwiseAnd(LuaParser.ExpBitwiseAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpBitwiseOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBitwiseOr(LuaParser.ExpBitwiseOrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpBitwiseXor}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBitwiseXor(LuaParser.ExpBitwiseXorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpMulDivMod}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpMulDivMod(LuaParser.ExpMulDivModContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpComparison}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpComparison(LuaParser.ExpComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpConcat}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpConcat(LuaParser.ExpConcatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpAddSub}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpAddSub(LuaParser.ExpAddSubContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpBitwiseShift}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBitwiseShift(LuaParser.ExpBitwiseShiftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpUnop}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpUnop(LuaParser.ExpUnopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpAtom}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpAtom(LuaParser.ExpAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpLogicalAnd}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpLogicalAnd(LuaParser.ExpLogicalAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpLogicalOr}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpLogicalOr(LuaParser.ExpLogicalOrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpPower}
	 * labeled alternative in {@link LuaParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpPower(LuaParser.ExpPowerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpNil}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpNil(LuaParser.ExpNilContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpFalse}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpFalse(LuaParser.ExpFalseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpTrue}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpTrue(LuaParser.ExpTrueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpNumber}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpNumber(LuaParser.ExpNumberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpString}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpString(LuaParser.ExpStringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpVararg}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpVararg(LuaParser.ExpVarargContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpFunctionDef}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpFunctionDef(LuaParser.ExpFunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpTableConstructor}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpTableConstructor(LuaParser.ExpTableConstructorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpPrefixExpAtom}
	 * labeled alternative in {@link LuaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpPrefixExpAtom(LuaParser.ExpPrefixExpAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(LuaParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PrefixParen}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixParen(LuaParser.PrefixParenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarIndexed}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarIndexed(LuaParser.VarIndexedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarMemberAccess}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarMemberAccess(LuaParser.VarMemberAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarName}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarName(LuaParser.VarNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MethodCall}
	 * labeled alternative in {@link LuaParser#prefixexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(LuaParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(LuaParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#functioncall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctioncall(LuaParser.FunctioncallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArgsParen}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgsParen(LuaParser.ArgsParenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArgsTable}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgsTable(LuaParser.ArgsTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArgsString}
	 * labeled alternative in {@link LuaParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgsString(LuaParser.ArgsStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#functiondef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctiondef(LuaParser.FunctiondefContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#funcbody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncbody(LuaParser.FuncbodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParlistNamelist}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParlistNamelist(LuaParser.ParlistNamelistContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParlistVararg}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParlistVararg(LuaParser.ParlistVarargContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParlistEmpty}
	 * labeled alternative in {@link LuaParser#parlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParlistEmpty(LuaParser.ParlistEmptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#tableconstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableconstructor(LuaParser.TableconstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#fieldlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldlist(LuaParser.FieldlistContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldKeyExpr}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldKeyExpr(LuaParser.FieldKeyExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldKeyName}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldKeyName(LuaParser.FieldKeyNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link LuaParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldValue(LuaParser.FieldValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link LuaParser#fieldsep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldsep(LuaParser.FieldsepContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberInt}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberInt(LuaParser.NumberIntContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberHex}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberHex(LuaParser.NumberHexContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberFloat(LuaParser.NumberFloatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberHexFloat}
	 * labeled alternative in {@link LuaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberHexFloat(LuaParser.NumberHexFloatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringNormal}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringNormal(LuaParser.StringNormalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringChar}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringChar(LuaParser.StringCharContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringLong}
	 * labeled alternative in {@link LuaParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLong(LuaParser.StringLongContext ctx);
}
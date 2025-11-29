/*
 * The parser and lexer need to be generated using "mx create-sl-parser".
 */

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

parser grammar LuaParser;

options {
    tokenVocab = LuaLexer;
    superClass = LuaParserBase;
}

lualanguage
    : chunk EOF
    ;

chunk
    : block
    ;

block
    : stat* retstat?
    ;

stat
    : ';'                                                                   # StatEmpty
    | varlist '=' explist                                                   # StatAssign
    | functioncall                                                          # StatFunctionCall
    | label                                                                 # StatLabel
    | 'break'                                                               # StatBreak
    | 'goto' NAME                                                           # StatGoto
    | 'do' block 'end'                                                      # StatDo
    | 'while' exp 'do' block 'end'                                          # StatWhile
    | 'repeat' block 'until' exp                                            # StatRepeat
    | 'if' exp 'then' block ('elseif' exp 'then' block)* ('else' block)? 'end' # StatIf
    | 'for' NAME '=' exp ',' exp (',' exp)? 'do' block 'end'                # StatForNumeric
    | 'for' namelist 'in' explist 'do' block 'end'                          # StatForGeneric
    | 'function' funcname funcbody                                          # StatFunctionDef
    | 'local' 'function' NAME funcbody                                      # StatLocalFunctionDef
    | 'local' attnamelist ('=' explist)?                                    # StatLocalVarAssign
    ;

attnamelist
    : NAME attrib (',' NAME attrib)*
    ;

attrib
    : ('<' NAME '>')?
    ;

retstat
    : 'return' explist? ';'?
    ;

label
    : '::' NAME '::'
    ;

funcname
    : NAME ('.' NAME)* (':' NAME)?
    ;

// 注意：这里使用了 generic_prefixexp (原 var)
varlist
    : var (',' var)*
    ;

namelist
    : NAME (',' NAME)*
    ;

explist
    : exp (',' exp)*
    ;

exp
    : <assoc=right> exp '^' exp                 # ExpPower
    | op=('-' | 'not' | '#' | '~') exp          # ExpUnop
    | exp op=('*' | '/' | '//' | '%') exp       # ExpMulDivMod
    | exp op=('+' | '-') exp                    # ExpAddSub
    | <assoc=right> exp '..' exp                # ExpConcat
    | exp op=('<<' | '>>') exp                  # ExpBitwiseShift
    | exp op='&' exp                            # ExpBitwiseAnd
    | exp op='~' exp                            # ExpBitwiseXor
    | exp op='|' exp                            # ExpBitwiseOr
    | exp op=('<' | '>' | '<=' | '>=' | '~=' | '==') exp # ExpComparison
    | exp op='and' exp                          # ExpLogicalAnd
    | exp op='or' exp                           # ExpLogicalOr
    | atom                                      # ExpAtom
    ;

atom
    : 'nil'                                             # ExpNil
    | 'false'                                           # ExpFalse
    | 'true'                                            # ExpTrue
    | number                                            # ExpNumber
    | string                                            # ExpString
    | '...'                                             # ExpVararg
    | functiondef                                       # ExpFunctionDef
    | tableconstructor                                  # ExpTableConstructor
    | prefixexp                                         # ExpPrefixExpAtom // 引用统一的前缀表达式
    ;

// ============================================================================
// 核心修复：统一前缀表达式 (Flattened Prefix Expressions)
// ============================================================================
// 我们将 var, functioncall, prefixexp 合并为一个直接左递归规则。
// 这样可以支持无限链式调用，如: getmetatable("").__band()
//
// 注意：这也意味着 parser 允许 "f() = 1" 这样的语法。
// 你需要在 Java Visitor 的 visitStatAssign 中检查左值是否合法。
// ============================================================================

prefixexp
    : NAME                                              # VarName           // 基础：变量名
    | '(' exp ')'                                       # PrefixParen       // 基础：括号表达式
    | prefixexp '[' exp ']'                             # VarIndexed        // 递归：索引访问
    | prefixexp '.' NAME                                # VarMemberAccess   // 递归：成员访问
    | prefixexp args                                    # FuncCall          // 递归：函数调用
    | prefixexp ':' NAME args                           # MethodCall        // 递归：冒号调用
    ;

// 为了兼容现有的 stat 规则引用，我们定义 var 和 functioncall 为 prefixexp 的别名。
// ANTLR 会生成对应的 Context，但底层都是 PrefixexpContext。

var
    : prefixexp
    ;

functioncall
    : prefixexp
    ;

// ============================================================================

args
    : '(' explist? ')'                                  # ArgsParen
    | tableconstructor                                  # ArgsTable
    | string                                            # ArgsString
    ;

functiondef
    : 'function' funcbody
    ;

funcbody
    : '(' parlist ')' block 'end'
    ;

parlist
    : namelist (',' '...')?                             # ParlistNamelist
    | '...'                                             # ParlistVararg
    |                                                   # ParlistEmpty
    ;

tableconstructor
    : '{' fieldlist? '}'
    ;

fieldlist
    : field (fieldsep field)* fieldsep?
    ;

field
    : '[' exp ']' '=' exp                               # FieldKeyExpr
    | NAME '=' exp                                      # FieldKeyName
    | exp                                               # FieldValue
    ;

fieldsep
    : ','
    | ';'
    ;

number
    : INT                                               # NumberInt
    | HEX                                               # NumberHex
    | FLOAT                                             # NumberFloat
    | HEX_FLOAT                                         # NumberHexFloat
    ;

string
    : NORMALSTRING                                      # StringNormal
    | CHARSTRING                                        # StringChar
    | LONGSTRING                                        # StringLong
    ;
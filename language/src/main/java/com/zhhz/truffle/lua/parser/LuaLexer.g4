lexer grammar LuaLexer;

options {
    superClass = LuaLexerBase;
}

// Insert here @header for lexer.

SEMI : ';';
EQ   : '=';

BREAK    : 'break';
GOTO     : 'goto';
DO       : 'do';
END      : 'end';
WHILE    : 'while';
REPEAT   : 'repeat';
UNTIL    : 'until';
IF       : 'if';
THEN     : 'then';
ELSEIF   : 'elseif';
ELSE     : 'else';
FOR      : 'for';
COMMA    : ',';
IN       : 'in';
FUNCTION : 'function';
LOCAL    : 'local';
LT       : '<';
GT       : '>';
RETURN   : 'return';
CONTINUE : 'continue';
CC       : '::';
NIL      : 'nil';
FALSE    : 'false';
TRUE     : 'true';
DOT      : '.';
SQUIG    : '~';
MINUS    : '-';
POUND    : '#';
OP       : '(';
CP       : ')';
NOT      : 'not';
LL       : '<<';
GG       : '>>';
AMP      : '&';
SS       : '//';
PER      : '%';
COL      : ':';
LE       : '<=';
GE       : '>=';
AND      : 'and';
OR       : 'or';
PLUS     : '+';
STAR     : '*';
OCU      : '{';
CCU      : '}';
OB       : '[';
CB       : ']';
EE       : '==';
DD       : '..';
PIPE     : '|';
CARET    : '^';
SLASH    : '/';
DDD      : '...';
SQEQ     : '~=';

NAME: [a-zA-Z_][a-zA-Z_0-9]*;

// 标准 Lua：短字符串内不允许出现原始换行（read_string 对裸 \r/\n 报
// "unfinished string"），多行须用 \<LF>/\<CR> 续行转义或 \z 跳过空白
NORMALSTRING: '"' ( EscapeSequence | ~('\\' | '"' | '\r' | '\n'))* '"' { this.AdjustShortStringLine(); };

CHARSTRING: '\'' ( EscapeSequence | ~('\'' | '\\' | '\r' | '\n'))* '\'' { this.AdjustShortStringLine(); };

LONGSTRING: '[' NESTED_STR ']' { this.AdjustLongStringLine(); };

fragment NESTED_STR: '=' NESTED_STR '=' | '[' .*? ']';

INT: Digit+;

HEX: '0' [xX] HexDigit+;

FLOAT: Digit+ '.' Digit* ExponentPart? | '.' Digit+ ExponentPart? | Digit+ ExponentPart;

HEX_FLOAT:
    '0' [xX] HexDigit+ '.' HexDigit* HexExponentPart?
    | '0' [xX] '.' HexDigit+ HexExponentPart?
    | '0' [xX] HexDigit+ HexExponentPart
;

fragment ExponentPart: [eE] [+-]? Digit+;

fragment HexExponentPart: [pP] [+-]? Digit+;

fragment EscapeSequence:
    '\\' [abfnrtv"'|$#\\] // World of Warcraft Lua additionally escapes |$#
    | '\\' 'z' [ \t\u000B\u000C\r\n]* // \z 跳过后继空白（含换行，标准 readsp 语义）
    | '\\' '\r' '\n'?  // \<CR> 与 \<CR><LF> 续行转义（标准 Lua inclinenumber 语义）
    | '\\' '\n' '\r'?  // \<LF> 与 \<LF><CR> 续行转义
    | DecimalEscape
    | HexEscape
    | UtfEscape
;

fragment DecimalEscape: '\\' Digit | '\\' Digit Digit | '\\' [0-2] Digit Digit;

fragment HexEscape: '\\' 'x' HexDigit HexDigit;

fragment UtfEscape: '\\' 'u{' HexDigit+ '}';

fragment Digit: [0-9];

fragment HexDigit: [0-9a-fA-F];

fragment SingleLineInputCharacter: ~[\r\n\u0085\u2028\u2029];

COMMENT: '--' { this.HandleComment(); } -> channel(HIDDEN);

WS: [ \t\u000B\u000C]+ -> channel(HIDDEN);

NL: ('\r\n' | '\r' | '\n') { this.AdjustNewlineLine(); } -> channel(2);

SHEBANG: '#' { this.IsLine1Col0() }? '!'? SingleLineInputCharacter* -> channel(HIDDEN);
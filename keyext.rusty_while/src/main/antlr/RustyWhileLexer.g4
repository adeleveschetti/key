lexer grammar RustyWhileLexer;

@ header
{
    package org.key_project.rusty.parsing;
}
// https://doc.rust-lang.org/reference/keywords.html strict

KW_AS
   : 'as'
   ;

KW_BREAK
   : 'break'
   ;

KW_CONST
   : 'const'
   ;

KW_CONTINUE
   : 'continue'
   ;

KW_CRATE
   : 'crate'
   ;

KW_ELSE
   : 'else'
   ;

KW_ENUM
   : 'enum'
   ;

KW_EXTERN
   : 'extern'
   ;

KW_FALSE
   : 'false'
   ;

KW_FN
   : 'fn'
   ;

KW_FOR
   : 'for'
   ;

KW_IF
   : 'if'
   ;

KW_IMPL
   : 'impl'
   ;

KW_IN
   : 'in'
   ;

KW_LET
   : 'let'
   ;

KW_LOOP
   : 'loop'
   ;

KW_MATCH
   : 'match'
   ;

KW_MOD
   : 'mod'
   ;

KW_MOVE
   : 'move'
   ;

KW_MUT
   : 'mut'
   ;

KW_PUB
   : 'pub'
   ;

KW_REF
   : 'ref'
   ;

KW_RETURN
   : 'return'
   ;

KW_SELFVALUE
   : 'self'
   ;

KW_SELFTYPE
   : 'Self'
   ;

KW_STATIC
   : 'static'
   ;

KW_STRUCT
   : 'struct'
   ;

KW_SUPER
   : 'super'
   ;

KW_TRAIT
   : 'trait'
   ;

KW_TRUE
   : 'true'
   ;

KW_TYPE
   : 'type'
   ;

KW_UNSAFE
   : 'unsafe'
   ;

KW_USE
   : 'use'
   ;

KW_WHERE
   : 'where'
   ;

KW_WHILE
   : 'while'
   ;
   // 2018+
   
KW_ASYNC
   : 'async'
   ;

KW_AWAIT
   : 'await'
   ;

KW_DYN
   : 'dyn'
   ;
   // reserved
   
KW_ABSTRACT
   : 'abstract'
   ;

KW_BECOME
   : 'become'
   ;

KW_BOX
   : 'box'
   ;

KW_DO
   : 'do'
   ;

KW_FINAL
   : 'final'
   ;

KW_MACRO
   : 'macro'
   ;

KW_OVERRIDE
   : 'override'
   ;

KW_PRIV
   : 'priv'
   ;

KW_TYPEOF
   : 'typeof'
   ;

KW_UNSIZED
   : 'unsized'
   ;

KW_VIRTUAL
   : 'virtual'
   ;

KW_YIELD
   : 'yield'
   ;
   // reserved 2018+
   
KW_TRY
   : 'try'
   ;
   // weak
   
KW_UNION
   : 'union'
   ;

KW_STATICLIFETIME
   : '\'static'
   ;

KW_MACRORULES
   : 'macro_rules'
   ;

KW_UNDERLINELIFETIME
   : '\'_'
   ;

KW_DOLLARCRATE
   : '$crate'
   ;
   // rule itself allow any identifier, but keyword has been matched before
   
NON_KEYWORD_IDENTIFIER
   : XID_Start XID_Continue*
   | '_' XID_Continue+
   ;
   // [\p{L}\p{Nl}\p{Other_ID_Start}-\p{Pattern_Syntax}-\p{Pattern_White_Space}]
   
fragment XID_Start
   : [\p{L}\p{Nl}]
   | UNICODE_OIDS
   ;
   // [\p{ID_Start}\p{Mn}\p{Mc}\p{Nd}\p{Pc}\p{Other_ID_Continue}-\p{Pattern_Syntax}-\p{Pattern_White_Space}]
   
fragment XID_Continue
   : XID_Start
   | [\p{Mn}\p{Mc}\p{Nd}\p{Pc}]
   | UNICODE_OIDC
   ;

fragment UNICODE_OIDS
   : '\u1885' .. '\u1886'
   | '\u2118'
   | '\u212e'
   | '\u309b' .. '\u309c'
   ;

fragment UNICODE_OIDC
   : '\u00b7'
   | '\u0387'
   | '\u1369' .. '\u1371'
   | '\u19da'
   ;

RAW_IDENTIFIER
   : 'r#' NON_KEYWORD_IDENTIFIER
   ;
   // comments https://doc.rust-lang.org/reference/comments.html
   
LINE_COMMENT
   : ('//' (~ [/!] | '//') ~ [\r\n]* | '//') -> channel (HIDDEN)
   ;

BLOCK_COMMENT
   : ('/*' (~ [*!] | '**' | BLOCK_COMMENT_OR_DOC) (BLOCK_COMMENT_OR_DOC | ~ [*])*? '*/' | '/**/' | '/***/') -> channel (HIDDEN)
   ;

INNER_LINE_DOC
   : '//!' ~ [\n\r]* -> channel (HIDDEN)
   ; // isolated cr
   
INNER_BLOCK_DOC
   : '/*!' (BLOCK_COMMENT_OR_DOC | ~ [*])*? '*/' -> channel (HIDDEN)
   ;

OUTER_LINE_DOC
   : '///' (~ [/] ~ [\n\r]*)? -> channel (HIDDEN)
   ; // isolated cr
   
OUTER_BLOCK_DOC
   : '/**' (~ [*] | BLOCK_COMMENT_OR_DOC) (BLOCK_COMMENT_OR_DOC | ~ [*])*? '*/' -> channel (HIDDEN)
   ;

BLOCK_COMMENT_OR_DOC
   : (BLOCK_COMMENT | INNER_BLOCK_DOC | OUTER_BLOCK_DOC) -> channel (HIDDEN)
   ;
   // whitespace https://doc.rust-lang.org/reference/whitespace.html
   
WHITESPACE
   : [\p{Zs}] -> channel (HIDDEN)
   ;

NEWLINE
   : ('\r\n' | [\r\n]) -> channel (HIDDEN)
   ;
   // tokens char and string
   
CHAR_LITERAL
   : '\'' (~ ['\\\n\r\t] | QUOTE_ESCAPE | ASCII_ESCAPE | UNICODE_ESCAPE) '\''
   ;

STRING_LITERAL
   : '"' (~ ["] | QUOTE_ESCAPE | ASCII_ESCAPE | UNICODE_ESCAPE | ESC_NEWLINE)* '"'
   ;

RAW_STRING_LITERAL
   : 'r' RAW_STRING_CONTENT
   ;

fragment RAW_STRING_CONTENT
   : '#' RAW_STRING_CONTENT '#'
   | '"' .*? '"'
   ;

BYTE_LITERAL
   : 'b\'' (. | QUOTE_ESCAPE | BYTE_ESCAPE) '\''
   ;

BYTE_STRING_LITERAL
   : 'b"' (~ ["] | QUOTE_ESCAPE | BYTE_ESCAPE)* '"'
   ;

RAW_BYTE_STRING_LITERAL
   : 'br' RAW_STRING_CONTENT
   ;

fragment ASCII_ESCAPE
   : '\\x' OCT_DIGIT HEX_DIGIT
   | COMMON_ESCAPE
   ;

fragment BYTE_ESCAPE
   : '\\x' HEX_DIGIT HEX_DIGIT
   | COMMON_ESCAPE
   ;

fragment COMMON_ESCAPE
   : '\\' [nrt\\0]
   ;

fragment UNICODE_ESCAPE
   : '\\u{' HEX_DIGIT HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? '}'
   ;

fragment QUOTE_ESCAPE
   : '\\' ['"]
   ;

fragment ESC_NEWLINE
   : '\\' '\n'
   ;

INTEGER_LITERAL
   : (DEC_LITERAL | BIN_LITERAL | OCT_LITERAL | HEX_LITERAL) INTEGER_SUFFIX?
   ;

DEC_LITERAL
   : DEC_DIGIT (DEC_DIGIT | '_')*
   ;

HEX_LITERAL
   : '0x' '_'* HEX_DIGIT (HEX_DIGIT | '_')*
   ;

OCT_LITERAL
   : '0o' '_'* OCT_DIGIT (OCT_DIGIT | '_')*
   ;

BIN_LITERAL
   : '0b' '_'* [01] [01_]*
   ;

fragment INTEGER_SUFFIX
   : 'u8'
   | 'u16'
   | 'u32'
   | 'u64'
   | 'u128'
   | 'usize'
   | 'i8'
   | 'i16'
   | 'i32'
   | 'i64'
   | 'i128'
   | 'isize'
   ;

fragment FLOAT_SUFFIX
   : 'f32'
   | 'f64'
   ;

fragment FLOAT_EXPONENT
   : [eE] [+-]? '_'* DEC_LITERAL
   ;

fragment OCT_DIGIT
   : [0-7]
   ;

fragment DEC_DIGIT
   : [0-9]
   ;

fragment HEX_DIGIT
   : [0-9a-fA-F]
   ;

LIFETIME_OR_LABEL
   : '\'' NON_KEYWORD_IDENTIFIER
   ;

PLUS
   : '+'
   ;

MINUS
   : '-'
   ;

STAR
   : '*'
   ;

SLASH
   : '/'
   ;

PERCENT
   : '%'
   ;

CARET
   : '^'
   ;

NOT
   : '!'
   ;

AND
   : '&'
   ;

OR
   : '|'
   ;

ANDAND
   : '&&'
   ;

OROR
   : '||'
   ;

PLUSEQ
   : '+='
   ;

MINUSEQ
   : '-='
   ;

STAREQ
   : '*='
   ;

SLASHEQ
   : '/='
   ;

PERCENTEQ
   : '%='
   ;

CARETEQ
   : '^='
   ;

ANDEQ
   : '&='
   ;

OREQ
   : '|='
   ;

SHLEQ
   : '<<='
   ;

SHREQ
   : '>>='
   ;

EQ
   : '='
   ;

EQEQ
   : '=='
   ;

NE
   : '!='
   ;

GT
   : '>'
   ;

LT
   : '<'
   ;

GE
   : '>='
   ;

LE
   : '<='
   ;

AT
   : '@'
   ;

UNDERSCORE
   : '_'
   ;

DOT
   : '.'
   ;

DOTDOT
   : '..'
   ;

DOTDOTDOT
   : '...'
   ;

DOTDOTEQ
   : '..='
   ;

COMMA
   : ','
   ;

SEMI
   : ';'
   ;

COLON
   : ':'
   ;

PATHSEP
   : '::'
   ;

RARROW
   : '->'
   ;

FATARROW
   : '=>'
   ;

POUND
   : '#'
   ;

DOLLAR
   : '$'
   ;

QUESTION
   : '?'
   ;

LCURLYBRACE
   : '{'
   ;

RCURLYBRACE
   : '}'
   ;

LSQUAREBRACKET
   : '['
   ;

RSQUAREBRACKET
   : ']'
   ;

LPAREN
   : '('
   ;

RPAREN
   : ')'
   ;

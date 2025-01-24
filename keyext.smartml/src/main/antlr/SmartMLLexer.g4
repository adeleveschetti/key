lexer grammar SmartMLLexer;

// Tokens

RETURN      : 'return';
RETURNS     : 'returns';
VIEW        : 'view';
PURE        : 'pure';
CONTRACT    : 'contract';
FUN         : 'function';
FIELD       : 'field';
INT         : 'int';
BOOL        : 'bool';
ADDRESS     : 'Address';
STRING      : 'string';
WHILE       : 'while';
TRUE        : 'true';
FALSE       : 'false';
ASSERT      : 'assert';
REVERT      : 'revert';
DATATYPE    : 'datatype';
INTERFACE   : 'interface';
STORAGE     : 'storage';
IMPLEMENTS  : 'implements';
ADT_FUNCTION: 'adt_function';
DEFAULT     : 'default';
CONSTRUCTOR : 'constructor';
SAFE        : 'safe';
INIT        : 'init';
TRY         : 'try';
CATCH       : 'catch';
ABORT       : 'abort';
SUCCESS     : 'success';
COMMIT      : 'commit';
SWITCH      : 'switch';
CASE        : 'case';
IF          : 'if';
IN          : 'in';
THEN        : 'then';
ELSE        : 'else';
LET         : 'let';
EXCEPTION   : 'exception';
RESOURCE    : 'resource';
NEW         : 'new';
THIS        : 'this';
USES        : 'uses';
PLUS        : '+';
MINUS       : '-';
TIMES       : '*';
DIV         : '/';
PAR         : '|';
LPAR        : '(';
RPAR        : ')';
SLPAR       : '[';
SRPAR       : ']';
CLPAR       : '{';
CRPAR       : '}';
COMMA       : ',';
COLON       : ':';
DOUBLE_COLON: '::';
SEMIC       : ';';
QM          : '"';
DOLLAR      : '$';
LEQ         : '<=';
GEQ         : '>=';
LE          : '<';
GE          : '>';
OR          : '||';
AND         : '&&';
NOT         : '!';
EQ          : '==';
NEQ         : '!=';
ASM         : '=';
ASSIGN      : ':=';
UNDERSCORE  : '_';
OUT         : '?';
DOT         : '.';

// Whitespace, Comments, and Strings

WS
    : [ \t\r\n\u00a0]+ -> skip;

EOL_COMMENT
    : '//' .*? '\n' -> skip;

COMMENTS
    : LINECOMMENTS | BLOCKCOMMENTS;

LINECOMMENTS
    : '//' (~('\n'|'\r'))* -> skip;

BLOCKCOMMENTS
    : '/*' (~('/'|'*') | '/'~'*' | '*'~'/')* '*/' -> skip;

SINGLE_STRING
    : '\'' ~('\'' )+ '\'';

DOUBLE_STRING
    : '"' ~('"')+ '"';

// Numbers
fragment DIGIT
    : '0'..'9';

INTEGER
    : '-'? DIGIT+;

// Literals
LITERALS
    : SINGLE_STRING | DOUBLE_STRING | INTEGER;

// IDs
fragment CHAR
    : 'a'..'z' | 'A'..'Z';

ID
    : (CHAR | UNDERSCORE)+ (CHAR | DIGIT | UNDERSCORE)* CHAR*;

parser grammar SmartMLParser;

options {
  tokenVocab=SmartMLLexer;
}

// Parser rules

program
    : (datatypeDec | exceptionDec | resourceDec)*
      (interfaceDec)*
      (contractDec)+
    ;

datatypeDec
    : DATATYPE id CLPAR dataTypeConstr (adtFunctionDec)* CRPAR
    ;

dataTypeConstr
    : CONSTRUCTOR CLPAR (typeParams (PAR typeParams)*)? CRPAR
    ;

adtFunctionDec
    : type id LPAR (vardec (COMMA vardec)*)? RPAR adtStatBlock
    ;

adtStatBlock
    : CLPAR (adtExpression)* CRPAR
    ;

adtExpression
    : (RETURN)? expr (SEMIC)?
    | (RETURN)? switchExpr
    | ifExpression
    | (RETURN)? adtCall (SEMIC)?
    | adtAssign SEMIC
    ;

dataTypeCall
    : funName=id LPAR (vardec (COMMA vardec)*)? RPAR
    ;

ifExpression
    : IF expr adtStatBlock ELSE adtStatBlock
    ;

adtCall
    : funName=id LPAR params RPAR
    ;

switchExpr
    : SWITCH expr CLPAR (caseExpr)* CRPAR
    ;

caseExpr
    : (CASE (valuesCase+=adtExpression) COLON (blockCase+=adtExpression)* SEMIC?)*
      (DEFAULT COLON (defaultCase+=adtExpression)* SEMIC?)
    ;

adtAssign
    : vardec ASM (expr | dataTypeCall)
    ;

exceptionDec
    : EXCEPTION ID (LPAR vardec (COMMA vardec)*)? RPAR
    ;

resourceDec
    : RESOURCE ID CLPAR (field)* constructor (function)* CRPAR
    ;

interfaceDec
    : INTERFACE id (COLON subtypeId=id)? CLPAR (functionDec)* CRPAR
    ;

contractDec
    : CONTRACT contractId=id (USES (resourceTypes+=id (COMMA resourceTypes+=id)*))?
      (COLON IMPLEMENTS subtypeId=id)?
      CLPAR body CRPAR
    ;

body
    : (datatypeDec)* (field)* constructor (function)*
    ;

constructor
    : CONSTRUCTOR LPAR (varParams+=vardec (COMMA varParams+=vardec)*)? RPAR CLPAR
      (assign SEMIC)* (varBody+=vardec SEMIC)* (internalCall SEMIC)* CRPAR
    ;

typeParams
    : (id | dataTypeCall) DOUBLE_COLON type
    ;

field
    : type id SEMIC
    ;

functionDec
    : (funType)? id LPAR (vardec (COMMA vardec)*)? RPAR (RETURNS returnType)?
    ;

function
    : functionDec statBlock
    ;

funType
    : VIEW | PURE
    ;

statement
    : ifStatement | exprStat | loop | assign SEMIC | funCall SEMIC
    | assertError SEMIC | transaction | returnStat | tryStatement | statBlock
    ;

ifStatement
    : IF LPAR cond+=expr RPAR blocks+=statBlock (ELSE elseBlock=statBlock)?
    ;

exprStat
    : expr SEMIC
    | letExpr
    ;

loop
    : WHILE expr statBlock
    ;

assign
    : vardec ASM ( expr | funCall)
    ;

funCall
    : internalCall | externalCall | adtCall
    ;

internalCall
    : idName=thisVal DOT funName=id LPAR params? RPAR
    ;

externalCall
    : (SAFE)? idName=id (resources)? DOT funName=id LPAR params? RPAR
    ;

assertError
    : ASSERT LPAR expr RPAR
    ;

letExpr
    : LET ident+=vardec ASM exprs+=expr (COMMA ident+=vardec ASM exprs+=expr)? IN valExpr=statement
    ;

transaction
    : TRY statement ((ABORT abortStat=statBlock)? (SUCCESS successStat=statBlock)?)
    ;

returnStat
    : RETURN expr SEMIC
    ;

tryStatement
    : TRY statement CATCH LPAR vardec RPAR statBlock
    ;

statBlock
    : CLPAR (statement)* CRPAR
    ;


params
    : (expr COMMA)* expr
    ;

expr
    : literalExpr # LiteralExpression
   | expr DOT identifier # FieldExpression
   | expr LPAREN params? RPAREN # CallExpression
   | expr DOLLAR expr # ResourceExpression
   | (MINUS | NOT) expr # NegationExpression
   | expr (PLUS | MINUS) expr # ArithmeticOrLogicalExpression
   | expr (shl | shr) expr # ArithmeticOrLogicalExpression
   | expr AND expr # ArithmeticOrLogicalExpression
   | expr CARET expr # ArithmeticOrLogicalExpression
   | expr OR expr # ArithmeticOrLogicalExpression
   | expr comparisonOperator expr # ComparisonExpression
   | expr EQ expr # AssignmentExpression
   ;

identifier
    : ID
    ;

comparisonOperator
   : '=='
   | '!='
   | '>'
   | '<'
   | '>='
   | '<='
   ;

shl
   : LT
   {_input.LA(1) == LT}? LT
   ;

shr
   : GT
   {_input.LA(1) == GT}? GT
   ;

literalExpr
   : CHAR
   | LITERALS
   | DOUBLE_STRING
   | SINGLE_STRING
   | ID
   | INTEGER
   | TRUE
   | FALSE
   ;

newVal
    : NEW id LPAR params? RPAR
    ;

thisVal
    : THIS
    ;

vardec
    : (type)? (STORAGE)? (id | qualifiedAccess)
    ;

qualifiedAccess
    : (id | thisVal) DOT fieldName=id
    ;

adtFunCall
    : id DOT adtCall
    ;

id
    : ID
    ;

returnType
    : INT
    | BOOL
    | STRING
    ;

type
    : INT
    | BOOL
    | ADDRESS
    | STRING
    | ID
    | EXCEPTION
    ;

bool
    : TRUE | FALSE
    ;

string
    : SINGLE_STRING | DOUBLE_STRING | ID
    ;

address
    : id
    ;


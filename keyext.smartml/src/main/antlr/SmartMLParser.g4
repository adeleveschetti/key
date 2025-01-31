parser grammar SmartMLParser;

options {
  tokenVocab=SmartMLLexer;
}

// Parser rules

program
    : (adtDec | exceptionDec | resourceDec)*
      (interfaceDec)*
      (contractDec)+ EOF
    ;

adtDec
    : DATATYPE (id ) CLPAR adtConstr (adtFunctionDec)* CRPAR
    ;

adtConstr
    : CONSTRUCTOR CLPAR (typeParams (PAR typeParams)*)? CRPAR
    ;

adtFunctionDec
    : type id LPAR (params)? RPAR adtblockExpr
    ;

adtblockExpr
    : CLPAR (adtExpression)* CRPAR
    ;

adtExpression
    : (RETURN)? expr (SEMIC)?
    | (RETURN)? switchExpr
    | ifExpression
    | (RETURN)? adtCall (SEMIC)?
    | adtAssign SEMIC
    ;

ifExpression
    : IF expr adtblockExpr ELSE adtblockExpr
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
    : VardecExpression ASM (expr | adtCall)
    ;

exceptionDec
    : EXCEPTION ID (LPAR VardecExpression (COMMA VardecExpression)*)? RPAR
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
    : (adtDec)* (field)* constructor (function)*
    ;

constructor
    : CONSTRUCTOR LPAR (params)? RPAR CLPAR (expr SEMIC)* CRPAR
    ;

typeParams
    : (id | adtCall | NIL) DOUBLE_COLON type
    ;

field
    : type id SEMIC
    ;

functionDec
    : id LPAR (params )? RPAR (RETURNS returnType)?
    ;

function
    : functionDec blockExpr
    ;

ifStatement
    : IF LPAR cond+=expr RPAR blocks+=blockExpr (ELSE elseBlock=blockExpr)?
    ;

exprStat
    : expr SEMIC
    | letExpr
    ;

loop
    : WHILE expr blockExpr
    ;

//funCall
//    : internalCall
//    | externalCall
//    | adtCall
//    ;

internalCall
    : idName=thisVal DOT funName=id LPAR params? RPAR
    ;

externalCall
    : (SAFE)? idName=id (DOLLAR expr)? DOT funName=id LPAR params? RPAR
    ;

assert
    : ASSERT LPAR expr RPAR
    ;

letExpr
    : LET ident+=VardecExpression ASM exprs+=expr (COMMA ident+=VardecExpression ASM exprs+=expr)? IN valExpr=stmt
    ;

tryStatement
    : TRY stmt CATCH LPAR params RPAR blockExpr
    ;

tryAbortStatement
    : TRY expr SEMIC ABORT blockExpr SUCCESS blockExpr
    ;

params
    : (param COMMA)* param
    ;

param
    : type id ;

expr
    : literalExpr                                         # LiteralExpression
    | id                                                  # IdentifierExpression
    | LPAR expr RPAR                                      # ParenthesizedExpression
    | expr (DOT identifier)                               # FieldAccess
    | expr LPAR params? RPAR                              # FunctionCall
    | expr DOLLAR expr                                    # ResourceExpression
    | expr comparisonOperator expr                        # ComparisonExpression
    | (NOT | MINUS) expr                                  # UnaryExpression
    | expr (PLUS | MINUS) expr                            # AdditiveExpression
    | expr (shl | shr) expr                               # ShiftExpression
    | expr (AND | OR ) expr                               # LogicalExpression
    | expr ASM expr                                       # AssignmentExpression
    | RETURN expr                                         # ReturnExpression
    | NEW id LPAR params? RPAR                            # NewValsExpression
    | type expr                                          # VardecExpression
    ;

stmt
   : ';'
   | ifStatement
   | expr
   | letExpr
   | loop
   | assert
   | tryAbortStatement
   | tryStatement
   | blockExpr
   ;

blockExpr
   : '{' stmts? '}'
   ;

stmts
   : stmt+ expr?
   | expr
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
   | THIS
   | LITERALS
   | DOUBLE_STRING
   | SINGLE_STRING
   | ID
   | INTEGER
   | TRUE
   | FALSE
   | NIL
   ;

thisVal
    : THIS
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


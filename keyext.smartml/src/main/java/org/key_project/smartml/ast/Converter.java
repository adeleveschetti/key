/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast;

import java.math.BigInteger;
import java.util.*;

import org.key_project.logic.Name;
import org.key_project.smartml.ast.expr.*;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.abstraction.Type;
import org.key_project.smartml.ast.fn.Constructor;
import org.key_project.smartml.ast.fn.Function;
import org.key_project.smartml.ast.stmt.*;
import org.key_project.smartml.ast.ty.SmartMLType;
import org.key_project.smartml.logic.op.ProgramVariable;
import org.key_project.smartml.parser.SmartMLParser;
import org.key_project.util.collection.ImmutableArray;
import org.key_project.util.collection.ImmutableList;

public class Converter {
    // TODO: Rework this properly
    private final Map<String, VariableDeclaration> variables = new HashMap<>();
    private final Map<VariableDeclaration, ProgramVariable> programVariables = new HashMap<>();

    private final Services services;

    public Converter(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }

    private Program convertProgram(SmartMLParser.ProgramContext ctx) { // TODO: change it, we can have more than one declaration at time
        if(ctx.adtDec()!=null || ctx.exceptionDec()!=null || ctx.resourceDec()!=null || ctx.interfaceDec()!=null || ctx.contractDec()!=null  ) {
            ImmutableArray<Adt> adts = ctx.adtDec() == null ? new ImmutableArray<>()
                        : new ImmutableArray<>(
                        ctx.adtDec().stream().map(this::convertAdtDec).toList());
            ImmutableArray<Resource> resourceDecs = ctx.resourceDec() == null ? new ImmutableArray<>()
                    : new ImmutableArray<>(
                    ctx.resourceDec().stream().map(this::convertResourceDec).toList());
            ImmutableArray<ExceptionDec> exceptionDecs = ctx.exceptionDec() == null ? new ImmutableArray<>()
                    : new ImmutableArray<>(
                    ctx.exceptionDec().stream().map(this::convertExceptionDec).toList());
            ImmutableArray<Interface> interfaceDecs = ctx.interfaceDec() == null ? new ImmutableArray<>()
                    : new ImmutableArray<>(
                    ctx.interfaceDec().stream().map(this::convertInterfaceDec).toList());
            ImmutableArray<Contract> contractDecs = ctx.contractDec() == null ? new ImmutableArray<>()
                    : new ImmutableArray<>(
                    ctx.contractDec().stream().map(this::convertContractDec).toList());
            return new Program(adts,exceptionDecs,resourceDecs,interfaceDecs,contractDecs);
        }
        throw new UnsupportedOperationException("Unknown item: " + ctx.getText());
    }

    private Adt convertAdtDec(SmartMLParser.AdtDecContext ctx){
        Name name = convertIdentifier(ctx.id()).name();
        LinkedList<Field> vars = new LinkedList<>();
        LinkedList<Function> functions = new LinkedList<>();
        for (var param : ctx.adtConstr().typeParams()) {
            vars.add(new Field(convertType(param.type()), convertIdentifier(param.id())));
        }
        for (var fun : ctx.adtFunctionDec()) {
            functions.add(convertAdtFunctionDec(fun));
        }
        return new Adt(name,vars,functions);
    }

    private SmartMLType convertType(SmartMLParser.TypeContext ctx){
        return null;
    }
    private SmartMLType convertReturnType(SmartMLParser.ReturnTypeContext ctx){
        return null;
    }

    private Function convertAdtFunctionDec(SmartMLParser.AdtFunctionDecContext ctx){
        ImmutableArray<Var> vars = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        return new Function(convertIdentifier(ctx.id()).name(),vars,convertType(ctx.type()),convertAdtBlockExpr(ctx.adtblockExpr()));
    }

    private BlockExpression convertAdtBlockExpr(SmartMLParser.AdtblockExprContext ctx){
        ImmutableArray<Statement> statements = ctx.adtExpression() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.adtExpression().stream().map(this::convertAdtExpression).toList());
        return new BlockExpression(statements);
    }

    private Statement convertAdtExpression(SmartMLParser.AdtExpressionContext ctx){ // TODO: !!
        return null;
    }

    private ExceptionDec convertExceptionDec(SmartMLParser.ExceptionDecContext ctx){
        return null;
    }

    private Interface convertInterfaceDec(SmartMLParser.InterfaceDecContext ctx){
        return null;
    }

    private Resource convertResourceDec(SmartMLParser.ResourceDecContext ctx){
        return null;
    }

    private Contract convertContractDec(SmartMLParser.ContractDecContext ctx){
        Name name = convertIdentifier(ctx.contractId).name();
        ImmutableArray<Identifier> resources = ctx.resourceTypes == null ? new ImmutableArray<>()
                : new ImmutableArray<>(
                ctx.resourceTypes.stream().map(this::convertIdentifier).toList());
        ImmutableArray<Field> fields = ctx.body().field() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(
                ctx.body().field().stream().map(this::convertField).toList());
        ImmutableArray<Adt> adts = ctx.body().adtDec() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(
                ctx.body().adtDec().stream().map(this::convertAdtDec).toList());
        Constructor constructor = convertConstructor(ctx.body().constructor());
        ImmutableArray<Function> functions = ctx.body().function() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(
                ctx.body().function().stream().map(this::convertFunction).toList());
        return new Contract(name,resources,adts,fields,constructor,functions);
    }

    private Constructor convertConstructor(SmartMLParser.ConstructorContext ctx){
        ImmutableArray<Var> vars = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        ImmutableArray<Expr> exprs = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.expr().stream().map(this::convertExpr).toList());
        return new Constructor(vars,exprs);
    }

    private Function convertFunction(SmartMLParser.FunctionContext ctx){
        Name name = convertIdentifier(ctx.functionDec().id()).name();
        ImmutableArray<Var> vars = ctx.functionDec().params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.functionDec().params().param().stream().map(this::convertParam).toList());
        SmartMLType type = convertReturnType(ctx.functionDec().returnType());
        BlockExpression blockExpr = convertBlockExpr(ctx.blockExpr());
        return new Function(name,vars,type,blockExpr);
    }

    private BlockExpression convertBlockExpr(SmartMLParser.BlockExprContext ctx){ // TODO: check why we have expressions in statements
        ImmutableArray<Statement> stmts = ctx.stmts() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.stmts().stmt().stream().map(this::convertStatement).toList());
        return new BlockExpression(stmts);
    }

    private Statement convertStatement(SmartMLParser.StmtContext ctx){
        if(ctx.expr()!=null){ return convertExpr(ctx.expr()); }
        else if(ctx.blockExpr()!=null){ return convertBlockExpr(ctx.blockExpr()); }
        else if(ctx.ifStatement()!=null){ return convertIfStatement(ctx.ifStatement()); }
        else if(ctx.letExpr()!=null){ return convertLetExpr(ctx.letExpr()); }
        else if(ctx.loop()!=null){ return convertLoop(ctx.loop()); }
        else if(ctx.assert_()!=null){ return convertAssert(ctx.assert_()); }
        else if(ctx.tryStatement()!=null){ return convertTryStatement(ctx.tryStatement()); }
        else if(ctx.tryAbortStatement()!=null){ return convertTryAbortStatement(ctx.tryAbortStatement()); }
        else if(ctx.return_()!=null){ return convertReturnStatement(ctx.return_());}
        else if(ctx.funCall()!=null){ return convertFunCallStatement(ctx.funCall());}
        throw new UnsupportedOperationException("Unknown statement: " + ctx.getText());
    }

    private ReturnStatement convertReturnStatement(SmartMLParser.ReturnContext ctx){
        return new ReturnStatement(convertExpr(ctx.expr()));
    }

    private Statement convertFunCallStatement(SmartMLParser.FunCallContext ctx){
        if(ctx.internalCall()!=null) { return convertInternalCallContext(ctx.internalCall());}
        if(ctx.externalCall()!=null) { return convertExternalCallContext(ctx.externalCall());}
        if(ctx.adtCall()!=null) { return convertAdtCallContext(ctx.adtCall());}
        throw new UnsupportedOperationException("Unknown function call: " + ctx.getText());
    }

    private ExternalCall convertExternalCallContext(SmartMLParser.ExternalCallContext ctx){
        ImmutableArray<Var> params = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        Identifier funId = convertIdentifier(ctx.funName);
        Identifier calledId = convertIdentifier(ctx.idName);
        Expr expr = ctx.expr() == null ? null : convertExpr(ctx.expr());
        return new ExternalCall(funId,calledId,expr,params);
    }

    private InternalCall convertInternalCallContext(SmartMLParser.InternalCallContext ctx){
        ImmutableArray<Var> params = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        Identifier funId = convertIdentifier(ctx.funName);
        return new InternalCall(funId,params);
    }

    private AdtCall convertAdtCallContext(SmartMLParser.AdtCallContext ctx){
        ImmutableArray<Var> params = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        Identifier id = convertIdentifier(ctx.id());
        return new AdtCall(id,params);
    }

    private IfStatement convertIfStatement(SmartMLParser.IfStatementContext ctx){
        var cond = convertExpr(ctx.expr());
        var then = convertBlockExpr(ctx.blockExpr(0));
        var else_ = ctx.blockExpr().size() > 1 ? convertBlockExpr(ctx.blockExpr(1))
                : ctx.ifStatement() != null ? convertIfStatement(ctx.ifStatement())
                : null;
        return new IfStatement(cond, then, (BlockExpression) else_);    }

    private LetStatement convertLetExpr(SmartMLParser.LetExprContext ctx){
        return null;
    }

    private LoopStatement convertLoop(SmartMLParser.LoopContext ctx){
        return new LoopStatement(convertExpr(ctx.expr()),convertBlockExpr(ctx.blockExpr()));
    }

    private AssertStatement convertAssert(SmartMLParser.AssertContext ctx){
        return new AssertStatement(convertExpr(ctx.expr()));
    }

    private TryStatement convertTryStatement(SmartMLParser.TryStatementContext ctx){
        var stm = convertStatement(ctx.stmt());
        ImmutableArray<Var> vars = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        return new TryStatement(stm,vars,convertBlockExpr(ctx.blockExpr()));
    }

    private TryAbortStatement convertTryAbortStatement(SmartMLParser.TryAbortStatementContext ctx){
        return new TryAbortStatement(convertExpr(ctx.expr()),convertBlockExpr(ctx.blockExpr(0)), convertBlockExpr(ctx.blockExpr(1)));
    }

    private Expr convertExpr(SmartMLParser.ExprContext ctx){
        if (ctx instanceof SmartMLParser.LiteralExpressionContext x) { return convertLiteralExpr(x.literalExpr()); }
        if (ctx instanceof SmartMLParser.IdentifierExpressionContext x) { return new Identifier(convertIdentifier(x.id()).name()); }
        if (ctx instanceof SmartMLParser.ParenthesizedExpressionContext x) { return convertExpr(x.expr()); }
        if (ctx instanceof SmartMLParser.FieldAccessContext) { return convertFieldExpr((SmartMLParser.FieldAccessContext) ctx); }
        if (ctx instanceof SmartMLParser.ResourceExpressionContext) { return convertResourceExpr((SmartMLParser.ResourceExpressionContext) ctx); }
        if (ctx instanceof SmartMLParser.AssignmentExpressionContext x){ return convertAssignmentExpression(x); }
        if (ctx instanceof SmartMLParser.ComparisonExpressionContext x) { return convertComparisonExpression(x); }
        if (ctx instanceof SmartMLParser.UnaryExpressionContext x) { return convertUnaryExpression(x); }
        if (ctx instanceof SmartMLParser.NewValsExpressionContext x) { return convertNewValsExpression(x); }
        if (ctx instanceof SmartMLParser.VardecExpressionContext x) { return convertVardecExpression(x); }
        if (ctx instanceof SmartMLParser.ArithmeticOrLogicalExpressionContext x) { return convertArithmeticOrLogicalExpression(x); }
        throw new UnsupportedOperationException("Unknown expr: " + ctx.getText() + " class: " + ctx.getClass());
    }


    private Expr convertLiteralExpr(SmartMLParser.LiteralExprContext ctx) {
        if (ctx.KW_TRUE() != null)
            return new BooleanLiteralExpression(true);
        if (ctx.KW_FALSE() != null)
            return new BooleanLiteralExpression(false);
        var intLit = ctx.INTEGER_LITERAL();
        if (intLit != null) {
            var text = intLit.getText();
            var signed = text.contains("i");
            var split = text.split("[ui]");
            var size = split[split.length - 1];
            var suffix = IntegerLiteralExpression.IntegerSuffix.get(signed, size);
            var lit = split[0];
            var value = new BigInteger(lit);
            return new IntegerLiteralExpression(value, suffix);
        }
        throw new IllegalArgumentException("Expected boolean or integer literal");
    }

    private ResourceExpression convertResourceExpr(SmartMLParser.ResourceExpressionContext ctx){
        return new ResourceExpression(convertExpr(ctx.expr(0)),convertExpr(ctx.expr(1)));
    }

    private Expr convertArithmeticOrLogicalExpression(SmartMLParser.ArithmeticOrLogicalExpressionContext ctx) {
        ArithLogicalExpression.Operator op = null;
        if (ctx.AND() != null)
            op = ArithLogicalExpression.Operator.BitwiseAnd;
        if (ctx.OR() != null)
            op = ArithLogicalExpression.Operator.BitwiseOr;
        if (ctx.CARET() != null)
            op = ArithLogicalExpression.Operator.BitwiseXor;
        if (ctx.PLUS() != null)
            op = ArithLogicalExpression.Operator.Plus;
        if (ctx.MINUS() != null)
            op = ArithLogicalExpression.Operator.Minus;
        if (ctx.PERCENT() != null)
            op = ArithLogicalExpression.Operator.Modulo;
        if (ctx.STAR() != null)
            op = ArithLogicalExpression.Operator.Multiply;
        if (ctx.SLASH() != null)
            op = ArithLogicalExpression.Operator.Divide;
        assert op != null;
        return new ArithLogicalExpression(convertExpr(ctx.expr(0)), op,
                convertExpr(ctx.expr(1)));
    }


    private AssignmentExpression convertAssignmentExpression(SmartMLParser.AssignmentExpressionContext ctx) {
        var lhs = convertExpr(ctx.expr(0));
        var rhs = convertExpr(ctx.expr(1));
        return new AssignmentExpression(lhs, rhs);
    }

    private ComparisonExpression convertComparisonExpression(SmartMLParser.ComparisonExpressionContext ctx) {
        var left = convertExpr(ctx.expr(0));
        var right = convertExpr(ctx.expr(1));
        var opCtx = ctx.comparisonOperator();
        var op = opCtx.EQEQ() != null ? ComparisonExpression.Operator.Equal
                : opCtx.GT() != null ? ComparisonExpression.Operator.Greater
                : opCtx.LT() != null ? ComparisonExpression.Operator.Less
                : opCtx.NE() != null ? ComparisonExpression.Operator.NotEqual
                : opCtx.GE() != null
                ? ComparisonExpression.Operator.GreaterOrEqual
                : opCtx.LE() != null
                ? ComparisonExpression.Operator.LessOrEqual
                : null;
        assert op != null;
        return new ComparisonExpression(left, op, right);
    }

    private UnaryExpression convertUnaryExpression(SmartMLParser.UnaryExpressionContext ctx) {
        var base = convertExpr(ctx.expr());
        var op =
                ctx.NOT() != null ? UnaryExpression.Operator.Not : UnaryExpression.Operator.Neg;
        return new UnaryExpression(base, op);
    }

    private FieldExpression convertFieldExpr(SmartMLParser.FieldAccessContext ctx) {
        var base = convertExpr(ctx.expr());
        var ident = convertIdentifier(ctx.id());
        return new FieldExpression(base, ident);
    }

    private Field convertField(SmartMLParser.FieldContext ctx){
        return new Field(convertType(ctx.type()), convertIdentifier(ctx.id()));
    }

    private NewValExpression convertNewValsExpression(SmartMLParser.NewValsExpressionContext ctx){
        ImmutableArray<Var> params = ctx.params().param() == null ? new ImmutableArray<>()
                : new ImmutableArray<>(ctx.params().param().stream().map(this::convertParam).toList());
        return new NewValExpression(convertIdentifier(ctx.id()), params);
    }

    private Identifier convertIdentifier(SmartMLParser.IdContext ctx) {
        return new Identifier(new Name(ctx.getText()));
    }

    private Var convertParam(SmartMLParser.ParamContext ctx){
        return new Var(convertType(ctx.type()), convertIdentifier(ctx.id()));
    }

    private Var convertVardecExpression(SmartMLParser.VardecExpressionContext ctx){
        return new Var(convertType(ctx.type()), convertIdentifier(ctx.id()));
    }



/*
    private Function convertFunction(SmartMLParser.FunctionDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        ImmutableList<Parameter> params = convertParameters(ctx.parameterList());
        Type returnType = convertType(ctx.type());
        Block body = convertBlock(ctx.block());
        return new Function(name, params, returnType, body);
    }

    private ImmutableList<Parameter> convertParameters(SmartMLParser.ParameterListContext ctx) {
        if (ctx == null) return new ImmutableList<>();
        return ctx.parameter().stream().map(this::convertParameter).collect(ImmutableList.collector());
    }

    private Parameter convertParameter(SmartMLParser.ParameterContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Type type = convertType(ctx.type());
        return new Parameter(name, type);
    }

    private Type convertType(SmartMLParser.TypeContext ctx) {
        if (ctx.BOOL() != null) return PrimitiveType.BOOL;
        if (ctx.INT() != null) return PrimitiveType.INT;
        if (ctx.STRING() != null) return PrimitiveType.STRING;
        throw new IllegalArgumentException("Unknown type: " + ctx.getText());
    }

    private Block convertBlock(SmartMLParser.BlockContext ctx) {
        ImmutableList<Statement> statements = ctx.statement().stream().map(this::convertStatement).collect(ImmutableList.collector());
        return new Block(statements);
    }

    private Statement convertStatement(SmartMLParser.StatementContext ctx) {
        if (ctx.variableDecl() != null) return convertVariableDecl(ctx.variableDecl());
        if (ctx.expr() != null) return new ExpressionStatement(convertExpr(ctx.expr()));
        throw new UnsupportedOperationException("Unknown statement: " + ctx.getText());
    }

    private VariableDeclaration convertVariableDecl(SmartMLParser.VariableDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Type type = convertType(ctx.type());
        Expression initializer = convertExpr(ctx.expr());
        VariableDeclaration decl = new VariableDeclaration(name, type, initializer);
        variables.put(name, decl);
        return decl;
    }

    private Expression convertExpr(SmartMLParser.ExprContext ctx) {
        if (ctx.literal() != null) return convertLiteral(ctx.literal());
        if (ctx.IDENTIFIER() != null) return new VariableExpression(ctx.IDENTIFIER().getText());
        if (ctx.binaryOp() != null) return convertBinaryOp(ctx);
        throw new UnsupportedOperationException("Unknown expression: " + ctx.getText());
    }

    private LiteralExpression convertLiteral(SmartMLParser.LiteralContext ctx) {
        if (ctx.BOOL_LITERAL() != null)
            return new BooleanLiteralExpression(Boolean.parseBoolean(ctx.BOOL_LITERAL().getText()));
        if (ctx.INT_LITERAL() != null)
            return new IntegerLiteralExpression(Integer.parseInt(ctx.INT_LITERAL().getText()));
        if (ctx.STRING_LITERAL() != null) return new StringLiteralExpression(ctx.STRING_LITERAL().getText());
        throw new IllegalArgumentException("Unknown literal: " + ctx.getText());
    }

    private BinaryExpression convertBinaryOp(SmartMLParser.ExprContext ctx) {
        Expression left = convertExpr(ctx.expr(0));
        Expression right = convertExpr(ctx.expr(1));
        String operator = ctx.binaryOp().getText();
        return new BinaryExpression(left, operator, right);
    }*/
}

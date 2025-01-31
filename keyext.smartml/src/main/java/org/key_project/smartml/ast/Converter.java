/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast;

import java.util.*;

import org.key_project.logic.Name;
import org.key_project.rusty.parsing.RustyParser;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.abstraction.Type;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.fn.Constructor;
import org.key_project.smartml.ast.fn.Function;
import org.key_project.smartml.ast.stmt.ExceptionDec;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.ty.SmartMLType;
import org.key_project.smartml.logic.op.ProgramVariable;
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
        return null;
    }

    private Expr convertExpr(SmartMLParser.ExprContext ctx){
        return null;
    }

    private Field convertField(SmartMLParser.FieldContext ctx){
        return new Field(convertType(ctx.type()), convertIdentifier(ctx.id()));
    }

    private Identifier convertIdentifier(SmartMLParser.IdContext ctx) {
        return new Identifier(new Name(ctx.getText()));
    }

    private Var convertParam(SmartMLParser.ParamContext ctx){
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

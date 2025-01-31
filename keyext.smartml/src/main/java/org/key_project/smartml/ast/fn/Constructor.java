package org.key_project.smartml.ast.fn;

import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.ty.SmartMLType;
import org.key_project.smartml.ast.visitor.Visitor;

public record Constructor(org.key_project.util.collection.ImmutableArray<Var> params, org.key_project.util.collection.ImmutableArray<Expr> body) implements Statement {
    @Override
    public SyntaxElement getChild(int n) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void visit(Visitor v) {

    }
}

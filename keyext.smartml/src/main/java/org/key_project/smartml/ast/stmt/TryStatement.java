package org.key_project.smartml.ast.stmt;

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.visitor.Visitor;

public record TryStatement(Statement stm, org.key_project.util.collection.ImmutableArray<Var> params, BlockExpression blockExpr) implements Statement {
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

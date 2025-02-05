package org.key_project.smartml.ast.stmt;

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record TryAbortStatement(Expr expr, BlockExpression abortBlock, BlockExpression successBlock ) implements Statement{
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

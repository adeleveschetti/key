package org.key_project.smartml.ast.stmt;

import org.jspecify.annotations.Nullable;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.ElseBranch;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.expr.ThenBranch;
import org.key_project.smartml.ast.visitor.Visitor;

public record LetStatement(org.key_project.util.collection.ImmutableArray<Expr> exprs, org.key_project.util.collection.ImmutableArray<Var> vars, Statement stm) implements Statement {
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

package org.key_project.smartml.ast.stmt;

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record ExternalCall(Identifier funName, Identifier calledName, Expr resource, org.key_project.util.collection.ImmutableArray<Var> params) implements Statement{
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

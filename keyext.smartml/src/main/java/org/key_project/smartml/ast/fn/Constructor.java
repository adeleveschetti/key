package org.key_project.smartml.ast.fn;

import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.ty.SmartMLType;
import org.key_project.smartml.ast.visitor.Visitor;

public record Constructor(Name name, org.key_project.util.collection.ImmutableArray<FunctionParam> params, SmartMLType returnType,
                       BlockExpression body) implements Statement {
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

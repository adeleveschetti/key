package org.key_project.smartml.ast;

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.visitor.Visitor;

public record Resource(String name, org.key_project.util.collection.ImmutableArray<FunctionParam> params) implements Statement {
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

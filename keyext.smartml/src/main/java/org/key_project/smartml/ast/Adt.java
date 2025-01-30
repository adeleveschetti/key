package org.key_project.smartml.ast;

import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.fn.Constructor;
import org.key_project.smartml.ast.fn.Function;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.visitor.Visitor;

import java.util.LinkedList;

public record Adt(Name name, LinkedList<Field> fields, LinkedList<Function> functions) implements Statement {
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

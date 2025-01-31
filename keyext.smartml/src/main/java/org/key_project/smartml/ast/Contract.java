package org.key_project.smartml.ast;

import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.fn.Constructor;
import org.key_project.smartml.ast.fn.Function;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.visitor.Visitor;

public record Contract(Name name, org.key_project.util.collection.ImmutableArray<Identifier> resources, org.key_project.util.collection.ImmutableArray<Adt> adts, org.key_project.util.collection.ImmutableArray<Field> fields, Constructor constructor, org.key_project.util.collection.ImmutableArray<Function> funs) implements SmartMLProgramElement {
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

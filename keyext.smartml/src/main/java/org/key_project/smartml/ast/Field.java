package org.key_project.smartml.ast;

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.VariableDeclaration;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.ast.abstraction.Type;
import org.key_project.smartml.ast.ty.SmartMLType;

public record Field(SmartMLType type, Identifier id) implements SmartMLProgramElement, VariableDeclaration {


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

package org.key_project.smartml.ast.expr;

import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.ast.visitor.Visitor;

public interface Expr extends SmartMLProgramElement {
    void visit(Visitor v);
}

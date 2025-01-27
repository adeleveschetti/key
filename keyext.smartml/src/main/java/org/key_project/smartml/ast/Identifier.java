/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.Named;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.ast.visitor.Visitor;

public record Identifier(Name name) implements Named, SmartMLProgramElement {

    @Override
    public @NonNull Name name() {
        return name;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("Identifier has no children");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String toString() {
        return name().toString();
    }

    @Override
    public void visit(Visitor v) {
        throw new RuntimeException("Should never be called");
    }
}

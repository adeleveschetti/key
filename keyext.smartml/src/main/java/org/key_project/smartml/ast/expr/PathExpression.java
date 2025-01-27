/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record PathExpression(Identifier var) implements Expr {
    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0) {
            return var;
        }
        throw new IndexOutOfBoundsException("PathExpression has only one child");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public String toString() {
        return var.toString();
    }

    @Override
    public void visit(Visitor v) {
        throw new RuntimeException("Shouldn't be called");
    }
}

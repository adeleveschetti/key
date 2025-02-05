/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record FieldExpression(org.key_project.smartml.ast.expr.Expr base, Identifier field) implements Expr {
    @Override
    public void visit(Visitor v) {
        v.performActionOnFieldExpression(this);
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0) {
            return base;
        }
        if (n == 1) {
            return field;
        }
        throw new IndexOutOfBoundsException("FieldExpression has less than " + n + " children");
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public String toString() {
        return base + "."  + field;
    }
}

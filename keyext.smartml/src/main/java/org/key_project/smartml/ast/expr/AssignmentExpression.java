/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public class AssignmentExpression implements org.key_project.smartml.ast.expr.Expr {
    private final org.key_project.smartml.ast.expr.Expr lhs;
    private final org.key_project.smartml.ast.expr.Expr rhs;
    private int hashCode = -1;

    public AssignmentExpression(org.key_project.smartml.ast.expr.Expr lhs, org.key_project.smartml.ast.expr.Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }


    @Override
    public @NonNull SyntaxElement getChild(int n) {
        return switch (n) {
        case 0 -> lhs;
        case 1 -> rhs;
        default -> throw new IndexOutOfBoundsException(
            "AssignmentExpression has only two children");
        };
    }

    public org.key_project.smartml.ast.expr.Expr getLhs() {
        return lhs;
    }

    public Expr getRhs() {
        return rhs;
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public String toString() {
        return lhs + " = " + rhs;
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnAssignmentExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AssignmentExpression that = (AssignmentExpression) o;
        return lhs.equals(that.lhs) && rhs.equals(that.rhs);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            return hashCode;
        }
        final int hash = computeHashCode();
        this.hashCode = hash;
        return hash;
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public class NegationExpression implements org.key_project.smartml.ast.expr.Expr {
    public enum Operator {
        Neg, Not;

        @Override
        public String toString() {
            return switch (this) {
            case Neg -> "!";
            case Not -> "~";
            };
        }
    }

    private final org.key_project.smartml.ast.expr.Expr expr;
    private final Operator op;

    public NegationExpression(org.key_project.smartml.ast.expr.Expr expr, Operator op) {
        this.expr = expr;
        this.op = op;
    }

    public Operator getOp() {
        return op;
    }

    public Expr getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return op.toString() + expr;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0) {
            return expr;
        }
        throw new IndexOutOfBoundsException(
            "NegationExpression has only 1 children");
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnNegationExpression(this);
    }
}

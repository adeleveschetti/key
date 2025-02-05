/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record ComparisonExpression(org.key_project.smartml.ast.expr.Expr left, Operator op, org.key_project.smartml.ast.expr.Expr right) implements Expr {
    @Override
    public void visit(Visitor v) {
        v.performActionOnComparisonExpression(this);
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        return switch (n) {
            case 0 -> left;
            case 1 -> right;
            default -> throw new IndexOutOfBoundsException(
                    "ComparisonExpression has only 2 children");
        };
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public String toString() {
        return left + " " + op + " " + right;
    }

    public enum Operator {
        Equal, NotEqual, Greater, Less, GreaterOrEqual, LessOrEqual;

        @Override
        public String toString() {
            return switch (this) {
                case Equal -> "==";
                case NotEqual -> "!=";
                case Greater -> ">";
                case Less -> "<";
                case GreaterOrEqual -> ">=";
                case LessOrEqual -> "<=";
            };
        }
    }}

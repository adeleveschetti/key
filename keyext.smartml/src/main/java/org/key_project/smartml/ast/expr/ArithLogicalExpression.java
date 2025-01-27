/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.visitor.Visitor;

public record ArithLogicalExpression(org.key_project.smartml.ast.expr.Expr left, ArithLogicalExpression.Operator op,
                                     org.key_project.smartml.ast.expr.Expr right)implements Expr {

    @Override
    public void visit(org.key_project.rusty.ast.visitor.Visitor v) {

    }

    public enum Operator {
    Plus, Minus, Multiply, Divide, Modulo, BitwiseAnd, BitwiseOr, BitwiseXor, Shl, Shr;

    @Override
    public String toString() {
        return switch (this) {
        case Plus -> "+";
        case Minus -> "-";
        case Multiply -> "*";
        case Divide -> "/";
        case Modulo -> "%";
        case BitwiseAnd -> "&";
        case BitwiseOr -> "|";
        case BitwiseXor -> "^";
        case Shl -> "<<";
        case Shr -> ">>";
        };
    }

    }

    @Override
    public String toString() {
        return left.toString() + " " + op + " " + right.toString();
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        return switch (n) {
        case 0 -> left;
        case 1 -> right;
        default -> throw new IndexOutOfBoundsException(
            "ArithLogicalExpression has only 2 children");
        };
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnArithLogicalExpression(this);
    }
}

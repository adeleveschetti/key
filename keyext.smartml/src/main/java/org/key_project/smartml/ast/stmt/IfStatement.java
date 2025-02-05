/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.stmt;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.ElseBranch;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.expr.ThenBranch;
import org.key_project.smartml.ast.visitor.Visitor;

public record IfStatement(Expr condition, BlockExpression thenExpr, @Nullable BlockExpression elseExpr) implements Statement {
    @Override
    public void visit(Visitor v) {
        v.performActionOnIfStatement(this);
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0) return condition;
        if (n == 1) return thenExpr;
        if (n == 2 && elseExpr != null) return elseExpr;
        throw new IndexOutOfBoundsException("IfExpression has less than " + n + " children");
    }

    @Override
    public int getChildCount() {
        return 2 + (elseExpr == null ? 0 : 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("if ").append(condition).append(thenExpr);
        if (elseExpr != null) sb.append(" else ").append(elseExpr);
        return sb.toString();
    }
}

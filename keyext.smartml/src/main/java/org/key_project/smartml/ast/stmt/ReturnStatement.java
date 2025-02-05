/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.stmt;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record ReturnStatement(@Nullable Expr expr) implements Statement {

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0 && expr != null) {return expr;}
        throw new IndexOutOfBoundsException("ReturnStatement has only " + getChildCount() + " children");
    }

    @Override
    public int getChildCount() {
        int c =0;
        if (expr != null) {++c;}
        return c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("return");
        if (expr != null) {
            sb.append(" ").append(expr);
        }
        return sb.toString();
    }

    @Override
    public void visit(Visitor v) {

    }
}

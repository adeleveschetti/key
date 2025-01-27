/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.ldt.BoolLDT;

public class BooleanLiteralExpression extends LiteralExpression {
    private final boolean value;

    public BooleanLiteralExpression(boolean value) {
        this.value = value;
    }


    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("BooleanLiteralExpression has no children");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String toString() {
        return "" + value;
    }


    @Override
    public Name getLDTName() {
        return BoolLDT.NAME;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnBooleanLiteralExpression(this);
    }

    @Override
    public int hashCode() {
        return value ? 29 : 37;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj.getClass() != this.getClass())
            return false;
        BooleanLiteralExpression other = (BooleanLiteralExpression) obj;
        return value == other.value;
    }
}

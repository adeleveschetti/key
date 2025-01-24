/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.ty;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.abstraction.PrimitiveType;
import org.key_project.smartml.ast.visitor.Visitor;

public record PrimitiveSmartMLType(PrimitiveType type) implements SmartMLType {
    //@Override
   // public void visit(Visitor v) {

    //    v.performActionOnPrimitiveSmartMLType(this);
    //}

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("PrimitiveSmartMLType has no children");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.ty;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.abstraction.KeYSmartMLType;
import org.key_project.smartml.ast.abstraction.Type;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.logic.op.sv.ProgramSV;

import java.util.Objects;

/**
 * Only in SchemaRust
 */
public final class TypeOf implements SmartMLType {
    private final ProgramSV sv;
    private final KeYSmartMLType type;

    /**
     *
     */
    public TypeOf(ProgramSV sv) {
        this.sv = sv;
        this.type = new KeYSmartMLType(sv.sort());
    }


    @Override
    public Type type() {
        return type;
    }

    @Override
    public void visit(Visitor v) {

    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0)
            return sv;
        throw new IndexOutOfBoundsException(getClass() + " has 1 child");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public ProgramSV sv() {
        return sv;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (TypeOf) obj;
        return Objects.equals(this.sv, that.sv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sv);
    }

    @Override
    public String toString() {
        return "typeOf(" +
            sv + ')';
    }

}

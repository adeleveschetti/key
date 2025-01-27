/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic.op;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.logic.op.AbstractSortedOperator;
import org.key_project.logic.op.Modifier;
import org.key_project.logic.op.UpdateableOperator;
import org.key_project.logic.sort.Sort;
import org.key_project.smartml.ast.SourceData;
import org.key_project.smartml.ast.abstraction.KeYSmartMLType;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.rule.MatchConditions;

public class ProgramVariable extends AbstractSortedOperator implements Expr, UpdateableOperator {
    private final KeYSmartMLType type;

    public ProgramVariable(Name name, Sort s, KeYSmartMLType type) {
        super(name, s, Modifier.NONE);
        this.type = type;
    }

    public ProgramVariable(Name name, KeYSmartMLType type) {
        this(name, type.getSort(), type);
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("Program variable does not have a child");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    public KeYSmartMLType getKeYSmartMLType() {
        return type;
    }


    @Override
    public MatchConditions match(SourceData source, MatchConditions mc) {
        final var src = source.getSource();
        source.next();
        if (src == this) {
            return mc;
        } else {
            return null;
        }
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnProgramVariable(this);
    }
}

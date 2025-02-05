package org.key_project.smartml.ast.stmt;

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */

import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Identifier;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.visitor.Visitor;

public record InternalCall(Identifier funName, org.key_project.util.collection.ImmutableArray<Var> params) implements Statement{
    @Override
    public SyntaxElement getChild(int n) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void visit(Visitor v) {

    }
}



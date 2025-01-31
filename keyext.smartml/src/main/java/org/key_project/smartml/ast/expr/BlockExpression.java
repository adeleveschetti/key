/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.ElseBranch;
import org.key_project.smartml.ast.ProgramPrefixUtil;
import org.key_project.smartml.ast.Var;
import org.key_project.smartml.logic.ProgramPrefix;
import org.key_project.smartml.ast.expr.Expr;
import org.key_project.smartml.ast.expr.ThenBranch;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.logic.PosInProgram;
import org.key_project.smartml.logic.ProgramPrefix;
import org.key_project.util.ExtList;
import org.key_project.util.collection.ImmutableArray;
import org.key_project.util.collection.ImmutableList;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public record BlockExpression(org.key_project.util.collection.ImmutableArray<Statement> statements) implements Statement {

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

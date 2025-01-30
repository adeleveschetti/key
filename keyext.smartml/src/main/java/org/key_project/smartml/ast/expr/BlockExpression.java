/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.ElseBranch;
import org.key_project.smartml.ast.ProgramPrefixUtil;
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

public class BlockExpression implements org.key_project.smartml.ast.expr.Expr, ProgramPrefix, ThenBranch, ElseBranch {
    protected final ImmutableList<Statement> statements;
    protected final org.key_project.smartml.ast.expr.Expr value;
    private final int prefixLength;

    private int hashCode = -1;

    public BlockExpression(ImmutableList<Statement> statements, org.key_project.smartml.ast.expr.Expr value) {
        this.statements = statements;
        this.value = value;
        ProgramPrefixUtil.ProgramPrefixInfo info = ProgramPrefixUtil.computeEssentials(this);
        prefixLength = info.getLength();
    }

    public BlockExpression(ExtList children) {
        statements = ImmutableList.of(children.collect(Statement.class));
        value = children.get(org.key_project.smartml.ast.expr.Expr.class);
        ProgramPrefixUtil.ProgramPrefixInfo info = ProgramPrefixUtil.computeEssentials(this);
        prefixLength = info.getLength();
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (0 <= n && n < statements.size())
            return Objects.requireNonNull(statements.get(n));
        if (n == statements.size())
            return value;
        throw new IndexOutOfBoundsException("BlockExpression has less than " + n + " children");
    }

    @Override
    public int getChildCount() {
        return statements.size() + 1;
    }

    public ImmutableList<Statement> getStatements() {
        return statements;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{\n"
            + statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n"))
            + "\n\t" + value.toString()
            + "\n}";
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnBlockExpression(this);
    }


    @Override
    public boolean hasNextPrefixElement() {
        return getChildCount() != 0 && getChild(0) instanceof ProgramPrefix;
    }

    @Override
    public ProgramPrefix getNextPrefixElement() {
        if (hasNextPrefixElement()) {
            return (ProgramPrefix) getChild(0);
        }
        throw new IndexOutOfBoundsException("No next prefix element " + this);
    }

    @Override
    public ProgramPrefix getLastPrefixElement() {
        return hasNextPrefixElement() ? getNextPrefixElement().getLastPrefixElement() : this;
    }

    @Override
    public ImmutableArray<ProgramPrefix> getPrefixElements() {
        return computePrefixElements(this);
    }

    @Override
    public PosInProgram getFirstActiveChildPos() {
        return PosInProgram.ZERO;
    }

    @Override
    public int getPrefixLength() {
        return prefixLength;
    }

    /** computes the prefix elements for the given array of statment block */
    public static ImmutableArray<ProgramPrefix> computePrefixElements(
            ProgramPrefix current) {
        final ArrayList<ProgramPrefix> prefix = new ArrayList<>();
        prefix.add(current);

        while (current.hasNextPrefixElement()) {
            current = current.getNextPrefixElement();
            prefix.add(current);
        }

        return new ImmutableArray<>(prefix);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            return hashCode;
        }
        final int hash = computeHashCode();
        this.hashCode = hash;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BlockExpression that = (BlockExpression) o;
        return prefixLength == that.prefixLength && Objects.equals(statements, that.statements)
                && Objects.equals(value, that.value);
    }
}

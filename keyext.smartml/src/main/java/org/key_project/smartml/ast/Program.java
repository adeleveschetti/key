/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.stmt.ExceptionDec;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.util.collection.ImmutableArray;
import org.key_project.util.collection.ImmutableList;

import java.util.Objects;
import java.util.stream.Collectors;

public class Program implements SmartMLProgramElement {
    private final ImmutableArray<org.key_project.smartml.ast.Adt> adts;
    private final ImmutableArray<ExceptionDec> exceptions;
    private final ImmutableArray<Resource> resources;
    private final ImmutableArray<org.key_project.smartml.ast.Interface> interfaces;
    private final ImmutableArray<org.key_project.smartml.ast.Contract> contracts;

    public Program(org.key_project.util.collection.ImmutableArray<Adt> adts, org.key_project.util.collection.ImmutableArray<ExceptionDec> exceptions, org.key_project.util.collection.ImmutableArray<Resource> resources, org.key_project.util.collection.ImmutableArray<Interface> interfaces, org.key_project.util.collection.ImmutableArray<Contract> contracts) {
        this.adts = adts;
        this.exceptions = exceptions;
        this.resources = resources;
        this.interfaces = interfaces;
        this.contracts = contracts;
    }

    @Override
    public int getChildCount() {
        /*return items.size();*/
        return 0;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        //return Objects.requireNonNull(items.get(n));
        return null;
    }

    @Override
    public String toString() {
        //return items.map(Item::toString).stream().collect(Collectors.joining("\n"));
        return "";
    }

    @Override
    public void visit(Visitor v) {
        throw new RuntimeException("Shouldn't be called");
    }
}

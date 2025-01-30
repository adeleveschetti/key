/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast;


import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.stmt.ExceptionDec;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.visitor.Visitor;

public record Item(org.key_project.util.collection.ImmutableArray<Adt> adts, org.key_project.util.collection.ImmutableArray<ExceptionDec> exceptionDecs, org.key_project.util.collection.ImmutableArray<Resource> resourceDecs, org.key_project.util.collection.ImmutableArray<Interface> interfaceDecs, org.key_project.util.collection.ImmutableArray<Contract> contractDecs) implements Statement {
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

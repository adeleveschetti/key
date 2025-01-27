/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Program;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.SmartMLProgramElement;

public record SmartMLBlock(SmartMLProgramElement program) implements Program {
    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (n == 0)
            return program;
        throw new IndexOutOfBoundsException("SmartMLBlock " + this + " has only one child");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return program.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SmartMLBlock block)) {
            return false;
        } else {

            if (block.program() == null) {
                return program() == null;
            } else {
                return block.program().equals(program());
            }
        }
    }

    /** returns the hashCode */
    @Override
    public int hashCode() {
        return 17 + ((program() == null) ? 0 : program().hashCode());
    }
}

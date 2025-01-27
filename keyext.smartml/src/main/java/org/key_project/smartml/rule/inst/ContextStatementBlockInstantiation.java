/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.inst;

import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.logic.PosInProgram;

/**
 * this class is created if the scheme given by a context term has matched to a Rust program. The
 * ContextBlockExpressionInstantiation class stores the instantiations of the prefix and the suffix.
 */
public class ContextStatementBlockInstantiation {
    /** the end position of the prefix omega */
    private final PosInProgram prefixEnd;

    /** the start position of the suffix omega */
    private final PosInProgram suffixStart;

    /** the whole program element this context term inst refers to */
    private final SmartMLProgramElement programElement;

    /**
     * creates a ContextStatementBlockInstantiation of a context term
     *
     * @param prefixEnd the PosInProgram describing the first statement after the end of the prefix
     * @param suffixStart the PosInProgram describing the statement just before the suffix begins
     * @param pe the ProgramElement the context positions are related to
     */
    public ContextStatementBlockInstantiation(PosInProgram prefixEnd, PosInProgram suffixStart,
                                               SmartMLProgramElement pe) {

        this.prefixEnd = prefixEnd;
        this.suffixStart = suffixStart;
        this.programElement = pe;
    }

    /**
     * returns the end position of the prefix
     *
     * @return the end position of the prefix
     */
    public PosInProgram prefix() {
        return prefixEnd;
    }

    /**
     * returns the PosInProgram describing the statement just before the suffix begins
     */
    public PosInProgram suffix() {
        return suffixStart;
    }

    /**
     * returns the program element this context term instantiation refers to
     *
     * @return returns the program element this context term instantiation refers to
     */
    public SmartMLProgramElement programElement() {
        return programElement;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ContextStatementBlockInstantiation inst)) {
            return false;
        }

        if (isDifferent(suffixStart, inst.suffixStart)) {
            return false;
        }

        if (isDifferent(prefixEnd, inst.prefixEnd)) {
            return false;
        }

        return !isDifferent(programElement, inst.programElement);

    }

    private boolean isDifferent(Object self, Object other) {
        if (self != null && other != null) {
            return !self.equals(other);
        } else {
            return self != other;
        }
    }

    public int hashCode() {
        int hashCode = 1;
        if (prefixEnd != null) {
            hashCode = 17 * prefixEnd.hashCode();
        }
        if (suffixStart != null) {
            hashCode += 17 * suffixStart.hashCode();
        }
        if (programElement != null) {
            hashCode += 17 * programElement.hashCode();
        }
        return hashCode;
    }

    /** toString */
    public String toString() {
        String result = "ContextStatementBlockInstantiation:\n";
        result += "Prefix ends before " + prefixEnd.toString();
        result += "\nSuffix starts after " + suffixStart.toString();
        result += "\nRefers to Program " + programElement;
        return result + "\n";
    }
}

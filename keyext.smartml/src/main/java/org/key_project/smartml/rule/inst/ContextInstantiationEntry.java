/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.inst;

import org.key_project.prover.rules.instantiation.InstantiationEntry;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.logic.PosInProgram;
import org.key_project.smartml.rule.inst.ContextStatementBlockInstantiation;

/**
 * This class is used to store the information about a matched context of a dl formula. (the pi and
 * omega part) TODO: Check if there is a need for ContextStatementBlockInstantiation or if it could
 * be unified with this class
 */
public class ContextInstantiationEntry
        extends InstantiationEntry<ContextStatementBlockInstantiation> {

    /**
     * creates a new ContextInstantiationEntry
     *
     * @param pi the PosInProgram describing the position of the first statement after the prefix
     * @param omega the PosInProgram describing the position of the statement just before the suffix
     *        starts
     * @param pe the ProgramElement the context positions are related to
     */
    ContextInstantiationEntry(PosInProgram pi, PosInProgram omega,
                              SmartMLProgramElement pe) {
        super(new ContextStatementBlockInstantiation(pi, omega, pe));
    }

    /**
     * returns the position of the first statement after the prefix
     *
     * @return the position of the first statement after the prefix
     */
    public PosInProgram prefix() {
        return getInstantiation().prefix();
    }


    /**
     * returns the position of the statement just before the suffix starts
     *
     * @return the position of the statement just before the suffix starts
     */
    public PosInProgram suffix() {
        return getInstantiation().suffix();
    }

    /**
     * returns the context program with an ignorable part between prefix and suffix position
     */
    public SmartMLProgramElement contextProgram() {
        return getInstantiation().programElement();
    }

    /** toString */
    public String toString() {
        return "[\npi:" + prefix() + "\nomega:" + suffix() + "\n]";
    }
}

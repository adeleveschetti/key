/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.key_project.logic.LogicServices;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.Services;
import org.key_project.smartml.logic.op.sv.SchemaVariable;
import org.key_project.smartml.rule.MatchConditions;

/**
 * The instantiations of a schemavariable can be restricted on rule scope by attaching conditions on
 * these variables. Such a condition is realized by a class which implements this interface.
 * <br>
 * The usual place where to put these implementations is inside package
 * <code>de.uka.ilkd.key.rule.conditions</code>. For variable conditions that know only black and
 * white answers there exists a convenience class
 * .
 */
public interface VariableCondition extends org.key_project.prover.rules.VariableCondition {
    /**
     * checks if the condition for a correct instantiation is fulfilled
     *
     * @param var the SchemaVariable to be instantiated
     * @param instCandidate the SVSubstitute (e.g. Term, ProgramElement) to be mapped to var
     * @param matchCond the MatchCondition with the current matching state and in particular the
     *        SVInstantiations that are already known to be needed
     * @param services the program information object
     * @return modified match results if the condition can be satisfied, or <code>null</code>
     *         otherwise
     */
    org.key_project.smartml.rule.MatchConditions check(SchemaVariable var, SyntaxElement instCandidate,
                                                     org.key_project.smartml.rule.MatchConditions matchCond,
                                                     Services services);

    @Override
    default org.key_project.prover.rules.instantiation.MatchConditions check(
            org.key_project.logic.op.sv.SchemaVariable var, SyntaxElement instCandidate,
            org.key_project.prover.rules.instantiation.MatchConditions matchCond,
            LogicServices services) {
        return check((SchemaVariable) var, instCandidate, (MatchConditions) matchCond,
            (Services) services);
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.match.instructions;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.LogicServices;
import org.key_project.logic.Term;
import org.key_project.logic.op.sv.OperatorSV;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.prover.rules.instantiation.IllegalInstantiationException;
import org.key_project.prover.rules.instantiation.MatchConditions;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.rule.inst.SVInstantiations;
import org.key_project.smartml.rule.match.instructions.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.key_project.smartml.logic.equality.RenamingTermProperty.RENAMING_TERM_PROPERTY;

public abstract class MatchSchemaVariableInstruction<SV extends @NonNull OperatorSV>
        extends Instruction<SV> {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(MatchSchemaVariableInstruction.class);

    protected MatchSchemaVariableInstruction(SV op) {
        super(op);
    }

    /**
     * Tries to add the pair <tt>(this,term)</tt> to the match conditions. If successful the
     * resulting conditions are returned, otherwise null. Failure is possible e.g. if this
     * schemavariable has been already matched to a term <tt>t2</tt> which is not unifiable with the
     * given term.
     */
    protected final MatchConditions addInstantiation(Term term,
            MatchConditions matchCond,
            LogicServices services) {
        if (op.isRigid() && !term.isRigid()) {
            return null;
        }

        final SVInstantiations inst = (SVInstantiations) matchCond.getInstantiations();

        final Term t = inst.getTermInstantiation(op, services);
        if (t != null) {
            if (!RENAMING_TERM_PROPERTY.equalsModThisProperty(t, term)) {
                return null;
            } else {
                return matchCond;
            }
        }

        try {
            return matchCond.setInstantiations(inst.add(op, term, services));
        } catch (IllegalInstantiationException e) {
            return null;
        }
    }

    /**
     * tries to match the schema variable of this instruction with the specified
     * {@link SmartMLProgramElement} {@code instantiationCandidate} w.r.t. the given constraints by
     * {@link MatchConditions}
     *
     * @param instantiationCandidate the {@link SmartMLProgramElement} to be matched
     * @param mc the {@link MatchConditions} with additional constraints (e.g. previous matches of
     *        this instructions {@link SchemaVariable})
     * @param services the {@link Services}
     * @return {@code null} if no matches have been found or the new {@link MatchConditions} with
     *         the pair ({@link SchemaVariable}, {@link SmartMLProgramElement}) added
     */
    public MatchConditions match(SmartMLProgramElement instantiationCandidate,
            MatchConditions mc,
            LogicServices services) {
        return null;
    }
}

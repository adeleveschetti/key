/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.match.instructions;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.LogicServices;
import org.key_project.logic.Term;
import org.key_project.logic.op.Operator;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.prover.rules.instantiation.MatchConditions;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.logic.op.ElementaryUpdate;
import org.key_project.smartml.logic.op.Modality;
import org.key_project.smartml.logic.op.sv.*;
import org.key_project.smartml.rule.match.instructions.MatchInstruction;
import org.key_project.smartml.rule.match.instructions.MatchProgramSVInstruction;
import org.key_project.smartml.rule.match.instructions.MatchSchemaVariableInstruction;
import org.key_project.smartml.rule.match.instructions.*;

public abstract class Instruction<Op extends Operator> implements org.key_project.smartml.rule.match.instructions.MatchInstruction {
    public static Instruction<@NonNull Operator> matchOp(Operator op) {
        return new MatchOpIdentityInstruction<>(op);
    }

    // public static Instruction<SortDependingFunction> matchSortDependingFunction(
    // SortDependingFunction op) {
    // return new MatchSortDependingFunctionInstruction(op);
    // }

    public static MatchModalOperatorSVInstruction matchModalOperatorSV(
            ModalOperatorSV sv) {
        return new MatchModalOperatorSVInstruction(sv);
    }

    public static MatchModalityInstruction matchModalOperator(Modality mod) {
        return new MatchModalityInstruction(mod);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchSchemaVariableInstruction<? extends @NonNull SchemaVariable> matchFormulaSV(
            FormulaSV sv) {
        return new MatchFormulaSVInstruction(sv);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchSchemaVariableInstruction<? extends @NonNull SchemaVariable> matchTermSV(
            TermSV sv) {
        return new MatchTermSVInstruction(sv);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchSchemaVariableInstruction<? extends @NonNull SchemaVariable> matchVariableSV(
            VariableSV sv) {
        return new MatchVariableSVInstruction(sv);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchSchemaVariableInstruction<? extends @NonNull SchemaVariable> matchProgramSV(
            ProgramSV sv) {
        return new MatchProgramSVInstruction(sv);
    }

    public static MatchSchemaVariableInstruction<? extends @NonNull SchemaVariable> matchUpdateSV(
            UpdateSV sv) {
        return new MatchUpdateSVInstruction(sv);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchInstruction matchProgram(SmartMLProgramElement prg) {
        return new MatchProgramInstruction(prg);
    }

    public static org.key_project.smartml.rule.match.instructions.MatchInstruction matchAndBindVariable(
            QuantifiableVariable var) {
        return BindVariablesInstruction.create(var);
    }

    public static MatchInstruction matchElementaryUpdate(ElementaryUpdate elementaryUpdate) {
        return new MatchElementaryUpdateInstruction(elementaryUpdate);
    }

    protected final Op op;

    protected Instruction(Op op) {
        this.op = op;
    }

    /**
     * tries to match the schema variable of this instruction with the specified {@link Term}
     * {@code instantiationCandidate} w.r.t. the given constraints by {@link MatchConditions}
     *
     * @param instantiationCandidate the {@link Term} to be matched
     * @param matchCond the {@link MatchConditions} with additional constraints (e.g. previous
     *        matches of this schemavariable)
     * @param services the {@link Services}
     * @return {@code null} if no matches have been found or the new {@link MatchConditions} with
     *         the pair {@code (sv, instantiationCandidate)} added
     */
    public abstract MatchConditions match(Term instantiationCandidate,
            MatchConditions matchCond,
            LogicServices services);
}

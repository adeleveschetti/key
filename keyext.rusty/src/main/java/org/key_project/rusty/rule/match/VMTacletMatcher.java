/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.rule.match;

import java.util.HashMap;

import org.key_project.logic.LogicServices;
import org.key_project.logic.SyntaxElement;
import org.key_project.logic.Term;
import org.key_project.logic.op.Operator;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.prover.rules.*;
import org.key_project.prover.rules.conditions.NotFreeIn;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstSeq;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstantiation;
import org.key_project.prover.rules.instantiation.AssumesMatchResult;
import org.key_project.rusty.ast.RustyProgramElement;
import org.key_project.rusty.logic.op.UpdateApplication;
import org.key_project.rusty.rule.*;
import org.key_project.rusty.rule.MatchConditions;
import org.key_project.rusty.rule.Taclet;
import org.key_project.rusty.rule.VariableCondition;
import org.key_project.rusty.rule.match.instructions.MatchSchemaVariableInstruction;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;
import org.key_project.util.collection.ImmutableSet;
import org.key_project.util.collection.Pair;

import static org.key_project.rusty.logic.equality.RenamingTermProperty.RENAMING_TERM_PROPERTY;


public class VMTacletMatcher implements TacletMatcher {
    /** the matcher for the find expression of the taclet */
    private final TacletMatchProgram findMatchProgram;
    /** the matcher for the taclet's assumes formulas */
    private final HashMap<Term, TacletMatchProgram> assumesMatchPrograms = new HashMap<>();

    /**
     * the variable conditions of the taclet that need to be satisfied by found schema variable
     * instantiations
     */
    private final ImmutableList<VariableCondition> varconditions;
    /** the built-in notFreeIn variable conditions */
    private final ImmutableList<NotFreeIn> varsNotFreeIn;

    /** the assumes sequent of the taclet */
    private final org.key_project.prover.sequent.Sequent assumesSequent;
    /** the bound variables */
    private final ImmutableSet<QuantifiableVariable> boundVars;

    /**
     * flag indicating if preceding updates of the term to be matched should be ignored this
     * requires the taclet to ignore updates and that the find term does not start with an
     * {@link UpdateApplication} operator
     */
    private final boolean ignoreTopLevelUpdates;
    /**
     * the find expression of the taclet of {@code null} if it is a {@link NoFindTaclet}
     */
    private final Term findExp;

    public VMTacletMatcher(Taclet taclet) {
        varconditions = (ImmutableList<VariableCondition>) taclet.getVariableConditions();
        assumesSequent = taclet.assumesSequent();
        boundVars = taclet.getBoundVariables();
        varsNotFreeIn = (ImmutableList<NotFreeIn>) taclet.varsNotFreeIn();

        if (taclet instanceof FindTaclet ft) {
            findExp = ft.find();
            ignoreTopLevelUpdates = ft.ignoreTopLevelUpdates()
                    && !(findExp.op() instanceof UpdateApplication);
            findMatchProgram = TacletMatchProgram.createProgram(findExp);

        } else {
            ignoreTopLevelUpdates = false;
            findExp = null;
            findMatchProgram = TacletMatchProgram.EMPTY_PROGRAM;
        }

        for (var sf : assumesSequent) {
            assumesMatchPrograms.put(sf.formula(), TacletMatchProgram.createProgram(sf.formula()));
        }
    }

    @Override
    public org.key_project.prover.rules.instantiation.MatchConditions matchFind(
            Term term,
            org.key_project.prover.rules.instantiation.MatchConditions matchCond,
            LogicServices services) {
        if (findMatchProgram == TacletMatchProgram.EMPTY_PROGRAM) {
            return null;
        }
        if (ignoreTopLevelUpdates) {
            Pair</* term below updates */Term, MatchConditions> resultUpdateMatch =
                matchAndIgnoreUpdatePrefix(term, (MatchConditions) matchCond);
            term = resultUpdateMatch.first;
            matchCond = resultUpdateMatch.second;
        }
        return checkConditions(findMatchProgram.match(term, matchCond, services), services);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MatchConditions checkConditions(
            org.key_project.prover.rules.instantiation.MatchConditions cond,
            LogicServices services) {
        MatchConditions result = (MatchConditions) cond;
        if (result != null) {
            final var svIterator = result.getInstantiations().svIterator();

            if (!svIterator.hasNext()) {
                return checkVariableConditions(null, null, result, services);// XXX
            }

            while (result != null && svIterator.hasNext()) {
                final SchemaVariable sv = svIterator.next();
                final Object o = result.getInstantiations().getInstantiation(sv);
                if (o instanceof SyntaxElement) {
                    result = checkVariableConditions(sv, (SyntaxElement) o, result, services);
                }
            }
        }

        return result;
    }

    /**
     * returns true iff the given variable is bound either in the ifSequent or in any part of the
     * TacletGoalTemplates
     *
     * @param v the bound variable to be searched
     */
    private boolean varIsBound(SchemaVariable v) {
        return (v instanceof QuantifiableVariable) && boundVars.contains(v);
    }

    /**
     * looks if a variable is declared as not free in
     *
     * @param var the SchemaVariable to look for
     * @return true iff declared not free
     */
    private boolean varDeclaredNotFree(SchemaVariable var) {
        for (final NotFreeIn nfi : varsNotFreeIn) {
            if (nfi.first() == var) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MatchConditions checkVariableConditions(SchemaVariable var,
            SyntaxElement instantiationCandidate,
            org.key_project.prover.rules.instantiation.MatchConditions matchCond,
            LogicServices services) {
        if (matchCond != null) {
            if (instantiationCandidate instanceof Term term) {
                if (!(term.op() instanceof QuantifiableVariable)) {
                    if (varIsBound(var) || varDeclaredNotFree(var)) {
                        // match(x) is not a variable, but the corresponding template variable is
                        // bound
                        // or declared non free (so it has to be matched to a variable)
                        return null; // FAILED
                    }
                }
            }
            // check generic conditions
            for (final VariableCondition vc : varconditions) {
                matchCond = vc.check(var, instantiationCandidate, matchCond, services);
                if (matchCond == null) {
                    return null; // FAILED
                }
            }
        }
        return (MatchConditions) matchCond;
    }

    /**
     * ignores a possible update prefix This method assumes that the taclet allows to ignore updates
     * and the find expression does not start with an update application operator
     *
     * @param term the term to be matched
     * @param matchCond the accumulated match conditions for a successful match
     * @return a pair of updated match conditions and the unwrapped term without the ignored updates
     *         (Which have been added to the update context in the match conditions)
     */
    private Pair<Term, MatchConditions> matchAndIgnoreUpdatePrefix(final Term term,
            MatchConditions matchCond) {

        final Operator sourceOp = term.op();

        if (sourceOp instanceof UpdateApplication) {
            // updates can be ignored
            Term update = UpdateApplication.getUpdate(term);
            matchCond = matchCond.setInstantiations(
                matchCond.getInstantiations().addUpdate(update));
            return matchAndIgnoreUpdatePrefix(UpdateApplication.getTarget(term), matchCond);
        } else {
            return new Pair<>(term, matchCond);
        }
    }

    @Override
    public final AssumesMatchResult matchAssumes(Iterable<AssumesFormulaInstantiation> toMatch,
            org.key_project.logic.Term p_template,
            org.key_project.prover.rules.instantiation.MatchConditions p_matchCond,
            LogicServices p_services) {
        TacletMatchProgram prg = assumesMatchPrograms.get(p_template);
        MatchConditions matchCond = (MatchConditions) p_matchCond;

        ImmutableList<AssumesFormulaInstantiation> resFormulas =
            ImmutableSLList.nil();
        ImmutableList<org.key_project.prover.rules.instantiation.MatchConditions> resMC =
            ImmutableSLList.nil();

        final boolean updateContextPresent =
            !matchCond.getInstantiations().getUpdateContext().isEmpty();
        ImmutableList<Term> context =
            ImmutableSLList.nil();

        if (updateContextPresent) {
            context = matchCond.getInstantiations().getUpdateContext();
        }

        for (var cf : toMatch) {
            Term formula = cf.getSequentFormula().formula();

            if (updateContextPresent) {
                formula = matchUpdateContext(context, formula);
            }
            if (formula != null) {// update context not present or update context match succeeded
                final MatchConditions newMC =
                    checkConditions(prg.match(formula, matchCond, p_services), p_services);

                if (newMC != null) {
                    resFormulas = resFormulas.prepend(cf);
                    resMC = resMC.prepend(newMC);
                }
            }
        }
        return new AssumesMatchResult(resFormulas, resMC);
    }

    /**
     * the formula ensures that the update context described the update of the given formula.
     * If it does not then {@code null} is returned, otherwise the formula without the update
     * context.
     *
     * @param context the list of update label pairs describing the update context
     * @param formula the formula whose own update context must be equal (modulo renaming) to the
     *        given one
     * @return {@code null} if the update context does not match the one of the formula or the
     *         formula without the update context
     */
    private Term matchUpdateContext(ImmutableList<Term> context, Term formula) {
        ImmutableList<Term> curContext = context;
        for (int i = 0, size = context.size(); i < size; i++) {
            if (formula.op() instanceof UpdateApplication) {
                final Term update = UpdateApplication.getUpdate(formula);
                final Term u = curContext.head();
                if (RENAMING_TERM_PROPERTY.equalsModThisProperty(u, update)) {
                    curContext = curContext.tail();
                    formula = UpdateApplication.getTarget(formula);
                    continue;
                }
            }
            // update context does not match update prefix of formula
            return null;
        }
        return formula;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final MatchConditions matchAssumes(
            Iterable<AssumesFormulaInstantiation> p_toMatch,
            org.key_project.prover.rules.instantiation.MatchConditions p_matchCond,
            LogicServices p_services) {

        final var anteIterator = assumesSequent.antecedent().iterator();
        final var succIterator = assumesSequent.succedent().iterator();

        ImmutableList<org.key_project.prover.rules.instantiation.MatchConditions> newMC;

        for (final AssumesFormulaInstantiation candidateInst : p_toMatch) {
            // Part of fix for #1716: match antecedent with antecedent, succ with succ
            boolean candidateInAntec = (candidateInst instanceof AssumesFormulaInstSeq)
                    // Only IfFormulaInstSeq has inAntec() property ...
                    && (((AssumesFormulaInstSeq) candidateInst).inAntec())
                    || !(candidateInst instanceof AssumesFormulaInstSeq)
                            // ... and it seems we don't need the check for other implementations.
                            // Default: just take the next ante formula, else succ formula
                            && anteIterator.hasNext();

            var itIfSequent = candidateInAntec ? anteIterator : succIterator;
            // Fix end

            assert itIfSequent.hasNext()
                    : "toMatch and assumes sequent must have same number of elements";
            newMC = matchAssumes(
                ImmutableSLList.<AssumesFormulaInstantiation>nil().prepend(candidateInst),
                itIfSequent.next().formula(), p_matchCond, p_services).matchConditions();

            if (newMC.isEmpty()) {
                return null;
            }

            p_matchCond = newMC.head();
        }
        assert !anteIterator.hasNext() && !succIterator.hasNext()
                : "toMatch and assumes sequent must have same number of elements";

        return (MatchConditions) p_matchCond;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchConditions matchSV(SchemaVariable sv,
            SyntaxElement syntaxElement,
            org.key_project.prover.rules.instantiation.MatchConditions matchCond,
            LogicServices services) {

        final MatchSchemaVariableInstruction<? extends SchemaVariable> instr =
            TacletMatchProgram.getMatchInstructionForSV(sv);

        if (syntaxElement instanceof Term term) {
            matchCond = instr.match(term, matchCond, services);
            matchCond = checkVariableConditions(sv, syntaxElement, matchCond, services);
        } else if (syntaxElement instanceof RustyProgramElement pe) {
            matchCond = instr.match(pe, matchCond, services);
            matchCond = checkConditions(matchCond, services);
        }
        return (MatchConditions) matchCond;
    }
}

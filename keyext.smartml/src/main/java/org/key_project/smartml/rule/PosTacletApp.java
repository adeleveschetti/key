/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.key_project.logic.Term;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstantiation;
import org.key_project.prover.rules.instantiation.MatchConditions;
import org.key_project.prover.rules.instantiation.SVInstantiations;
import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.smartml.rule.TacletVariableSVCollector;
import org.key_project.smartml.Services;
import org.key_project.smartml.rule.FindTaclet;
import org.key_project.smartml.rule.RewriteTaclet;
import org.key_project.smartml.rule.Taclet;
import org.key_project.smartml.rule.TacletApp;
import org.key_project.smartml.rule.*;
import org.key_project.util.collection.DefaultImmutableSet;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSet;

import java.util.Iterator;


/**
 * A position taclet application object, contains already the information to which term/formula of
 * the sequent the taclet is attached. The position information has been determined by matching the
 * find-part of the corresponding taclet against the term described by the position information. If
 * such a match has not been performed or a taclet is a no find taclet, a no position taclet object
 * ({@link org.key_project.smartml.rule.NoPosTacletApp}) is used to keep track of the (partial)
 * instantiation information.
 */
public class PosTacletApp extends org.key_project.smartml.rule.TacletApp {
    /**
     * stores the information where the Taclet is to be applied. This means where the find section
     * of the taclet matches
     */
    private final PosInOccurrence pos;

    /**
     * creates a PosTacletApp for the given taclet with some known instantiations and a position
     * information and CHECKS variable conditions as well as it resolves collisions The
     * ifInstantiations parameter is not matched against the if sequence, but only stored. For
     * matching use the method "setIfFormulaInstantiations".
     *
     * @param taclet the FindTaclet
     * @param instantiations the SVInstantiations
     * @param pos the PosInOccurrence storing the position where to apply the Taclet
     * @return new PosTacletApp or null if conditions (assertions) have been hurted
     */
    public static PosTacletApp createPosTacletApp(org.key_project.smartml.rule.FindTaclet taclet,
                                                  SVInstantiations instantiations, PosInOccurrence pos, Services services) {
        return createPosTacletApp(taclet, instantiations, null, pos, services);
    }

    public static PosTacletApp createPosTacletApp(org.key_project.smartml.rule.FindTaclet taclet,
                                                  SVInstantiations instantiations,
                                                  ImmutableList<AssumesFormulaInstantiation> ifInstantiations,
                                                  PosInOccurrence pos, Services services) {
        instantiations = resolveCollisionWithContext(taclet,
            resolveCollisionVarSV(taclet, instantiations, services), pos, services);
        if (checkVarCondNotFreeIn(taclet, instantiations, pos)) {
            return new PosTacletApp(taclet, instantiations, ifInstantiations, pos);
        }

        return null;
    }

    public static PosTacletApp createPosTacletApp(org.key_project.smartml.rule.FindTaclet taclet, MatchConditions matchCond,
                                                  PosInOccurrence pos, Services services) {
        return createPosTacletApp(taclet, matchCond.getInstantiations(), null, pos, services);
    }

    /**
     * creates a PosTacletApp for the given taclet with some known instantiations and a position
     * information
     *
     * @param taclet the FindTaclet
     * @param instantiations the SVInstantiations
     * @param pos the PosInOccurrence storing the position where to apply the Taclet
     */
    private PosTacletApp(org.key_project.smartml.rule.FindTaclet taclet, SVInstantiations instantiations,
                         ImmutableList<AssumesFormulaInstantiation> ifInstantiations, PosInOccurrence pos) {
        super(taclet, instantiations, ifInstantiations);
        this.pos = pos;
    }


    /**
     * returns the LogicVariables that are bound above the PositionInOccurrence of the PosTacletApp.
     * __OPTIMIZE__ If this method is needed more than once caching the result should be considered.
     *
     * @return the set of the logicvariables that are bound for the indicated application position
     *         of the TacletApp.
     */
    private static ImmutableSet<QuantifiableVariable> varsBoundAboveFindPos(org.key_project.smartml.rule.Taclet taclet,
                                                                            PosInOccurrence pos) {

        if (!(taclet instanceof RewriteTaclet)) {
            return DefaultImmutableSet.nil();
        }

        return collectBoundVarsAbove(pos);
    }

    private static Iterator<SchemaVariable> allVariableSV(org.key_project.smartml.rule.Taclet taclet) {
        TacletVariableSVCollector coll = new TacletVariableSVCollector();
        coll.visit(taclet, true); // __CHANGE__ true or false???
        return coll.varIterator();
    }


    // @Override
    protected ImmutableSet<QuantifiableVariable> contextVars(SchemaVariable sv) {
        if (!taclet().getPrefix(sv).context()) {
            return DefaultImmutableSet.nil();
        }
        return varsBoundAboveFindPos(taclet(), posInOccurrence());
    }

    @Override
    public org.key_project.smartml.rule.TacletApp addInstantiation(SVInstantiations svi, Services services) {
        return createPosTacletApp((org.key_project.smartml.rule.FindTaclet) taclet(), svi.union(instantiations(), services),
            assumesFormulaInstantiations(), posInOccurrence(), services);
    }

    /**
     * resolves collisions with the context in an SVInstantiation
     *
     * @param insts the original SVInstantiations
     * @return the resolved SVInstantiations
     */
    private static SVInstantiations resolveCollisionWithContext(Taclet taclet,
                                                                SVInstantiations insts, PosInOccurrence pos, Services services) {

        if (taclet.isContextInPrefix()) {
            ImmutableSet<QuantifiableVariable> k = varsBoundAboveFindPos(taclet, pos);
            Iterator<SchemaVariable> it = allVariableSV(taclet);
            while (it.hasNext()) {
                SchemaVariable varSV = it.next();
                Term inst = (Term) insts.getInstantiation(varSV);
                if (inst != null && k.contains(inst.op())) {
                    insts = replaceInstantiation(taclet, insts, varSV, services);
                }
            }
        }
        return insts;
    }


    /**
     * adds a new instantiation to this TacletApp
     *
     * @param sv the SchemaVariable to be instantiated
     * @param term the Term the SchemaVariable is instantiated with
     * @return the new TacletApp
     */
    // @Override
    public org.key_project.smartml.rule.TacletApp addInstantiation(SchemaVariable sv, Term term,
                                                                 Services services) {

        return createPosTacletApp((org.key_project.smartml.rule.FindTaclet) taclet(),
            instantiations().add(sv, term, services), assumesFormulaInstantiations(),
            posInOccurrence(), services);
    }


    @Override
    public org.key_project.smartml.rule.TacletApp setMatchConditions(MatchConditions mc, Services services) {
        return createPosTacletApp((org.key_project.smartml.rule.FindTaclet) taclet(), mc.getInstantiations(),
            assumesFormulaInstantiations(), posInOccurrence(), services);
    }

    @Override
    public PosInOccurrence posInOccurrence() {
        return pos;
    }

    /**
     * returns true iff all necessary information is collected, so that the Taclet can be applied.
     *
     * @return true iff all necessary information is collected, so that the Taclet can be applied.
     */
    @Override
    public boolean complete() {
        return posInOccurrence() != null && uninstantiatedVars().isEmpty() && ifInstsComplete();
    }

    /**
     * creates a new Taclet application containing all the instantiations, constraints, new
     * metavariables and if formula instantiations given and forget the old ones
     */
    @Override
    protected org.key_project.smartml.rule.TacletApp setAllInstantiations(MatchConditions mc,
                                                                        ImmutableList<AssumesFormulaInstantiation> ifInstantiations, Services services) {
        return createPosTacletApp((org.key_project.smartml.rule.FindTaclet) taclet(), mc.getInstantiations(), ifInstantiations,
            posInOccurrence(), services);
    }

    /**
     * adds a new instantiation to this TacletApp
     *
     * @param sv the SchemaVariable to be instantiated
     * @param term the Term the SchemaVariable is instantiated with
     * @return the new TacletApp
     */
    @Override
    public TacletApp addInstantiation(SchemaVariable sv, Term term, boolean interesting,
                                      Services services) {

        /*
         * if (interesting) {
         * return createPosTacletApp((FindTaclet) taclet(),
         * instantiations().addInteresting(sv, term, services), ifFormulaInstantiations(),
         * posInOccurrence(), services);
         * } else
         */ {
            return createPosTacletApp((FindTaclet) taclet(),
                instantiations().add(sv, term, services), assumesFormulaInstantiations(),
                posInOccurrence(), services);
        }
    }


}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof;

import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.smartml.Services;
import org.key_project.smartml.proof.Goal;
import org.key_project.smartml.proof.TacletAppIndex;
import org.key_project.smartml.proof.TacletIndex;
import org.key_project.smartml.rule.NoPosTacletApp;
import org.key_project.smartml.rule.TacletApp;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

public class RuleAppIndex {

    private final org.key_project.smartml.proof.Goal goal;

    private final TacletIndex tacletIndex;

    private final TacletAppIndex tacletAppIndex;

    public RuleAppIndex(TacletIndex tacletIndex, org.key_project.smartml.proof.Goal goal, Services services) {
        this.goal = goal;
        this.tacletIndex = tacletIndex;
        tacletAppIndex = new TacletAppIndex(tacletIndex, goal, services);
    }

    private RuleAppIndex(TacletIndex tacletIndex, org.key_project.smartml.proof.Goal goal, TacletAppIndex tacletAppIndex) {
        this.goal = goal;
        this.tacletIndex = tacletIndex;
        this.tacletAppIndex = tacletAppIndex;
    }

    /**
     * returns the set of rule applications for the given heuristics at the given position of the
     * given sequent.
     *
     * //@param filter the TacletFiler filtering the taclets of interest
     *
     * @param pos the PosInOccurrence to focus
     * @param services the Services object encapsulating information about the Rust datastructures
     *        like (static)types etc.
     */
    public ImmutableList<TacletApp> getTacletAppAt(PosInOccurrence pos,
            Services services) {
        ImmutableList<TacletApp> result = ImmutableSLList.nil();
        result = result.prepend(tacletAppIndex.getTacletAppAt(pos, services));
        return result;
    }

    /**
     * returns a new RuleAppIndex with a copied TacletIndex. Attention: the listener lists are not
     * copied
     */
    public RuleAppIndex copy(Goal goal) {
        TacletIndex copiedTacletIndex = tacletIndex.copy();
        return new RuleAppIndex(copiedTacletIndex, goal,
            tacletAppIndex.copyWith(copiedTacletIndex, goal));
    }


    /**
     * adds a new Taclet with instantiation information to the Taclet Index of this TacletAppIndex.
     *
     * @param tacletApp the NoPosTacletApp describing a partial instantiated Taclet to add
     */
    public void addNoPosTacletApp(NoPosTacletApp tacletApp) {
        tacletIndex.add(tacletApp);

        tacletAppIndex.addedNoPosTacletApp(tacletApp);
    }

    public TacletIndex tacletIndex() {
        return tacletIndex;
    }

    /**
     * returns the rule applications at the given PosInOccurrence and at all Positions below this.
     * The method calls getTacletAppAt for all the Positions below.
     *
     * @param pos the position where to start from
     * @param services the Services object encapsulating information about the java datastructures
     *        like (static)types etc.
     * @return the possible rule applications
     */
    public ImmutableList<TacletApp> getTacletAppAtAndBelow(PosInOccurrence pos,
            Services services) {
        ImmutableList<TacletApp> result = ImmutableSLList.nil();
        result =
            result.prepend(tacletAppIndex.getTacletAppAtAndBelow(pos, services));
        return result;
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof;

import org.jspecify.annotations.NonNull;
import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.prover.sequent.Sequent;
import org.key_project.smartml.Services;
import org.key_project.smartml.proof.Goal;
import org.key_project.smartml.proof.Node;
import org.key_project.smartml.proof.Proof;
import org.key_project.smartml.proof.TacletIndex;
import org.key_project.smartml.proof.*;
import org.key_project.smartml.rule.*;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;


public class TacletAppIndex {
    private final org.key_project.smartml.proof.TacletIndex tacletIndex;

    private SemisequentTacletAppIndex antecIndex;
    private SemisequentTacletAppIndex succIndex;

    private final org.key_project.smartml.proof.Goal goal;

    /**
     * The sequent with the formulas for which taclet indices are hold by this object. Invariant:
     * <code>seq != null</code> implies that the indices <code>antecIndex</code>,
     * <code>succIndex</code> are up-to-date for the sequent <code>seq</code>
     */
    private Sequent seq;

    public TacletAppIndex(org.key_project.smartml.proof.TacletIndex tacletIndex, org.key_project.smartml.proof.Goal goal, Services services) {
        this(tacletIndex, null, null, goal, null);
    }

    private TacletAppIndex(org.key_project.smartml.proof.TacletIndex tacletIndex, SemisequentTacletAppIndex antecIndex,
                           SemisequentTacletAppIndex succIndex, org.key_project.smartml.proof.Goal goal,
                           Sequent seq) {
        this.tacletIndex = tacletIndex;
        this.antecIndex = antecIndex;
        this.succIndex = succIndex;
        this.goal = goal;
        this.seq = seq;
    }

    static TacletApp createTacletApp(NoPosTacletApp tacletApp, PosInOccurrence pos,
            Services services) {
        if (tacletApp.taclet() instanceof FindTaclet) {
            return tacletApp.setPosInOccurrence(pos, services);
        } else {
            return tacletApp;
        }
    }

    public org.key_project.smartml.proof.TacletIndex tacletIndex() {
        return tacletIndex;
    }

    public ImmutableList<TacletApp> getTacletAppAt(PosInOccurrence pos, Services services) {
        return prepend(getFindTacletWithPos(pos, services), getNoFindTaclet(services));
    }

    private ImmutableList<TacletApp> getFindTacletWithPos(PosInOccurrence pos,
            Services services) {
        ImmutableList<NoPosTacletApp> tacletInsts = getFindTaclet(pos);
        return createTacletApps(tacletInsts, pos, services);
    }


    /**
     * collects all NoFindTacletInstantiations
     *
     * @param services the Services object encapsulating information about the Rust datastructures
     *        like (static)types etc.
     * @return list of all possible instantiations
     */
    public ImmutableList<NoPosTacletApp> getNoFindTaclet(Services services) {
        return tacletIndex().getNoFindTaclet(services);
    }

    /**
     * collects all FindTaclets with instantiations and position
     *
     * @param pos the PosInOccurrence to focus
     * @return list of all possible instantiations
     */
    public ImmutableList<NoPosTacletApp> getFindTaclet(PosInOccurrence pos) {
        return getIndex(pos).getTacletAppAt(pos);
    }

    private void ensureIndicesExist() {
        if (isOutdated()) {
            // Indices are not up-to-date
            createAllFromGoal();
        }
    }

    private void createAllFromGoal() {
        this.seq = getNode().sequent();

        antecIndex =
            new SemisequentTacletAppIndex(getSequent(), true, getServices(), tacletIndex());
        succIndex =
            new SemisequentTacletAppIndex(getSequent(), false, getServices(), tacletIndex());
    }

    private Services getServices() {
        return getProof().getServices();
    }

    private Proof getProof() {
        return getNode().proof();
    }

    /**
     * @return true iff this index is currently outdated with respect to the sequent of the
     *         associated goal; this does not detect other modifications
     *         like an altered user
     *         constraint
     */
    private boolean isOutdated() {
        return getGoal() == null || getSequent() != getNode().sequent();
    }

    private org.key_project.smartml.proof.Goal getGoal() {
        return goal;
    }

    private Sequent getSequent() {
        return seq;
    }

    private Node getNode() {
        return goal.getNode();
    }

    private SemisequentTacletAppIndex getIndex(PosInOccurrence pos) {
        ensureIndicesExist();
        return pos.isInAntec() ? antecIndex : succIndex;
    }

    private static ImmutableList<TacletApp> prepend(ImmutableList<TacletApp> l1,
            ImmutableList<NoPosTacletApp> l2) {
        for (NoPosTacletApp aL2 : l2) {
            l1 = l1.prepend(aL2);
        }
        return l1;
    }

    /**
     * creates TacletApps out of each single NoPosTacletApp object
     *
     * @param tacletInsts the list of NoPosTacletApps the TacletApps are to be created from
     * @param pos the PosInOccurrence to focus
     * @return list of all created TacletApps
     */
    static ImmutableList<TacletApp> createTacletApps(ImmutableList<NoPosTacletApp> tacletInsts,
            PosInOccurrence pos, Services services) {
        ImmutableList<TacletApp> result = ImmutableSLList.nil();
        for (NoPosTacletApp tacletApp : tacletInsts) {
            if (tacletApp.taclet() instanceof FindTaclet) {
                PosTacletApp newTacletApp = tacletApp.setPosInOccurrence(pos, services);
                if (newTacletApp != null) {
                    result = result.prepend(newTacletApp);
                }
            } else {
                result = result.prepend(tacletApp);
            }
        }
        return result;
    }

    /**
     * returns a new TacletAppIndex with a given TacletIndex
     */
    TacletAppIndex copyWith(TacletIndex p_tacletIndex, Goal goal) {
        return new TacletAppIndex(p_tacletIndex, antecIndex, succIndex, goal, getSequent());
    }

    private void updateIndices(final NoPosTacletApp newTaclet) {
        antecIndex =
            antecIndex.addTaclet(newTaclet, getServices(), tacletIndex);
        succIndex =
            succIndex.addTaclet(newTaclet, getServices(), tacletIndex);
    }

    /**
     * updates the internal caches after a new Taclet with instantiation information has been added
     * to the TacletIndex.
     *
     * @param tacletApp the partially instantiated Taclet to add
     */
    public void addedNoPosTacletApp(NoPosTacletApp tacletApp) {
        if (tacletApp.taclet() instanceof NoFindTaclet) {
            return;
        }

        updateIndices(tacletApp);
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
        final ImmutableList<TacletApp> findTaclets =
            getIndex(pos).getTacletAppAtAndBelow(pos, services);
        return prepend(findTaclets, getNoFindTaclet(services));
    }
}

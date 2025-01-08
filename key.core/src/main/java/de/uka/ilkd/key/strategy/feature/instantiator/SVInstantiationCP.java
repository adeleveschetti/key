/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package de.uka.ilkd.key.strategy.feature.instantiator;

import java.util.Iterator;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.strategy.NumberRuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.feature.Feature;
import de.uka.ilkd.key.strategy.feature.MutableState;
import de.uka.ilkd.key.strategy.termProjection.ProjectionToTerm;
import de.uka.ilkd.key.util.Debug;

import org.key_project.logic.Name;
import org.key_project.logic.Term;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.prover.rules.RuleApp;
import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.util.collection.ImmutableSLList;
import org.key_project.util.collection.ImmutableSet;


/**
 * Feature representing a <code>ChoicePoint</code> for instantiating a schema variable of a taclet
 * with the term that is returned by a <code>ProjectionToTerm</code>. This feature is useful in
 * particular combined with <code>ForEachCP</code>. Although the feature formally is a choice point,
 * it will always have exactly one branch
 */
public class SVInstantiationCP implements Feature {

    private final Name svToInstantiate;
    private final ProjectionToTerm value;

    public static Feature create(Name svToInstantiate, ProjectionToTerm value) {
        return new SVInstantiationCP(svToInstantiate, value);
    }

    public static Feature createTriggeredVarCP(ProjectionToTerm value) {
        return new SVInstantiationCP(null, value);
    }


    private SVInstantiationCP(Name svToInstantiate, ProjectionToTerm value) {
        this.svToInstantiate = svToInstantiate;
        this.value = value;
    }

    public RuleAppCost computeCost(org.key_project.prover.rules.RuleApp app, PosInOccurrence pos,
                                   Goal goal,
                                   MutableState mState) {
        final BackTrackingManager manager = mState.getBacktrackingManager();
        manager.passChoicePoint(new CP(app, pos, goal, mState), this);
        return NumberRuleAppCost.getZeroCost();
    }

    private SchemaVariable findSVWithName(TacletApp app) {

        if (svToInstantiate == null) {
            return app.taclet().getTrigger().triggerVar();
        }

        final ImmutableSet<SchemaVariable> vars = app.uninstantiatedVars();
        for (SchemaVariable svt : vars) {
            if (svt.name().equals(svToInstantiate)) {
                return svt;
            }
        }

        Debug.fail("Did not find schema variable " + svToInstantiate
            + " that I was supposed to instantiate\n" + "(taclet " + app.taclet().name() + ")\n"
            + "Either the name of the variable is wrong, or the variable\n"
            + "has already been instantiated.");
        return null;
    }


    private class CP implements ChoicePoint {

        private final PosInOccurrence pos;
        private final org.key_project.prover.rules.RuleApp app;
        private final Goal goal;
        private final MutableState mState;

        private CP(org.key_project.prover.rules.RuleApp app, PosInOccurrence pos, Goal goal, MutableState mState) {
            this.pos = pos;
            this.app = app;
            this.goal = goal;
            this.mState = mState;
        }

        public Iterator<CPBranch> getBranches(org.key_project.prover.rules.RuleApp oldApp) {
            if (!(oldApp instanceof final TacletApp tapp)) {
                Debug.fail("Instantiation feature is only applicable to " + "taclet apps, but got ",
                    oldApp);
                throw new IllegalArgumentException(
                    "Rule application must be a taclet application, but is " + oldApp);
            }

            final SchemaVariable sv = findSVWithName(tapp);
            final Term instTerm = value.toTerm(app, pos, goal, mState);

            final org.key_project.prover.rules.RuleApp newApp =
                tapp.addCheckedInstantiation(sv, (de.uka.ilkd.key.logic.Term) instTerm,
                    goal.proof().getServices(), true);

            final CPBranch branch = new CPBranch() {
                public void choose() {}

                public RuleApp getRuleAppForBranch() { return newApp; }
            };

            return ImmutableSLList.<CPBranch>nil().prepend(branch).iterator();
        }

    }
}

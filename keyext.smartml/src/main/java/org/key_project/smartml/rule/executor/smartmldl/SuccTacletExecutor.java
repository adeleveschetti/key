/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.executor.smartmldl;

import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.prover.sequent.Sequent;
import org.key_project.prover.sequent.SequentChangeInfo;
import org.key_project.smartml.Services;
import org.key_project.smartml.proof.Goal;
import org.key_project.smartml.rule.MatchConditions;
import org.key_project.smartml.rule.SuccTaclet;
import org.key_project.smartml.rule.TacletApp;
import org.key_project.smartml.rule.tacletbuilder.AntecSuccTacletGoalTemplate;
import org.key_project.smartml.rule.tacletbuilder.TacletGoalTemplate;

public class SuccTacletExecutor extends FindTacletExecutor {

    public SuccTacletExecutor(SuccTaclet taclet) {
        super(taclet);
    }

    @Override
    protected void applyAdd(Sequent add,
            SequentChangeInfo currentSequent,
            PosInOccurrence whereToAdd, PosInOccurrence posOfFind, MatchConditions matchCond,
            Goal goal, TacletApp ruleApp, Services services) {
        addToAntec(add.antecedent(),
            currentSequent, null,
            posOfFind, matchCond, goal, ruleApp, services);
        addToSucc(add.succedent(), currentSequent, whereToAdd,
            posOfFind, matchCond, goal, ruleApp, services);
    }

    @Override
    protected void applyReplacewith(TacletGoalTemplate gt, SequentChangeInfo currentSequent,
            PosInOccurrence posOfFind, MatchConditions matchCond, Goal goal, TacletApp ruleApp,
            Services services) {
        if (gt instanceof AntecSuccTacletGoalTemplate astgt) {
            final Sequent replWith = astgt.replaceWith();

            replaceAtPos(replWith.succedent(), currentSequent, posOfFind, matchCond,
                goal, ruleApp,
                services);
            if (!replWith.antecedent().isEmpty()) {
                addToAntec(replWith.antecedent(),
                    currentSequent, null, posOfFind, matchCond, goal, ruleApp, services);
            }
        }
    }
}

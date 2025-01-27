/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.smartml.Services;
import org.key_project.smartml.rule.MatchConditions;
import org.key_project.smartml.rule.NoPosTacletApp;
import org.key_project.smartml.rule.RewriteTaclet;
import org.key_project.smartml.rule.Taclet;

public class UninstantiatedNoPosTacletApp extends NoPosTacletApp {
    UninstantiatedNoPosTacletApp(Taclet taclet) {
        super(taclet);
    }

    @Override
    protected org.key_project.smartml.rule.MatchConditions setupMatchConditions(PosInOccurrence pos, Services services) {
        if (taclet() instanceof RewriteTaclet rwt) {
            return rwt.checkPrefix(pos,
                org.key_project.smartml.rule.MatchConditions.EMPTY_MATCHCONDITIONS);
        }

        return MatchConditions.EMPTY_MATCHCONDITIONS;
    }
}

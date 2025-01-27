/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.key_project.logic.Name;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.prover.rules.RuleSet;
import org.key_project.prover.rules.TacletApplPart;
import org.key_project.prover.rules.TacletAttributes;
import org.key_project.prover.rules.tacletbuilder.TacletGoalTemplate;
import org.key_project.smartml.rule.Taclet;
import org.key_project.smartml.rule.executor.smartmldl.NoFindTacletExecutor;
import org.key_project.util.collection.DefaultImmutableSet;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableMap;
import org.key_project.util.collection.ImmutableSet;

/**
 * Used to implement a Taclet that has no <I>find</I> part. This kind of taclet is not attached to
 * term or formula, but to a complete sequent. A typical representant is the <code>cut</code> rule.
 */
public class NoFindTaclet extends org.key_project.smartml.rule.Taclet {
    /**
     * creates a {@link Taclet} (previously Schematic Theory Specific Rule) with the given
     * parameters.
     *
     * @param name the name of the Taclet
     * @param applPart contains the application part of a Taclet that is the if-sequent, the
     *        variable conditions
     * @param goalTemplates the IList containing all goal descriptions of the
     *        taclet to be created
     * @param attrs attributes for the Taclet; these are boolean values
     * @param prefixMap a ImmutableMap that contains the prefix for each
     *        SchemaVariable in the Taclet
     */
    public NoFindTaclet(Name name, TacletApplPart applPart,
            ImmutableList<TacletGoalTemplate> goalTemplates,
            ImmutableList<RuleSet> ruleSets,
            TacletAttributes attrs,
            ImmutableMap<org.key_project.logic.op.sv.SchemaVariable, org.key_project.prover.rules.TacletPrefix> prefixMap,
            ImmutableSet<org.key_project.prover.rules.TacletAnnotation> tacletAnnotations) {
        super(name, applPart, goalTemplates, ruleSets, attrs, prefixMap,
            tacletAnnotations);
        createTacletServices();
    }

    @Override
    protected void createAndInitializeExecutor() {
        executor = new NoFindTacletExecutor(this);
    }

    /**
     * @return Set of schemavariables of {@code if} and the (optional) find part
     */
    @Override
    public ImmutableSet<org.key_project.logic.op.sv.SchemaVariable> getAssumesAndFindVariables() {
        return getAssumesVariables();
    }

    /**
     * the empty set as a no find taclet has no other entities where variables cann occur bound than
     * in the goal templates
     *
     * @return empty set
     */
    @Override
    protected ImmutableSet<QuantifiableVariable> getBoundVariablesHelper() {
        return DefaultImmutableSet.nil();
    }

    @Override
    public NoFindTaclet setName(String s) {
        final TacletApplPart applPart =
            new TacletApplPart(assumesSequent(), varsNew(), varsNotFreeIn(),
                varsNewDependingOn(), getVariableConditions());
        final var attrs = new TacletAttributes(displayName(), null);

        return new NoFindTaclet(new Name(s), applPart,
            goalTemplates(), ruleSets, attrs,
            prefixMap, tacletAnnotations);
    }
}

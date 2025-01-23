/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package de.uka.ilkd.key.proof.init;


import de.uka.ilkd.key.logic.label.TermLabelManager;
import de.uka.ilkd.key.logic.label.TermLabelManager.TermLabelConfiguration;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.io.RuleSourceFactory;
import de.uka.ilkd.key.proof.mgt.AxiomJustification;
import de.uka.ilkd.key.proof.mgt.RuleJustification;
import de.uka.ilkd.key.prover.impl.DefaultGoalChooserBuilder;
import de.uka.ilkd.key.prover.impl.DepthFirstGoalChooserBuilder;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.strategy.StrategyFactory;

import org.key_project.logic.Name;
import org.key_project.prover.engine.GoalChooserBuilder;
import org.key_project.util.collection.DefaultImmutableSet;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;
import org.key_project.util.collection.ImmutableSet;

import org.jspecify.annotations.NonNull;

public abstract class AbstractProfile implements Profile {
    /**
     * The default profile which is used if no profile is defined in custom problem files (loaded
     * via {@link KeYUserProblemFile}).
     */
    private static Profile defaultProfile = JavaProfile.getDefaultInstance();

    private final RuleCollection standardRules;

    private final ImmutableSet<StrategyFactory> strategies;

    private final ImmutableSet<String> supportedGC;
    private final ImmutableSet<GoalChooserBuilder<Proof, Goal>> supportedGCB;

    private GoalChooserBuilder<Proof, Goal> prototype;

    private TermLabelManager termLabelManager;

    private static ImmutableSet<String> extractNames(
            ImmutableSet<GoalChooserBuilder<Proof, Goal>> supportedGCB) {

        ImmutableSet<String> result = DefaultImmutableSet.nil();

        for (var goalChooserBuilder : supportedGCB) {
            result = result.add(goalChooserBuilder.name());
        }

        return result;
    }

    protected AbstractProfile(String standardRuleFilename) {
        standardRules = new RuleCollection(
            RuleSourceFactory.fromDefaultLocation(standardRuleFilename), initBuiltInRules());
        strategies = getStrategyFactories();
        this.supportedGCB = computeSupportedGoalChooserBuilder();
        this.supportedGC = extractNames(supportedGCB);
        this.prototype = getDefaultGoalChooserBuilder();
        assert (this.prototype != null);
        initTermLabelManager();
    }

    protected ImmutableSet<GoalChooserBuilder<Proof, Goal>> computeSupportedGoalChooserBuilder() {
        return DefaultImmutableSet.<GoalChooserBuilder<Proof, Goal>>nil()
                .add(new DefaultGoalChooserBuilder())
                .add(new DepthFirstGoalChooserBuilder());
    }

    /**
     * Initializes the {@link TermLabelManager}.
     */
    protected void initTermLabelManager() {
        this.termLabelManager = new TermLabelManager(computeTermLabelConfiguration());
    }

    /**
     * Computes the {@link TermLabelConfiguration} to use in this {@link Profile}.
     *
     * @return The {@link TermLabelConfiguration} to use in this {@link Profile}.
     */
    protected abstract ImmutableList<TermLabelConfiguration> computeTermLabelConfiguration();

    public RuleCollection getStandardRules() {
        return standardRules;
    }

    protected ImmutableSet<StrategyFactory> getStrategyFactories() {
        return DefaultImmutableSet.nil();
    }

    protected ImmutableList<BuiltInRule> initBuiltInRules() {
        return ImmutableSLList.nil();
    }


    public ImmutableSet<StrategyFactory> supportedStrategies() {
        return strategies;
    }

    public boolean supportsStrategyFactory(Name strategy) {
        return getStrategyFactory(strategy) != null;
    }

    public StrategyFactory getStrategyFactory(Name n) {
        for (StrategyFactory sf : getStrategyFactories()) {
            if (sf.name().equals(n)) {
                return sf;
            }
        }
        return null;
    }

    /**
     * returns the names of the supported goal chooser builders
     */
    public ImmutableSet<String> supportedGoalChoosers() {
        return supportedGC;
    }

    /**
     * returns the default builder for a goal chooser
     *
     * @return this implementation returns a new instance of {@link DefaultGoalChooserBuilder}
     */
    @Override
    public GoalChooserBuilder<Proof, @NonNull Goal> getDefaultGoalChooserBuilder() {
        return new DefaultGoalChooserBuilder();
    }

    /**
     * sets the user selected goal chooser builder to be used as prototype
     *
     * @throws IllegalArgumentException if a goal chooser of the given name is not supported
     */
    public void setSelectedGoalChooserBuilder(String name) {

        this.prototype = lookupGC(name);

        if (this.prototype == null) {
            throw new IllegalArgumentException(
                "Goal chooser:" + name + " is not supported by this profile.");
        }
    }

    /**
     * looks up the demanded goal chooser is supported and returns a new instance if possible
     * otherwise <code>null</code> is returned
     *
     * @param name the String with the goal choosers name
     * @return a new instance of the builder or <code>null</code> if the demanded chooser is not
     *         supported
     */
    public GoalChooserBuilder<Proof, Goal> lookupGC(String name) {
        for (var supprotedGCB : supportedGCB) {
            if (supprotedGCB.name().equals(name)) {
                return supprotedGCB.copy();
            }
        }
        return null;
    }

    /**
     * returns a copy of the selected goal chooser builder
     */
    @Override
    public GoalChooserBuilder<Proof, @NonNull Goal> getSelectedGoalChooserBuilder() {
        return prototype.copy();
    }

    /**
     * any standard rule has is by default justified by an axiom rule justification
     *
     * @return the justification for the standard rules
     */
    @Override
    public RuleJustification getJustification(Rule r) {
        if (r instanceof Taclet) {
            return ((Taclet) r).getRuleJustification();
        } else {
            return AxiomJustification.INSTANCE;
        }
    }


    @Override
    public String getInternalClassDirectory() {
        return "";
    }


    @Override
    public String getInternalClasslistFilename() {
        return "JAVALANG.TXT";
    }

    /**
     * Returns the default profile which is used if no profile is defined in custom problem files
     * (loaded via {@link KeYUserProblemFile}).
     *
     * @return The default profile which is used if no profile is defined in custom problem files
     *         (loaded via {@link KeYUserProblemFile}).
     */
    public static Profile getDefaultProfile() {
        return defaultProfile;
    }

    /**
     * Sets the default profile which is used if no profile is defined in custom problem files
     * (loaded via {@link KeYUserProblemFile}).
     *
     * @param defaultProfile The default profile which is used if no profile is defined in custom
     *        problem files (loaded via {@link KeYUserProblemFile}).
     */
    public static void setDefaultProfile(Profile defaultProfile) {
        assert defaultProfile != null;
        AbstractProfile.defaultProfile = defaultProfile;
    }

    @Override
    public TermLabelManager getTermLabelManager() {
        return termLabelManager;
    }
}

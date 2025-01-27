/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof.calculus;

import org.jspecify.annotations.NonNull;
import org.key_project.prover.sequent.Semisequent;
import org.key_project.prover.sequent.SequentFormula;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

import java.util.Iterator;

/**
 * Realises a sequent. This class implements the necessary factory methods.
 * Outside of this package only the supertype must be used.
 */
class Sequent extends org.key_project.prover.sequent.Sequent {
    static final org.key_project.prover.sequent.Sequent EMPTY_SEQUENT =
        new Sequent(org.key_project.smartml.proof.calculus.Semisequent.EMPTY_SEMISEQUENT) {
            @Override
            protected org.key_project.prover.sequent.Sequent getEmptySequent() {
                return this;
            }

            @Override
            protected org.key_project.prover.sequent.Sequent createSequent(
                    Semisequent newAntecedent,
                    Semisequent newSuccedent) {
                return newAntecedent.isEmpty() && newSuccedent.isEmpty() ? this
                        : new Sequent(newAntecedent, newSuccedent);
            }

            @Override
            protected Semisequent createSemisequent(ImmutableList<SequentFormula> formulas) {
                return formulas.isEmpty()
                        ? org.key_project.smartml.proof.calculus.Semisequent.EMPTY_SEMISEQUENT
                        : new org.key_project.smartml.proof.calculus.Semisequent(formulas);
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public @NonNull Iterator<SequentFormula> iterator() {
                return ImmutableSLList.<SequentFormula>nil().iterator();
            }
        };

    /** creates new Sequent with antecedence and succedence */
    Sequent(Semisequent antecedent, Semisequent succedent) {
        super(antecedent, succedent);
    }

    /** used by NILSequent implementations */
    Sequent(Semisequent emptySeq) {
        super(emptySeq);
    }

    @Override
    protected org.key_project.prover.sequent.Sequent getEmptySequent() {
        return EMPTY_SEQUENT;
    }

    @Override
    protected org.key_project.prover.sequent.Sequent createSequent(Semisequent newAntecedent,
            Semisequent newSuccedent) {
        return newAntecedent.isEmpty() && newSuccedent.isEmpty() ? EMPTY_SEQUENT
                : new Sequent(newAntecedent, newSuccedent);
    }

    protected Semisequent createSemisequent(final ImmutableList<SequentFormula> formulas) {
        return formulas.isEmpty()
                ? org.key_project.smartml.proof.calculus.Semisequent.EMPTY_SEMISEQUENT
                : new org.key_project.smartml.proof.calculus.Semisequent(formulas);
    }

}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.logic;

import org.key_project.logic.Term;

/**
 * A sequent formula is a wrapper around a formula that occurs as top level formula in a sequent.
 * SequentFormula instances have to be unique in the sequent as they are used by PosInOccurrence to
 * determine the exact position. In earlier KeY versions this class was called ConstrainedFormula as
 * it was equipped with an additional constraints. It would be interesting to add more value to this
 * class by providing a way to add additional annotations or to cache local information about the
 * formula.
 */
public class SequentFormula extends org.key_project.prover.sequent.SequentFormula {
    /**
     * creates a new SequentFormula
     *
     * @param term a Term of sort {@link RustyDLTheory#FORMULA}
     */
    public SequentFormula(Term term) {
        super(term);
        if (term.sort() != RustyDLTheory.FORMULA) {
            throw new RuntimeException("A Term instead of a formula: " + term);
        }
    }
}
/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.logic;

import org.key_project.util.collection.ImmutableList;

public class SemisequentChangeInfo
        extends org.key_project.prover.sequent.SemisequentChangeInfo<SequentFormula> {
    public SemisequentChangeInfo() {
    }

    public SemisequentChangeInfo(
            ImmutableList<SequentFormula> formulas) {
        super(formulas);
    }

    private SemisequentChangeInfo(SemisequentChangeInfo o) {
        super(o);
    }

    public SemisequentChangeInfo copy() {
        return new SemisequentChangeInfo(this);
    }

    @Override
    public Semisequent semisequent() {
        final Semisequent semisequent;
        if (modifiedSemisequent().isEmpty()) {
            semisequent = org.key_project.rusty.logic.Semisequent.EMPTY_SEMISEQUENT;
        } else {
            semisequent = new org.key_project.rusty.logic.Semisequent(modifiedSemisequent());
        }
        return semisequent;
    }
}
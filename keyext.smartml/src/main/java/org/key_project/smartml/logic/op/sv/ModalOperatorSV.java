/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic.op.sv;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.smartml.logic.op.Modality;
import org.key_project.smartml.logic.op.sv.SchemaVariable;
import org.key_project.util.collection.ImmutableSet;

public class ModalOperatorSV extends Modality.SmartMLModalityKind implements SchemaVariable {
    /**
     * the set of modalities this sv can match
     */
    private final ImmutableSet<Modality.SmartMLModalityKind> modalities;


    /**
     * creates a new SchemaVariable that is used as placeholder for modal operators.
     *
     * @param name the Name of the SchemaVariable
     * @param modalityKinds modal operators matched by this SV
     */
    ModalOperatorSV(Name name, ImmutableSet<Modality.SmartMLModalityKind> modalityKinds) {
        super(name);
        this.modalities = modalityKinds;
    }

    /**
     * returns an unmodifiable set of operators this schemavariable can match
     */
    public ImmutableSet<Modality.SmartMLModalityKind> getModalities() {
        return modalities;
    }


    @Override
    public @NonNull String toString() {
        // TODO: HACKS, remove new-line and re-generate taclets.old.txt
        return name() + " ((modal operator))";
    }


    @Override
    public boolean isStrict() {
        return false;
    }

}

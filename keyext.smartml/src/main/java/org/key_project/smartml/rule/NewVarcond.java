/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.key_project.smartml.ast.abstraction.KeYSmartMLType;
import org.key_project.smartml.logic.op.sv.SchemaVariable;

public class NewVarcond implements org.key_project.prover.rules.conditions.NewVarcond {
    private final SchemaVariable sv;
    private final SchemaVariable peerSV;
    private final KeYSmartMLType type;

    public NewVarcond(SchemaVariable sv, SchemaVariable peerSV) {
        assert sv != null;
        assert peerSV != null;
        this.sv = sv;
        this.peerSV = peerSV;
        type = null;
    }

    public NewVarcond(SchemaVariable sv, KeYSmartMLType type) {
        assert sv != null;
        assert type != null;
        this.sv = sv;
        this.peerSV = null;
        this.type = type;
    }

    public SchemaVariable getSchemaVariable() {
        return sv;
    }

    public SchemaVariable getPeerSchemaVariable() {
        return peerSV;
    }
}

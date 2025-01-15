/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.prover.rules.conditions;


import org.key_project.logic.op.sv.SchemaVariable;

public interface NewVarcond {
    SchemaVariable getSchemaVariable();

    SchemaVariable getPeerSchemaVariable();
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.ty;

import org.key_project.logic.sort.Sort;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.abstraction.Type;
import org.key_project.smartml.ast.SmartMLProgramElement;

/**
 * A type occurring in Rust code.
 */
public interface SmartMLType extends SmartMLProgramElement {
    Type type();

    default Sort getSort(Services services) {
        return type().getSort(services);
    }
}

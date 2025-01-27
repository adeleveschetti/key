/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.abstraction;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.sort.Sort;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.abstraction.Type;

import java.util.Objects;

public class KeYSmartMLType implements org.key_project.smartml.ast.abstraction.Type {
    /** the AST type */
    private org.key_project.smartml.ast.abstraction.Type smartmlType = null;
    /** the logic sort */
    private Sort sort = null;

    public KeYSmartMLType() {
    }

    public KeYSmartMLType(org.key_project.smartml.ast.abstraction.Type smartmlType, Sort sort) {
        this.smartmlType = smartmlType;
        this.sort = sort;
    }

    public KeYSmartMLType(org.key_project.smartml.ast.abstraction.Type smartmlType) {
        this.smartmlType = smartmlType;
    }

    public KeYSmartMLType(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Sort getSort(Services services) {
        return sort;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public org.key_project.smartml.ast.abstraction.Type getSmartMLType() {
        return smartmlType;
    }

    public void setSmartMLType(Type smartmlType) {
        this.smartmlType = smartmlType;
    }

    @Override
    public @NonNull Name name() {
        return smartmlType == null ? sort.name() : smartmlType.name();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        try {
            return Objects.equals(smartmlType, ((KeYSmartMLType) o).smartmlType)
                    && Objects.equals(sort, ((KeYSmartMLType) o).sort);
        } catch (Exception e) {
            return false;
        }
    }
}

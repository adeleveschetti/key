/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule.inst;

/**
 * This exception thrown if there is no appropriate instantiation of the generic sorts occurring
 * within an "SVInstantiations"-object
 */

import org.key_project.smartml.rule.inst.GenericSortCondition;
import org.key_project.smartml.rule.inst.SortException;
import org.key_project.util.collection.ImmutableList;

import java.io.Serial;

/**
 * This exception thrown if there is no appropriate instantiation of the generic sorts occurring
 * within an "SVInstantiations"-object
 */
public class GenericSortException extends SortException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1372231759025588273L;

    private ImmutableList<org.key_project.smartml.rule.inst.GenericSortCondition> conditions;

    public GenericSortException(String description,
                                ImmutableList<org.key_project.smartml.rule.inst.GenericSortCondition> pConditions) {
        super(description);
        this.conditions = pConditions;
    }

    public GenericSortException(String description) {
        super(description);
    }

    public void setConditions(ImmutableList<GenericSortCondition> pConditions) {
        this.conditions = pConditions;
    }

    public String getMessage() {
        return super.getMessage() + (conditions == null ? "" : conditions);
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof;

import org.key_project.smartml.proof.Node;

import java.util.Iterator;

class NodeIterator implements Iterator<org.key_project.smartml.proof.Node> {
    private final Iterator<org.key_project.smartml.proof.Node> it;

    NodeIterator(Iterator<org.key_project.smartml.proof.Node> it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Node next() {
        return it.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
            "Changing the proof tree " + "structure this way is not allowed.");
    }
}

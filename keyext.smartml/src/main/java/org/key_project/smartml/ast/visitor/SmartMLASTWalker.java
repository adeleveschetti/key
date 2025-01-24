package org.key_project.smartml.ast.visitor;

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */

import org.key_project.smartml.ast.SmartMLProgramElement;

/**
 * walks through a Rust AST in depth-left-fist-order at default. Implementing method doAction
 * specifies its behaviour at the different Nodes. The depth-left-fist behaviour can be changed by
 * overwriting the method <code> walk(ProgramElement) </code>.
 */
public abstract class SmartMLASTWalker {
    /**
     * the root the walker starts
     */
    private final SmartMLProgramElement root;

    /**
     * the current visited level
     */
    private int depth = -1;

    /**
     * create the RustyASTWalker
     *
     * @param root the ProgramElement where to begin
     */
    protected SmartMLASTWalker(SmartMLProgramElement root) {
        this.root = root;
    }

    /**
     * returns start point of the walker
     *
     * @return root of the AST to walk through
     */
    public SmartMLProgramElement root() {
        return root;
    }

    /**
     * starts the walker
     */
    public void start() {
        walk(root);
    }

    /**
     * returns the current visited level
     */
    public int depth() {
        return depth;
    }

    /**
     * walks through the AST. While keeping track of the current node
     *
     * @param node the RustyProgramElement the walker is at
     */
    protected void walk(SmartMLProgramElement node) {
        if (node.getChildCount() > 0) {
            depth++;
            for (int i = 0; i < node.getChildCount(); i++) {
                walk((SmartMLProgramElement) node.getChild(i));
            }
            depth--;
        }
        // Otherwise, the node is left, so perform the action
        doAction(node);
    }

    /**
     * the action that is performed just before leaving the node the last time
     */
    protected abstract void doAction(SmartMLProgramElement node);
}


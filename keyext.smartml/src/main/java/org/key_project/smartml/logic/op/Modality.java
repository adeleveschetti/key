/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic.op;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.TermCreationException;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.logic.SmartMLBlock;
import org.key_project.smartml.logic.SmartMLDLTheory;
import org.key_project.util.collection.Pair;
import org.key_project.util.collection.WeakValueLinkedHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to represent a dynamic logic modality like diamond and box (but also
 * extensions of DL like preserves and throughout are possible in the future).
 */
public class Modality extends org.key_project.logic.op.Modality {
    /**
     * keeps track of created modalities
     */
    private static final WeakValueLinkedHashMap<Pair<SmartMLModalityKind, SmartMLProgramElement>, Modality> modalities =
        new WeakValueLinkedHashMap<>();

    /**
     * Retrieves the modality of the given kind and program.
     *
     * @param kind the kind of the modality such as diamond or box
     * @param jb the program of this modality
     * @return the modality of the given kind and program.
     */
    public static synchronized Modality getModality(SmartMLModalityKind kind, SmartMLBlock jb) {
        var pair = new Pair<>(kind, jb.program());
        Modality mod = modalities.get(pair);
        if (mod == null) {
            mod = new Modality(jb, kind);
            modalities.put(pair, mod);
        }
        return mod;
    }

    private final SmartMLBlock block;

    /**
     * Creates a modal operator with the given name
     * <strong>Creation must only be done by ???!</strong>
     *
     */
    private Modality(SmartMLBlock prg, SmartMLModalityKind kind) {
        super(kind.name(), SmartMLDLTheory.FORMULA, kind);
        this.block = prg;
    }

    @Override
    public @NonNull SmartMLBlock program() {
        return block;
    }

    @Override
    public void validTopLevelException(org.key_project.logic.Term term)
            throws TermCreationException {
        if (1 != term.arity()) {
            throw new TermCreationException(this, term);
        }

        if (1 != term.subs().size()) {
            throw new TermCreationException(this, term);
        }

        if (!term.boundVars().isEmpty()) {
            throw new TermCreationException(this, term);
        }

        if (term.sub(0) == null) {
            throw new TermCreationException(this, term);
        }
    }

    public static class SmartMLModalityKind extends Kind {
        private static final Map<String, SmartMLModalityKind> kinds = new HashMap<>();
        /**
         * The diamond operator of dynamic logic. A formula <alpha;>Phi can be read as after
         * processing
         * the program alpha there exists a state such that Phi holds.
         */
        public static final SmartMLModalityKind DIA = new SmartMLModalityKind(new Name("diamond"));
        /**
         * The box operator of dynamic logic. A formula [alpha;]Phi can be read as 'In all states
         * reachable processing the program alpha the formula Phi holds'.
         */
        public static final SmartMLModalityKind BOX = new SmartMLModalityKind(new Name("box"));

        public SmartMLModalityKind(Name name) {
            super(name);
            kinds.put(name.toString(), this);
        }

        public static SmartMLModalityKind getKind(String name) {
            return kinds.get(name);
        }

        /**
         * Whether this modality is termination sensitive, i.e., it is a "diamond-kind" modality.
         */
        public boolean terminationSensitive() {
            return (this == DIA);
        }
    }
}

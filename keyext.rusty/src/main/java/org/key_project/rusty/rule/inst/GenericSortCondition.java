/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.rule.inst;

import org.key_project.logic.sort.Sort;
import org.key_project.rusty.logic.RustyDLTheory;
import org.key_project.rusty.logic.op.sv.OperatorSV;
import org.key_project.rusty.logic.op.sv.SchemaVariable;
import org.key_project.rusty.logic.op.sv.TermSV;
import org.key_project.rusty.logic.sort.GenericSort;

public abstract class GenericSortCondition {
    private final GenericSort gs;

    /**
     * Create the condition that needs to be fulfilled for the given instantiation of a metavariable
     * to be correct, i.e. if the schemavariable is of generic sort, the instantiation of that sort
     * has to match the sort of the schemavariable's instantiation
     *
     * @return the resulting condition, if the schemavariable is of generic sort; null, if the sorts
     *         are either always compatible (no generic sorts) or never compatible (non-generic
     *         sorts that don't match)
     */
    public static GenericSortCondition createCondition(SchemaVariable sv,
            InstantiationEntry<?> p_entry) {

        if (!(p_entry instanceof TermInstantiation ti)) {
            return null;
        }

        return createCondition(((OperatorSV) sv).sort(), ti.getInstantiation().sort(),
            !subSortsAllowed(sv));
    }

    /**
     * @return <code>true</code> iff the variable <code>p_sv</code> is allowed to be instantiated
     *         with expressions that have a real subtype of the type of <code>p_sv</code>.
     *         Otherwise,
     *         the sorts have to match exactly
     */
    static boolean subSortsAllowed(SchemaVariable p_sv) {
        return p_sv instanceof TermSV && !p_sv.isStrict();
    }

    /**
     * Create the condition to make a generic sort (s0) (or a collection sort of a generic sort) and
     * a concrete sort (s1) equal
     *
     * @param p_identity true iff an identity condition should be generated (otherwise: a supersort
     *        condition is generated)
     * @return the resulting condition, if "s0" is of generic sort; null, if the sorts are either
     *         always compatible (no generic sorts) or never compatible (e.g. non-generic sorts that
     *         don't match)
     */
    protected static GenericSortCondition createCondition(Sort s0, Sort s1, boolean p_identity) {
        if (!(s0 instanceof GenericSort gs) || s1 == RustyDLTheory.FORMULA
                || s1 == RustyDLTheory.UPDATE) {
            return null;
        }

        if (p_identity) {
            return createIdentityCondition(gs, s1);
        } else {
            return createSupersortCondition(gs, s1);
        }
    }

    /**
     * Create the condition to force the instantiation of a given (possibly generic) sort
     *
     * @param p_maximum hint whether the generic sort should be instantiated with the maximum or
     *        mimimum possible concrete sort (this hint is currently not used by
     *        GenericSortInstantiations)
     * @return the resulting condition, or null if "p_s" is not generic
     */
    public static GenericSortCondition forceInstantiation(Sort p_s, boolean p_maximum) {

        if (p_s instanceof GenericSort) {
            return createForceInstantiationCondition((GenericSort) p_s, p_maximum);
        }

        return null;
    }

    /**
     * @return a condition that specifies the given generic sort to be instantiated with a supersort
     *         of the given concrete sort
     */
    public static GenericSortCondition createSupersortCondition(GenericSort p_gs, Sort p_s) {
        return new GSCSupersort(p_gs, p_s);
    }

    /**
     * @return a condition that specifies the given generic sort to be instantiated (exactly) with
     *         the given concrete sort
     */
    public static GenericSortCondition createIdentityCondition(GenericSort p_gs, Sort p_s) {
        return new GSCIdentity(p_gs, p_s);
    }

    public static GenericSortCondition createForceInstantiationCondition(GenericSort p_gs,
            boolean p_maximum) {
        return new GSCForceInstantiation(p_gs, p_maximum);
    }

    public GenericSort getGenericSort() {
        return gs;
    }


    protected GenericSortCondition(GenericSort p_gs) {
        gs = p_gs;
    }

    /**
     * returns true if the given sort <code>s</code> satisfies this generic sort condition
     *
     * @param s the Sort to check
     * @param insts a map containing already found instantiations
     * @return true if the given sort <code>s</code> satisfies this generic sort condition
     */
    public abstract boolean check(Sort s, GenericSortInstantiations insts);

    static class GSCSupersort extends GenericSortCondition {
        final Sort s;

        protected GSCSupersort(GenericSort p_gs, Sort p_s) {
            super(p_gs);
            s = p_s;
        }

        public Sort getSubsort() {
            return s;
        }

        /**
         * checks if sort <code>p_s</code> is a supersort of the <code>getSubsort</code>
         */
        public boolean check(Sort p_s, GenericSortInstantiations insts) {
            return getSubsort().extendsTrans(p_s);
        }


        /** toString */
        public String toString() {
            return "Supersort condition: " + getGenericSort() + " >= " + getSubsort();
        }

    }

    static class GSCIdentity extends GenericSortCondition {
        final Sort s;

        protected GSCIdentity(GenericSort p_gs, Sort p_s) {
            super(p_gs);
            s = p_s;
        }

        public Sort getSort() {
            return s;
        }

        /**
         * tests if <code>p_s</code> is identical to @link GSCIdentity#getSort()
         */
        public boolean check(Sort p_s, GenericSortInstantiations insts) {
            return p_s == getSort();
        }

        /** toString */
        public String toString() {
            return "Sort Identity: " + getGenericSort() + " = " + getSort();
        }
    }


    static class GSCForceInstantiation extends GenericSortCondition {
        final boolean maximum;

        protected GSCForceInstantiation(GenericSort p_gs, boolean p_maximum) {
            super(p_gs);
            maximum = p_maximum;
        }

        public boolean getMaximum() {
            return maximum;
        }

        /**
         * checks if @link GenericSortcondition#getgenericSort() has been already instantiated by
         * some sort (maximum, minimum is currently not checked)
         */
        public boolean check(Sort p_s, GenericSortInstantiations insts) {
            return insts.isInstantiated(getGenericSort());
        }

        /** toString */
        public String toString() {
            return "Force instantiation: " + getGenericSort() + ", "
                + (getMaximum() ? "maximum" : "minimum");
        }
    }
}
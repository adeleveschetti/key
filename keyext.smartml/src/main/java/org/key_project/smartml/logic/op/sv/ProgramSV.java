/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic.op.sv;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.logic.Term;
import org.key_project.logic.op.UpdateableOperator;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.ast.SourceData;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.logic.ProgramConstruct;
import org.key_project.smartml.logic.op.sv.OperatorSV;
import org.key_project.smartml.logic.sort.ProgramSVSort;
import org.key_project.smartml.rule.MatchConditions;
import org.key_project.smartml.rule.inst.ProgramList;
import org.key_project.smartml.rule.inst.SVInstantiations;
import org.key_project.util.collection.ImmutableArray;

import static org.key_project.smartml.rule.match.instructions.MatchProgramSVInstruction.convertToLogicElement;

public final class ProgramSV extends OperatorSV
        implements UpdateableOperator, ProgramConstruct {
    private final boolean isListSV;

    private static final ProgramList EMPTY_LIST_INSTANTIATION =
        new ProgramList(new ImmutableArray<>(new SmartMLProgramElement[0]));

    /**
     * creates a new SchemaVariable used as a placeholder for program constructs
     *
     * @param name the Name of the SchemaVariable allowed to match a list of program constructs
     */
    ProgramSV(Name name, ProgramSVSort s, boolean isListSV) {
        super(name, s, false, false);
        this.isListSV = isListSV;
    }

    public boolean isListSV() {
        return isListSV;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("ProgramSV " + this + " has no children");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnSchemaVariable(this);
    }

    @Override
    public MatchConditions match(SourceData source, MatchConditions matchCond) {
        if (isListSV()) {
            return matchListSV(source, matchCond);
        }

        final Services services = source.getServices();
        final SmartMLProgramElement src = source.getSource();

        final SVInstantiations instantiations = matchCond.getInstantiations();

        if (!check(src, services)) {
            return null;
        }

        final Object instant = instantiations.getInstantiation(this);
        if (instant == null || instant.equals(src)
                || (instant instanceof Term t && t.op().equals(src))) {

            matchCond = addProgramInstantiation(src, matchCond, services);

            if (matchCond == null) {
                // FAILED due to incompatibility with already found matchings
                // (e.g. generic sorts)
                return null;
            }
        } else {
            return null; // FAILED mismatch
        }
        source.next();
        return matchCond;
    }

    private MatchConditions matchListSV(SourceData source, MatchConditions matchCond) {
        final Services services = source.getServices();
        SmartMLProgramElement src = source.getSource();

        if (src == null) {
            return addProgramInstantiation(EMPTY_LIST_INSTANTIATION, matchCond, services);
        }

        final java.util.ArrayList<SmartMLProgramElement> matchedElements =
            new java.util.ArrayList<>();

        while (src != null) {
            if (!check(src, services)) {
                break;
            }
            matchedElements.add(src);
            source.next();
            src = source.getSource();
        }

        return addProgramInstantiation(
            new ProgramList(new ImmutableArray<>(matchedElements)), matchCond,
            services);
    }

    /**
     * adds a found mapping from schema variable <code>var</code> to program element <code>pe</code>
     * and returns the updated match conditions or null if mapping is not possible because of
     * violating some variable condition
     *
     * @param pe the ProgramElement <code>var</code> is mapped to
     * @param matchCond the MatchConditions to be updated
     * @param services the Services provide access to the Java model
     * @return the updated match conditions including mapping <code>var</code> to <code>pe</code> or
     *         null if some variable condition would be hurt by the mapping
     */
    private MatchConditions addProgramInstantiation(SmartMLProgramElement pe,
            MatchConditions matchCond,
            Services services) {
        if (matchCond == null) {
            return null;
        }

        SVInstantiations insts = matchCond.getInstantiations();

        final Object foundInst = insts.getInstantiation(this);

        if (foundInst != null) {
            final Object newInst;
            if (foundInst instanceof Term) {
                newInst = convertToLogicElement(pe, services);
            } else {
                newInst = pe;
            }

            if (foundInst.equals(newInst)) {
                return matchCond;
            } else {
                return null;
            }
        }

        insts = insts.add(this, pe, services);
        return insts == null ? null : matchCond.setInstantiations(insts);
    }

    /**
     * adds a found mapping from schema variable <code>var</code> to the list of program elements
     * <code>list</code> and returns the updated match conditions or null if mapping is not possible
     * because of violating some variable condition
     *
     * @param list the ProgramList <code>var</code> is mapped to
     * @param matchCond the MatchConditions to be updated
     * @param services the Services provide access to the Java model
     * @return the updated match conditions including mapping <code>var</code> to <code>list</code>
     *         or null if some variable condition would be hurt by the mapping
     */
    private MatchConditions addProgramInstantiation(ProgramList list, MatchConditions matchCond,
            Services services) {
        if (matchCond == null) {
            return null;
        }

        SVInstantiations insts = matchCond.getInstantiations();
        final ProgramList pl = (ProgramList) insts.getInstantiation(this);
        if (pl != null) {
            if (pl.equals(list)) {
                return matchCond;
            } else {
                return null;
            }
        }

        insts = insts.add(this, list, services);
        return insts == null ? null : matchCond.setInstantiations(insts);
    }

    /**
     * returns true, if the given SchemaVariable can stand for the ProgramElement
     *
     * @param match the ProgramElement to be matched
     * @param services the Services object encapsulating information about the java datastructures
     *        like (static)types etc.
     * @return true if the SchemaVariable can stand for the given element
     */
    private boolean check(SmartMLProgramElement match, Services services) {
        if (match == null) {
            return false;
        }
        return ((ProgramSVSort) sort()).canStandFor(match, services);
    }
}

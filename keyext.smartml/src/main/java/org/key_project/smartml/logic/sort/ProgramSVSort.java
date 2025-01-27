/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.logic.sort;

import org.key_project.logic.Name;
import org.key_project.logic.Term;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.ast.expr.*;
import org.key_project.smartml.logic.op.ProgramVariable;
import org.key_project.util.collection.DefaultImmutableSet;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ProgramSVSort extends SortImpl {
    private static final Map<Name, ProgramSVSort> NAME2SORT = new LinkedHashMap<>(60);

    // ----------- Types of Expression Program SVs ----------------------------
    public static final ProgramSVSort LEFT_HAND_SIDE = new LeftHandSideSort();
    public static final ProgramSVSort VARIABLE = new ProgramVariableSort();
    public static final ProgramSVSort SIMPLE_EXPRESSION = new SimpleExpressionSort();
    public static final ProgramSVSort NON_SIMPLE_EXPRESSION = new NonSimpleExpressionSort();
    public static final ProgramSVSort EXPRESSION = new ExpressionSort();
    //public static final ProgramSVSort BLOCK_EXPRESSION = new BlockExpressionSort();
    public static final ProgramSVSort BOOL_EXPRESSION = new BoolExpressionSort();
    public static final ProgramSVSort SIMPLE_BOOL_EXPRESSION = new SimpleBoolExpressionSort();
    public static final ProgramSVSort NON_SIMPLE_BOOL_EXPRESSION =
        new NonSimpleBoolExpressionSort();

    // ----------- Types of Statement Program SVs -----------------------------
    //public static final ProgramSVSort STATEMENT = new StatementSort();
    public static final ProgramSVSort TYPE = new TypeReferenceSort();
    public static final ProgramSVSort TYPE_PRIMITIVE = new TypeReferencePrimitiveSort();

    protected ProgramSVSort(Name name) {
        super(name, false, DefaultImmutableSet.nil());
        NAME2SORT.put(name, this);
    }

    public boolean canStandFor(Term t) {
        return true;
    }

    public abstract boolean canStandFor(SmartMLProgramElement check, Services services);

    public ProgramSVSort createInstance(String parameter) {
        throw new UnsupportedOperationException();
    }

    /**
     * TODO: <a href=
     * "https://doc.rust-lang.org/reference/expressions.html#place-expressions-and-value-expressions">Follow
     * this</a>
     */
    private static class LeftHandSideSort extends ProgramSVSort {

        public LeftHandSideSort() {
            super(new Name("LeftHandSide"));
        }

        public LeftHandSideSort(Name name) {
            super(name);
        }

        @Override
        public boolean canStandFor(Term t) {
            return t.op() instanceof ProgramVariable;
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement pe, Services services) {
            // TODO: unify PathExpr and PV?
            return pe instanceof ProgramVariable || pe instanceof PathExpression;
        }
    }

    /**
     * This sort represents a type of program schema variables that match only on
     * program variables
     */
    private static class ProgramVariableSort extends LeftHandSideSort {
        public ProgramVariableSort() {
            super(new Name("Variable"));
        }
    }

    /**
     * This sort represents a type of program schema variables that match only on
     * <ul>
     * <li>program variables or
     * <li>(negated) literal expressions
     * </ul>
     */
    private static class SimpleExpressionSort extends ProgramSVSort {

        public SimpleExpressionSort() {
            super(new Name("SimpleExpression"));
        }

        protected SimpleExpressionSort(Name n) {
            super(n);
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement pe, Services services) {
            if (pe instanceof NegationExpression ne
                    && ne.getChild(0) instanceof IntegerLiteralExpression) {
                return true;
            }

            if (pe instanceof LiteralExpression)
                return true;

            return VARIABLE.canStandFor(pe, services);
        }
    }

    /**
     * This sort represents a type of program schema variables that match only on all expressions
     * which are not matched by simple expression SVs.
     */
    private static class NonSimpleExpressionSort extends ProgramSVSort {

        public NonSimpleExpressionSort() {
            super(new Name("NonSimpleExpression"));
        }

        protected NonSimpleExpressionSort(Name n) {
            super(n);
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            if (!(check instanceof Expr))
                return false;
            return !SIMPLE_EXPRESSION.canStandFor(check, services);
        }
    }

    /**
     * This sort represents a type of program schema variables that match on all expressions only.
     */
    private static class ExpressionSort extends ProgramSVSort {
        public ExpressionSort() {
            super(new Name("Expression"));
        }

        protected ExpressionSort(Name n) {
            super(n);
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement pe, Services services) {
            return pe instanceof Expr;
        }
    }

   /* private static class BlockExpressionSort extends ProgramSVSort {
        public BlockExpressionSort() {
            super(new Name("BlockExpression"));
        }

        protected BlockExpressionSort(Name n) {
            super(n);
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            return check instanceof BlockExpression;
        }
    }*/

    /**
     * This sort represents a type of program schema variables that match only on statements
     */
    /*private static class StatementSort extends ProgramSVSort {
        public StatementSort() {
            super(new Name("Statement"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement pe, Services services) {
            return pe instanceof Statement;
        }
    }*/

    /**
     * This sort represents a type of program schema variables that match only on type references.
     */
    private static final class TypeReferenceSort extends ProgramSVSort {
        public TypeReferenceSort() {
            super(new Name("Type"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            // TODO
            return false;
        }
    }

    /**
     * This sort represents a type of program schema variables that matches byte,
     * char, short, int, and long.
     */
    private static final class TypeReferencePrimitiveSort extends ProgramSVSort {
        public TypeReferencePrimitiveSort() {
            super(new Name("PrimitiveType"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            // TODO
            return false;
        }
    }

    public static Map<Name, ProgramSVSort> name2sort() {
        return NAME2SORT;
    }

    private static final class BoolExpressionSort extends ProgramSVSort {
        public BoolExpressionSort() {
            super(new Name("BoolExpression"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            return check instanceof Expr;
            // TODO: check type here
        }
    }

    private static final class SimpleBoolExpressionSort extends ProgramSVSort {
        public SimpleBoolExpressionSort() {
            super(new Name("SimpleBoolExpression"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            return SIMPLE_EXPRESSION.canStandFor(check, services)
                    && BOOL_EXPRESSION.canStandFor(check, services);
        }
    }

    private static final class NonSimpleBoolExpressionSort extends ProgramSVSort {
        public NonSimpleBoolExpressionSort() {
            super(new Name("NonSimpleBoolExpression"));
        }

        @Override
        public boolean canStandFor(SmartMLProgramElement check, Services services) {
            return NON_SIMPLE_EXPRESSION.canStandFor(check, services)
                    && BOOL_EXPRESSION.canStandFor(check, services);
        }
    }
}

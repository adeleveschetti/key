/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.rule;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Term;
import org.key_project.logic.Visitor;
import org.key_project.logic.op.Operator;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.logic.op.UpdateableOperator;
import org.key_project.prover.rules.Rule;
import org.key_project.prover.rules.RuleApp;
import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
//import org.key_project.smartml.ast.expr.ContextBlockExpression;
//import org.key_project.smartml.ast.visitor.ProgramContextAdder;
//import org.key_project.smartml.ast.visitor.ProgramReplaceVisitor;
import org.key_project.smartml.logic.SmartMLBlock;
import org.key_project.smartml.logic.TermBuilder;
import org.key_project.smartml.logic.op.ElementaryUpdate;
import org.key_project.smartml.logic.op.Modality;
import org.key_project.smartml.logic.op.SubstOp;
import org.key_project.smartml.logic.op.sv.ModalOperatorSV;
import org.key_project.smartml.logic.op.sv.ProgramSV;
import org.key_project.smartml.logic.op.sv.SchemaVariable;
import org.key_project.smartml.proof.Goal;
import org.key_project.smartml.rule.inst.ContextInstantiationEntry;
import org.key_project.smartml.rule.inst.SVInstantiations;
import org.key_project.util.collection.ImmutableArray;

import java.util.Stack;

/**
 * visitor for <t> execPostOrder </t> of {@link Term}. Called with that method
 * on a term, the visitor builds a new term replacing SchemaVariables with their instantiations that
 * are given as a SVInstantiations object.
 */
public class SyntacticalReplaceVisitor implements Visitor<@NonNull Term> {
    protected final SVInstantiations svInst;
    protected final Services services;
    /** the termbuilder used to construct terms */
    protected final TermBuilder tb;
    private Term computedResult = null;
    protected final PosInOccurrence applicationPosInOccurrence;
    protected final Rule rule;
    protected final Goal goal;
    protected final RuleApp ruleApp;


    /**
     * the stack contains the subterms that will be added in the next step of execPostOrder in Term
     * in order to build the new term. A boolean value between or under the subterms on the stack
     * indicate that a term using these subterms should build a new term instead of using the old
     * one, because one of its subterms has been built, too.
     */
    private final Stack<Object> subStack; // of Term (and Boolean)
    private final Boolean newMarker = Boolean.TRUE;

    /**
     * constructs a term visitor replacing any occurrence of a schemavariable found in
     * {@code svInst} by its instantiation
     *
     * @param applicationPosInOccurrence the application position
     * @param svInst mapping of schemavariables to their instantiation
     * @param goal the current goal
     * @param rule the applied rule
     * @param ruleApp the rule application
     * @param services the Services
     * @param termBuilder the TermBuilder to use (allows to use the non cached version)
     */
    private SyntacticalReplaceVisitor(
            PosInOccurrence applicationPosInOccurrence, SVInstantiations svInst, Goal goal,
            Rule rule, RuleApp ruleApp, Services services, TermBuilder termBuilder) {
        this.services = services;
        this.tb = termBuilder;
        this.svInst = svInst;
        this.applicationPosInOccurrence = applicationPosInOccurrence;
        this.rule = rule;
        this.ruleApp = ruleApp;
        this.goal = goal;
        subStack = new Stack<>(); // of Term
    }

    /**
     * constructs a term visitor replacing any occurrence of a schemavariable found in
     * {@code svInst} by its instantiation
     *
     * @param applicationPosInOccurrence the application position
     * @param svInst mapping of schemavariables to their instantiation
     * @param goal the current goal
     * @param rule the applied rule
     * @param ruleApp the rule application
     * @param services the Services
     */
    public SyntacticalReplaceVisitor(
            PosInOccurrence applicationPosInOccurrence, SVInstantiations svInst, Goal goal,
            Rule rule, RuleApp ruleApp, Services services) {
        this(applicationPosInOccurrence, svInst, goal, rule, ruleApp,
            services, services.getTermBuilder());
    }

    /**
     * performs the syntactic replacement of schemavariables with their instantiations
     */
    @Override
    public void visit(final Term visited) {
        // Sort equality has to be ensured before calling this method
        Operator visitedOp = visited.op();
        if (visitedOp instanceof SchemaVariable sv && visitedOp.arity() == 0
                && svInst.isInstantiated(sv)
                && (!(visitedOp instanceof ProgramSV psv && psv.isListSV()))) {
            final Term newTerm = toTerm(svInst.getTermInstantiation(sv,
                /* svInst.getExecutionContext(), */ services));
            pushNew(newTerm);
        } else {
            // instantiation of Rust block
            boolean rBlockChanged = false;

            if (visitedOp instanceof Modality mod) {
                var rb = mod.program();
                var olfRb = rb;
                rb = replacePrg(svInst, rb);
                if (rb != olfRb) {
                    rBlockChanged = true;
                }

                visitedOp = instantiateModality(mod, rb);
            }

            final Operator newOp = instantiateOperator(visitedOp);

            // instantiate bound variables
            final var boundVars =
                instantiateBoundVariables(visited);

            // instantiate sub terms
            final Term[] neededsubs = neededSubs(newOp != null ? newOp.arity() : 0);
            if (boundVars != visited.boundVars() || rBlockChanged || (newOp != visitedOp)
                    || (!subStack.empty() && subStack.peek() == newMarker)) {
                final Term newTerm = tb.tf().createTerm(newOp, neededsubs,
                    (ImmutableArray<QuantifiableVariable>) boundVars);
                pushNew(resolveSubst(newTerm));
            } else {
                Term t;
                t = visited;
                t = resolveSubst(t);
                if (t == visited) {
                    subStack.push(t);
                } else {
                    pushNew(t);
                }
            }
        }
    }

    /*private SmartMLProgramElement addContext(ContextBlockExpression pe) {
        final ContextInstantiationEntry cie = svInst.getContextInstantiation();
        if (cie == null) {
            throw new IllegalStateException("Context should also be instantiated");
        }

        if (cie.prefix() != null) {
            return ProgramContextAdder.INSTANCE.start(
                cie.contextProgram(), pe, cie.getInstantiation());
        }

        return pe;
    }*/

    private SmartMLBlock replacePrg(SVInstantiations svInst, SmartMLBlock rb) {
        return null;
        /*
        if (svInst.isEmpty()) {
            return rb;
        }

        ProgramReplaceVisitor trans;
        SmartMLProgramElement result;

        if (rb.program() instanceof ContextBlockExpression cbe) {
            trans = new ProgramReplaceVisitor(
                cbe,
                services, svInst);
            trans.start();
            result = addContext((ContextBlockExpression) trans.result());
        } else {
            trans = new ProgramReplaceVisitor(rb.program(), services, svInst);
            trans.start();
            result = trans.result();
        }
        return (result == rb.program()) ? rb : new SmartMLBlock(result);

         */
    }

    protected void pushNew(Object t) {
        if (subStack.empty() || subStack.peek() != newMarker) {
            subStack.push(newMarker);
        }
        subStack.push(t);
    }

    /**
     * the method is only still invoked to allow the
     * TODO to recursively replace meta variables
     */
    protected Term toTerm(Term o) {
        return o;
    }

    private Term resolveSubst(Term t) {
        if (t.op() instanceof SubstOp substOp) {
            return substOp.apply(t, tb);
        } else {
            return t;
        }
    }

    private Term[] neededSubs(int n) {
        boolean newTerm = false;
        Term[] result = new Term[n];
        for (int i = n - 1; i >= 0; i--) {
            Object top = subStack.pop();
            if (top == newMarker) {
                newTerm = true;
                top = subStack.pop();
            }
            result[i] = (Term) top;
        }
        if (newTerm && (subStack.empty() || subStack.peek() != newMarker)) {
            subStack.push(newMarker);
        }
        return result;
    }

    private ImmutableArray<? extends QuantifiableVariable> instantiateBoundVariables(Term visited) {
        var vBoundVars = visited.boundVars();
        if (!vBoundVars.isEmpty()) {
            final QuantifiableVariable[] newVars = new QuantifiableVariable[vBoundVars.size()];
            boolean varsChanged = false;

            for (int j = 0, size = vBoundVars.size(); j < size; j++) {
                QuantifiableVariable boundVar = vBoundVars.get(j);
                if (boundVar instanceof SchemaVariable boundSchemaVariable) {
                    final Term instantiationForBoundSchemaVariable =
                        (Term) svInst.getInstantiation(boundSchemaVariable);
                    // instantiation case might be null in case of PO generation for taclets
                    if (instantiationForBoundSchemaVariable != null) {
                        boundVar = (QuantifiableVariable) instantiationForBoundSchemaVariable.op();
                        varsChanged = true;
                    }
                }
                newVars[j] = boundVar;
            }

            if (varsChanged) {
                vBoundVars = new ImmutableArray<>(newVars);
            }
        }
        return vBoundVars;
    }

    private Operator instantiateOperator(Operator p_operatorToBeInstantiated) {
        Operator instantiatedOp = p_operatorToBeInstantiated;
        /*
         * if (p_operatorToBeInstantiated instanceof SortDependingFunction) {
         * instantiatedOp =
         * handleSortDependingSymbol((SortDependingFunction) p_operatorToBeInstantiated);
         * } else
         */ if (p_operatorToBeInstantiated instanceof ElementaryUpdate elementaryUpdate) {
            instantiatedOp =
                instantiateElementaryUpdate(elementaryUpdate);
        } else if (p_operatorToBeInstantiated instanceof SchemaVariable sv) {
            if (!(p_operatorToBeInstantiated instanceof ProgramSV programSV)
                    || !programSV.isListSV()) {
                instantiatedOp = (Operator) svInst.getInstantiation(sv);
            }
        }
        assert instantiatedOp != null;

        return instantiatedOp;
    }

    private ElementaryUpdate instantiateElementaryUpdate(ElementaryUpdate op) {
        final UpdateableOperator originalLhs = op.lhs();
        if (originalLhs instanceof SchemaVariable orginalLhsAsSV) {
            Object lhsInst = svInst.getInstantiation(orginalLhsAsSV);
            if (lhsInst instanceof Term lhsInstAsTerm) {
                lhsInst = lhsInstAsTerm.op();
            }

            if (!(lhsInst instanceof final UpdateableOperator newLhs)) {
                assert false : "not updateable: " + lhsInst;
                throw new IllegalStateException("Encountered non-updateable operator " + lhsInst
                    + " on left-hand side of update.");
            }
            return newLhs == originalLhs ? op : ElementaryUpdate.getInstance(newLhs);
        } else {
            return op;
        }
    }

    private Operator instantiateModality(Modality op, SmartMLBlock rb) {
        Modality.SmartMLModalityKind kind = op.kind();
        if (op.kind() instanceof ModalOperatorSV) {
            kind = (Modality.SmartMLModalityKind) svInst.getInstantiation(op.kind());
        }
        if (rb != op.program() || kind != op.kind()) {
            return Modality.getModality(kind, rb);
        }
        return op;
    }

    /**
     * delivers the new built term
     */
    public Term getTerm() {
        if (computedResult == null) {
            Object o;
            do {
                o = subStack.pop();
            } while (o == newMarker);
            // Term t = (Term) o;
            // CollisionDeletingSubstitutionTermApplier substVisit
            // = new CollisionDeletingSubstitutionTermApplier();
            // t.execPostOrder(substVisit);
            // t=substVisit.getTerm();
            computedResult = (Term) o;
        }
        return computedResult;
    }
}

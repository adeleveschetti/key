/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.proof.io;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.Namespace;
import org.key_project.logic.PosInTerm;
import org.key_project.logic.Term;
import org.key_project.logic.op.Function;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstDirect;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstSeq;
import org.key_project.prover.rules.instantiation.AssumesFormulaInstantiation;
import org.key_project.prover.sequent.PosInOccurrence;
import org.key_project.prover.sequent.Sequent;
import org.key_project.prover.sequent.SequentFormula;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.SmartMLProgramElement;
import org.key_project.smartml.logic.NamespaceSet;
import org.key_project.smartml.logic.op.LogicVariable;
import org.key_project.smartml.logic.op.Modality;
import org.key_project.smartml.logic.op.ProgramVariable;
import org.key_project.smartml.logic.op.sv.ModalOperatorSV;
import org.key_project.smartml.logic.op.sv.ProgramSV;
import org.key_project.smartml.logic.op.sv.SkolemTermSV;
import org.key_project.smartml.logic.op.sv.VariableSV;
import org.key_project.smartml.parser.KeYIO;
import org.key_project.smartml.proof.Goal;
import org.key_project.smartml.proof.Node;
import org.key_project.smartml.proof.Proof;
import org.key_project.smartml.proof.io.intermediate.AppNodeIntermediate;
import org.key_project.smartml.proof.io.intermediate.BranchNodeIntermediate;
import org.key_project.smartml.proof.io.intermediate.NodeIntermediate;
import org.key_project.smartml.proof.io.intermediate.TacletAppIntermediate;
import org.key_project.smartml.rule.*;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;
import org.key_project.util.collection.ImmutableSet;
import org.key_project.util.collection.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IntermediateProofReplayer {
    private static final String ERROR_LOADING_PROOF_LINE = "Error loading proof.\n";
    private static final String NOT_APPLICABLE =
        " not available or not applicable in this context.";

    /** The problem loader, for reporting errors */
    private final AbstractProblemLoader loader;
    /** The proof object into which to load the replayed proof */
    private Proof proof = null;

    /** Encountered errors */
    private final List<Throwable> errors = new LinkedList<>();
    /** Error status */
    private String status = "";

    /** Stores open branches */
    private final LinkedList<Pair<Node, NodeIntermediate>> queue =
        new LinkedList<>();

    /** The current open goal */
    private Goal currGoal = null;

    /**
     * Constructs a new {@link IntermediateProofReplayer}.
     *
     * @param loader The problem loader, for reporting errors.
     * @param proof The proof object into which to load the replayed proof.
     * @param parserResult the result of the proof file parser to be replayed
     */
    public IntermediateProofReplayer(AbstractProblemLoader loader, Proof proof,
            IntermediatePresentationProofFileParser.Result parserResult) {
        this.proof = proof;
        this.loader = loader;

        queue.addFirst(
            new Pair<>(proof.root(), parserResult.parsedResult()));
    }

    /**
     * @return the lastSelectedGoal
     */
    public Goal getLastSelectedGoal() {
        return currGoal;
    }

    /**
     * Starts the actual replay process. Results are stored in the supplied proof object; the last
     * selected goal may be obtained by {@link #getLastSelectedGoal()}.
     * Note: This method deletes the intermediate proof tree!
     */
    public Result replay() {
        return replay(true);
    }

    /**
     * Starts the actual replay process. Results are stored in the supplied
     * proof object; the last selected goal may be obtained by
     * {@link #getLastSelectedGoal()}.
     *
     * @param deleteIntermediateTree indicates if the intermediate proof tree should be
     *        deleted (set to false if it shal be kept for further use)
     * @return result of the replay procedure (see {@link Result})
     */
    public Result replay(boolean deleteIntermediateTree) {
        // initialize progress monitoring
        int stepIndex = 0;
        int reportInterval = 1;
        int max = 0;
        var time = System.nanoTime();

        while (!queue.isEmpty()) {
            stepIndex++;

            final Pair<Node, NodeIntermediate> currentP = queue.pollFirst();
            final Node currNode = currentP.first;
            final NodeIntermediate currNodeInterm = currentP.second;
            currGoal = proof.getOpenGoal(currNode);

            try {
                if (currNodeInterm instanceof BranchNodeIntermediate) {
                    assert currNodeInterm.getChildren().size() <= 1
                            : "Branch node should have exactly one child.";
                    if (currNodeInterm.getChildren().size() == 1) {
                        // currNode.getNodeInfo().setBranchLabel(
                        // bn.getBranchTitle());
                        queue.addFirst(new Pair<>(currNode,
                            currNodeInterm.getChildren().get(0)));
                    }
                } else if (currNodeInterm instanceof AppNodeIntermediate currInterm) {
                    // currNode.getNodeInfo().setNotes(currInterm.getNotes());

                    // Register name proposals
                    // proof.getServices().getNameRecorder()
                    // .setProposals(currInterm.getIntermediateRuleApp().getNewNames());

                    if (currInterm.getIntermediateRuleApp() instanceof TacletAppIntermediate tai) {
                        TacletAppIntermediate appInterm =
                            tai;

                        try {
                            currGoal.apply(constructTacletApp(appInterm, currGoal));

                            final Iterator<Node> children = currNode.childrenIterator();
                            final LinkedList<NodeIntermediate> intermChildren =
                                currInterm.getChildren();

                            addChildren(children, intermChildren);

                            // set information about SUCCESSFUL rule application
                            // currNode.getNodeInfo().setInteractiveRuleApplication(
                            // currInterm.isInteractiveRuleApplication());
                            // currNode.getNodeInfo()
                            // .setScriptRuleApplication(currInterm.isScriptRuleApplication());

                            if (deleteIntermediateTree) {
                                // Children are no longer needed, set them to null
                                // to free memory.
                                currInterm.setChildren(null);
                            }


                        } catch (Exception | AssertionError e) {
                            reportError(ERROR_LOADING_PROOF_LINE + "Line " +
                                appInterm.getLineNr()
                                + ", goal " + currGoal.getNode().getSerialNr() + ", rule "
                                + appInterm.getRuleName() + NOT_APPLICABLE, e);
                        }
                    }
                }
            } catch (Throwable throwable) {
                // Default exception catcher -- proof should not stop loading
                // if anything goes wrong, but instead continue with the next
                // node in the queue.
                reportError(ERROR_LOADING_PROOF_LINE, throwable);
            }
        }
        return new Result(status, errors, currGoal);
    }

    /**
     * Adds the pairs of proof node children and intermediate children to the queue. At the moment,
     * they are added in the order they were parsed. For the future, it may be sensible to choose a
     * different procedure, for instance one that minimizes the number of open goals per time
     * interval to save memory. Note that in this case, some test cases might be adapted which
     * depend on fixed node serial numbers.
     *
     * @param children Iterator of proof node children.
     * @param intermChildren List of corresponding intermediate children.
     */
    private void addChildren(Iterator<Node> children, LinkedList<NodeIntermediate> intermChildren) {
        int i = 0;
        while (!currGoal.getNode().isClosed() && children.hasNext() && !intermChildren.isEmpty()) {
            // NOTE: In the case of an unfinished proof, there
            // is another node after the last application which
            // is not represented by an intermediate
            // application. Therefore, we have to add the last
            // check in the above conjunction.

            Node child = children.next();
            // if (!proof.getOpenGoal(child).isLinked()) {
            queue.add(i, new Pair<>(child, intermChildren.get(i++)));
            // }
        }
    }

    /**
     * Stores an error in the list.
     *
     * @param string Description text.
     * @param e Error encountered.
     */
    private void reportError(String string, Throwable e) {
        status = "Errors while reading the proof. Not all branches could be load successfully.";
        errors.add(new ProblemLoaderException(loader, string, e));
    }

    /**
     * Communicates a non-fatal condition to the caller. Empty string means everything is OK. The
     * message will be displayed to the user in the GUI after the proof has been parsed.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return errors encountered during replay.
     */
    public Collection<Throwable> getErrors() {
        return errors;
    }

    /**
     * Constructs a taclet application from an intermediate one.
     *
     * @param currInterm The intermediate taclet application to create a "real" application for.
     * @param currGoal The goal on which to apply the taclet app.
     * @return The taclet application corresponding to the supplied intermediate representation.
     * @throws TacletAppConstructionException In case of an error during construction.
     */
    private TacletApp constructTacletApp(TacletAppIntermediate currInterm, Goal currGoal)
            throws TacletAppConstructionException {

        final String tacletName = currInterm.getRuleName();
        final int currFormula = currInterm.getPosInfo().first;
        final PosInTerm currPosInTerm = currInterm.getPosInfo().second;
        final Sequent seq = currGoal.sequent();

        TacletApp ourApp;
        PosInOccurrence pos = null;

        Name name = new Name(tacletName);
        Taclet t = proof.getInitConfig().lookupActiveTaclet(name);
        if (t == null) {
            ourApp = currGoal.indexOfTaclets().lookup(name);
        } else {
            ourApp = NoPosTacletApp.createNoPosTacletApp(t);
        }

        if (ourApp == null) {
            throw new TacletAppConstructionException(
                "Unknown taclet with name \"" + tacletName + "\"");
        }

        Services services = proof.getServices();

        if (currFormula != 0) { // otherwise we have no pos
            try {
                pos = PosInOccurrence.findInSequent(currGoal.sequent(), currFormula, currPosInTerm);

                /*
                 * part of the fix for #1716: ensure that position of find term
                 * (antecedent/succedent) matches the kind of the taclet.
                 */
                Taclet taclet = ourApp.taclet();
                if (taclet instanceof AntecTaclet && !pos.isInAntec()) {
                    throw new TacletAppConstructionException("The taclet " + taclet.name()
                        + " can not be applied to a formula/term in succedent.");
                } else if (taclet instanceof SuccTaclet && pos.isInAntec()) {
                    throw new TacletAppConstructionException("The taclet " + taclet.name()
                        + " can not be applied to a formula/term in antecedent.");
                }

                ourApp = ((NoPosTacletApp) ourApp).matchFind(pos, services);
                ourApp = ourApp.setPosInOccurrence(pos, services);
            } catch (TacletAppConstructionException e) {
                throw e;
            } catch (Exception e) {
                throw new TacletAppConstructionException("Wrong position information: " + pos, e);
            }
        }

        ourApp = constructInsts(ourApp, currGoal, currInterm.getInsts(), services);

        ImmutableList<AssumesFormulaInstantiation> ifFormulaList =
            ImmutableSLList.nil();
        for (String ifFormulaStr : currInterm.getIfSeqFormulaList()) {
            ifFormulaList =
                ifFormulaList
                        .append(new AssumesFormulaInstSeq(seq, Integer.parseInt(ifFormulaStr)));
        }
        for (String ifFormulaStr : currInterm.getIfDirectFormulaList()) {
            // MU 2019: #1487. We have to use the right namespaces to not
            // ignore branch-local functions
            NamespaceSet nss = currGoal.getLocalNamespaces();
            Term term = parseTerm(ifFormulaStr, proof, nss.variables(), nss.programVariables(),
                nss.functions());
            ifFormulaList =
                ifFormulaList.append(new AssumesFormulaInstDirect(new SequentFormula(term)));
        }

        if (!ourApp.ifInstsCorrectSize(ifFormulaList)) {
            // LOGGER.warn("Proof contains wrong number of \\assumes instatiations for {}",
            // tacletName);
            // try to find instantiations automatically
            ImmutableList<TacletApp> instApps = ourApp.findIfFormulaInstantiations(seq, services);
            if (instApps.size() != 1) {
                // none or not a unique result
                throw new TacletAppConstructionException("\nCould not apply " + tacletName
                    + "\nUnknown instantiations for \\assumes. " + instApps.size()
                    + " candidates.\n" + "Perhaps the rule's definition has been changed in KeY.");
            }

            TacletApp newApp = instApps.head();
            ifFormulaList = newApp.assumesFormulaInstantiations();
        }

        // TODO: In certain cases, the below method call returns null and
        // induces follow-up NullPointerExceptions. This was encountered
        // in a proof of the TimSort method binarySort with several joins.
        ourApp = ourApp.setIfFormulaInstantiations(ifFormulaList, services);

        if (!ourApp.complete()) {
            ourApp = ourApp.tryToInstantiate(proof.getServices());
        }

        return ourApp;
    }

    /**
     * Instantiates schema variables in the given taclet application.
     *
     * @param app The taclet application to instantiate.
     * @param currGoal The corresponding goal.
     * @param loadedInsts Loaded schema variable instantiations.
     * @param services The services object.
     * @return The instantiated taclet.
     */
    public static TacletApp constructInsts(@NonNull TacletApp app, Goal currGoal,
            Collection<String> loadedInsts, Services services) {
        if (loadedInsts == null) {
            return app;
        }
        ImmutableSet<SchemaVariable> uninsts = app.uninstantiatedVars();

        // first pass: add variables
        for (final String s : loadedInsts) {
            int eq = s.indexOf('=');
            final String varname = s.substring(0, eq);

            SchemaVariable sv = lookupName(uninsts, varname);
            if (sv == null) {
                // throw new IllegalStateException(
                // varname+" from \n"+loadedInsts+"\n is not in\n"+uninsts);
                // LOGGER.error("{} from {} is not in uninsts", varname, app.rule().name());
                continue;
            }
            final String value = s.substring(eq + 1);
            if (sv instanceof VariableSV vsv) {
                app = parseSV1(app, vsv, value, services);
            }
        }

        // second pass: add everything else
        uninsts = app.uninstantiatedVars();
        for (final String s : loadedInsts) {
            int eq = s.indexOf('=');
            final String varname = s.substring(0, eq);
            final SchemaVariable sv = lookupName(uninsts, varname);
            if (sv == null) {
                continue;
            }

            String value = s.substring(eq + 1);
            app = parseSV2(app, sv, value, currGoal);
        }

        return app;
    }

    /**
     * Finds a schema variable in the given set.
     *
     * @param set The set to search.
     * @param name The name to search for.
     * @return The found schema variable, or null if it is not present in the set.
     */
    private static SchemaVariable lookupName(ImmutableSet<SchemaVariable> set, String name) {
        for (SchemaVariable v : set) {
            if (v.name().toString().equals(name)) {
                return v;
            }
        }
        return null; // handle this better!
    }

    /**
     * Parses a given term in String representation.
     *
     * @param value String to parse.
     * @param proof Proof object (for namespaces and Services object).
     * @param varNS Variable namespace.
     * @param progVarNS Program variable namespace.
     * @return The parsed term.
     */
    public static Term parseTerm(String value, Proof proof,
            Namespace<@NonNull QuantifiableVariable> varNS,
            Namespace<@NonNull ProgramVariable> progVarNS, Namespace<@NonNull Function> functNS) {
        var io = new KeYIO(proof.getServices());
        return io.parseExpression(value);
    }

    /**
     * Instantiates a schema variable in the given taclet application. 1st pass: only VariableSV.
     *
     * @param app Application to instantiate.
     * @param sv VariableSV to instantiate.
     * @param value Name for the instantiated logic variable.
     * @param services The services object.
     * @return An instantiated taclet application, where the schema variable has been instantiated
     *         by a logic variable of the given name.
     */
    public static TacletApp parseSV1(TacletApp app, VariableSV sv, String value,
            Services services) {
        // TODO
        LogicVariable lv = new LogicVariable(1, app.getRealSort(sv));
        Term instance = services.getTermFactory().createTerm(lv);
        return app.addCheckedInstantiation(sv, instance, services, true);
    }

    /**
     * Instantiates a schema variable in the given taclet application. 2nd pass: All other schema
     * variables.
     *
     * @param app Application to instantiate.
     * @param sv Schema variable to instantiate.
     * @param value Name for the instantiated Skolem constant, program element or term..
     * @param targetGoal The goal corresponding to the given application.
     * @return An instantiated taclet application, where the schema variable has been instantiated,
     *         depending on its type, by a Skolem constant, program element, or term of the given
     *         name.
     * @see #parseSV1(TacletApp, VariableSV, String, Services)
     */
    public static TacletApp parseSV2(TacletApp app, SchemaVariable sv, String value,
            Goal targetGoal) {
        final Proof p = targetGoal.proof();
        final Services services = p.getServices();
        TacletApp result;
        if (sv instanceof VariableSV) {
            // ignore -- already done
            result = app;
        } else if (sv instanceof ProgramSV psv) {
            final SmartMLProgramElement pe = app.getProgramElement(value, psv, services);
            result = app.addCheckedInstantiation(sv, pe, services, true);
        } else if (sv instanceof SkolemTermSV skolemSv) {
            result = app.createSkolemConstant(value, skolemSv, true, services);
        } else if (sv instanceof ModalOperatorSV msv) {
            result = app.addInstantiation(
                app.instantiations().add(msv, Modality.SmartMLModalityKind.getKind(value), services),
                services);
        } else {
            var varNS = p.getNamespaces().variables();
            var prgVarNS =
                targetGoal.getLocalNamespaces().programVariables();
            var funcNS = targetGoal.getLocalNamespaces().functions();
            varNS = app.extendVarNamespaceForSV(varNS, sv);
            Term instance = parseTerm(value, p, varNS, prgVarNS, funcNS);
            result = app.addCheckedInstantiation(sv, instance, services, true);
        }
        return result;
    }

    /**
     * Signals an error during construction of a taclet app.
     */
    public static class TacletAppConstructionException extends Exception {
        private static final long serialVersionUID = 7859543482157633999L;

        TacletAppConstructionException(String s) {
            super(s);
        }

        TacletAppConstructionException(String s, Throwable cause) {
            super(s, cause);
        }
    }

    /**
     * Simple structure containing the results of the replay procedure.
     *
     * @author Dominic Scheurer
     */
    public static class Result {
        private final String status;
        private final List<Throwable> errors;
        private Goal lastSelectedGoal = null;

        public Result(String status, List<Throwable> errors, Goal lastSelectedGoal) {
            this.status = status;
            this.errors = errors;
            this.lastSelectedGoal = lastSelectedGoal;
        }

        public String getStatus() {
            return status;
        }

        public List<Throwable> getErrors() {
            return errors;
        }

        public Goal getLastSelectedGoal() {
            return lastSelectedGoal;
        }
    }
}

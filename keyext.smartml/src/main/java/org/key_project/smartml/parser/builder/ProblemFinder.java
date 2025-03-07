/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.parser.builder;

import org.jspecify.annotations.Nullable;
import org.key_project.logic.Term;
import org.key_project.prover.sequent.Sequent;
import org.key_project.prover.sequent.SequentFormula;
import org.key_project.smartml.Services;
import org.key_project.smartml.logic.NamespaceSet;
import org.key_project.smartml.parser.KeYSmartMLParser;
import org.key_project.smartml.parser.ParsingFacade;
import org.key_project.smartml.proof.calculus.SmartMLSequentKit;
import org.key_project.smartml.settings.Configuration;
import org.key_project.smartml.util.parsing.BuildingException;
import org.key_project.util.collection.ImmutableSLList;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class ProblemFinder extends ExpressionBuilder {
    private @Nullable Sequent problem;
    private @Nullable String chooseContract;
    private @Nullable Configuration proofObligation;

    public ProblemFinder(Services services, NamespaceSet nss) {
        super(services, nss);
    }

    @Override
    public @Nullable Object visitFile(KeYSmartMLParser.FileContext ctx) {
        each(ctx.problem());
        return null;
    }

    /**
     * Try to find a problem defined in the {@link de.uka.ilkd.key.proof.init.KeYUserProblemFile}
     * located in the
     * given AST.
     * <p>
     * After this method is called, you can retrieve the chosen contract via
     * {@link #getChooseContract()} or the
     * proof obligation information via {@link #getProofObligation()}.
     *
     * @param ctx the parse tree
     * @return a term if {@code \problem} entry exists.
     * @throws BuildingException if the
     */
    @Override
    public @Nullable Term visitProblem(KeYSmartMLParser.ProblemContext ctx) {
        if (ctx.CHOOSECONTRACT() != null) {
            if (ctx.chooseContract != null) {
                // TODO
                chooseContract = ""; // ParsingFacade.getValueDocumentation(ctx.chooseContract);
            }
            // .replace("\\\\:", ":");
            else {
                chooseContract = "";
            }
        }
        if (ctx.PROOFOBLIGATION() != null) {
            var obl = ctx.proofObligation;
            if (obl instanceof KeYSmartMLParser.CstringContext stringContext) {
                try {
                    Properties p = new Properties();
                    var value = stringContext.STRING_LITERAL().getText();
                    value = value.substring(1, value.length() - 1).replace("\\\\", "\\");
                    p.load(new StringReader(value));
                    proofObligation = new Configuration();
                    p.forEach((k, v) -> proofObligation.set(k.toString(), v.toString()));
                } catch (IOException e) {
                    throw new BuildingException(ctx,
                        "Could not load the proof obligation given " +
                            "as a property file due to an error in the properties format",
                        e);
                }
            } else if (obl instanceof KeYSmartMLParser.TableContext tbl) {
                proofObligation = ParsingFacade.getConfiguration(tbl);
            } else {
                throw new BuildingException(ctx,
                    "Found a proof obligation entry, but the value is not a string or a JSON object");
            }
        }
        if (ctx.PROBLEM() != null) {
            problem = accept(ctx.termorseq());
        }
        return null;
    }

    @Override
    public @Nullable Sequent visitTermorseq(KeYSmartMLParser.TermorseqContext ctx) {
        var obj = super.visitTermorseq(ctx);
        if (obj instanceof Sequent s)
            return s;
        if (obj instanceof Term t)
            return SmartMLSequentKit
                    .createSuccSequent(ImmutableSLList.singleton(new SequentFormula(t)));
        return null;
    }

    public @Nullable String getChooseContract() {
        return chooseContract;
    }

    public @Nullable Configuration getProofObligation() {
        return proofObligation;
    }

    public @Nullable Sequent getProblem() {
        return problem;
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.parser.builder;

import org.jspecify.annotations.NonNull;
import org.key_project.smartml.parser.KeYSmartMLParser;
import org.key_project.smartml.parser.ProblemInformation;
import org.key_project.util.java.StringUtil;

public class FindProblemInformation extends AbstractBuilder<Object> {
    private final @NonNull ProblemInformation information = new ProblemInformation();

    @Override
    public Object visitFile(KeYSmartMLParser.FileContext ctx) {
        if (ctx.profile() != null) {
            information.setProfile(accept(ctx.profile()));
        }
        if (ctx.preferences() != null) {
            information.setPreferences(accept(ctx.preferences()));
        }
        each(ctx.decls(), ctx.problem());
        return null;
    }

    @Override
    public Object visitDecls(KeYSmartMLParser.DeclsContext ctx) {
        information.setRustSource(acceptFirst(ctx.programSource()));
        return null;
    }

    @Override
    public Object visitProblem(KeYSmartMLParser.ProblemContext ctx) {
        if (ctx.CHOOSECONTRACT() != null) {
            if (ctx.chooseContract != null) {
                information.setChooseContract(accept(ctx.chooseContract));
            } else {
                information.setChooseContract("");
            }
        }
        if (ctx.PROOFOBLIGATION() != null) {
            if (ctx.proofObligation != null) {
                information.setProofObligation(accept(ctx.proofObligation));
            } else {
                information.setProofObligation("");
            }
        }
        information.setHasProblemTerm(ctx.PROBLEM() != null);
        return null;
    }

    @Override
    public String visitString_value(KeYSmartMLParser.String_valueContext ctx) {
        return null; // ParsingFacade.getValueDocumentation(ctx);
    }


    @Override
    public String visitProgramSource(KeYSmartMLParser.ProgramSourceContext ctx) {
        return ctx.oneProgramSource() != null ? (String) accept(ctx.oneProgramSource()) : null;
    }

    @Override
    public String visitOneProgramSource(KeYSmartMLParser.OneProgramSourceContext ctx) {
        return StringUtil.trim(ctx.getText(), '"');
    }

    @Override
    public Object visitProfile(KeYSmartMLParser.ProfileContext ctx) {
        return accept(ctx.name);
    }

    @Override
    public String visitPreferences(KeYSmartMLParser.PreferencesContext ctx) {
        return ctx.s != null ? (String) accept(ctx.s) : null;
    }

    /**
     * The found problem information.
     */
    public @NonNull ProblemInformation getProblemInformation() {
        return information;
    }
}

/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.parser.builder;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.Named;
import org.key_project.logic.Namespace;
import org.key_project.logic.ParsableVariable;
import org.key_project.logic.op.Function;
import org.key_project.logic.op.Operator;
import org.key_project.logic.op.QuantifiableVariable;
import org.key_project.logic.op.sv.SchemaVariable;
import org.key_project.logic.sort.Sort;
import org.key_project.prover.rules.RuleSet;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.abstraction.KeYSmartMLType;
import org.key_project.smartml.logic.NamespaceSet;
import org.key_project.smartml.logic.SmartMLDLTheory;
import org.key_project.smartml.logic.op.ProgramVariable;
import org.key_project.smartml.logic.op.sv.OperatorSV;
import org.key_project.smartml.parser.KeYSmartMLParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultBuilder extends AbstractBuilder<Object> {
    protected final Services services;
    protected final NamespaceSet nss;
    private Namespace<@NonNull SchemaVariable> schemaVariablesNamespace = new Namespace<>();


    public DefaultBuilder(Services services, NamespaceSet nss) {
        this.services = services;
        this.nss = nss;
    }

    protected Named lookup(Name n) {
        final Namespace<?>[] lookups =
            { programVariables(), variables(), functions() };
        return doLookup(n, lookups);
    }

    protected <T> T doLookup(Name n, Namespace<?>... lookups) {
        for (Namespace<?> lookup : lookups) {
            Object l;
            if (lookup != null && (l = lookup.lookup(n)) != null) {
                try {
                    return (T) l;
                } catch (ClassCastException e) {
                }
            }
        }
        return null;
    }

    public NamespaceSet namespaces() {
        return nss;
    }

    protected Namespace<@NonNull QuantifiableVariable> variables() {
        return namespaces().variables();
    }

    protected Namespace<@NonNull Sort> sorts() {
        return namespaces().sorts();
    }

    protected Namespace<@NonNull Function> functions() {
        return namespaces().functions();
    }

    protected Namespace<RuleSet> ruleSets() {
        return namespaces().ruleSets();
    }

    protected Namespace<@NonNull ProgramVariable> programVariables() {
        return namespaces().programVariables();
    }

    public String visitSimple_ident_dots(KeYSmartMLParser.Simple_ident_dotsContext ctx) {
        return ctx.getText();
    }

    public List<Sort> visitArg_sorts_or_formula(KeYSmartMLParser.Arg_sorts_or_formulaContext ctx) {
        return mapOf(ctx.arg_sorts_or_formula_helper());
    }

    public Sort visitArg_sorts_or_formula_helper(
            KeYSmartMLParser.Arg_sorts_or_formula_helperContext ctx) {
        if (ctx.FORMULA() != null) {
            return SmartMLDLTheory.FORMULA;
        } else {
            return accept(ctx.sortId());
        }
    }

    protected void unbindVars(Namespace<@NonNull QuantifiableVariable> orig) {
        namespaces().setVariables(orig);
    }

    /**
     * looks up and returns the sort of the given name or null if none has been found
     */
    protected Sort lookupSort(String name) {
        return sorts().lookup(new Name(name));
    }

    /**
     * looks up a function, (program) variable or static query of the given name varfunc_id and the
     * argument terms args in the namespaces and Rust info.
     *
     * @param varfuncName the String with the symbols name
     */
    protected Operator lookupVarfuncId(ParserRuleContext ctx, String varfuncName, String sortName,
            Sort sort) {
        Name name = new Name(varfuncName);
        Operator[] operators =
            { (OperatorSV) schemaVariables().lookup(name), variables().lookup(name),
                programVariables().lookup(new Name(varfuncName)),
                functions().lookup(name) };

        for (Operator op : operators) {
            if (op != null) {
                return op;
            }
        }

        if (sort != null || sortName != null) {
            Name fqName =
                new Name((sort != null ? sort.toString() : sortName) + "::" + varfuncName);
            operators =
                new Operator[] { (OperatorSV) schemaVariables().lookup(fqName),
                    variables().lookup(fqName),
                    programVariables().lookup(new Name(fqName.toString())),
                    functions().lookup(fqName) };

            for (Operator op : operators) {
                if (op != null) {
                    return op;
                }
            }

            // SortDependingFunction firstInstance =
            // SortDependingFunction.getFirstInstance(new Name(varfuncName), getServices());
            if (sort == null)
                semanticError(ctx, "Could not find sort: %s", sortName);
            /*
             * if (firstInstance != null) {
             * SortDependingFunction v = firstInstance.getInstanceFor(sort, getServices());
             * if (v != null) {
             * return v;
             * }
             * }
             */
        }
        semanticError(ctx, "Could not find (program) variable or constant %s", varfuncName);
        return null;
    }

    public String visitString_value(KeYSmartMLParser.String_valueContext ctx) {
        return ctx.getText().substring(1, ctx.getText().length() - 1);
    }

    public Services getServices() {
        return services;
    }

    public Namespace<SchemaVariable> schemaVariables() {
        return schemaVariablesNamespace;
    }

    public void setSchemaVariables(Namespace<SchemaVariable> ns) {
        this.schemaVariablesNamespace = ns;
    }

    @Override
    public Object visitVarIds(KeYSmartMLParser.VarIdsContext ctx) {
        Collection<String> ids = accept(ctx.simple_ident_comma_list());
        List<ParsableVariable> list = new ArrayList<>(ids.size());
        for (String id : ids) {
            ParsableVariable v = (ParsableVariable) lookup(new Name(id));
            if (v == null) {
                semanticError(ctx, "Variable " + id + " not declared.");
            }
            list.add(v);
        }
        return list;
    }

    @Override
    public Object visitSimple_ident_dots_comma_list(
            KeYSmartMLParser.Simple_ident_dots_comma_listContext ctx) {
        return mapOf(ctx.simple_ident_dots());
    }

    @Override
    public String visitSimple_ident(KeYSmartMLParser.Simple_identContext ctx) {
        return ctx.IDENT().getText();
    }

    @Override
    public List<String> visitSimple_ident_comma_list(
            KeYSmartMLParser.Simple_ident_comma_listContext ctx) {
        return mapOf(ctx.simple_ident());
    }

    @Override
    public List<Boolean> visitWhere_to_bind(KeYSmartMLParser.Where_to_bindContext ctx) {
        List<Boolean> list = new ArrayList<>(ctx.children.size());
        ctx.b.forEach(it -> list.add(it.getText().equalsIgnoreCase("true")));
        return list;
    }

    @Override
    public List<Sort> visitArg_sorts(KeYSmartMLParser.Arg_sortsContext ctx) {
        return mapOf(ctx.sortId());
    }

    @Override
    public Sort visitSortId(KeYSmartMLParser.SortIdContext ctx) {
        return lookupSort(ctx.id.getText());
    }

    public KeYSmartMLType visitTypemapping(KeYSmartMLParser.TypemappingContext ctx) {
        StringBuilder type = new StringBuilder(visitSimple_ident_dots(ctx.simple_ident_dots()));
        Sort sort = lookupSort(type.toString());
        KeYSmartMLType krt = null;
        if (sort != null) {
            krt = new KeYSmartMLType(null, sort);
        }

        if (krt == null) {
            semanticError(ctx, "Unknown type: " + type);
        }

        return krt;
    }

    public Object visitFuncpred_name(KeYSmartMLParser.Funcpred_nameContext ctx) {
        return ctx.getText();
    }

}

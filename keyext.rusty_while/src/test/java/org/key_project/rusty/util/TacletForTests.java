/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.util;

import java.io.File;

import org.key_project.logic.Namespace;
import org.key_project.logic.Term;
import org.key_project.logic.op.Function;
import org.key_project.rusty.Services;
import org.key_project.rusty.logic.NamespaceSet;
import org.key_project.rusty.logic.op.sv.SchemaVariable;
import org.key_project.rusty.parser.KeYIO;
import org.key_project.rusty.proof.TacletIndex;
import org.key_project.rusty.proof.init.*;
import org.key_project.rusty.proof.io.KeYFileForTests;
import org.key_project.rusty.proof.io.RuleSourceFactory;
import org.key_project.util.collection.ImmutableSLList;

import org.jspecify.annotations.NonNull;

import static org.junit.jupiter.api.Assertions.fail;
import static org.key_project.rusty.proof.io.RuleSource.LDT_FILE;

public class TacletForTests {

    private TacletForTests() {}

    public static final String testRules =
        TestHelper.TESTCASE_DIRECTORY + File.separator + "testrules.key";
    public static String standardFile = testRules;

    public static NamespaceSet nss = new NamespaceSet();
    public static TacletIndex rules = null;
    public static Services services;
    public static InitConfig initConfig;
    public static File lastFile = null;

    private static Namespace<@NonNull SchemaVariable> schemaVariables;

    // private static Namespace<QuantifiableVariable> variables = null;

    public static final Profile profile = new RustProfile() {
        // we do not want normal standard rules, but ruleSetsDeclarations is needed for string
        // library (HACK)
        public RuleCollection getStandardRules() {
            return new RuleCollection(RuleSourceFactory.fromDefaultLocation(LDT_FILE),
                ImmutableSLList.nil());
        }
    };

    public static String getStandardFile() {
        return standardFile;
    }

    public static void setStandardFile(String standardFile) {
        TacletForTests.standardFile = standardFile;
    }

    public static TacletIndex getRules() {
        return rules;
    }

    public static void clear() {
        lastFile = null;
        services = null;
        initConfig = null;
        rules = null;
        // variables = null;
        // scm = new AbbrevMap();
        nss = new NamespaceSet();
    }

    public static void parse() {
        parse(new File(standardFile));
    }

    public static void parse(File file) {
        try {
            if (!file.equals(lastFile)) {
                var envInput = new KeYFileForTests("Test", file, profile);
                ProblemInitializer pi = new ProblemInitializer(envInput.getProfile());
                initConfig = pi.prepare(envInput);
                nss = initConfig.namespaces();
                rules = initConfig.createTacletIndex();
                services = initConfig.getServices();
                lastFile = file;
                // variables = envInput.variables();
                schemaVariables = envInput.schemaVariables();
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while parsing " + file, e);
        }
    }

    // public static NoPosTacletApp getTaclet(String name) {
    // return rules.lookup(new Name(name));
    // }

    public static InitConfig initConfig() {
        if (initConfig == null) {
            parse();
        }
        // return initConfig.deepCopy();
        return initConfig;
    }

    public static Services services() {
        if (services == null) {
            parse();
        }
        return services;
    }

    public static Term parseTerm(String termstr, Services services) {
        if (termstr.isEmpty()) {
            return null;
        }

        try {
            var io = new KeYIO(services, nss);
            // TacletForTests.getAbbrevs()
            return io.parseExpression(termstr);
        } catch (Exception e) {
            fail("Exception occurred while parsing of " + termstr, e);
            return null;
        }
    }

    public static Term parseTerm(String termstr, NamespaceSet set) {
        if (termstr.isEmpty()) {
            return null;
        }
        return new KeYIO(services(), set).parseExpression(termstr);
    }

    public static Term parseTerm(String termstr) {
        return parseTerm(termstr, services());
    }

    public static Namespace<@NonNull SchemaVariable> getSchemaVariables() {
        return schemaVariables;
    }

    public static Namespace<@NonNull Function> getFunctions() {
        return nss.functions();
    }
}
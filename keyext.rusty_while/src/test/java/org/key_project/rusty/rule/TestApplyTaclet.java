/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.rusty.rule;

import java.util.Iterator;

import org.key_project.logic.Name;
import org.key_project.logic.Term;
import org.key_project.rusty.Services;
import org.key_project.rusty.logic.*;
import org.key_project.rusty.logic.op.Quantifier;
import org.key_project.rusty.logic.op.sv.SchemaVariable;
import org.key_project.rusty.proof.Goal;
import org.key_project.rusty.proof.Node;
import org.key_project.rusty.proof.Proof;
import org.key_project.rusty.proof.TacletIndex;
import org.key_project.rusty.util.TacletForTests;
import org.key_project.util.collection.DefaultImmutableSet;
import org.key_project.util.collection.ImmutableList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestApplyTaclet {
    final static String[] strings = {
        "", "(A -> B) -> (!(!(A -> B)))",
        "", "\\forall s z; p(z)",
        "(A -> B) -> (!(!(A -> B)))", "(A -> B) -> (!(!(A -> B)))",
        "(A -> B) -> (!(!(A -> B)))", "",
        "", "\\<{x=3u32}\\>A",
        "A & B", "",
        "", ""
    };
    Proof[] proof;


    private static Semisequent parseTermForSemisequent(String t) {
        if ("".equals(t)) {
            return Semisequent.EMPTY_SEMISEQUENT;
        }
        SequentFormula cf0 = new SequentFormula(TacletForTests.parseTerm(t));
        return Semisequent.EMPTY_SEMISEQUENT.insert(0, cf0).semisequent();
    }

    private Goal createGoal(Node n, TacletIndex tacletIndex) {
        // final BuiltInRuleAppIndex birIndex = new BuiltInRuleAppIndex(new BuiltInRuleIndex());
        return new Goal(n, tacletIndex, n.proof().getServices());
    }

    @BeforeEach
    public void setUp() {
        TacletForTests.setStandardFile(TacletForTests.testRules);
        TacletForTests.parse();
        assert TacletForTests.services().getNamespaces().programVariables()
                .lookup(new Name("i")) != null;

        proof = new Proof[strings.length / 2];

        for (int i = 0; i < proof.length; i++) {
            Semisequent antec = parseTermForSemisequent(strings[2 * i]);
            Semisequent succ = parseTermForSemisequent(strings[2 * i + 1]);
            Sequent s = Sequent.createSequent(antec, succ);
            proof[i] = new Proof("TestApplyTaclet", TacletForTests.initConfig());
            proof[i].setRoot(new Node(proof[i], s));
        }
    }

    @Test
    public void testSuccTacletWithoutIf() {
        Term fma = proof[0].root().sequent().succedent().getFirst().formula();
        NoPosTacletApp impright = TacletForTests.getRules().lookup(new Name("imp_right"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(impright);
        Goal goal = createGoal(proof[0].root(), tacletIndex);
        PosInOccurrence applyPos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel(), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(applyPos, null);
        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        RuleApp rApp = rApplist.head();
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(1, goals.size(), "Too many or zero goals for imp-right.");
        Sequent seq = goals.head().sequent();
        assertEquals(seq.antecedent().getFirst().formula(), fma.sub(0),
            "Wrong antecedent after imp-right");
        assertEquals(seq.succedent().getFirst().formula(), fma.sub(1),
            "Wrong succedent after imp-right");
    }

    @Test
    public void testAddingRule() {
        Term fma = proof[0].root().sequent().succedent().getFirst().formula();
        NoPosTacletApp imprightadd =
            TacletForTests.getRules().lookup(new Name("TestApplyTaclet_imp_right_add"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(imprightadd);
        Goal goal = createGoal(proof[0].root(), tacletIndex);
        PosInOccurrence applyPos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel(), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(applyPos, null);
        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        RuleApp rApp = rApplist.head();
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(1, goals.size(), "Too many or zero goals for imp_right_add.");
        Sequent seq = goals.head().sequent();
        assertEquals(seq.antecedent().getFirst().formula(), fma.sub(0),
            "Wrong antecedent after imp_right_add");
        assertEquals(seq.succedent().getFirst().formula(), fma.sub(1),
            "Wrong succedent after imp_right_add");
        ImmutableList<NoPosTacletApp> nfapp = goals.head().indexOfTaclets()
                .getNoFindTaclet(null);
        Term aimpb = TacletForTests.parseTerm("A -> B");
        assertEquals(1, nfapp.size(), "Cut Rule should be inserted to TacletIndex.");
        assertEquals(
            nfapp.head().instantiations()
                    .getInstantiation(TacletForTests.getSchemaVariables().lookup(new Name("b"))),
            aimpb, "Inserted cut rule's b should be instantiated to A -> B.");
        assertTrue(rApp.complete(), "Rule App should be complete");
        goals = nfapp.head().execute(goals.head(), TacletForTests.services());
        Sequent seq1 = goals.head().sequent();
        Sequent seq2 = goals.tail().head().sequent();
        assertEquals(2, goals.size(), "Preinstantiated cut-rule should be executed");
        assertTrue(
            seq1.succedent().getFirst().formula().equals(aimpb)
                    || seq2.succedent().getFirst().formula().equals(aimpb)
                    || (seq1.succedent().get(1) != null
                            && seq1.succedent().get(1).formula().equals(aimpb))
                    || (seq2.succedent().get(1) != null
                            && seq2.succedent().get(1).formula().equals(aimpb)),
            "A->B should be in the succedent of one of the new goals now, "
                + "it's in the antecedent, anyway.");
    }

    @Test
    public void testSuccTacletAllRight() {
        NoPosTacletApp allright = TacletForTests.getRules().lookup(new Name("all_right"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(allright);
        Goal goal = createGoal(proof[1].root(), tacletIndex);
        PosInOccurrence applyPos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel(), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(applyPos, null);
        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        TacletApp rApp = rApplist.head();
        rApp = rApp.tryToInstantiate(TacletForTests.services());
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(1, goals.size(), "Too many or zero goals for all-right.");
        Sequent seq = goals.head().sequent();
        assertEquals(seq.antecedent(), Semisequent.EMPTY_SEMISEQUENT,
            "Wrong antecedent after all-right");
        assertEquals(seq.succedent().getFirst().formula().op(),
            TacletForTests.getFunctions().lookup(new Name("p")),
            "Wrong succedent after all-right (op mismatch)");
    }

    @Test
    public void testTacletWithAssumes() {
        NoPosTacletApp close = TacletForTests.getRules().lookup(new Name("close_goal"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(close);
        Goal goal = createGoal(proof[2].root(), tacletIndex);
        PosInOccurrence applyPos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel(), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(applyPos, null);
        assertEquals(1, rApplist.size(),
            "Too many or zero rule applications.\napp list:" + rApplist);

        TacletApp rApp = rApplist.head();
        ImmutableList<TacletApp> appList =
            rApp.findIfFormulaInstantiations(goal.sequent(), TacletForTests.services());
        assertFalse(appList.isEmpty(), "Match Failed.");
        assertEquals(1, appList.size(), "Too many matches.");
        assertSame(appList.head().instantiations(), rApp.instantiations(), "Wrong match found."); // TODO:
                                                                                                  // Why
                                                                                                  // should
                                                                                                  // this
                                                                                                  // ever
                                                                                                  // pass??
        assertTrue(appList.head().complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = appList.head().execute(goal, TacletForTests.services());
        assertEquals(1, goals.size(), "Wrong number of goals for close.");
        proof[2].closeGoal(goals.head());
        assertTrue(proof[2].closed(), "Proof should be closed.");
        /*
         * IList<SVInstantiations> svilist=rApp.taclet().matchIf(goal.sequent(),
         * rApp.instantiations(), null); assertTrue("Match Failed.", !svilist.isEmpty());
         * assertTrue("Too many matches.", svilist.size()==1); assertTrue("Wrong match found.",
         * svilist.head()==rApp.instantiations()); assertTrue("Rule App should be complete",
         * rApp.complete()); IList<Goal> goals=rApp.execute(goal, TacletForTests.services());
         * assertTrue("Too many goals for close.", goals.size()==0);
         */
    }

    @Test
    public void testAntecTacletWithoutAssumes() { // TODO: rename all "if"s in taclets to "assumes"
        Term fma = proof[3].root().sequent().antecedent().getFirst().formula();
        NoPosTacletApp impleft = TacletForTests.getRules().lookup(new Name("imp_left"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(impleft);
        Goal goal = createGoal(proof[3].root(), tacletIndex);
        PosInOccurrence applyPos = new PosInOccurrence(goal.sequent().antecedent().getFirst(),
            PosInTerm.getTopLevel(), true);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(applyPos, null);
        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        RuleApp rApp = rApplist.head();
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(2, goals.size(), "Too many or zero goals for imp-left.");
        Sequent seq = goals.head().sequent();
        if (!seq.succedent().isEmpty()) {
            assertEquals(seq.succedent().getFirst().formula(), fma.sub(0),
                "Wrong succedent after imp-left");
            goals = goals.tail();
            seq = goals.head().getNode().sequent();
            assertEquals(seq.antecedent().getFirst().formula(), fma.sub(1),
                "Wrong antecedent after imp-left");
        } else {
            assertEquals(seq.antecedent().getFirst().formula(), fma.sub(1),
                "Wrong antecedent after imp-left");
            goals = goals.tail();
            seq = goals.head().getNode().sequent();

            assertEquals(seq.succedent().getFirst().formula(), fma.sub(0),
                "Wrong succedent after imp-left");
        }
    }

    @Test
    public void testRewriteTacletWithoutIf() {
        NoPosTacletApp contradiction =
            TacletForTests.getRules().lookup(new Name("TestApplyTaclet_contradiction"));
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(contradiction);
        Goal goal = createGoal(proof[0].root(), tacletIndex);
        PosInOccurrence pos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel().down(1).down(0).down(0), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(pos, null);

        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        RuleApp rApp = rApplist.head();
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(1, goals.size(), "Too many or zero goals for contradiction.");
        Sequent seq = goals.head().sequent();
        Term term = seq.succedent().getFirst().formula().sub(1).sub(0).sub(0);
        assertEquals(term, TacletForTests.parseTerm("!B -> !A"));
    }


    @Test
    public void testNoFindTacletWithoutIf() {
        NoPosTacletApp cut = TacletForTests.getRules().lookup(new Name("TestApplyTaclet_cut"));
        TacletIndex tacletIndex = new TacletIndex();
        Term t_c = TacletForTests.parseTerm("D");
        tacletIndex.add(cut);
        Goal goal = createGoal(proof[0].root(), tacletIndex);
        PosInOccurrence pos = new PosInOccurrence(goal.sequent().succedent().getFirst(),
            PosInTerm.getTopLevel(), false);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(pos, null);
        assertEquals(1, rApplist.size(), "Too many or zero rule applications.");
        TacletApp rApp = rApplist.head().addInstantiation(
            TacletForTests.getSchemaVariables().lookup(new Name("b")), t_c, false,
            proof[0].getServices());
        assertTrue(rApp.complete(), "Rule App should be complete");
        ImmutableList<Goal> goals = rApp.execute(goal, TacletForTests.services());
        assertEquals(2, goals.size(), "Too many or too few goals.");
        Sequent seq1 = goals.head().sequent();
        goals = goals.tail();
        Sequent seq2 = goals.head().sequent();
        if (!seq1.antecedent().isEmpty() && seq1.antecedent().getFirst().formula().equals(t_c)) {
            assertTrue(
                seq2.succedent().getFirst().formula().equals(t_c)
                        || seq2.succedent().get(1).formula().equals(t_c),
                "D is in antecedent of 1st goal but not in succedent of 2nd");
        } else {
            assertTrue(
                seq1.succedent().getFirst().formula().equals(t_c)
                        || seq1.succedent().get(1).formula().equals(t_c),
                "D is not in antecedent and not in succedent " + "of first new goal");
            assertEquals(seq2.antecedent().getFirst().formula(), t_c,
                "D is in succedent of first new goal, but not in antecedent "
                    + "of second new goal");
        }
    }

    @Test
    public void testIncompleteNoFindTacletApp() {
        NoPosTacletApp cut = TacletForTests.getRules().lookup(new Name("TestApplyTaclet_cut"));
        assertFalse(cut.complete(), "TacletApp should not be complete, as b is not instantiated");
        SchemaVariable b = TacletForTests.getSchemaVariables().lookup(new Name("b"));
        assertTrue(cut.uninstantiatedVars().contains(b),
            "b should be in the set of not instantiated SVs");
    }

    @Test
    public void testIncompleteSuccTacletApp() {
        TacletApp orright = TacletForTests.getRules().lookup(new Name("or_right"));
        assertFalse(orright.complete(),
            "TacletApp should not be complete, as SVs are not instantiated");

        Services services = TacletForTests.services();
        SchemaVariable b = TacletForTests.getSchemaVariables().lookup(new Name("b"));
        SchemaVariable c = TacletForTests.getSchemaVariables().lookup(new Name("c"));
        assertEquals(orright.uninstantiatedVars(),
            DefaultImmutableSet.<SchemaVariable>nil().add(b).add(c),
            "b and c should be in the set of not instantiated SVs");
        orright = orright.addInstantiation(b, TacletForTests.parseTerm("A"), false, services);
        assertFalse(orright.complete(),
            "TacletApp should not be complete, as B is not instantiated");
        orright = orright.addInstantiation(c, TacletForTests.parseTerm("B"), false, services);
        assertFalse(orright.complete(), "TacletApp should not be complete, as Position unknown");
        Sequent seq = proof[0].root().sequent();
        orright = orright.setPosInOccurrence(
            new PosInOccurrence(seq.succedent().get(0), PosInTerm.getTopLevel(), false), services);
        assertTrue(orright.complete(),
            "TacletApp should now be complete with Position set and SVs " + "instantiated");
    }

    @Test
    public void testPrgTacletApp() {
        // TODO
    }

    @Test
    public void testBugBrokenApply() {
        // several times the following bug got broken
        // The application of
        // 'find (b==>) replacewith(b==>); add (==>b);'
        // resulted in
        // ==> , b==>b instead of
        // b==> , b==>b
        NoPosTacletApp cdr =
            TacletForTests.getRules().lookup(new Name("TestApplyTaclet_cut_direct_r"));

        Sequent seq = proof[1].root().sequent();
        PosInOccurrence pio =
            new PosInOccurrence(seq.succedent().get(0), PosInTerm.getTopLevel(), false);
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(cdr);
        Goal goal = createGoal(proof[1].root(), tacletIndex);
        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(pio, null);
        ImmutableList<Goal> goals = rApplist.head().execute(goal, TacletForTests.services);

        assertEquals(2, goals.size(), "Expected two goals");
        assertTrue(
            goals.head().sequent().antecedent().size() == 1
                    && goals.head().sequent().antecedent().iterator().next().formula()
                            .op() == Quantifier.ALL
                    && goals.head().sequent().succedent().size() == 1
                    && goals.head().sequent().succedent().iterator().next().formula()
                            .op() == Quantifier.ALL,
            "First goal should be 'b==>b', but is " + goals.head().sequent());
        goals = goals.tail();
        assertTrue(
            goals.head().sequent().antecedent().size() == 0
                    && goals.head().sequent().succedent().size() == 1
                    && goals.head().sequent().succedent().iterator().next().formula()
                            .op() == Quantifier.ALL,
            "Second goal should be '==>b', but is " + goals.head().sequent());
    }

    @Test
    public void testBugID176() {
        // the last time the bug above had been fixed, the hidden
        // taclets got broken (did not hide anymore)
        // also known as bug #176
        NoPosTacletApp hide_r =
            TacletForTests.getRules().lookup(new Name("TestApplyTaclet_hide_r"));

        Sequent seq = proof[1].root().sequent();
        PosInOccurrence pio =
            new PosInOccurrence(seq.succedent().get(0), PosInTerm.getTopLevel(), false);
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(hide_r);
        Goal goal = createGoal(proof[1].root(), tacletIndex);

        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(pio, null);
        ImmutableList<Goal> goals = rApplist.head().execute(goal, TacletForTests.services());

        assertEquals(1, goals.size(), "Expected one goal");
        assertTrue(goals.head().sequent().isEmpty(),
            "Expected '==>', but is " + goals.head().sequent());

    }

    @Test
    public void testBugID177() {
        // bug #177
        NoPosTacletApp al = TacletForTests.getRules().lookup(new Name("and_left"));

        Sequent seq = proof[5].root().sequent();
        PosInOccurrence pio =
            new PosInOccurrence(seq.antecedent().get(0), PosInTerm.getTopLevel(), true);
        TacletIndex tacletIndex = new TacletIndex();
        tacletIndex.add(al);
        Goal goal = createGoal(proof[5].root(), tacletIndex);

        ImmutableList<TacletApp> rApplist =
            goal.ruleAppIndex().getTacletAppAt(pio, null);
        ImmutableList<Goal> goals = rApplist.head().execute(goal, TacletForTests.services());


        assertEquals(1, goals.size(), "Expected one goal");
        Iterator<SequentFormula> it = goals.head().sequent().antecedent().iterator();
        assertTrue(
            goals.head().sequent().antecedent().size() == 2
                    && it.next().formula().equals(TacletForTests.parseTerm("A"))
                    && it.next().formula().equals(TacletForTests.parseTerm("B")),
            "Expected 'A, B ==>', but is " + goals.head().sequent());
    }
}
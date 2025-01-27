/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.visitor;

//import org.key_project.smartml.ast.PathInExpression;
import org.key_project.smartml.ast.expr.ArithLogicalExpression;
import org.key_project.smartml.ast.expr.BooleanLiteralExpression;
import org.key_project.smartml.ast.expr.IntegerLiteralExpression;
import org.key_project.smartml.ast.expr.NegationExpression;
import org.key_project.smartml.logic.op.ProgramVariable;
import org.key_project.smartml.logic.op.sv.SchemaVariable;
//import org.key_project.smartml.ast.fn.SelfParam;
//import org.key_project.smartml.ast.stmt.EmptyStatement;
//import org.key_project.smartml.ast.stmt.ExpressionStatement;
//import org.key_project.smartml.ast.ty.PrimitiveRustType;
//import org.key_project.smartml.logic.op.ProgramVariable;
//import org.key_project.smartml.logic.op.sv.SchemaVariable;

/**
 * This class is implemented by visitors/walkers. Each AST node implements a visit(Visitor) method
 * that calls the doActionAt<NodeType> method. Similar to the pretty print mechanism.
 */
public interface Visitor {
    void performActionOnArithLogicalExpression(ArithLogicalExpression x);

    //  void performActionOnAssignmentExpression(AssignmentExpression x);

    //  void performActionOnBlockExpression(BlockExpression x);

    void performActionOnBooleanLiteralExpression(BooleanLiteralExpression x);

    //  void performActionOnContextBlockExpression(ContextBlockExpression x);

    void performActionOnIntegerLiteralExpression(IntegerLiteralExpression x);

    void performActionOnNegationExpression(NegationExpression x);

    void performActionOnSchemaVariable(SchemaVariable x);

    void performActionOnProgramVariable(ProgramVariable x);

    //  void performActionOnEmptyStatement(EmptyStatement x);

    //  void performActionOnMethodCall(MethodCallExpression x);

    //  void performActionOnFieldExpression(FieldExpression x);

    //  void performActionOnTupleIndexingExpression(TupleIndexingExpression x);

    //  void performActionOnCallExpression(CallExpression x);

    //  void performActionOnIndexExpression(IndexExpression x);

    //  void performActionOnErrorPropagationExpression(ErrorPropagationExpression x);

    //  void performActionOnBorrowExpression(BorrowExpression x);

    //  void performActionOnDereferenceExpression(DereferenceExpression x);

    //  void performActionOnTypeCastExpression(TypeCastExpression x);

    //  void performActionOnComparisonExpression(ComparisonExpression x);

    //  void performActionOnRangeExpression(RangeExpression x);

    //  void performActionOnLazyBooleanExpression(LazyBooleanExpression x);

    //  void performActionOnCompoundAssignmentExpression(CompoundAssignmentExpression x);

    //  void performActionOnContinueExpression(ContinueExpression x);

    //  void performActionOnBreakExpression(BreakExpression x);

    //  void performActionOnReturnExpression(ReturnExpression x);

    //  void performActionOnGroupedExpression(GroupedExpression x);

    //  void performActionOnEnumeratedArrayExpression(EnumeratedArrayExpression x);

    //  void performActionOnRepeatedArrayExpression(RepeatedArrayExpression x);

    //  void performActionOnTupleExpression(TupleExpression x);

    //  void performActionOnPathInExpression(PathInExpression x);

    //  void performActionOnTupleStructExpression(TupleStructExpression x);

    //  void performActionOnUnitStructExpression(UnitStructExpression x);

    //  void performActionOnFieldStructExpression(StructStructExpression x);

    //  void performActionOnEnumVariantFieldless(EnumVariantFieldless x);

    //  void performActionOnEnumVariantTuple(EnumVariantTuple x);

    //  void performActionOnClosureExpression(ClosureExpression x);

    //  void performActionOnSelfParam(SelfParam x);

    //  void performActionOnEnumVariantStruct(EnumVariantStruct x);

    //  void performActionOnInfiniteLoop(InfiniteLoopExpression x);

    //  void performActionOnPredicatePatternLoopExpression(PredicatePatternLoopExpression x);

    //  void performActionOnIteratorLoopExpression(IteratorLoopExpression x);

    //  void performActionOnIfExpression(IfExpression x);

    //  void performActionOnMatchExpression(MatchExpression x);

    //  void performActionOnMatchArm(MatchArm x);

    //  void performActionOnIfLetExpression(IfLetExpression x);

    //  void performActionOnExpressionStatement(ExpressionStatement x);

    //  void performActionOnPrimitiveRustType(PrimitiveRustType x);

  //  //  void performActionOnSchemaRustType(SchemaSmartMLType x);
}

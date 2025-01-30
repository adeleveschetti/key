package org.key_project.smartml.ast.fn;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.Named;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Item;
import org.key_project.smartml.ast.expr.BlockExpression;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.stmt.Statement;
import org.key_project.smartml.ast.ty.SmartMLType;
import org.key_project.smartml.ast.visitor.Visitor;

import java.util.Objects;
import java.util.stream.Collectors;

public record Function(Name name, org.key_project.util.collection.ImmutableArray<FunctionParam> params, SmartMLType returnType,
                       BlockExpression body) implements Statement, Named {

    @Override
    public int getChildCount() {
        return 3 + params.size();
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        if (0 <= n && n < params.size())
            return Objects.requireNonNull(params.get(n));
        n -= params.size();
        if (n == 0) return returnType;
        if (n == 1)
            return body;
        throw new IndexOutOfBoundsException("No child at index " + n);
    }

    @Override
    public String toString() {
        return "fn " + name() + "("
                + params.stream().map(Object::toString).collect(Collectors.joining(", ")) + ") -> "
                + returnType + " " + body;
    }

    @Override
    public void visit(Visitor v) {
        throw new RuntimeException("TODO @ DD");
    }
}
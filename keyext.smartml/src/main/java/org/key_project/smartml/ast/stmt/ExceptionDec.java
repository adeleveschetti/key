package org.key_project.smartml.ast.stmt;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.Item;
import org.key_project.smartml.ast.fn.FunctionParam;
import org.key_project.smartml.ast.visitor.Visitor;

import java.util.Objects;
import java.util.stream.Collectors;

public record ExceptionDec(String name, org.key_project.util.collection.ImmutableArray<FunctionParam> params) implements Statement {

    @Override
    public int getChildCount() {
        return 3 + params.size();
    } // TODO: fix that

    @Override
    public @NonNull SyntaxElement getChild(int n) { // TODO: fix that
        if (0 <= n && n < params.size())
            return Objects.requireNonNull(params.get(n));
        n -= params.size();
        throw new IndexOutOfBoundsException("No child at index " + n);
    }

    @Override
    public String toString() {
        return "Exception " + name + " ("
                + params.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public void visit(Visitor v) {
        throw new RuntimeException("TODO @ DD");
    }
}
/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.ast.expr;

import org.jspecify.annotations.NonNull;
import org.key_project.logic.Name;
import org.key_project.logic.SyntaxElement;
import org.key_project.smartml.ast.expr.LiteralExpression;
import org.key_project.smartml.ast.visitor.Visitor;
import org.key_project.smartml.ldt.IntLDT;

import java.math.BigInteger;

public class IntegerLiteralExpression extends LiteralExpression {
    public enum IntegerSuffix {
        u8,
        u16,
        u32,
        u64,
        u128,
        usize,
        i8,
        i16,
        i32,
        i64,
        i128,
        isize;

        public static IntegerSuffix get(boolean signed, String size) {
            if (signed) {
                return getIntegerSuffix(size, i8, i16, i32, i64, i128, isize);
            } else {
                return getIntegerSuffix(size, u8, u16, u32, u64, u128, usize);
            }
        }

        private static IntegerSuffix getIntegerSuffix(String size, IntegerSuffix integerSuffix,
                IntegerSuffix integerSuffix2, IntegerSuffix integerSuffix3,
                IntegerSuffix integerSuffix4, IntegerSuffix integerSuffix5,
                IntegerSuffix integerSuffix6) {
            return switch (size) {
            case "8" -> integerSuffix;
            case "16" -> integerSuffix2;
            case "32" -> integerSuffix3;
            case "64" -> integerSuffix4;
            case "128" -> integerSuffix5;
            case "size" -> integerSuffix6;
            default -> throw new IllegalArgumentException("Unknown size: " + size);
            };
        }
    }

    private final BigInteger value;
    private final IntegerSuffix suffix;


    public IntegerLiteralExpression(BigInteger value, IntegerSuffix suffix) {
        this.value = value;
        this.suffix = suffix;
    }

    @Override
    public @NonNull SyntaxElement getChild(int n) {
        throw new IndexOutOfBoundsException("IntegerLiteralExpression has no children");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String toString() {
        return value + suffix.toString();
    }

    @Override
    public Name getLDTName() {
        return IntLDT.NAME;
    }

    public BigInteger getValue() {
        return value;
    }

    public IntegerSuffix getSuffix() {
        return suffix;
    }

    @Override
    public void visit(Visitor v) {
        v.performActionOnIntegerLiteralExpression(this);
    }

    @Override
    public int hashCode() {
        return 5 + value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj.getClass() != this.getClass())
            return false;
        IntegerLiteralExpression other = (IntegerLiteralExpression) obj;
        return value.equals(other.value);
    }
}

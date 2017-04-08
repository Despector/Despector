/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.ast.members.insn.arg.operator;

/**
 * An enumeration of all operators.
 */
public enum OperatorType {

    ADD(11, "+"),
    SUBTRACT(11, "-"),
    MULTIPLY(12, "*"),
    DIVIDE(12, "/"),
    REMAINDER(12, "%"),

    SHIFT_LEFT(10, "<<"),
    SHIFT_RIGHT(10, ">>"),
    UNSIGNED_SHIFT_RIGHT(10, ">>>"),

    AND(7, "&"),
    OR(5, "|"),
    XOR(6, "^");

    private final String symbol;
    private final int precedence;

    OperatorType(int p, String s) {
        this.precedence = p;
        this.symbol = s;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public int getPrecedence() {
        return this.precedence;
    }

}

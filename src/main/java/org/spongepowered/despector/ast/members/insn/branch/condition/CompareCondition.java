/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.despector.ast.members.insn.branch.condition;

import static org.objectweb.asm.Opcodes.*;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

/**
 * A condition which compares two object or numerical values.
 */
public class CompareCondition extends Condition {

    /**
     * Gets the compare operator for the given conditional jump operator.
     */
    public static CompareOp fromOpcode(int op) {
        switch (op) {
        case IFEQ:
        case IF_ACMPEQ:
        case IF_ICMPEQ:
            return CompareOp.EQUAL;
        case IFNE:
        case IF_ACMPNE:
        case IF_ICMPNE:
            return CompareOp.NOT_EQUAL;
        case IFGE:
        case IF_ICMPGE:
            return CompareOp.GREATER_EQUAL;
        case IFGT:
        case IF_ICMPGT:
            return CompareOp.GREATER;
        case IFLE:
        case IF_ICMPLE:
            return CompareOp.LESS_EQUAL;
        case IFLT:
        case IF_ICMPLT:
            return CompareOp.LESS;
        default:
            throw new UnsupportedOperationException("Not a conditional jump opcode: " + op);
        }
    }

    /**
     * Represents a specific comparison operator.
     */
    public static enum CompareOp {
        EQUAL(" == "),
        NOT_EQUAL(" != "),
        LESS_EQUAL(" <= "),
        GREATER_EQUAL(" >= "),
        LESS(" < "),
        GREATER(" > ");

        static {
            EQUAL.inverse = NOT_EQUAL;
            NOT_EQUAL.inverse = EQUAL;
            LESS_EQUAL.inverse = GREATER;
            GREATER_EQUAL.inverse = LESS;
            LESS.inverse = GREATER_EQUAL;
            GREATER.inverse = LESS_EQUAL;
        }

        private final String str;
        private CompareOp inverse;

        CompareOp(String str) {
            this.str = str;
        }

        public String asString() {
            return this.str;
        }

        public CompareOp inverse() {
            return this.inverse;
        }
    }

    private final Instruction left;
    private final Instruction right;
    private final CompareOp op;

    public CompareCondition(Instruction left, Instruction right, CompareOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Instruction getLeft() {
        return this.left;
    }

    public Instruction getRight() {
        return this.right;
    }

    public CompareOp getOp() {
        return this.op;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitCompareCondition(this);
        this.left.accept(visitor);
        this.right.accept(visitor);
    }

    @Override
    public String toString() {
        return this.left.toString() + this.op.asString() + this.right.toString();
    }

}

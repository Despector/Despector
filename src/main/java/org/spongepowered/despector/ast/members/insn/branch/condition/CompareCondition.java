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
package org.spongepowered.despector.ast.members.insn.branch.condition;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.*;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * A condition which compares two object or numerical values.
 */
public class CompareCondition extends Condition {

    /**
     * Gets the compare operator for the given conditional jump operator.
     */
    public static CompareOperator fromOpcode(int op) {
        switch (op) {
        case IFEQ:
        case IF_ACMPEQ:
        case IF_ICMPEQ:
            return CompareOperator.EQUAL;
        case IFNE:
        case IF_ACMPNE:
        case IF_ICMPNE:
            return CompareOperator.NOT_EQUAL;
        case IFGE:
        case IF_ICMPGE:
            return CompareOperator.GREATER_EQUAL;
        case IFGT:
        case IF_ICMPGT:
            return CompareOperator.GREATER;
        case IFLE:
        case IF_ICMPLE:
            return CompareOperator.LESS_EQUAL;
        case IFLT:
        case IF_ICMPLT:
            return CompareOperator.LESS;
        default:
            throw new UnsupportedOperationException("Not a conditional jump opcode: " + op);
        }
    }

    /**
     * Represents a specific comparison operator.
     */
    public static enum CompareOperator {
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
        private CompareOperator inverse;

        CompareOperator(String str) {
            this.str = checkNotNull(str, "str");
        }

        /**
         * Gets the operator as its string representation.
         */
        public String asString() {
            return this.str;
        }

        /**
         * Gets the operator which is the inverse of this operator.
         */
        public CompareOperator inverse() {
            return this.inverse;
        }
    }

    private Instruction left;
    private Instruction right;
    private CompareOperator op;

    public CompareCondition(Instruction left, Instruction right, CompareOperator op) {
        this.left = checkNotNull(left, "left");
        this.right = checkNotNull(right, "right");
        this.op = checkNotNull(op, "op");
    }

    /**
     * Gets the left operand of the comparison.
     */
    public Instruction getLeft() {
        return this.left;
    }

    /**
     * Sets the left operand of the comparison.
     */
    public void setLeft(Instruction left) {
        this.left = checkNotNull(left, "left");
    }

    /**
     * Gets the right operand of the comparison.
     */
    public Instruction getRight() {
        return this.right;
    }

    /**
     * Sets the right operand of the comparison.
     */
    public void setRight(Instruction right) {
        this.right = checkNotNull(right, "right");
    }

    /**
     * Gets the comparison operator.
     */
    public CompareOperator getOperator() {
        return this.op;
    }

    /**
     * Sets the comparison operator.
     */
    public void setOperator(CompareOperator op) {
        this.op = checkNotNull(op, "op");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitCompareCondition(this);
        this.left.accept(visitor);
        this.right.accept(visitor);
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(4);
        pack.writeString("id").writeInt(AstSerializer.CONDITION_ID_COMPARE);
        pack.writeString("left");
        this.left.writeTo(pack);
        pack.writeString("right");
        this.right.writeTo(pack);
        pack.writeString("op").writeInt(this.op.ordinal());
    }

    @Override
    public String toString() {
        return this.left + this.op.asString() + this.right;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CompareCondition)) {
            return false;
        }
        CompareCondition and = (CompareCondition) o;
        return and.right.equals(this.right) && and.left.equals(this.left) && this.op == and.op;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.right.hashCode();
        h = h * 37 + this.left.hashCode();
        h = h * 37 + this.op.hashCode();
        return h;
    }

}

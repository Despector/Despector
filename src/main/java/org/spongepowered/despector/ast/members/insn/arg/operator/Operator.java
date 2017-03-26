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
package org.spongepowered.despector.ast.members.insn.arg.operator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

/**
 * An abstract instruction arg for binary operator.
 */
public abstract class Operator implements Instruction {

    private static final String PRIMATIVE_ORDERING = "ZBCSIJFD";

    protected Instruction left;
    protected Instruction right;

    public Operator(Instruction left, Instruction right) {
        this.left = checkNotNull(left, "left");
        this.right = checkNotNull(right, "right");
    }

    /**
     * Gets the left operand.
     */
    public Instruction getLeftOperand() {
        return this.left;
    }

    /**
     * Sets the left operand.
     */
    public void setLeftOperand(Instruction left) {
        this.left = checkNotNull(left, "left");
    }

    /**
     * Gets the right operand.
     */
    public Instruction getRightOperand() {
        return this.right;
    }

    /**
     * Sets the right operand.
     */
    public void setRightOperand(Instruction right) {
        this.right = checkNotNull(right, "right");
    }

    /**
     * Gets the operator representation.
     */
    public abstract String getOperator();

    @Override
    public void accept(InstructionVisitor visitor) {
        this.left.accept(visitor);
        this.right.accept(visitor);
    }

    @Override
    public String inferType() {
        int left_index = PRIMATIVE_ORDERING.indexOf(this.left.inferType().charAt(0));
        int right_index = PRIMATIVE_ORDERING.indexOf(this.right.inferType().charAt(0));
        return String.valueOf(PRIMATIVE_ORDERING.charAt(Math.max(left_index, right_index)));
    }

    @Override
    public String toString() {
        return this.left.toString() + " " + getOperator() + " " + this.right.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        Operator insn = (Operator) obj;
        return this.left.equals(insn.left) && this.right.equals(insn.right);
    }

}

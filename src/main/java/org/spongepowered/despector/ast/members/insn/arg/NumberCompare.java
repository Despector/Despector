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
package org.spongepowered.despector.ast.members.insn.arg;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;

/**
 * An instruction performing a comparison between two numerical values.
 */
public class NumberCompare implements Instruction {

    protected Instruction left;
    protected Instruction right;

    public NumberCompare(Instruction left, Instruction right) {
        this.left = checkNotNull(left, "left");
        this.right = checkNotNull(right, "right");
    }

    /**
     * Gets the left operand of the comparison.
     */
    public Instruction getLeftOperand() {
        return this.left;
    }

    /**
     * Sets the left operand of the comparison.
     */
    public void setLeftOperand(Instruction left) {
        this.left = checkNotNull(left, "left");
    }

    /**
     * Gets the right operand of the comparison.
     */
    public Instruction getRightOperand() {
        return this.right;
    }

    /**
     * Sets the right operand of the comparison.
     */
    public void setRightOperand(Instruction right) {
        this.right = checkNotNull(right, "right");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitNumberCompare(this);
        this.left.accept(visitor);
        this.right.accept(visitor);
    }

    @Override
    public TypeSignature inferType() {
        return this.left.inferType();
    }

    @Override
    public String toString() {
        return "Integer.signum(" + this.right + " - " + this.left + ");";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NumberCompare)) {
            return false;
        }
        NumberCompare insn = (NumberCompare) obj;
        return this.left.equals(insn.left) && this.right.equals(insn.right);
    }
}

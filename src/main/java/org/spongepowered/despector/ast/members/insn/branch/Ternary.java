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
package org.spongepowered.despector.ast.members.insn.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

/**
 * A ternary instruction.
 */
public class Ternary implements Instruction {

    private Condition condition;
    private Instruction true_val;
    private Instruction false_val;

    public Ternary(Condition cond, Instruction true_val, Instruction false_val) {
        this.condition = checkNotNull(cond, "condition");
        this.true_val = checkNotNull(true_val, "true_val");
        this.false_val = checkNotNull(false_val, "false_val");
    }

    /**
     * Gets the condition of this ternary.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Sets the condition of this ternary.
     */
    public void setCondition(Condition condition) {
        this.condition = checkNotNull(condition, "condition");
    }

    /**
     * Gets the value if the condition is true.
     */
    public Instruction getTrueValue() {
        return this.true_val;
    }

    /**
     * Sets the value if the condition is true.
     */
    public void setTrueValue(Instruction val) {
        this.true_val = checkNotNull(val, "true_val");
    }

    /**
     * Gets the value if the condition is false.
     */
    public Instruction getFalseValue() {
        return this.false_val;
    }

    /**
     * Sets the value if the condition is false.
     */
    public void setFalseValue(Instruction val) {
        this.false_val = checkNotNull(val, "false_val");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitTernary(this);
        this.condition.accept(visitor);
        this.true_val.accept(visitor);
        this.false_val.accept(visitor);
    }

    @Override
    public String inferType() {
        return this.true_val.inferType();
    }

    @Override
    public String toString() {
        return this.condition + " ? " + this.true_val + " : " + this.false_val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Ternary)) {
            return false;
        }
        Ternary insn = (Ternary) obj;
        return this.condition.equals(insn.condition) && this.true_val.equals(insn.true_val) && this.false_val.equals(insn.false_val);
    }

}

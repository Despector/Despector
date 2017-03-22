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
package org.spongepowered.despector.ast.members.insn.assign;

import static org.spongepowered.despector.util.TypeHelper.checkType;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An array assignment statement.
 * 
 * <p>Example: {@code var[i] = val;}</p>
 */
public class ArrayAssignment extends Assignment {

    private Instruction array;
    private Instruction index;

    public ArrayAssignment(Instruction array, Instruction index, Instruction val) {
        super(val);
        this.index = checkType(index, "I", "index");
        this.array = checkType(array, TypeHelper.IS_ARRAY, "array");
    }

    /**
     * Gets the instruction providing the array object.
     */
    public Instruction getArray() {
        return this.array;
    }

    /**
     * Sets the instruction providing the array object.
     */
    public void setArray(Instruction array) {
        checkType(array, TypeHelper.IS_ARRAY, "array");
        checkType(this.val, (t) -> t.equals(array.inferType().substring(1)), "array");
        this.array = array;
    }

    /**
     * Gets the instruction providing the array index.
     */
    public Instruction getIndex() {
        return this.index;
    }

    /**
     * Sets the instruction providing the array index.
     */
    public void setIndex(Instruction index) {
        this.index = checkType(index, "I", "index");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitArrayAssign(this);
        this.array.accept(visitor);
        this.index.accept(visitor);
        this.val.accept(visitor);
    }

    @Override
    public String toString() {
        return this.array + "[" + this.index + "] = " + this.val + ";";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayAssignment)) {
            return false;
        }
        ArrayAssignment insn = (ArrayAssignment) obj;
        return this.val.equals(insn.val) && this.array.equals(insn.array) && this.index.equals(insn.index);
    }

}

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
package org.spongepowered.despector.ast.members.insn.arg.field;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

/**
 * Loads an array value from a value.
 * 
 * <p>Example: local[0]</p>
 */
public class ArrayAccess implements Instruction {

    private Instruction array;
    private Instruction index;
    private TypeSignature component = null;

    public ArrayAccess(Instruction array, Instruction index) {
        this.array = checkNotNull(array, "array");
        this.index = checkNotNull(index, "index");
    }

    /**
     * Gets the instruction providing the array object.
     */
    public Instruction getArrayVar() {
        return this.array;
    }

    /**
     * Sets the instruction providing the array object.
     */
    public void setArrayVar(Instruction array) {
        this.array = checkNotNull(array, "array");
        this.component = null;
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
        this.index = checkNotNull(index, "index");
    }

    @Override
    public TypeSignature inferType() {
        if (this.component == null) {
            this.component = TypeSignature.getArrayComponent(this.array.inferType());
        }
        return this.component;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitArrayAccess(this);
        this.array.accept(visitor);
        this.index.accept(visitor);
    }

    @Override
    public String toString() {
        return this.array + "[" + this.index + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayAccess)) {
            return false;
        }
        ArrayAccess insn = (ArrayAccess) obj;
        return this.array.equals(insn.array) && this.index.equals(insn.index);
    }

}

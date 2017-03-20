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

import static org.spongepowered.despector.util.TypeHelper.checkType;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;

import javax.annotation.Nullable;

public class NewArray implements Instruction {

    private String type;
    private Instruction size;
    private Instruction[] values;

    public NewArray(String type, Instruction size, @Nullable Instruction[] values) {
        this.type = checkNotNull(type, "type");
        this.size = checkType(size, "I", "size");
        this.values = values;
    }

    /**
     * Gets the component type of the new array.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the component type of the new array.
     */
    public void setType(String type) {
        this.type = checkNotNull(type, "type");
    }

    /**
     * Gets the size of the new array.
     */
    public Instruction getSize() {
        return this.size;
    }

    /**
     * Gets the size of the new array, must type check as an integer.
     */
    public void setSize(Instruction size) {
        this.size = checkType(size, "I", "size");
    }

    /**
     * Gets the initial values of the array, if specified.
     */
    @Nullable
    public Instruction[] getInitializer() {
        return this.values;
    }

    /**
     * Sets the initial values of the array, if specified.
     */
    public void setInitialValues(@Nullable Instruction... values) {
        this.values = values;
    }

    @Override
    public String inferType() {
        return "[" + this.type;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitNewArrayArg(this);
        this.size.accept(visitor);
        if (this.values != null) {
            for (Instruction value : this.values) {
                value.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        if (this.values == null) {
            return "new " + TypeHelper.descToType(this.type) + "[" + this.size + "]";
        }
        String result = "new " + TypeHelper.descToType(this.type) + "[] {";
        for (int i = 0; i < this.values.length; i++) {
            result += this.values[i];
            if (i < this.values.length - 1) {
                result += ", ";
            }
        }
        result += "}";
        return result;
    }

}

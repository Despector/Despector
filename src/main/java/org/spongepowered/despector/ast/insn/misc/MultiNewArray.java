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
package org.spongepowered.despector.ast.insn.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.Arrays;

/**
 * An instruction initializing a new array.
 */
public class MultiNewArray implements Instruction {

    private ClassTypeSignature type;
    private Instruction[] sizes;

    public MultiNewArray(ClassTypeSignature type, Instruction[] sizes) {
        this.type = checkNotNull(type, "type");
        this.sizes = checkNotNull(sizes, "size");
    }

    /**
     * Gets the component type of the new array.
     */
    public ClassTypeSignature getType() {
        return this.type;
    }

    /**
     * Sets the component type of the new array.
     */
    public void setType(ClassTypeSignature type) {
        this.type = checkNotNull(type, "type");
    }

    /**
     * Gets the size of the new array.
     */
    public Instruction[] getSizes() {
        return this.sizes;
    }

    /**
     * Gets the size of the new array, must type check as an integer.
     */
    public void setSizes(Instruction[] size) {
        this.sizes = checkNotNull(size, "size");
    }

    @Override
    public TypeSignature inferType() {
        return ClassTypeSignature.of("[" + this.type);
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitMultiNewArray(this);
            for (Instruction size : this.sizes) {
                size.accept(visitor);
            }
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(3);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_MULTI_NEW_ARRAY);
        pack.writeString("type").writeString(this.type.getDescriptor());
        pack.writeString("sizes");
        pack.startArray(this.sizes.length);
        for (Instruction size : this.sizes) {
            size.writeTo(pack);
        }
        pack.endArray();
        pack.endMap();
    }

    @Override
    public String toString() {
        String result = "new " + this.type.getClassName();
        for (int i = 0; i < this.sizes.length; i++) {
            result += "[";
            result += this.sizes[i];
            result += "]";
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MultiNewArray)) {
            return false;
        }
        MultiNewArray insn = (MultiNewArray) obj;
        return Arrays.equals(this.sizes, insn.sizes) && this.type.equals(insn.type);
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (Instruction size : this.sizes) {
            h = h * 37 + size.hashCode();
        }
        h = h * 37 + this.type.hashCode();
        return h;
    }

}

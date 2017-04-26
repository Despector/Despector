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
import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * An instruction initializing a new array.
 */
public class NewArray implements Instruction {

    private String type;
    private Instruction size;
    private Instruction[] values;

    public NewArray(String type, Instruction size, @Nullable Instruction[] values) {
        this.type = checkNotNull(type, "type");
        this.size = checkNotNull(size, "size");
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
        this.size = checkNotNull(size, "size");
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
    public TypeSignature inferType() {
        return ClassTypeSignature.of("[" + this.type);
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitNewArray(this);
            this.size.accept(visitor);
            if (this.values != null) {
                for (Instruction value : this.values) {
                    value.accept(visitor);
                }
            }
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(this.values == null ? 3 : 4);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_NEW_ARRAY);
        pack.writeString("type").writeString(this.type);
        pack.writeString("size");
        this.size.writeTo(pack);
        if (this.values != null) {
            pack.writeString("values").startArray(this.values.length);
            for (Instruction insn : this.values) {
                insn.writeTo(pack);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NewArray)) {
            return false;
        }
        NewArray insn = (NewArray) obj;
        if (this.values.length != insn.values.length) {
            return false;
        }
        for (int i = 0; i < this.values.length; i++) {
            if (!this.values[i].equals(insn.values[i])) {
                return false;
            }
        }
        return this.size.equals(insn.size) && this.type.equals(insn.type);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.size.hashCode();
        h = h * 37 + this.type.hashCode();
        for (Instruction insn : this.values) {
            h = h * 37 + insn.hashCode();
        }
        return h;
    }

}

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
package org.spongepowered.despector.ast.insn.cst;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * A constant float value.
 */
public class FloatConstant extends Constant {

    private float cst;

    public FloatConstant(float val) {
        this.cst = val;
    }

    /**
     * Gets the constant value.
     */
    public float getConstant() {
        return this.cst;
    }

    /**
     * Sets the constant value.
     */
    public void setConstant(float cst) {
        this.cst = cst;
    }

    @Override
    public TypeSignature inferType() {
        return ClassTypeSignature.FLOAT;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitFloatConstant(this);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_FLOAT_CONSTANT);
        pack.writeString("cst").writeFloat(this.cst);
    }

    @Override
    public String toString() {
        return String.valueOf(this.cst);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FloatConstant)) {
            return false;
        }
        FloatConstant cast = (FloatConstant) obj;
        return this.cst == cast.cst;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(this.cst);
    }
}

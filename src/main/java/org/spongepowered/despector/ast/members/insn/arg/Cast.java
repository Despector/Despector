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

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An instruction which casts the inner value to a specific type.
 * 
 * <p>Example: (type) (val)</p>
 */
public class Cast implements Instruction {

    private String type;
    private Instruction val;

    public Cast(String type, Instruction val) {
        this.type = checkNotNull(type, "type");
        this.val = checkNotNull(val, "val");
    }

    /**
     * Gets the type which the valye is casted to.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type which the valye is casted to.
     */
    public void setType(String type) {
        this.type = checkNotNull(type, "type");
    }

    /**
     * Gets the instruction for the type being cast.
     */
    public Instruction getValue() {
        return this.val;
    }

    /**
     * Sets the instruction for the type being cast.
     */
    public void setValue(Instruction val) {
        this.val = checkNotNull(val, "val");
    }

    @Override
    public String inferType() {
        return this.type;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitCastArg(this);
        this.val.accept(visitor);
    }

    @Override
    public String toString() {
        return "((" + TypeHelper.descToType(this.type) + ") " + this.val + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cast)) {
            return false;
        }
        Cast cast = (Cast) obj;
        return this.type.equals(cast.type) && this.val.equals(cast.val);
    }

}

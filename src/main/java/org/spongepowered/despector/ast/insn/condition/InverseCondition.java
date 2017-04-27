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
package org.spongepowered.despector.ast.insn.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * A condition based off of a simple boolean value.
 */
public class InverseCondition extends Condition {

    private Condition value;

    public InverseCondition(Condition value) {
        this.value = checkNotNull(value, "value");
    }

    /**
     * Gets the condition value being inverted.
     */
    public Condition getConditionValue() {
        return this.value;
    }

    /**
     * Sets the condition value being inverted.
     */
    public void setConditionValue(Condition val) {
        this.value = checkNotNull(val, "value");
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof ConditionVisitor) {
            ((ConditionVisitor) visitor).visitInverseCondition(this);
            this.value.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.CONDITION_ID_INVERSE);
        pack.writeString("val");
        this.value.writeTo(pack);
        pack.endMap();
    }

    @Override
    public String toString() {
        return "!" + this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InverseCondition)) {
            return false;
        }
        InverseCondition and = (InverseCondition) o;
        return this.value.equals(and.value);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.value.hashCode();
        return h;
    }

}

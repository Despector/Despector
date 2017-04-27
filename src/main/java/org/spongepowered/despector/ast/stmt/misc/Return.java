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
package org.spongepowered.despector.ast.stmt.misc;

import javax.annotation.Nullable;
import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.Optional;

/**
 * A return statement which returns a value.
 */
public class Return implements Statement {

    @Nullable
    private Instruction value;

    public Return() {
        this.value = null;
    }

    public Return(@Nullable Instruction val) {
        this.value = val;
    }

    /**
     * Gets the value being returned, if present.
     */
    public Optional<Instruction> getValue() {
        return Optional.ofNullable(this.value);
    }

    /**
     * Sets the value being returned, may be null.
     */
    public void setValue(@Nullable Instruction insn) {
        this.value = insn;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof StatementVisitor) {
            ((StatementVisitor) visitor).visitReturn(this);
        }
        if (this.value != null) {
            this.value.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_RETURN);
        pack.writeString("value");
        if (this.value != null) {
            this.value.writeTo(pack);
        } else {
            pack.writeNil();
        }
        pack.endMap();
    }

    @Override
    public String toString() {
        if (this.value == null) {
            return "return;";
        }
        return "return " + this.value + ";";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Return)) {
            return false;
        }
        Return insn = (Return) obj;
        return this.value.equals(insn.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

}

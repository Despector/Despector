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
package org.spongepowered.despector.ast.stmt.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.ast.stmt.branch.Break.Breakable;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A for loop.
 */
public class ForEach implements Statement, Breakable {

    private Instruction collection;
    private LocalInstance val;
    private StatementBlock body;
    private List<Break> breaks = new ArrayList<>();

    public ForEach(Instruction collection, LocalInstance val, StatementBlock body) {
        this.collection = checkNotNull(collection, "collection");
        this.val = checkNotNull(val, "val");
        this.body = checkNotNull(body, "body");
    }

    /**
     * Gets the collection being iterated over.
     */
    public Instruction getCollectionValue() {
        return this.collection;
    }

    /**
     * Sets the collection being iterated over.
     */
    public void setCollectionValue(Instruction collection) {
        this.collection = checkNotNull(collection, "collection");
    }

    /**
     * Gets the local that the current loop value is set to.
     */
    public LocalInstance getValueAssignment() {
        return this.val;
    }

    /**
     * Sets the local that the current loop value is set to.
     */
    public void setValueAssignment(LocalInstance val) {
        this.val = checkNotNull(val, "val");
    }

    /**
     * Gets the loop body.
     */
    public StatementBlock getBody() {
        return this.body;
    }

    /**
     * Sets the loop body.
     */
    public void setBody(StatementBlock block) {
        this.body = checkNotNull(block, "block");
    }

    @Override
    public List<Break> getBreaks() {
        return this.breaks;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof StatementVisitor) {
            ((StatementVisitor) visitor).visitForEach(this);
        }
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitLocalInstance(this.val);
        }
        this.collection.accept(visitor);
        for (Statement stmt : this.body.getStatements()) {
            stmt.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(5);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_FOREACH);
        pack.writeString("local");
        this.val.writeToSimple(pack);
        pack.writeString("collection");
        this.collection.writeTo(pack);
        pack.writeString("body");
        pack.startArray(this.body.getStatementCount());
        for (Statement stmt : this.body.getStatements()) {
            stmt.writeTo(pack);
        }
        pack.writeString("breakpoints").startArray(this.breaks.size());
        for (Break br : this.breaks) {
            pack.writeInt(((Object) br).hashCode());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("for (");
        sb.append(this.val.getTypeName());
        sb.append(" ").append(this.val.getName());
        sb.append(": ");
        sb.append(this.collection);
        sb.append(") {\n");
        for (Statement insn : this.body.getStatements()) {
            sb.append("    ").append(insn).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ForEach)) {
            return false;
        }
        ForEach insn = (ForEach) obj;
        return this.collection.equals(insn.collection) && this.body.equals(insn.body) && this.val.equals(insn.val);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.collection.hashCode();
        h = h * 37 + this.body.hashCode();
        h = h * 37 + this.val.hashCode();
        return h;
    }

}

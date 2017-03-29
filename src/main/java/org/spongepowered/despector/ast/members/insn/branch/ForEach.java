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
package org.spongepowered.despector.ast.members.insn.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.Break.Breakable;

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
    public void accept(InstructionVisitor visitor) {
        visitor.visitForEach(this);
        visitor.visitLocalInstance(this.val);
        this.collection.accept(visitor);
        for (Statement stmt : this.body.getStatements()) {
            stmt.accept(visitor);
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

}

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
package org.spongepowered.despector.ast.members.insn.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.branch.Break.Breakable;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A do-while loop.
 */
public class DoWhile implements Statement, Breakable {

    private Condition condition;
    private StatementBlock body;
    private List<Break> breaks = new ArrayList<>();

    public DoWhile(Condition condition, StatementBlock body) {
        this.condition = checkNotNull(condition, "condition");
        this.body = checkNotNull(body, "body");
    }

    /**
     * Gets the loop condition.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Sets the loop condition.
     */
    public void setCondition(Condition condition) {
        this.condition = checkNotNull(condition, "condition");
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
        visitor.visitDoWhile(this);
        this.condition.accept(visitor);
        for (Statement stmt : this.body.getStatements()) {
            stmt.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("do {\n");
        for (Statement insn : this.body.getStatements()) {
            sb.append("    ").append(insn).append("\n");
        }
        sb.append("} while (");
        sb.append(this.condition);
        sb.append(");");
        return sb.toString();
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(4);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_DO_WHILE);
        pack.writeString("condition");
        this.condition.writeTo(pack);
        pack.writeString("body");
        this.body.writeTo(pack);
        pack.writeString("breakpoints").startArray(this.breaks.size());
        for (Break br : this.breaks) {
            pack.writeInt(((Object) br).hashCode());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DoWhile)) {
            return false;
        }
        DoWhile insn = (DoWhile) obj;
        return this.condition.equals(insn.condition) && this.body.equals(insn.body);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.condition.hashCode();
        h = h * 37 + this.body.hashCode();
        return h;
    }

}

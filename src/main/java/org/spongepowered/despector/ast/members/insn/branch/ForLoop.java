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

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

import javax.annotation.Nullable;

/**
 * A for loop.
 */
public class ForLoop implements Statement {

    private Statement init;
    private Condition condition;
    private Statement incr;
    private StatementBlock body;

    public ForLoop(@Nullable Statement init, Condition condition, @Nullable Statement incr, StatementBlock body) {
        this.init = init;
        this.condition = checkNotNull(condition, "condition");
        this.incr = incr;
        this.body = checkNotNull(body, "body");
    }

    @Nullable
    public Statement getInit() {
        return this.init;
    }

    public void setInit(@Nullable Statement init) {
        this.init = init;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition condition) {
        this.condition = checkNotNull(condition, "condition");
    }

    @Nullable
    public Statement getIncr() {
        return this.incr;
    }

    public void setIncr(@Nullable Statement incr) {
        this.incr = incr;
    }

    public StatementBlock getBody() {
        return this.body;
    }

    public void setBody(StatementBlock block) {
        this.body = checkNotNull(block, "block");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitForLoop(this);
        this.init.accept(visitor);
        this.condition.accept(visitor);
        this.incr.accept(visitor);
        for (Statement stmt : this.body.getStatements()) {
            stmt.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("for (");
        if (this.init != null) {
            sb.append(this.init);
        }
        sb.append(";");
        sb.append(this.condition);
        sb.append(";");
        if (this.incr != null) {
            sb.append(this.incr);
        }
        sb.append(") {\n");
        for (Statement insn : this.body.getStatements()) {
            sb.append("    ").append(insn).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

}

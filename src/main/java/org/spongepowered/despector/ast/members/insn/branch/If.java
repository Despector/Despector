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
 * An if block.
 */
public class If implements Statement {

    // TODO better elif support

    private Condition condition;
    private StatementBlock block;
    private Else else_block;

    public If(Condition condition, StatementBlock insn) {
        this.condition = checkNotNull(condition, "condition");
        this.block = checkNotNull(insn, "block");
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition condition) {
        this.condition = checkNotNull(condition, "condition");
    }

    public StatementBlock getIfBody() {
        return this.block;
    }

    public void setBody(StatementBlock block) {
        this.block = checkNotNull(block, "block");
    }

    @Nullable
    public Else getElseBlock() {
        return this.else_block;
    }

    public void setElseBlock(@Nullable Else else_block) {
        this.else_block = else_block;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitIfBlock(this);
        this.condition.accept(visitor);
        for (Statement stmt : this.block.getStatements()) {
            stmt.accept(visitor);
        }
        if (this.else_block != null) {
            this.else_block.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("if (");
        sb.append(this.condition).append(") {\n");
        for (Statement insn : this.block.getStatements()) {
            sb.append("    ").append(insn).append("\n");
        }
        sb.append("}");
        if (this.else_block != null) {
            sb.append(" ").append(this.else_block);
        }
        return sb.toString();
    }

    public class Else {

        private StatementBlock block;

        public Else(StatementBlock block) {
            this.block = checkNotNull(block, "block");
        }

        public StatementBlock getElseBody() {
            return this.block;
        }

        public void setElseBody(StatementBlock block) {
            this.block = checkNotNull(block, "block");
        }

        public void accept(InstructionVisitor visitor) {
            visitor.visitElseBlock(this);
            for (Statement stmt : this.block.getStatements()) {
                stmt.accept(visitor);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("else {\n");
            for (Statement insn : this.block.getStatements()) {
                sb.append("    ").append(insn).append("\n");
            }
            sb.append("}");
            return sb.toString();
        }

    }

}

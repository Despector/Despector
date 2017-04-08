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
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * An if block, possibly with attached else if and else blocks.
 */
public class If implements Statement {

    private Condition condition;
    private StatementBlock block;
    private final List<Elif> elif_blocks = new ArrayList<>();
    @Nullable
    private Else else_block;

    public If(Condition condition, StatementBlock insn) {
        this.condition = checkNotNull(condition, "condition");
        this.block = checkNotNull(insn, "block");
    }

    /**
     * Gets the condition.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Sets the condition.
     */
    public void setCondition(Condition condition) {
        this.condition = checkNotNull(condition, "condition");
    }

    /**
     * Gets the body of the if statement.
     */
    public StatementBlock getIfBody() {
        return this.block;
    }

    /**
     * Sets the body of the if statement.
     */
    public void setBody(StatementBlock block) {
        this.block = checkNotNull(block, "block");
    }

    /**
     * Gets any else-if blocks attached to this if statement.
     */
    public List<Elif> getElifBlocks() {
        return this.elif_blocks;
    }

    /**
     * Adds the given else-if block to this if statement.
     */
    public void addElifBlock(Elif e) {
        this.elif_blocks.add(checkNotNull(e, "elif"));
    }

    /**
     * Gets the else block of this if statement, may be null.
     */
    @Nullable
    public Else getElseBlock() {
        return this.else_block;
    }

    /**
     * Sets the else block of this if statement, may be null.
     */
    public void setElseBlock(@Nullable Else else_block) {
        this.else_block = else_block;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitIf(this);
        this.condition.accept(visitor);
        for (Statement stmt : this.block.getStatements()) {
            stmt.accept(visitor);
        }
        if (this.else_block != null) {
            this.else_block.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(this.else_block != null ? 5 : 4);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_IF);
        pack.writeString("condition");
        this.condition.writeTo(pack);
        pack.writeString("body");
        this.block.writeTo(pack);
        pack.writeString("elif").startArray(this.elif_blocks.size());
        for (Elif elif : this.elif_blocks) {
            pack.startMap(2);
            pack.writeString("condition");
            elif.getCondition().writeTo(pack);
            pack.writeString("body");
            elif.getBody().writeTo(pack);
        }
        if (this.else_block != null) {
            pack.writeString("else");
            this.else_block.getElseBody().writeTo(pack);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof If)) {
            return false;
        }
        If insn = (If) obj;
        if (this.else_block != null) {
            if (!this.else_block.equals(insn.else_block)) {
                return false;
            }
        } else if (insn.else_block != null) {
            return false;
        }
        if (!this.elif_blocks.equals(insn.elif_blocks)) {
            return false;
        }
        return this.condition.equals(insn.condition) && this.block.equals(insn.block);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.condition.hashCode();
        h = h * 37 + this.block.hashCode();
        h = h * 37 + (this.else_block == null ? 0 : this.else_block.hashCode());
        for (Elif elif : this.elif_blocks) {
            h = h * 37 + elif.hashCode();
        }
        return h;
    }

    /**
     * An else-if block attached to an if statement.
     */
    public class Elif {

        private StatementBlock block;
        private Condition condition;

        public Elif(Condition cond, StatementBlock block) {
            this.condition = checkNotNull(cond, "condition");
            this.block = checkNotNull(block, "block");
            If.this.addElifBlock(this);
        }

        /**
         * Gets the condition of this block.
         */
        public Condition getCondition() {
            return this.condition;
        }

        /**
         * Sets the condition of this block.
         */
        public void setCondition(Condition cond) {
            this.condition = checkNotNull(cond, "condition");
        }

        /**
         * Gets the body of this block.
         */
        public StatementBlock getBody() {
            return this.block;
        }

        /**
         * Sets the body of this block.
         */
        public void setBody(StatementBlock block) {
            this.block = checkNotNull(block, "block");
        }

        /**
         * Accepts the given visitor.
         */
        public void accept(InstructionVisitor visitor) {
            visitor.visitElif(this);
            for (Statement stmt : this.block.getStatements()) {
                stmt.accept(visitor);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("else if(");
            sb.append(this.condition.toString());
            sb.append(") {\n");
            for (Statement insn : this.block.getStatements()) {
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
            if (!(obj instanceof Elif)) {
                return false;
            }
            Elif insn = (Elif) obj;
            return this.condition.equals(insn.condition) && this.block.equals(insn.block);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h = h * 37 + this.condition.hashCode();
            h = h * 37 + this.block.hashCode();
            return h;
        }
    }

    /**
     * The else block of an if statement.
     */
    public class Else {

        private StatementBlock block;

        public Else(StatementBlock block) {
            this.block = checkNotNull(block, "block");
            If.this.setElseBlock(this);
        }

        /**
         * Gets the body of this block.
         */
        public StatementBlock getElseBody() {
            return this.block;
        }

        /**
         * Sets the body of this block.
         */
        public void setElseBody(StatementBlock block) {
            this.block = checkNotNull(block, "block");
        }

        /**
         * Accepts the given visitor.
         */
        public void accept(InstructionVisitor visitor) {
            visitor.visitElse(this);
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Else)) {
                return false;
            }
            Else insn = (Else) obj;
            return this.block.equals(insn.block);
        }

        @Override
        public int hashCode() {
            return this.block.hashCode();
        }

    }

}

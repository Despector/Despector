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
package org.spongepowered.despector.decompiler.method.graph.data.block;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.If.Elif;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section representing an if statement, optionally with a number of
 * else-if blocks and an else block.
 */
public class IfBlockSection extends BlockSection {

    private final Condition condition;
    private final List<BlockSection> body = new ArrayList<>();
    private final List<BlockSection> else_ = new ArrayList<>();
    private final List<ElifBlockSection> elif = new ArrayList<>();

    public IfBlockSection(Condition cond) {
        this.condition = cond;
    }

    /**
     * Gets the condition of the initial if statement.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Gets the {@link BlockSection}s that form the body of the initial if
     * statement.
     */
    public List<BlockSection> getBody() {
        return this.body;
    }

    /**
     * Appends the given {@link BlockSection} to the body of the initial if
     * statement.
     */
    public void appendBody(BlockSection section) {
        this.body.add(section);
    }

    /**
     * Gets the {@link BlockSection}s that form the else block of this if
     * statement.
     */
    public List<BlockSection> getElseBody() {
        return this.else_;
    }

    /**
     * Appends the given {@link BlockSection} to the else body of this if
     * statement.
     */
    public void appendElseBody(BlockSection section) {
        this.else_.add(section);
    }

    /**
     * Gets any else-if blocks attached to this if statement.
     */
    public List<ElifBlockSection> getElifBlocks() {
        return this.elif;
    }

    /**
     * Adds an else-if block to this if statement.
     */
    public void addElif(ElifBlockSection elif) {
        this.elif.add(elif);
    }

    @Override
    public void appendTo(StatementBlock block, Locals locals, Deque<Instruction> stack) {
        StatementBlock body = new StatementBlock(StatementBlock.Type.IF);
        Deque<Instruction> body_stack = new ArrayDeque<>();
        for (BlockSection body_section : this.body) {
            body_section.appendTo(body, locals, body_stack);
        }
        if (!body_stack.isEmpty()) {
            throw new IllegalStateException();
        }
        If iff = new If(this.condition, body);
        for (ElifBlockSection elif : this.elif) {
            StatementBlock elif_body = new StatementBlock(StatementBlock.Type.IF);
            Deque<Instruction> elif_stack = new ArrayDeque<>();
            for (BlockSection body_section : elif.getBody()) {
                body_section.appendTo(elif_body, locals, elif_stack);
            }
            checkState(elif_stack.isEmpty());
            iff.new Elif(elif.getCondition(), elif_body);
        }
        if (!this.else_.isEmpty()) {
            StatementBlock else_body = new StatementBlock(StatementBlock.Type.IF);
            Deque<Instruction> else_stack = new ArrayDeque<>();
            for (BlockSection body_section : this.else_) {
                body_section.appendTo(else_body, locals, else_stack);
            }
            checkState(else_stack.isEmpty());

            if (else_body.getStatements().size() == 1 && else_body.getStatements().get(0) instanceof If) {
                If internal_if = (If) else_body.getStatements().get(0);
                iff.new Elif(internal_if.getCondition(), internal_if.getBody());
                for (Elif el : internal_if.getElifBlocks()) {
                    iff.new Elif(el.getCondition(), el.getBody());
                }
                if (internal_if.getElseBlock() != null) {
                    iff.new Else(internal_if.getElseBlock().getBody());
                }
            } else {
                iff.new Else(else_body);
            }
        }
        block.append(iff);
    }

    /**
     * An elif block attached to an if statement.
     */
    public class ElifBlockSection {

        private final Condition condition;
        private final List<BlockSection> body = new ArrayList<>();

        /**
         * Creates a new {@link ElifBlockSection}. The new block is
         * automatically added to the parent {@link IfBlockSection}.
         */
        public ElifBlockSection(Condition cond) {
            this.condition = cond;
            IfBlockSection.this.addElif(this);
        }

        /**
         * Gets the condition of this else-if block.
         */
        public Condition getCondition() {
            return this.condition;
        }

        /**
         * Gets the {@link BlockSection}s that form the body.
         */
        public List<BlockSection> getBody() {
            return this.body;
        }

        /**
         * Appends the given {@link BlockSection} to the body.
         */
        public void append(BlockSection section) {
            this.body.add(section);
        }

    }
}

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
package org.spongepowered.despector.decompiler.kotlin.method.graph.data;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.kotlin.When;
import org.spongepowered.despector.ast.kotlin.When.Case;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.StatementBlock.Type;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * a block section containing a processed kotlin when statement.
 */
public class WhenBlockSection extends BlockSection {

    private final List<WhenCondition> conditions = new ArrayList<>();
    private final List<BlockSection> else_body = new ArrayList<>();

    public WhenBlockSection() {
    }

    public List<WhenCondition> getConditions() {
        return this.conditions;
    }

    /**
     * Gets the {@link BlockSection}s forming the false case of this ternary.
     */
    public List<BlockSection> getElseBody() {
        return this.else_body;
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        Statement last = block.popStatement();
        checkState(last instanceof LocalAssignment);
        When when = new When(((LocalAssignment) last).getLocal(), ((LocalAssignment) last).getValue());
        boolean has_last = false;
        for (WhenCondition cond : this.conditions) {
            StatementBlock case_body = new StatementBlock(Type.SWITCH, block.getLocals());
            Deque<Instruction> case_stack = new ArrayDeque<>();
            for (BlockSection section : cond.getBody()) {
                section.appendTo(case_body, case_stack);
            }
            Case cs = new Case(cond.getCondition(), case_body, case_stack.peek());
            has_last = cs.getLast() != null;
            when.getCases().add(cs);
        }
        StatementBlock case_body = new StatementBlock(Type.SWITCH, block.getLocals());
        Deque<Instruction> case_stack = new ArrayDeque<>();
        for (BlockSection section : this.else_body) {
            section.appendTo(case_body, case_stack);
        }
        when.setElseBody(case_body, case_stack.peek());
        if (has_last) {
            stack.push(when);
        } else {
            block.append(new InvokeStatement(when));
        }
    }

    /**
     * A condition of a when statement.
     */
    public static class WhenCondition {

        private Condition condition;
        private final List<BlockSection> body = new ArrayList<>();

        public WhenCondition(Condition cond) {
            this.condition = cond;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public void setCondition(Condition cond) {
            this.condition = cond;
        }

        public List<BlockSection> getBody() {
            return this.body;
        }

    }
}

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

import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.branch.While;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section representing a while block.
 */
public class WhileBlockSection extends BlockSection implements BreakableBlockSection {

    private final Condition condition;
    private final List<BlockSection> body = new ArrayList<>();
    private final List<BreakBlockSection> breaks = new ArrayList<>();

    public WhileBlockSection(Condition cond) {
        this.condition = cond;
    }

    /**
     * Gets the condition of the while block.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Gets the block sections forming the body.
     */
    public List<BlockSection> getBody() {
        return this.body;
    }

    /**
     * Appends the given {@link BlockSection} to the body.
     */
    public void appendBody(BlockSection block) {
        this.body.add(block);
    }

    @Override
    public List<BreakBlockSection> getBreaks() {
        return this.breaks;
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        StatementBlock body = new StatementBlock(StatementBlock.Type.WHILE, block.getLocals());
        While wwhile = new While(this.condition, body);
        for (BreakBlockSection bbreak : this.breaks) {
            bbreak.setBreakable(wwhile);
        }
        Deque<Instruction> body_stack = new ArrayDeque<>();
        for (BlockSection body_section : this.body) {
            body_section.appendTo(body, body_stack);
        }
        checkState(body_stack.isEmpty());
        block.append(wwhile);
    }
}

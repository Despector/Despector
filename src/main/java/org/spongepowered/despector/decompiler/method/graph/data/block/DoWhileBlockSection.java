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
package org.spongepowered.despector.decompiler.method.graph.data.block;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section representing a do-while loop.
 */
public class DoWhileBlockSection extends BlockSection {

    private final Condition condition;
    private final List<BlockSection> body = new ArrayList<>();

    public DoWhileBlockSection(Condition cond) {
        this.condition = cond;
    }

    /**
     * Gets the condition of the loop.
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Gets the {@link BlockSection}s that form the body of the loop.
     */
    public List<BlockSection> getBody() {
        return this.body;
    }

    /**
     * Appends the given {@link BlockSection} to the body of the loop.
     */
    public void append(BlockSection section) {
        this.body.add(section);
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        StatementBlock body = new StatementBlock(StatementBlock.Type.WHILE, block.getLocals());
        Deque<Instruction> body_stack = new ArrayDeque<>();
        for (BlockSection body_section : this.body) {
            body_section.appendTo(body, body_stack);
        }
        checkState(body_stack.isEmpty());
        DoWhile dowhile = new DoWhile(this.condition, body);
        block.append(dowhile);
    }
}

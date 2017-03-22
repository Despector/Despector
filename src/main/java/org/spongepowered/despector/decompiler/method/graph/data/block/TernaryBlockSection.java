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
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TernaryBlockSection extends BlockSection {

    private final List<BlockSection> true_body = new ArrayList<>();
    private final List<BlockSection> false_body = new ArrayList<>();
    private final Condition condition;

    public TernaryBlockSection(Condition cond) {
        this.condition = cond;
    }

    /**
     * Gets the {@link BlockSection}s forming the true case of this ternary.
     */
    public List<BlockSection> getTrueBody() {
        return this.true_body;
    }

    /**
     * Gets the {@link BlockSection}s forming the false case of this ternary.
     */
    public List<BlockSection> getFalseBody() {
        return this.false_body;
    }

    /**
     * Gets the condition of this ternary.
     */
    public Condition getCondition() {
        return this.condition;
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        StatementBlock dummy = new StatementBlock(StatementBlock.Type.IF, block.getLocals());
        Deque<Instruction> dummy_stack = new ArrayDeque<>();
        for (BlockSection sec : this.true_body) {
            sec.appendTo(dummy, dummy_stack);
        }
        checkState(dummy_stack.size() == 1);
        Instruction true_val = dummy_stack.pop();
        for (BlockSection sec : this.false_body) {
            sec.appendTo(dummy, dummy_stack);
        }
        checkState(dummy_stack.size() == 1);
        Instruction false_val = dummy_stack.pop();
        Ternary ternary = new Ternary(this.condition, true_val, false_val);
        stack.push(ternary);
    }
}

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
package org.spongepowered.despector.decompiler.method.graph.data;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.decompiler.method.StatementBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section representing a switch statement.
 */
public class SwitchBlockSection extends BlockSection {

    private final OpcodeBlock switchblock;
    private final List<SwitchCaseBlockSection> cases = new ArrayList<>();

    public SwitchBlockSection(OpcodeBlock s) {
        this.switchblock = s;
    }

    /**
     * Gets the opcode block ending with the initial switch opcode.
     */
    public OpcodeBlock getSwitchBlock() {
        return this.switchblock;
    }

    /**
     * Gets the cases of this switch block.
     */
    public List<SwitchCaseBlockSection> getCases() {
        return this.cases;
    }

    /**
     * Adds a case to this switch block.
     */
    public void addCase(SwitchCaseBlockSection cs) {
        this.cases.add(cs);
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        StatementBuilder.appendBlock(this.switchblock, block, block.getLocals(), stack);
        Switch sswitch = new Switch(stack.pop());
        for (SwitchCaseBlockSection cs : this.cases) {
            StatementBlock body = new StatementBlock(StatementBlock.Type.SWITCH, block.getLocals());
            Deque<Instruction> body_stack = new ArrayDeque<>();
            for (BlockSection body_section : cs.getBody()) {
                body_section.appendTo(body, body_stack);
            }
            checkState(body_stack.isEmpty());
            sswitch.new Case(body, cs.doesBreak(), cs.isDefault(), cs.getTargets());
        }
        block.append(sswitch);
    }

    /**
     * Represents a switch case block.
     */
    public class SwitchCaseBlockSection {

        private final List<BlockSection> body = new ArrayList<>();
        private final List<Integer> targets = new ArrayList<>();
        private boolean breaks;
        private boolean is_default;

        public SwitchCaseBlockSection() {
        }

        /**
         * Gets the {@link BlockSection}s forming the body.
         */
        public List<BlockSection> getBody() {
            return this.body;
        }

        /**
         * Gets the targets of this case.
         */
        public List<Integer> getTargets() {
            return this.targets;
        }

        /**
         * Gets if this case ends with a break or flows into the next case.
         */
        public boolean doesBreak() {
            return this.breaks;
        }

        /**
         * Sets if this case ends with a break or flows into the next case.
         */
        public void setBreaks(boolean state) {
            this.breaks = state;
        }

        /**
         * Gets if this case is the default case.
         */
        public boolean isDefault() {
            return this.is_default;
        }

        /**
         * Sets if this case is the default case.
         */
        public void setDefault(boolean state) {
            this.is_default = state;
        }

    }

}

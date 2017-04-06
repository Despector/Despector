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

import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.Break;
import org.spongepowered.despector.ast.members.insn.branch.Break.Breakable;
import org.spongepowered.despector.ast.members.insn.branch.Break.Type;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock.MarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section that contains a single {@link OpcodeBlock}.
 */
public class BreakBlockSection extends BlockSection {

    private BlockSection loop;
    private BreakMarkerOpcodeBlock marker;
    private MarkerType type;
    private boolean nested;
    private Breakable br;
    private Break final_break;

    private final List<ConditionalOpcodeBlock> inlined = new ArrayList<>();

    public BreakBlockSection(BreakMarkerOpcodeBlock marker, MarkerType type) {
        this.marker = marker;
        this.type = type;
    }

    /**
     * Gets the {@link OpcodeBlock} that is represented by this block section.
     */
    public BlockSection getLoop() {
        return this.loop;
    }

    public void setLoop(BlockSection section) {
        this.loop = section;
    }

    public BreakMarkerOpcodeBlock getMarker() {
        return this.marker;
    }

    public MarkerType getType() {
        return this.type;
    }

    public boolean isNested() {
        return this.nested;
    }

    public void setNested(boolean state) {
        this.nested = state;
    }

    public Breakable getBreakable() {
        return this.br;
    }

    public void setBreakable(Breakable br) {
        this.br = br;
        if (this.final_break != null) {
            this.final_break.setLoop(br);
        }
    }

    public List<ConditionalOpcodeBlock> getInlinedConditions() {
        return this.inlined;
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        this.final_break = new Break(this.br, this.type == MarkerType.BREAK ? Type.BREAK : Type.CONTINUE, this.nested);
        if (!this.inlined.isEmpty()) {
            ConditionalOpcodeBlock last = this.inlined.get(this.inlined.size() - 1);
            Condition cond = ConditionBuilder.makeCondition(this.inlined, block.getLocals(), last.getTarget(), last.getElseTarget());
            StatementBlock inner = new StatementBlock(StatementBlock.Type.IF, block.getLocals());
            inner.append(this.final_break);
            If ifblock = new If(cond, inner);
            block.append(ifblock);
            return;
        }
        block.append(this.final_break);
    }
}

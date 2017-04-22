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

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.Break.Breakable;
import org.spongepowered.despector.ast.stmt.branch.Break.Type;
import org.spongepowered.despector.ast.stmt.branch.If;
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
     * Gets the {@link OpcodeBlock} that broken by this break.
     */
    public BlockSection getLoop() {
        return this.loop;
    }

    /**
     * Sets the {@link OpcodeBlock} that broken by this break.
     */
    public void setLoop(BlockSection section) {
        this.loop = section;
    }

    /**
     * Gets the marker opcode block this section was formed from.
     */
    public BreakMarkerOpcodeBlock getMarker() {
        return this.marker;
    }

    /**
     * Gets the break marker type.
     */
    public MarkerType getType() {
        return this.type;
    }

    /**
     * Gets if this break is nested within an inner loop and will require a
     * label to break the outer loop.
     */
    public boolean isNested() {
        return this.nested;
    }

    /**
     * Sets if this break is nested within an inner loop and will require a
     * label to break the outer loop.
     */
    public void setNested(boolean state) {
        this.nested = state;
    }

    /**
     * Gets the breakable loop that this break is breaking.
     */
    public Breakable getBreakable() {
        return this.br;
    }

    /**
     * Sets the breakable loop that this break is breaking.
     */
    public void setBreakable(Breakable br) {
        this.br = br;
        if (this.final_break != null) {
            this.final_break.setLoop(br);
        }
    }

    /**
     * Gets any inlined conditional blocks for this control flow break.
     * 
     * <p>Sometimes a control flow break is formed from a set of conditions
     * rather than a goto if the goto would be the only thing in the body of an
     * if statement.</p>
     */
    public List<ConditionalOpcodeBlock> getInlinedConditions() {
        return this.inlined;
    }

    @Override
    public void appendTo(StatementBlock block, Locals locals, Deque<Instruction> stack) {
        this.final_break = new Break(this.br, this.type == MarkerType.BREAK ? Type.BREAK : Type.CONTINUE, this.nested);
        if (!this.inlined.isEmpty()) {
            ConditionalOpcodeBlock last = this.inlined.get(this.inlined.size() - 1);
            Condition cond = ConditionBuilder.makeCondition(this.inlined, locals, last.getTarget(), last.getElseTarget());
            StatementBlock inner = new StatementBlock(StatementBlock.Type.IF);
            inner.append(this.final_break);
            If ifblock = new If(cond, inner);
            block.append(ifblock);
            return;
        }
        block.append(this.final_break);
    }
}

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
package org.spongepowered.despector.decompiler.method.graph.create;

import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.List;
import java.util.Set;

/**
 * A graph producer that creates opcode blocks for condition and unconditional
 * jumps.
 */
public class JumpGraphProducerStep implements GraphProducerStep {

    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        InsnBlock instructions = partial.getOpcodes();

        for (int i = 0; i < instructions.size(); i++) {
            Insn next = instructions.get(i);
            if (next instanceof JumpInsn) {
                if(next.getOpcode() == Insn.GOTO && i > 0) {
                    break_points.add(i - 1);
                }
                break_points.add(i);
                // also break before labels targetted by jump opcodes to have a
                // break between the body of an if block and the statements
                // after it
                int target = ((JumpInsn) next).getTarget() - 1;
                if (target >= 0) {
                    break_points.add(target);
                }
                continue;
            }
        }
    }

    @Override
    public void formEdges(PartialMethod partial, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        for (int i = 0; i < block_list.size(); i++) {
            // Now we go through and form an edge from any block and the block
            // it flows (or jumps) into next.
            OpcodeBlock block = block_list.get(i);
            if (block.getLast() instanceof JumpInsn) {
                int label = ((JumpInsn) block.getLast()).getTarget();
                if (block.getLast().getOpcode() == Insn.GOTO) {
                    GotoOpcodeBlock replacement = new GotoOpcodeBlock(block.getStart(), block.getEnd());
                    block_list.set(i, replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(GraphProducerStep.find(block_list, label));
                    GraphOperation.remap(block_list, block, replacement);
                } else {
                    ConditionalOpcodeBlock replacement = new ConditionalOpcodeBlock(block.getStart(), block.getEnd());
                    OpcodeBlock next = block_list.get(block_list.indexOf(block) + 1);
                    block_list.set(i, replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(GraphProducerStep.find(block_list, label));
                    replacement.setElseTarget(next);
                    GraphOperation.remap(block_list, block, replacement);
                }
            }
        }
    }

}

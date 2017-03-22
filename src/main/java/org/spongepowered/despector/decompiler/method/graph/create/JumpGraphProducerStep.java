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
package org.spongepowered.despector.decompiler.method.graph.create;

import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph producer that creates opcode blocks for condition and unconditional
 * jumps.
 */
public class JumpGraphProducerStep implements GraphProducerStep {

    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        List<AbstractInsnNode> instructions = partial.getOpcodes();
        Map<Label, Integer> label_indices = partial.getLabelIndices();

        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode next = instructions.get(i);
            if (next instanceof JumpInsnNode) {
                break_points.add(i);
                // also break before labels targetted by jump opcodes to have a
                // break between the body of an if block and the statements
                // after it
                break_points.add(label_indices.get(((JumpInsnNode) next).label.getLabel()));
                continue;
            }
            int op = next.getOpcode();
            if (op <= RETURN && op >= IRETURN || op == ATHROW) {
                break_points.add(i);
            }
        }
    }

    @Override
    public void formEdges(PartialMethod partial, Map<Integer, OpcodeBlock> blocks, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        Map<Label, Integer> label_indices = partial.getLabelIndices();
        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            // Now we go through and form an edge from any block and the block
            // it flows (or jumps) into next.
            OpcodeBlock block = e.getValue();
            if (block.getLast() instanceof JumpInsnNode) {
                Label label = ((JumpInsnNode) block.getLast()).label.getLabel();
                if(block.getLast().getOpcode() == GOTO) {
                    GotoOpcodeBlock replacement = new GotoOpcodeBlock(block.getBreakpoint());
                    e.setValue(replacement);
                    block_list.set(block_list.indexOf(block), replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
                    GraphOperation.remap(block_list, block, replacement);
                } else {
                    ConditionalOpcodeBlock replacement = new ConditionalOpcodeBlock(block.getBreakpoint());
                    e.setValue(replacement);
                    block_list.set(block_list.indexOf(block), replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
                    OpcodeBlock next = blocks.get(sorted_break_points.get(sorted_break_points.indexOf(e.getKey()) + 1));
                    replacement.setElseTarget(next);
                    GraphOperation.remap(block_list, block, replacement);
                }
            }
        }
    }

}

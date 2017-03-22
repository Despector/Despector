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

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.OpcodeBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph producer that creates opcode blocks for switch statements.
 */
public class SwitchGraphProducerStep implements GraphProducerStep {

    @SuppressWarnings("unchecked")
    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        List<AbstractInsnNode> instructions = partial.getOpcodes();
        Map<Label, Integer> label_indices = partial.getLabelIndices();

        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode next = instructions.get(i);
            if (next instanceof TableSwitchInsnNode) {
                break_points.add(i);
                TableSwitchInsnNode ts = (TableSwitchInsnNode) next;
                for (LabelNode l : (List<LabelNode>) ts.labels) {
                    break_points.add(label_indices.get(l.getLabel()));
                }
                break_points.add(label_indices.get(ts.dflt.getLabel()));
            } else if (next instanceof LookupSwitchInsnNode) {
                break_points.add(i);
                LookupSwitchInsnNode ts = (LookupSwitchInsnNode) next;
                for (LabelNode l : (List<LabelNode>) ts.labels) {
                    break_points.add(label_indices.get(l.getLabel()));
                }
                break_points.add(label_indices.get(ts.dflt.getLabel()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void formEdges(PartialMethod partial, Map<Integer, OpcodeBlock> blocks, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        Map<Label, Integer> label_indices = partial.getLabelIndices();
        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            // Now we go through and form an edge from any block and the block
            // it flows (or jumps) into next.
            OpcodeBlock block = e.getValue();
            if (block.getLast() instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode ts = (TableSwitchInsnNode) block.getLast();
                for (LabelNode l : (List<LabelNode>) ts.labels) {
                    Label label = l.getLabel();
                    block.getAdditionalTargets().put(label,
                            blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
                }
                Label label = ts.dflt.getLabel();
                block.getAdditionalTargets().put(label,
                        blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
            } else if (block.getLast() instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode ts = (LookupSwitchInsnNode) block.getLast();
                for (LabelNode l : (List<LabelNode>) ts.labels) {
                    Label label = l.getLabel();
                    block.getAdditionalTargets().put(label,
                            blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
                }
                Label label = ts.dflt.getLabel();
                block.getAdditionalTargets().put(label,
                        blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1)));
            }
        }
    }

}

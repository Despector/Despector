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
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.SwitchOpcodeBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph producer that creates opcode blocks for switch statements.
 */
public class SwitchGraphProducerStep implements GraphProducerStep {

    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        InsnBlock instructions = partial.getOpcodes();

        for (int i = 0; i < instructions.size(); i++) {
            Insn next = instructions.get(i);
            if (next instanceof SwitchInsn) {
                break_points.add(i);
                SwitchInsn ts = (SwitchInsn) next;
                for (int l : ts.getTargets().values()) {
                    break_points.add(l - 1);
                }
                break_points.add(ts.getDefault() - 1);
            }
        }
    }

    @Override
    public void formEdges(PartialMethod partial, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        for (int i = 0; i < block_list.size(); i++) {
            // Now we go through and form an edge from any block and the block
            // it flows (or jumps) into next.
            OpcodeBlock block = block_list.get(i);
            if (!(block.getLast() instanceof SwitchInsn)) {
                continue;
            }
            SwitchOpcodeBlock replacement = new SwitchOpcodeBlock(block.getStart(), block.getEnd());
            replacement.getOpcodes().addAll(block.getOpcodes());
            replacement.setTarget(block.getTarget());
            block_list.set(i, replacement);
            GraphOperation.remap(block_list, block, replacement);
            SwitchInsn ts = (SwitchInsn) block.getLast();
            for (Map.Entry<Integer, Integer> r : ts.getTargets().entrySet()) {
                replacement.getAdditionalTargets().put(r.getKey(), GraphProducerStep.find(block_list, r.getValue()));
            }
            replacement.getAdditionalTargets().put(-1, GraphProducerStep.find(block_list, ts.getDefault()));
        }
    }

}

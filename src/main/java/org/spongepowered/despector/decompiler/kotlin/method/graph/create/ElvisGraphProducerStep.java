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
package org.spongepowered.despector.decompiler.kotlin.method.graph.create;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.kotlin.method.graph.data.ElvisBlockSection;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ProcessedOpcodeBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph operation that pre-processes ternaries before the rest of the graph
 * processing.
 */
public class ElvisGraphProducerStep implements GraphProducerStep {

    private Map<Integer, ElvisBlockSection> sections = new HashMap<>();

    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        this.sections.clear();
        InsnBlock ops = partial.getOpcodes();
        if (ops.size() == 0) {
            return;
        }
        int last = ops.get(0).getOpcode();
        for (int i = 1; i < ops.size(); i++) {
            // an elvis statement when compiled will look something like:
            //
            // Example: var a = b ?: ""
            //
            // ALOAD 0 the arg of the elvis
            // DUP
            // IFNULL L1 might be a numerical comparison as well
            // GOTO L2
            // L1
            // POP
            // LDC "" the default value
            // L2
            // ASTORE 1 the consumer of the value
            //
            // Sometimes with the condition inverted and the else case placed
            // before the GOTO
            int next = ops.get(i).getOpcode();
            if (last == Insn.DUP && ((next >= Insn.IFEQ && next <= Insn.IF_CMPNE))) {
                // Hello elvis
                int o = i;
                int start = i - 1;
                Insn search = ops.get(o++);
                int target = ((JumpInsn) search).getTarget();
                JumpInsn ggoto = null;
                // loop forwards and look for the label we're targeting with the
                // check, and pick up the last goto on the way
                //
                // There will occasionally be a cast or something in here, not
                // sure if I need to pay attention to it or not for the purposes
                // of decompiling.
                while (true) {
                    search = ops.get(o++);
                    if (o - 1 == target) {
                        break;
                    } else if (search.getOpcode() == Insn.GOTO) {
                        ggoto = (JumpInsn) search;
                    }
                }
                checkState(ggoto != null);
                // The target will be the end of the else body
                target = ggoto.getTarget();
                List<Insn> else_body = new ArrayList<>();
                boolean first_pop = false;
                while (true) {
                    search = ops.get(o++);
                    if (o - 1 == target) {
                        break;
                    }
                    // we ignore the first pop which is removing the checked
                    // value from the stack
                    if (search.getOpcode() == Insn.POP && !first_pop) {
                        first_pop = true;
                    } else {
                        else_body.add(search);
                    }
                }
                OpcodeBlock holder = new BodyOpcodeBlock(0);
                holder.getOpcodes().addAll(else_body);
                // remove any break points that were placed inside the elvis
                for (int l = start; l < o; l++) {
                    break_points.remove(l);
                }
                // add break points at the start and end of the elvis
                //
                // the one just before will create a body block that ends with
                // the checked value being left on the stack
                break_points.add(o - 1);
                break_points.add(start - 1);
                // TODO we need to parse any ternaries that might be contained
                // in the else body of the elvis statement.
                ElvisBlockSection elvis = new ElvisBlockSection(holder);
                // store the elvis to be created properly later
                this.sections.put(Integer.valueOf(o - 1), elvis);
            }
            last = next;
        }
    }

    @Override
    public void formEdges(PartialMethod partial, Map<Integer, OpcodeBlock> blocks, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            ElvisBlockSection elvis = this.sections.get(e.getKey());
            // Now we loop through the break points and find any that we
            // compiled an elvis statement for to create the block section.
            if (elvis != null) {
                OpcodeBlock block = e.getValue();
                ProcessedOpcodeBlock replacement = new ProcessedOpcodeBlock(block.getBreakpoint(), elvis);
                replacement.setTarget(block.getTarget());
                block_list.set(block_list.indexOf(block), replacement);
                // omit the next block from the ternary check as it will look
                // like a ternary since its missing the value which is the
                // result of the elvis statement
                block_list.get(block_list.indexOf(block) + 1).omitFromTernaryCheck(true);
                GraphOperation.remap(block_list, block, replacement);
            }
        }
        this.sections.clear();
    }

}

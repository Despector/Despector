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
package org.spongepowered.despector.decompiler.kotlin.method.graph.create;

import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.POP;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
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
        List<AbstractInsnNode> ops = partial.getOpcodes();
        if (ops.isEmpty()) {
            return;
        }
        int last = ops.get(0).getOpcode();
        for (int i = 1; i < ops.size(); i++) {
            int next = ops.get(i).getOpcode();
            if (last == DUP && ((next >= IFEQ && next <= IF_ACMPNE) || next == IFNULL || next == IFNONNULL)) {
                // Hello elvis
                int o = i;
                int start = i - 1;
                AbstractInsnNode search = ops.get(o++);
                Label target = ((JumpInsnNode) search).label.getLabel();
                JumpInsnNode ggoto = null;
                while (true) {
                    search = ops.get(o++);
                    if (search instanceof LabelNode) {
                        if (((LabelNode) search).getLabel() == target) {
                            break;
                        }
                    } else if (search.getOpcode() == GOTO) {
                        ggoto = (JumpInsnNode) search;
                    }
                }
                checkState(ggoto != null);
                target = ggoto.label.getLabel();
                List<AbstractInsnNode> else_body = new ArrayList<>();
                boolean first_pop = false;
                while (true) {
                    search = ops.get(o++);
                    if (search instanceof LabelNode) {
                        if (((LabelNode) search).getLabel() == target) {
                            break;
                        }
                    }
                    if (search.getOpcode() == POP && !first_pop) {
                        first_pop = true;
                    } else {
                        else_body.add(search);
                    }
                }
                OpcodeBlock holder = new BodyOpcodeBlock(0);
                holder.getOpcodes().addAll(else_body);
                for (int l = start; l < o; l++) {
                    break_points.remove(l);
                }
                break_points.add(o - 1);
                break_points.add(start - 1);
                ElvisBlockSection elvis = new ElvisBlockSection(holder);
                this.sections.put(Integer.valueOf(o - 1), elvis);
            }
            last = next;
        }
    }

    @Override
    public void formEdges(PartialMethod partial, Map<Integer, OpcodeBlock> blocks, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            ElvisBlockSection elvis = this.sections.get(e.getKey());
            if (elvis != null) {
                OpcodeBlock block = e.getValue();
                ProcessedOpcodeBlock replacement = new ProcessedOpcodeBlock(block.getBreakpoint(), elvis);
                replacement.setTarget(block.getTarget());
                block_list.set(block_list.indexOf(block), replacement);
                block_list.get(block_list.indexOf(block) + 1).omitFromTernaryCheck(true);
                GraphOperation.remap(block_list, block, replacement);
            }
        }
        this.sections.clear();
    }

}

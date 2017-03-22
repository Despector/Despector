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
package org.spongepowered.despector.decompiler.method;

import static org.mockito.asm.Opcodes.GOTO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.config.Constants;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.operate.TernaryPrePassOperation;
import org.spongepowered.despector.decompiler.method.postprocess.StatementPostProcessor;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A decompiler for method bodies.
 */
public class MethodDecompiler {

    private final List<GraphProducerStep> graph_producers = new ArrayList<>();
    private final List<GraphOperation> cleanup_operations = new ArrayList<>();
    private final List<GraphProcessor> processors = new ArrayList<>();
    private final List<RegionProcessor> region_processors = new ArrayList<>();
    private final List<StatementPostProcessor> post_processors = new ArrayList<>();

    public void addGraphProducer(GraphProducerStep step) {
        this.graph_producers.add(step);
    }

    public void addCleanupOperation(GraphOperation op) {
        this.cleanup_operations.add(op);
    }

    public void addProcessor(GraphProcessor proc) {
        this.processors.add(proc);
    }

    public void addRegionProcessor(RegionProcessor proc) {
        this.region_processors.add(proc);
    }

    public void addPostProcessor(StatementPostProcessor post) {
        this.post_processors.add(post);
    }

    @SuppressWarnings("unchecked")
    public StatementBlock decompile(MethodEntry entry, MethodNode asm) {
        if (asm.instructions.size() == 0) {
            return null;
        }

        PartialMethod partial = new PartialMethod(this, asm, entry);

        Locals locals = new Locals();
        for (LocalVariableNode node : (List<LocalVariableNode>) partial.getAsmNode().localVariables) {
            Local local = locals.getLocal(node.index);
            local.addLVT(node);
        }
        int offs = ((partial.getAsmNode().access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        List<String> param_types = TypeHelper.splitSig(partial.getAsmNode().desc);

        for (int i = 0; i < param_types.size() + offs; i++) {
            Local local = locals.getLocal(i);
            local.setAsParameter();
            if (local.getLVT().isEmpty()) {
                if (i < offs) {
                    local.setParameterInstance(new LocalInstance(local, null, "this", partial.getEntry().getOwner(), -1, -1));
                } else {
                    local.setParameterInstance(new LocalInstance(local, null, "param" + i, param_types.get(i - offs), -1, -1));
                }
            } else {
                LocalVariableNode lvt = local.getLVT().get(0);
                LocalInstance insn = new LocalInstance(local, lvt, lvt.name, lvt.desc, -1, -1);
                local.setParameterInstance(insn);
                if (lvt.signature != null) {
                    insn.setGenericTypes(SignatureParser.parseFieldTypeSignature(lvt.signature));
                }
            }
        }

        partial.setLocals(locals);

        List<AbstractInsnNode> ops = Lists.newArrayList(asm.instructions.iterator());
        partial.setOpcodes(ops);
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD, partial.getLocals());
        partial.setBlock(block);

        Map<Label, Integer> label_indices = Maps.newHashMap();
        for (int index = 0; index < ops.size(); index++) {
            AbstractInsnNode next = ops.get(index);
            if (next instanceof LabelNode) {
                label_indices.put(((LabelNode) next).getLabel(), index);
            }
        }
        partial.getLocals().bakeInstances(label_indices);
        partial.setLabelIndices(label_indices);

        List<OpcodeBlock> graph = makeGraph(partial);
        partial.setGraph(graph);

        for (GraphOperation op : this.cleanup_operations) {
            // TODO separate cleanup and pre-pass operations
            if (op instanceof TernaryPrePassOperation) {
                if (Constants.TRACE_ACTIVE) {
                    for (OpcodeBlock op2 : graph) {
                        op2.print();
                    }
                }
            }
            op.process(partial);
        }

        // Performs a sequence of transformations to convert the graph into a
        // simple array of partially decompiled block sections.
        List<BlockSection> flat_graph = flattenGraph(partial, graph, graph.size());

        // Append all block sections to the output in order. This finalizes all
        // decompilation of statements not already decompiled.
        Deque<Instruction> stack = new ArrayDeque<>();
        for (BlockSection op : flat_graph) {
            op.appendTo(block, stack);
        }

        for (StatementPostProcessor post : this.post_processors) {
            post.postprocess(block);
        }

        return block;
    }

    private List<OpcodeBlock> makeGraph(PartialMethod partial) {
        List<AbstractInsnNode> instructions = partial.getOpcodes();

        Set<Integer> break_points = new HashSet<>();

        for (GraphProducerStep step : this.graph_producers) {
            step.collectBreakpoints(partial, break_points);
        }

        // Sort the break points
        List<Integer> sorted_break_points = new ArrayList<>(break_points);
        sorted_break_points.sort(Comparator.naturalOrder());
        Map<Integer, OpcodeBlock> blocks = new HashMap<>();
        List<OpcodeBlock> block_list = new ArrayList<>();

        int last_brk = 0;
        for (int brk : sorted_break_points) {
            // accumulate the opcodes beween the next breakpoint and the last
            // breakpoint.
            OpcodeBlock block = new BodyOpcodeBlock(brk);
            block_list.add(block);
            for (int i = last_brk; i <= brk; i++) {
                block.getOpcodes().add(instructions.get(i));
            }
            blocks.put(brk, block);
            last_brk = brk + 1;
        }

        for (int i = 0; i < block_list.size() - 1; i++) {
            OpcodeBlock next = block_list.get(i);
            if (next.getLast() instanceof LabelNode) {
                next.setTarget(block_list.get(i + 1));
            }
        }

        for (GraphProducerStep step : this.graph_producers) {
            step.formEdges(partial, blocks, sorted_break_points, block_list);
        }

        return block_list;
    }

    public List<BlockSection> flattenGraph(PartialMethod partial, List<OpcodeBlock> blocks, int stop_point) {
        int stop_offs = blocks.size() - stop_point;
        List<BlockSection> final_blocks = new ArrayList<>();

        outer: for (int i = 0; i < blocks.size() - stop_offs; i++) {
            OpcodeBlock region_start = blocks.get(i);
            for (GraphProcessor processor : this.processors) {
                int next = processor.process(partial, blocks, region_start, final_blocks);
                if (next != -1) {
                    i = next;
                    continue outer;
                }
            }
        }
        return final_blocks;
    }

    public BlockSection processRegion(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start) {
        for (RegionProcessor proc : this.region_processors) {
            BlockSection block = proc.process(partial, region, ret, body_start);
            if (block != null) {
                return block;
            }
        }
        return null;
    }

}

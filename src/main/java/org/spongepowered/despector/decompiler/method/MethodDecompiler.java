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
package org.spongepowered.despector.decompiler.method;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.SwitchOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;
import org.spongepowered.despector.decompiler.method.postprocess.StatementPostProcessor;
import org.spongepowered.despector.decompiler.method.special.SpecialMethodProcessor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A decompiler for method bodies.
 */
public class MethodDecompiler {

    public static final String targeted_breakpoint = "";

    private final List<GraphProducerStep> graph_producers = new ArrayList<>();
    private final List<GraphOperation> cleanup_operations = new ArrayList<>();
    private final List<GraphProcessor> processors = new ArrayList<>();
    private final List<RegionProcessor> region_processors = new ArrayList<>();
    private final List<StatementPostProcessor> post_processors = new ArrayList<>();
    private final Map<Class<?>, SpecialMethodProcessor> special_processors = new HashMap<>();

    /**
     * Adds the given {@link GraphProducerStep} to the end of the graph
     * producers.
     */
    public void addGraphProducer(GraphProducerStep step) {
        this.graph_producers.add(checkNotNull(step, "step"));
    }

    /**
     * Adds the given {@link GraphOperation} to the end of the cleanup steps.
     */
    public void addCleanupOperation(GraphOperation op) {
        this.cleanup_operations.add(checkNotNull(op, "op"));
    }

    /**
     * Adds the given {@link GraphProcessor} to the end of the graph processors.
     */
    public void addProcessor(GraphProcessor proc) {
        this.processors.add(checkNotNull(proc, "proc"));
    }

    /**
     * Adds the given {@link RegionProcessor} to the end of the region
     * processors.
     */
    public void addRegionProcessor(RegionProcessor proc) {
        this.region_processors.add(checkNotNull(proc, "proc"));
    }

    /**
     * Adds the given {@link StatementPostProcessor} to the end of the post
     * processors.
     */
    public void addPostProcessor(StatementPostProcessor post) {
        this.post_processors.add(checkNotNull(post, "post"));
    }

    /**
     * Adds the given {@link SpecialMethodProcessor} to the special processors.
     */
    public <T extends SpecialMethodProcessor> void setSpecialProcessor(Class<T> type, T processor) {
        this.special_processors.put(checkNotNull(type, "type"), checkNotNull(processor, "processor"));
    }

    /**
     * Gets the registered {@link SpecialMethodProcessor} corresponding to the
     * given type, or null if not found.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends SpecialMethodProcessor> T getSpecialProcessor(Class<T> type) {
        return (T) this.special_processors.get(checkNotNull(type, "type"));
    }

    /**
     * Decompiles the given asm method to a statement block.
     */
    public StatementBlock decompile(MethodEntry entry) {
        if (entry.getIR() == null || entry.getIR().size() == 0) {
            return null;
        }

        // Setup the partial method
        PartialMethod partial = new PartialMethod(this, entry);

        // Convert the instructions linked list to an array list for easier
        // processing
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD);
        partial.setBlock(block);

        // Creates the initial form of the control flow graph
        List<OpcodeBlock> graph = makeGraph(partial);
        partial.setGraph(graph);

        for (int i = 0; i < graph.size() - 1; i++) {
            OpcodeBlock b = graph.get(i);
            if (b.getTarget() == null && !(b instanceof SwitchOpcodeBlock) && !(b instanceof TryCatchMarkerOpcodeBlock)) {
                System.err.println("Block with null target: " + b.getStart());
                for (OpcodeBlock bl : graph) {
                    System.out.print(bl.toString());
                }
                throw new IllegalStateException();
            }
        }

        if (partial.getEntry().getName().equals(targeted_breakpoint)) {
            System.out.println();
        }

        // process the graph to perform in-graph operations prior to flattening
        // it to a list of block sections
        for (GraphOperation op : this.cleanup_operations) {
            op.process(partial);
        }

        if (partial.getEntry().getName().equals(targeted_breakpoint)) {
            for (OpcodeBlock g : graph) {
                System.out.println(g.toString());
            }
            System.out.println();
        }

        // Performs a sequence of transformations to convert the graph into a
        // simple array of partially decompiled block sections.
        List<BlockSection> flat_graph = new ArrayList<>();

        flattenGraph(partial, graph, graph.size(), flat_graph);

        // Append all block sections to the output in order. This finalizes all
        // decompilation of statements not already decompiled.
        Deque<Instruction> stack = new ArrayDeque<>();
        // some compilers create what resembles a kotlin elvis statement in the
        // switch synthetic methods and it breaks our pure java decompiler
        //
        // TODO We should add a processor that can handle this even in java to
        // produce almost correct code rather than erroring
        int start = 0;
        if (entry.getName().startsWith("$SWITCH_TABLE$")) {
            start = 2;
            stack.push(new LocalAccess(entry.getLocals().getLocal(0).getInstance(0)));
        }
        for (int i = start; i < flat_graph.size(); i++) {
            BlockSection op = flat_graph.get(i);
            op.appendTo(block, entry.getLocals(), stack);
        }

        for (StatementPostProcessor post : this.post_processors) {
            try {
                post.postprocess(block);
            } catch (Exception e) {
                System.err.println("Failed to apply post processor: " + post.getClass().getSimpleName());
                e.printStackTrace();
            }
        }

        return block;
    }

    private List<OpcodeBlock> makeGraph(PartialMethod partial) {
        InsnBlock instructions = partial.getOpcodes();

        Set<Integer> break_points = new HashSet<>();

        // queries all graph producers to determine where the instructions
        // should be broken up to form the graph
        for (GraphProducerStep step : this.graph_producers) {
            step.collectBreakpoints(partial, break_points);
        }

        // Sort the break points
        List<Integer> sorted_break_points = new ArrayList<>(break_points);
        sorted_break_points.sort(Comparator.naturalOrder());
        List<OpcodeBlock> block_list = new ArrayList<>();

        // turn all blocks to the basic body opcode block, the various
        // processors will then replace these with the specialized opcode blocks
        int last_brk = 0;
        for (int brk : sorted_break_points) {
            // accumulate the opcodes beween the next breakpoint and the last
            // breakpoint.
            OpcodeBlock block = new BodyOpcodeBlock(last_brk, brk);
            block_list.add(block);
            for (int i = last_brk; i <= brk; i++) {
                block.getOpcodes().add(instructions.get(i));
            }
            last_brk = brk + 1;
        }

        if (last_brk < instructions.size()) {
            OpcodeBlock block = new BodyOpcodeBlock(last_brk, instructions.size() - 1);
            block_list.add(block);
            for (int i = last_brk; i < instructions.size(); i++) {
                block.getOpcodes().add(instructions.get(i));
            }
        }

        for (int i = 0; i < block_list.size() - 1; i++) {
            OpcodeBlock next = block_list.get(i);
            if (!(next.getLast() instanceof JumpInsn) && !(next.getLast() instanceof SwitchInsn)) {
                next.setTarget(block_list.get(i + 1));
            }
        }

        // form the edges of the graph
        for (GraphProducerStep step : this.graph_producers) {
            step.formEdges(partial, sorted_break_points, block_list);
        }

        return block_list;
    }

    /**
     * Processes the given region into one or more block sections.
     */
    public void flattenGraph(PartialMethod partial, List<OpcodeBlock> blocks, int stop_point, List<BlockSection> result) {
        int stop_offs = blocks.size() - stop_point;
        if (stop_offs < 0) {
            return;
        }
        outer: for (int i = 0; i < blocks.size() - stop_offs; i++) {
            OpcodeBlock region_start = blocks.get(i);
            for (GraphProcessor processor : this.processors) {
                int next = processor.process(partial, blocks, region_start, result);
                if (next != -1) {
                    i = next;
                    continue outer;
                }
            }
        }
    }

    /**
     * Processes the given region with the registered region processors.
     */
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

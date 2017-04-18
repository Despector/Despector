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
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
import org.spongepowered.despector.decompiler.loader.AsmTranslator;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.postprocess.StatementPostProcessor;
import org.spongepowered.despector.decompiler.method.special.SpecialMethodProcessor;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A decompiler for method bodies.
 */
public class MethodDecompiler {

    public static final String targeted_breakpoint = "canPush";

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
     * Creates the locals for the given method.
     */
    @SuppressWarnings("unchecked")
    public Locals createLocals(MethodEntry entry, MethodNode asm) {
        Locals locals = new Locals();
        if (asm.localVariables != null) {
            for (LocalVariableNode node : (List<LocalVariableNode>) asm.localVariables) {
                Local local = locals.getLocal(node.index);
                local.addLVT(node);
            }
        }
        int offs = ((asm.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        List<String> param_types = TypeHelper.splitSig(asm.desc);

        for (int i = 0; i < param_types.size() + offs; i++) {
            Local local = locals.getLocal(i);
            local.setAsParameter();
            if (local.getLVT().isEmpty()) {
                if (i < offs) {
                    local.setParameterInstance(new LocalInstance(local, "this", ClassTypeSignature.of(entry.getOwner()), -1, -1));
                } else {
                    local.setParameterInstance(new LocalInstance(local, "param" + i, ClassTypeSignature.of(param_types.get(i - offs)), -1, -1));
                }
            } else {
                LocalVariableNode lvt = local.getLVT().get(0);
                TypeSignature sig = null;
                if (lvt.signature != null) {
                    sig = SignatureParser.parseFieldTypeSignature(lvt.signature);
                } else {
                    sig = ClassTypeSignature.of(lvt.desc);
                }
                LocalInstance insn = new LocalInstance(local, lvt.name, sig, -1, -1);
                local.setParameterInstance(insn);
            }
        }

//        LocalsProcessor proc = getSpecialProcessor(LocalsProcessor.class);
//        if (proc != null) {
//            proc.process(asm, locals);
//        }

        Iterator<AbstractInsnNode> it = asm.instructions.iterator();
        if (it.hasNext()) {
            AbstractInsnNode last = it.next();
            int i = 1;
            while (it.hasNext()) {
                AbstractInsnNode next = it.next();
                if (next.getOpcode() >= ISTORE && next.getOpcode() <= ASTORE && last.getOpcode() >= ILOAD && last.getOpcode() <= ALOAD) {
                    Local store_local = locals.getLocal(((VarInsnNode) next).var);
                    LocalInstance store = store_local.getInstance(i);
                    LocalInstance load = locals.getLocal(((VarInsnNode) last).var).getInstance(i);
                    if (store == null && load != null) {
                        LocalInstance new_insn = new LocalInstance(store_local, load.getName(),
                                load.getType(), load.getStart(), load.getEnd());
                        store_local.addInstance(new_insn);
                    }
                }
                i++;
            }

        }

        return locals;
    }

    /**
     * Decompiles the given asm method to a statement block.
     */
    public StatementBlock decompile(MethodEntry entry, MethodNode asm, Locals locals) {
        if (asm.instructions.size() == 0) {
            return null;
        }

        // Setup the partial method
        PartialMethod partial = new PartialMethod(this, entry);
        partial.setLocals(locals);

        // Convert the instructions linked list to an array list for easier
        // processing
        InsnBlock ops = AsmTranslator.convert(partial, asm);
        partial.setOpcodes(ops);
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD, partial.getLocals());
        partial.setBlock(block);

        // Creates the initial form of the control flow graph
        List<OpcodeBlock> graph = makeGraph(partial);
        partial.setGraph(graph);

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
        int start = entry.getName().startsWith("$SWITCH_TABLE$") ? 2 : 0;
        for (int i = start; i < flat_graph.size(); i++) {
            BlockSection op = flat_graph.get(i);
            op.appendTo(block, stack);
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
        Map<Integer, OpcodeBlock> blocks = new HashMap<>();
        List<OpcodeBlock> block_list = new ArrayList<>();

        // turn all blocks to the basic body opcode block, the various
        // processors will then replace these with the specialized opcode blocks
        int last_brk = 0;
        for (int brk : sorted_break_points) {
            // accumulate the opcodes beween the next breakpoint and the last
            // breakpoint.
            OpcodeBlock block = new BodyOpcodeBlock(last_brk);
            block_list.add(block);
            for (int i = last_brk; i <= brk; i++) {
                block.getOpcodes().add(instructions.get(i));
            }
            blocks.put(last_brk, block);
            last_brk = brk + 1;
        }

        for (int i = 0; i < block_list.size() - 1; i++) {
            OpcodeBlock next = block_list.get(i);
            if (!(next.getLast() instanceof JumpInsn) && !(next.getLast() instanceof SwitchInsn)) {
                next.setTarget(block_list.get(i + 1));
            }
        }

        // form the edges of the graph
        for (GraphProducerStep step : this.graph_producers) {
            step.formEdges(partial, blocks, sorted_break_points, block_list);
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

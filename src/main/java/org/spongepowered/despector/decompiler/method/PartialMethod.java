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

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A structure of intermediate data of a method which is in the process of being
 * decompiled.
 */
public class PartialMethod {

    private final MethodDecompiler decompiler;
    private final MethodNode node;
    private final MethodEntry method;

    private Locals locals;
    private List<AbstractInsnNode> ops;
    private StatementBlock block;
    private Map<Label, Integer> label_indices;
    private List<OpcodeBlock> graph;
    private List<BlockSection> final_blocks = new ArrayList<>();

    public PartialMethod(MethodDecompiler decompiler, MethodNode node, MethodEntry method) {
        this.decompiler = decompiler;
        this.node = node;
        this.method = method;
    }

    /**
     * Gets the associated method decompiler.
     */
    public MethodDecompiler getDecompiler() {
        return this.decompiler;
    }

    /**
     * Gets the asm node of the method being decompiled.
     */
    public MethodNode getAsmNode() {
        return this.node;
    }

    /**
     * Gets the {@link MethodEntry} which is being populated.
     */
    public MethodEntry getEntry() {
        return this.method;
    }

    /**
     * Gets the method locals.
     */
    public Locals getLocals() {
        return this.locals;
    }

    /**
     * Sets the method locals.
     */
    public void setLocals(Locals locals) {
        this.locals = locals;
    }

    /**
     * Gets the method's opcodes.
     */
    public List<AbstractInsnNode> getOpcodes() {
        return this.ops;
    }

    /**
     * Sets the method's opcodes.
     */
    public void setOpcodes(List<AbstractInsnNode> ops) {
        this.ops = ops;
    }

    /**
     * Gets the {@link StatementBlock} that will be filled with the final
     * statements.
     */
    public StatementBlock getBlock() {
        return this.block;
    }

    /**
     * Sets the {@link StatementBlock}.
     */
    public void setBlock(StatementBlock block) {
        this.block = block;
    }

    /**
     * Gets the indices of labels within the list of opcodes.
     */
    public Map<Label, Integer> getLabelIndices() {
        return this.label_indices;
    }

    /**
     * Sets the indices of labels within the list of opcodes.
     */
    public void setLabelIndices(Map<Label, Integer> label_indices) {
        this.label_indices = label_indices;
    }

    /**
     * Gets the opcode blocks forming the graph of the method's control flow.
     */
    public List<OpcodeBlock> getGraph() {
        return this.graph;
    }

    /**
     * Sets the opcode blocks forming the graph of the method's control flow.
     */
    public void setGraph(List<OpcodeBlock> graph) {
        this.graph = graph;
    }

    /**
     * Gets the final block sections.
     */
    public List<BlockSection> getFinalBlocks() {
        return this.final_blocks;
    }

}

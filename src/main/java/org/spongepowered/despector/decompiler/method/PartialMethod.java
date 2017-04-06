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

    public MethodDecompiler getDecompiler() {
        return this.decompiler;
    }

    public MethodNode getAsmNode() {
        return this.node;
    }

    public MethodEntry getEntry() {
        return this.method;
    }

    public Locals getLocals() {
        return this.locals;
    }

    public void setLocals(Locals locals) {
        this.locals = locals;
    }

    public List<AbstractInsnNode> getOpcodes() {
        return this.ops;
    }

    public void setOpcodes(List<AbstractInsnNode> ops) {
        this.ops = ops;
    }

    public StatementBlock getBlock() {
        return this.block;
    }

    public void setBlock(StatementBlock block) {
        this.block = block;
    }

    public Map<Label, Integer> getLabelIndices() {
        return this.label_indices;
    }

    public void setLabelIndices(Map<Label, Integer> label_indices) {
        this.label_indices = label_indices;
    }

    public List<OpcodeBlock> getGraph() {
        return this.graph;
    }

    public void setGraph(List<OpcodeBlock> graph) {
        this.graph = graph;
    }

    public List<BlockSection> getFinalBlocks() {
        return this.final_blocks;
    }

}

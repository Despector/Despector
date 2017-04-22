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

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A structure of intermediate data of a method which is in the process of being
 * decompiled.
 */
public class PartialMethod {

    private final MethodDecompiler decompiler;
    private final MethodEntry method;

    private StatementBlock block;
    private List<OpcodeBlock> graph;
    private List<BlockSection> final_blocks = new ArrayList<>();

    public PartialMethod(MethodDecompiler decompiler, MethodEntry method) {
        this.decompiler = decompiler;
        this.method = method;
    }

    /**
     * Gets the associated method decompiler.
     */
    public MethodDecompiler getDecompiler() {
        return this.decompiler;
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
        return this.method.getLocals();
    }

    /**
     * Gets the method's opcodes.
     */
    public InsnBlock getOpcodes() {
        return this.method.getIR();
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

    public static class TryCatchRegion {

        private int start_pc;
        private int end_pc;
        private int catch_pc;
        private String ex;

        public TryCatchRegion(int start_pc, int end_pc, int catch_pc, String ex) {
            this.start_pc = start_pc;
            this.end_pc = end_pc;
            this.catch_pc = catch_pc;
            this.ex = ex;
        }

        public int getStart() {
            return this.start_pc;
        }

        public int getEnd() {
            return this.end_pc;
        }

        public int getCatch() {
            return this.catch_pc;
        }

        public String getException() {
            return this.ex;
        }

    }

}

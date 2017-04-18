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
package org.spongepowered.despector.decompiler.method.graph.data.block;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.branch.TryCatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A block section containing a processed try-catch block.
 */
public class TryCatchBlockSection extends BlockSection {

    private final List<BlockSection> body = new ArrayList<>();
    private final List<CatchBlockSection> catches = new ArrayList<>();

    public TryCatchBlockSection() {
    }

    /**
     * Gets the body of this try statement.
     */
    public List<BlockSection> getBody() {
        return this.body;
    }

    /**
     * Gets the catch blocks of this try statement.
     */
    public List<CatchBlockSection> getCatchBlocks() {
        return this.catches;
    }

    @Override
    public void appendTo(StatementBlock block, Deque<Instruction> stack) {
        StatementBlock body = new StatementBlock(StatementBlock.Type.TRY, block.getLocals());
        Deque<Instruction> body_stack = new ArrayDeque<>();
        for (BlockSection body_section : this.body) {
            body_section.appendTo(body, body_stack);
        }
        checkState(body_stack.isEmpty());
        TryCatch ttry = new TryCatch(body);
        block.append(ttry);
        for (CatchBlockSection c : this.catches) {
            StatementBlock cbody = new StatementBlock(StatementBlock.Type.WHILE, block.getLocals());
            Deque<Instruction> cbody_stack = new ArrayDeque<>();
            for (BlockSection body_section : c.getBody()) {
                body_section.appendTo(cbody, cbody_stack);
            }
            checkState(cbody_stack.isEmpty());
            if (c.getLocal() != null) {
                ttry.new CatchBlock(c.getLocal(), c.getExceptions(), cbody);
            } else {
                ttry.new CatchBlock("e", c.getExceptions(), cbody);
            }
        }
    }

    /**
     * A processed catch block.
     */
    public static class CatchBlockSection {

        private Locals.LocalInstance exlocal;
        private final List<String> exception = new ArrayList<>();
        private final List<BlockSection> body = new ArrayList<>();

        public CatchBlockSection(List<String> ex, Locals.LocalInstance local) {
            this.exception.addAll(ex);
            this.exlocal = local;
        }

        /**
         * Gets the local instance of the local holding the exception instance.
         */
        public Locals.LocalInstance getLocal() {
            return this.exlocal;
        }

        /**
         * Gets all exceptions for this catch block.
         */
        public List<String> getExceptions() {
            return this.exception;
        }

        /**
         * Gets the body of this catch block.
         */
        public List<BlockSection> getBody() {
            return this.body;
        }
    }
}

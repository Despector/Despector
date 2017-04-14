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
package com.voxelgenesis.despector.core.decompiler.graph;

import com.voxelgenesis.despector.core.ast.method.Locals;
import com.voxelgenesis.despector.core.ast.method.StatementBlock;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.decompiler.InsnAppender;
import com.voxelgenesis.despector.core.ir.Insn;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BodyNode extends GraphNode {

    private final List<Insn> instructions = new ArrayList<>();

    public BodyNode(List<Insn> insn, int start, int end) {
        for (int i = start; i < end; i++) {
            this.instructions.add(insn.get(i));
        }
    }

    public List<Insn> getInstructions() {
        return this.instructions;
    }

    @Override
    public void append(StatementBlock block, Locals locals, Deque<Instruction> stack) {
        for (Insn insn : this.instructions) {
            InsnAppender.append(insn, block, locals, stack);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("BodyNode:\n");
        for (Insn insn : this.instructions) {
            str.append(insn.toString()).append("\n");
        }

        return str.toString();
    }

}
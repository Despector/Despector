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
package com.voxelgenesis.despector.core.decompiler;

import com.voxelgenesis.despector.core.ast.SourceSet;
import com.voxelgenesis.despector.core.ast.method.StatementBlock;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.type.MethodEntry;
import com.voxelgenesis.despector.core.ast.type.SourceEntry;
import com.voxelgenesis.despector.core.decompiler.graph.BodyNode;
import com.voxelgenesis.despector.core.decompiler.graph.GraphNode;
import com.voxelgenesis.despector.core.ir.Insn;
import com.voxelgenesis.despector.core.ir.InsnBlock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseDecompiler {

    public void decompile(SourceEntry entry, SourceSet set) {

        for (MethodEntry method : entry.getMethods()) {
            decompile(method, entry, set);
        }

    }

    public void decompile(MethodEntry method, SourceEntry entry, SourceSet set) {
        if (method.getStatements() != null) {
            return;
        }
        if (method.getIR() == null) {
            throw new IllegalStateException("No body for method " + entry.getName() + " " + method.getName() + method.getSignature());
        }

        List<GraphNode> graph = makeGraph(method.getIR());

        // TODO region processing
        StatementBlock block = new StatementBlock();
        Deque<Instruction> stack = new ArrayDeque<>();
        for (GraphNode node : graph) {
            node.append(block, method.getLocals(), stack);
        }
        method.setStatements(block);
        method.setIR(null);
    }

    private List<GraphNode> makeGraph(InsnBlock block) {
        List<GraphNode> graph = new ArrayList<>();

        Set<Integer> break_points = new HashSet<>();

        for (Insn insn : block) {
            // TODO add break points for jumps, etc.
        }

        List<Integer> sorted = new ArrayList<>(break_points);
        Collections.sort(sorted);

        int last = 0;
        for (int br : sorted) {
            graph.add(new BodyNode(block.getInstructions(), last, br));
            last = br;
        }
        if (last < block.size()) {
            graph.add(new BodyNode(block.getInstructions(), last, block.size()));
        }
        return graph;
    }

}

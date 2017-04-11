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
package com.voxelgenesis.despector.core.ir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InsnBlock implements Iterable<Insn> {

    private final List<Insn> instructions;

    public InsnBlock() {
        this.instructions = new ArrayList<>();
    }

    public int size() {
        return this.instructions.size();
    }

    public Insn get(int i) {
        return this.instructions.get(i);
    }

    public void append(Insn insn) {
        this.instructions.add(insn);
    }

    public List<Insn> getInstructions() {
        return this.instructions;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Insn insn : this.instructions) {
            str.append(" ").append(insn).append("\n");
        }
        return str.toString();
    }

    @Override
    public Iterator<Insn> iterator() {
        return new Itr();
    }

    public class Itr implements Iterator<Insn> {

        private int index;

        @Override
        public boolean hasNext() {
            return InsnBlock.this.getInstructions().size() > this.index;
        }

        @Override
        public Insn next() {
            return InsnBlock.this.getInstructions().get(this.index++);
        }

    }

}

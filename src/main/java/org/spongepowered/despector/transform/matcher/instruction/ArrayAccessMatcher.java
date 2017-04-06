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
package org.spongepowered.despector.transform.matcher.instruction;

import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayAccess;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

public class ArrayAccessMatcher implements InstructionMatcher<ArrayAccess> {

    private InstructionMatcher<?> array;
    private InstructionMatcher<?> index;

    ArrayAccessMatcher(InstructionMatcher<?> array, InstructionMatcher<?> index) {
        this.array = array == null ? InstructionMatcher.ANY : array;
        this.index = index == null ? InstructionMatcher.ANY : index;
    }

    @Override
    public ArrayAccess match(MatchContext ctx, Instruction insn) {
        if (!(insn instanceof ArrayAccess)) {
            return null;
        }
        ArrayAccess invoke = (ArrayAccess) insn;
        if (!this.array.matches(ctx, invoke.getArrayVar())) {
            return null;
        }
        if (!this.index.matches(ctx, invoke.getIndex())) {
            return null;
        }
        return invoke;
    }

    public static class Builder {

        private InstructionMatcher<?> array;
        private InstructionMatcher<?> index;

        public Builder() {
            reset();
        }

        public Builder array(InstructionMatcher<?> val) {
            this.array = val;
            return this;
        }

        public Builder index(InstructionMatcher<?> index) {
            this.index = index;
            return this;
        }

        public Builder reset() {
            this.array = null;
            this.index = null;
            return this;
        }

        public ArrayAccessMatcher build() {
            return new ArrayAccessMatcher(this.array, this.index);
        }

    }

}

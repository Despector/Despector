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
package org.spongepowered.despector.transform.matcher.condition;

import org.spongepowered.despector.ast.insn.condition.CompareCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition.CompareOperator;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

import javax.annotation.Nullable;

/**
 * A matcher for compare conditions.
 */
public class CompareConditionMatcher implements ConditionMatcher<CompareCondition> {

    private InstructionMatcher<?> left;
    private InstructionMatcher<?> right;
    private CompareOperator op;

    CompareConditionMatcher(InstructionMatcher<?> left, InstructionMatcher<?> right, CompareOperator op) {
        this.left = left == null ? InstructionMatcher.ANY : left;
        this.right = right == null ? InstructionMatcher.ANY : right;
        this.op = op;
    }

    @Override
    public CompareCondition match(MatchContext ctx, Condition cond) {
        if (!(cond instanceof CompareCondition)) {
            return null;
        }
        CompareCondition compare = (CompareCondition) cond;
        if (!this.left.matches(ctx, compare.getLeft())) {
            return null;
        }
        if (!this.right.matches(ctx, compare.getRight())) {
            return null;
        }
        if (this.op != null && !this.op.equals(compare.getOperator())) {
            return null;
        }
        return compare;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private InstructionMatcher<?> left;
        private InstructionMatcher<?> right;
        private CompareOperator op;

        public Builder() {
            reset();
        }

        /**
         * Sets the matcher for the left value.
         */
        public Builder left(InstructionMatcher<?> val) {
            this.left = val;
            return this;
        }

        /**
         * Sets the matcher for the right value.
         */
        public Builder right(InstructionMatcher<?> val) {
            this.right = val;
            return this;
        }

        /**
         * Sets the operator.
         */
        public Builder operator(@Nullable CompareOperator op) {
            this.op = op;
            return this;
        }

        /**
         * Resets the builder to default values.
         */
        public Builder reset() {
            this.left = null;
            this.right = null;
            this.op = null;
            return this;
        }

        /**
         * Creates a new matcher.
         */
        public CompareConditionMatcher build() {
            return new CompareConditionMatcher(this.left, this.right, this.op);
        }

    }

}

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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.condition.OrCondition;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A matcher for or conditions.
 */
public class OrConditionMatcher implements ConditionMatcher<OrCondition> {

    private List<ConditionMatcher<?>> operands;

    OrConditionMatcher(List<ConditionMatcher<?>> operands) {
        this.operands = checkNotNull(operands, "operands");
    }

    @Override
    public OrCondition match(MatchContext ctx, Condition cond) {
        if (!(cond instanceof OrCondition)) {
            return null;
        }
        OrCondition and = (OrCondition) cond;
        if (and.getOperands().size() != this.operands.size()) {
            return null;
        }
        for (int i = 0; i < this.operands.size(); i++) {
            ConditionMatcher<?> op_match = this.operands.get(i);
            if (!op_match.matches(ctx, and.getOperands().get(i))) {
                return null;
            }
        }
        return and;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private List<ConditionMatcher<?>> operands = new ArrayList<>();

        public Builder() {
            reset();
        }

        /**
         * Adds the given matcher to the operand matchers.
         */
        public Builder operand(ConditionMatcher<?> val) {
            this.operands.add(val);
            return this;
        }

        /**
         * Resets the builder to default values.
         */
        public Builder reset() {
            this.operands.clear();
            return this;
        }

        /**
         * Creates a new matcher.
         */
        public OrConditionMatcher build() {
            return new OrConditionMatcher(this.operands);
        }

    }

}

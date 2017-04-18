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

import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.condition.InverseCondition;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

/**
 * A matcher for boolean conditions.
 */
public class InverseConditionMatcher implements ConditionMatcher<InverseCondition> {

    private ConditionMatcher<?> value;

    InverseConditionMatcher(ConditionMatcher<?> val) {
        this.value = val == null ? ConditionMatcher.ANY : val;
    }

    @Override
    public InverseCondition match(MatchContext ctx, Condition cond) {
        if (!(cond instanceof InverseCondition)) {
            return null;
        }
        InverseCondition bool = (InverseCondition) cond;
        if (!this.value.matches(ctx, bool.getConditionValue())) {
            return null;
        }
        return bool;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private ConditionMatcher<?> value;

        public Builder() {
            reset();
        }

        /**
         * Sets the matcher for the boolean value.
         */
        public Builder value(ConditionMatcher<?> val) {
            this.value = val;
            return this;
        }

        /**
         * Resets the builder to default values.
         */
        public Builder reset() {
            this.value = null;
            return this;
        }

        /**
         * Creates a new matcher.
         */
        public InverseConditionMatcher build() {
            return new InverseConditionMatcher(this.value);
        }

    }

}

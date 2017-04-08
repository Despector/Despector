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

import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.util.Tristate;

/**
 * A matcher for boolean conditions.
 */
public class BooleanConditionMatcher implements ConditionMatcher<BooleanCondition> {

    private InstructionMatcher<?> value;
    private Tristate inverse;

    BooleanConditionMatcher(InstructionMatcher<?> val, Tristate inv) {
        this.value = val == null ? InstructionMatcher.ANY : val;
        this.inverse = checkNotNull(inv, "inverse");
    }

    @Override
    public BooleanCondition match(MatchContext ctx, Condition cond) {
        if (!(cond instanceof BooleanCondition)) {
            return null;
        }
        BooleanCondition bool = (BooleanCondition) cond;
        if (!this.value.matches(ctx, bool.getConditionValue())) {
            return null;
        }
        if (this.inverse != Tristate.UNDEFINED && this.inverse.asBoolean() != bool.isInverse()) {
            return null;
        }
        return bool;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private InstructionMatcher<?> value;
        private Tristate inverse;

        public Builder() {
            reset();
        }

        /**
         * Sets the matcher for the boolean value.
         */
        public Builder value(InstructionMatcher<?> val) {
            this.value = val;
            return this;
        }

        /**
         * Sets the expected inverse state.
         */
        public Builder inverse(Tristate inv) {
            this.inverse = checkNotNull(inv, "inverse");
            return this;
        }

        /**
         * Resets the builder to default values.
         */
        public Builder reset() {
            this.value = null;
            this.inverse = Tristate.UNDEFINED;
            return this;
        }

        /**
         * Creates a new matcher.
         */
        public BooleanConditionMatcher build() {
            return new BooleanConditionMatcher(this.value, this.inverse);
        }

    }

}

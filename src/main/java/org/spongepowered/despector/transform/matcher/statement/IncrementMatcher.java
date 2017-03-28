/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.despector.transform.matcher.statement;

import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

public class IncrementMatcher implements StatementMatcher<Increment> {

    private Integer value;

    IncrementMatcher(Integer value) {
        this.value = value;
    }

    @Override
    public Increment match(MatchContext ctx, Statement insn) {
        if (!(insn instanceof Increment)) {
            return null;
        }
        Increment invoke = (Increment) insn;
        if (this.value != null && invoke.getIncrementValue() != this.value.intValue()) {
            return null;
        }
        return invoke;
    }

    public static class Builder {

        private Integer value;

        public Builder() {
            reset();
        }

        public Builder value(int val) {
            this.value = val;
            return this;
        }

        public Builder reset() {
            this.value = null;
            return this;
        }

        public IncrementMatcher build() {
            return new IncrementMatcher(this.value);
        }

    }

}

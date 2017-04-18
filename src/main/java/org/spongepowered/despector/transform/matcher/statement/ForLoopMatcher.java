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
package org.spongepowered.despector.transform.matcher.statement;

import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * A matcher for for loops.
 */
public class ForLoopMatcher implements StatementMatcher<For> {

    private StatementMatcher<?> init;
    private ConditionMatcher<?> condition;
    private StatementMatcher<?> incr;
    private Map<Integer, StatementMatcher<?>> body;

    ForLoopMatcher(StatementMatcher<?> init, ConditionMatcher<?> cond, StatementMatcher<?> incr, Map<Integer, StatementMatcher<?>> body) {
        this.init = init == null ? StatementMatcher.ANY : init;
        this.condition = cond == null ? ConditionMatcher.ANY : cond;
        this.incr = incr == null ? StatementMatcher.ANY : incr;
        this.body = body;
    }

    @Override
    public For match(MatchContext ctx, Statement stmt) {
        if (!(stmt instanceof For)) {
            return null;
        }
        For loop = (For) stmt;
        if (!this.init.matches(ctx, loop.getInit())) {
            return null;
        }
        if (!this.condition.matches(ctx, loop.getCondition())) {
            return null;
        }
        if (!this.incr.matches(ctx, loop.getIncr())) {
            return null;
        }
        for (Map.Entry<Integer, StatementMatcher<?>> e : this.body.entrySet()) {
            int index = e.getKey();
            if (index < 0) {
                index = loop.getBody().getStatementCount() - index;
            }
            if (index < 0 || index >= loop.getBody().getStatementCount()) {
                return null;
            }
            Statement body_stmt = loop.getBody().getStatement(index);
            if (!e.getValue().matches(ctx, body_stmt)) {
                return null;
            }
        }
        return loop;
    }

    /**
     * A builder for for loop matchers.
     */
    public static class Builder {

        private StatementMatcher<?> init;
        private ConditionMatcher<?> condition;
        private StatementMatcher<?> incr;
        private final Map<Integer, StatementMatcher<?>> body = new HashMap<>();

        public Builder() {
            reset();
        }

        public Builder init(StatementMatcher<?> s) {
            this.init = s;
            return this;
        }

        public Builder condition(ConditionMatcher<?> s) {
            this.condition = s;
            return this;
        }

        public Builder incr(StatementMatcher<?> s) {
            this.incr = s;
            return this;
        }

        public Builder body(int index, StatementMatcher<?> matcher) {
            this.body.put(index, matcher);
            return this;
        }

        /**
         * Resets this builder.
         */
        public Builder reset() {
            this.incr = null;
            this.init = null;
            this.condition = null;
            this.body.clear();
            return this;
        }

        public ForLoopMatcher build() {
            return new ForLoopMatcher(this.init, this.condition, this.incr, this.body);
        }

    }

}

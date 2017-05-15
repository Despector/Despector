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
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * A matcher for if statements.
 */
public class IfMatcher implements StatementMatcher<If> {

    private ConditionMatcher<?> condition;
    private Map<Integer, StatementMatcher<?>> body;

    IfMatcher(ConditionMatcher<?> cond, Map<Integer, StatementMatcher<?>> body) {
        this.condition = cond == null ? ConditionMatcher.ANY : cond;
        this.body = body;
    }

    public ConditionMatcher<?> getConditionMatcher() {
        return this.condition;
    }

    @Override
    public If match(MatchContext ctx, Statement stmt) {
        if (!(stmt instanceof If)) {
            return null;
        }
        If loop = (If) stmt;
        if (!this.condition.matches(ctx, loop.getCondition())) {
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
     * A matcher builder.
     */
    public static class Builder {

        private ConditionMatcher<?> condition;
        private final Map<Integer, StatementMatcher<?>> body = new HashMap<>();

        public Builder() {
            reset();
        }

        public Builder condition(ConditionMatcher<?> s) {
            this.condition = s;
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
            this.condition = null;
            this.body.clear();
            return this;
        }

        public IfMatcher build() {
            return new IfMatcher(this.condition, this.body);
        }

    }

}

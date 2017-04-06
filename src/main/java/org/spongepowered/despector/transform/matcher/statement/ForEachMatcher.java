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

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

import java.util.HashMap;
import java.util.Map;

public class ForEachMatcher implements StatementMatcher<ForEach> {

    private LocalInstance init;
    private InstructionMatcher<?> value;
    private Map<Integer, StatementMatcher<?>> body;

    ForEachMatcher(LocalInstance init, InstructionMatcher<?> value, Map<Integer, StatementMatcher<?>> body) {
        this.init = init;
        this.value = value == null ? InstructionMatcher.ANY : value;
        this.body = body;
    }

    @Override
    public ForEach match(MatchContext ctx, Statement stmt) {
        if (!(stmt instanceof ForEach)) {
            return null;
        }
        ForEach loop = (ForEach) stmt;
        if (this.init != null && !this.init.equals(loop.getValueAssignment())) {
            return null;
        }
        if (!this.value.matches(ctx, loop.getCollectionValue())) {
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

    public static class Builder {

        private LocalInstance init;
        private InstructionMatcher<?> value;
        private final Map<Integer, StatementMatcher<?>> body = new HashMap<>();

        public Builder() {
            reset();
        }

        public Builder init(LocalInstance s) {
            this.init = s;
            return this;
        }

        public Builder value(InstructionMatcher<?> s) {
            this.value = s;
            return this;
        }

        public Builder body(int index, StatementMatcher<?> matcher) {
            this.body.put(index, matcher);
            return this;
        }

        public Builder reset() {
            this.init = null;
            this.value = null;
            this.body.clear();
            return this;
        }

        public ForEachMatcher build() {
            return new ForEachMatcher(this.init, this.value, this.body);
        }

    }

}

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
package org.spongepowered.despector.transform.matcher;

import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.transform.matcher.statement.ForEachMatcher;
import org.spongepowered.despector.transform.matcher.statement.ForLoopMatcher;
import org.spongepowered.despector.transform.matcher.statement.IncrementMatcher;
import org.spongepowered.despector.transform.matcher.statement.LocalAssignmentMatcher;

public interface StatementMatcher<T extends Statement> {

    T match(MatchContext ctx, Statement stmt);

    default T match(Statement stmt) {
        return match(MatchContext.create(), stmt);
    }

    default boolean matches(MatchContext ctx, Statement stmt) {
        return match(ctx, stmt) != null;
    }

    static final StatementMatcher<?> ANY = new Any();
    static final StatementMatcher<?> NONE = new None();

    static ForEachMatcher.Builder foreach() {
        return new ForEachMatcher.Builder();
    }

    static ForLoopMatcher.Builder forloop() {
        return new ForLoopMatcher.Builder();
    }

    static LocalAssignmentMatcher.Builder localassign() {
        return new LocalAssignmentMatcher.Builder();
    }

    static IncrementMatcher.Builder increment() {
        return new IncrementMatcher.Builder();
    }

    public static class Any implements StatementMatcher<Statement> {

        Any() {
        }

        @Override
        public Statement match(MatchContext ctx, Statement stmt) {
            return stmt;
        }

    }

    public static class None implements StatementMatcher<Statement> {

        None() {
        }

        @Override
        public Statement match(MatchContext ctx, Statement stmt) {
            return null;
        }

        @Override
        public boolean matches(MatchContext ctx, Statement stmt) {
            return stmt == null;
        }

    }

}

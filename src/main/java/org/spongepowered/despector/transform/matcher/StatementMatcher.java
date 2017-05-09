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
package org.spongepowered.despector.transform.matcher;

import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.transform.matcher.statement.ForEachMatcher;
import org.spongepowered.despector.transform.matcher.statement.ForLoopMatcher;
import org.spongepowered.despector.transform.matcher.statement.IncrementMatcher;
import org.spongepowered.despector.transform.matcher.statement.InstanceFieldAssignmentMatcher;
import org.spongepowered.despector.transform.matcher.statement.LocalAssignmentMatcher;
import org.spongepowered.despector.transform.matcher.statement.StaticFieldAssignmentMatcher;
import org.spongepowered.despector.transform.matcher.statement.WhileLoopMatcher;

import javax.annotation.Nullable;

/**
 * A matcher for statements.
 */
public interface StatementMatcher<T extends Statement> {

    /**
     * Attempts to match against the given statement and return the matched
     * statement if successful.
     */
    @Nullable
    T match(MatchContext ctx, Statement stmt);

    /**
     * Attempts to match against the given statement and return the matched
     * statement if successful.
     */
    @Nullable
    default T match(Statement stmt) {
        return match(MatchContext.create(), stmt);
    }

    /**
     * Attempts to match against the given statement..
     */
    default boolean matches(MatchContext ctx, Statement stmt) {
        return match(ctx, stmt) != null;
    }

    /**
     * A matcher which matches any statement.
     */
    static final StatementMatcher<?> ANY = new Any();
    /**
     * A matcher which matches no statement. Does match true for a null
     * statement.
     */
    static final StatementMatcher<?> NONE = new None();

    static ForEachMatcher.Builder forEach() {
        return new ForEachMatcher.Builder();
    }

    static ForLoopMatcher.Builder forLoop() {
        return new ForLoopMatcher.Builder();
    }

    static LocalAssignmentMatcher.Builder localAssign() {
        return new LocalAssignmentMatcher.Builder();
    }

    static IncrementMatcher.Builder increment() {
        return new IncrementMatcher.Builder();
    }

    static WhileLoopMatcher.Builder whileLoop() {
        return new WhileLoopMatcher.Builder();
    }

    static StaticFieldAssignmentMatcher.Builder staticFieldAssign() {
        return new StaticFieldAssignmentMatcher.Builder();
    }

    static InstanceFieldAssignmentMatcher.Builder instanceFieldAssign() {
        return new InstanceFieldAssignmentMatcher.Builder();
    }

    /**
     * A statement matcher that matches any statement.
     */
    public static class Any implements StatementMatcher<Statement> {

        Any() {
        }

        @Override
        public Statement match(MatchContext ctx, Statement stmt) {
            return stmt;
        }

    }

    /**
     * A statement matcher that matches no statement excetp a null statement.
     */
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

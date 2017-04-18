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

import javax.annotation.Nullable;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.transform.matcher.condition.AndConditionMatcher;
import org.spongepowered.despector.transform.matcher.condition.BooleanConditionMatcher;
import org.spongepowered.despector.transform.matcher.condition.CompareConditionMatcher;
import org.spongepowered.despector.transform.matcher.condition.ConditionReferenceMatcher;
import org.spongepowered.despector.transform.matcher.condition.InverseConditionMatcher;
import org.spongepowered.despector.transform.matcher.condition.OrConditionMatcher;

/**
 * A matcher for conditions.
 */
public interface ConditionMatcher<T extends Condition> {

    /**
     * Matches against the given condition and return the matched condition if
     * successful.
     */
    @Nullable
    T match(MatchContext ctx, Condition cond);

    /**
     * Matches against the given condition and return the matched condition if
     * successful.
     */
    @Nullable
    default T match(Condition cond) {
        return match(MatchContext.create(), cond);
    }

    /**
     * Attempts to match the given condition.
     */
    default boolean matches(MatchContext ctx, Condition cond) {
        return match(ctx, cond) != null;
    }

    /**
     * Attempts to match the given condition.
     */
    default boolean matches(Condition cond) {
        return match(MatchContext.create(), cond) != null;
    }

    /**
     * A matcher which matches any condition.
     */
    static final ConditionMatcher<?> ANY = new Any();

    static BooleanConditionMatcher.Builder bool() {
        return new BooleanConditionMatcher.Builder();
    }

    static InverseConditionMatcher.Builder inverse() {
        return new InverseConditionMatcher.Builder();
    }

    static CompareConditionMatcher.Builder compare() {
        return new CompareConditionMatcher.Builder();
    }

    static AndConditionMatcher.Builder and() {
        return new AndConditionMatcher.Builder();
    }

    static OrConditionMatcher.Builder or() {
        return new OrConditionMatcher.Builder();
    }

    static ConditionReferenceMatcher references(LocalInstance local) {
        return new ConditionReferenceMatcher(local);
    }

    static ConditionReferenceMatcher references(String ctx) {
        return new ConditionReferenceMatcher(ctx);
    }

    /**
     * A condition matcher that matches any condition.
     */
    public static class Any implements ConditionMatcher<Condition> {

        Any() {
        }

        @Override
        public Condition match(MatchContext ctx, Condition stmt) {
            return stmt;
        }

    }

}

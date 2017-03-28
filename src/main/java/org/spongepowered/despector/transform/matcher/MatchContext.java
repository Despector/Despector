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

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;

import java.util.HashMap;
import java.util.Map;

public class MatchContext {

    public static <T extends Statement> StatementMatcher<T> storeLocal(String identifier, StatementMatcher<T> inner) {
        return new LocalStoreMatcher<>(identifier, inner);
    }

    public static MatchContext create() {
        return new MatchContext();
    }

    private Map<String, LocalInstance> locals;

    MatchContext() {

    }

    public void storeLocal(String ident, LocalInstance local) {
        if (this.locals == null) {
            this.locals = new HashMap<>();
        }
        this.locals.put(ident, local);
    }

    public LocalInstance getLocal(String ident) {
        if (this.locals == null) {
            return null;
        }
        return this.locals.get(ident);
    }

    private static class LocalStoreMatcher<T extends Statement> implements StatementMatcher<T> {

        private String identifier;
        private StatementMatcher<T> internal;

        public LocalStoreMatcher(String identifier, StatementMatcher<T> inner) {
            this.identifier = identifier;
            this.internal = inner;
        }

        @Override
        public T match(MatchContext ctx, Statement stmt) {
            T inner = this.internal.match(ctx, stmt);
            if (inner == null) {
                return null;
            }
            if (inner instanceof LocalAssignment) {
                ctx.storeLocal(this.identifier, ((LocalAssignment) inner).getLocal());
            }
            if (inner instanceof ForEach) {
                ctx.storeLocal(this.identifier, ((ForEach) inner).getValueAssignment());
            }
            return inner;
        }

    }

}

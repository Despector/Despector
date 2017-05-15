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
package org.spongepowered.despector.transform.matcher.instruction;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

/**
 * A matcher for local accesses.
 */
public class LocalAccessMatcher implements InstructionMatcher<LocalAccess> {

    private boolean allow_missing = false;
    private LocalInstance local;
    private String ctx_local;

    LocalAccessMatcher(LocalInstance local) {
        this.local = local;
    }

    LocalAccessMatcher(String local, boolean allow_missing) {
        this.ctx_local = local;
        this.allow_missing = allow_missing;
    }

    @Override
    public LocalAccess match(MatchContext ctx, Instruction insn) {
        if (!(insn instanceof LocalAccess)) {
            return null;
        }
        LocalAccess acc = (LocalAccess) insn;
        if (this.local != null) {
            if (!this.local.equals(acc.getLocal())) {
                return null;
            }
        } else if (this.ctx_local != null) {
            LocalInstance l = ctx.getLocal(this.ctx_local);
            if (l == null) {
                if (!this.allow_missing) {
                    return null;
                }
                ctx.setLocal(this.ctx_local, acc.getLocal());
            } else if (!l.equals(acc.getLocal())) {
                return null;
            }
        }
        return acc;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private boolean allow_missing;
        private LocalInstance local;
        private String ctx_local;

        public Builder() {
            reset();
        }

        public Builder local(LocalInstance local) {
            this.local = local;
            return this;
        }

        public Builder fromContext(String identifier) {
            this.ctx_local = identifier;
            return this;
        }

        public Builder allowMissing() {
            this.allow_missing = true;
            return this;
        }

        /**
         * Resets this builder.
         */
        public Builder reset() {
            this.local = null;
            this.ctx_local = null;
            this.allow_missing = false;
            return this;
        }

        /**
         * Creates a new matcher.
         */
        public LocalAccessMatcher build() {
            if (this.ctx_local != null) {
                return new LocalAccessMatcher(this.ctx_local, this.allow_missing);
            }
            return new LocalAccessMatcher(this.local);
        }

    }

}

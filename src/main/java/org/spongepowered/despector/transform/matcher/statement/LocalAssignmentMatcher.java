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

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

public class LocalAssignmentMatcher implements StatementMatcher<LocalAssignment> {

    private InstructionMatcher<?> value;
    private boolean unwrap;
    private TypeSignature type;

    LocalAssignmentMatcher(InstructionMatcher<?> value, TypeSignature type, boolean unwrap) {
        this.value = value == null ? InstructionMatcher.ANY : value;
        this.type = type;
        this.unwrap = unwrap;
    }

    @Override
    public LocalAssignment match(MatchContext ctx, Statement stmt) {
        if (!(stmt instanceof LocalAssignment)) {
            return null;
        }
        LocalAssignment assign = (LocalAssignment) stmt;
        Instruction val = assign.getValue();
        if (this.unwrap && val instanceof Cast) {
            val = ((Cast) val).getValue();
        }
        if (!this.value.matches(ctx, val)) {
            return null;
        }
        if (this.type != null && !this.type.equals(assign.getLocal().getType())) {
            return null;
        }
        return assign;
    }

    public static class Builder {

        private InstructionMatcher<?> value;
        private TypeSignature type;
        private boolean unwrap;

        public Builder() {
            reset();
        }

        public Builder value(InstructionMatcher<?> val) {
            this.value = val;
            return this;
        }

        public Builder type(TypeSignature type) {
            this.type = type;
            return this;
        }

        public Builder autoUnwrap() {
            this.unwrap = true;
            return this;
        }

        public Builder reset() {
            this.value = null;
            this.type = null;
            this.unwrap = false;
            return this;
        }

        public LocalAssignmentMatcher build() {
            return new LocalAssignmentMatcher(this.value, this.type, this.unwrap);
        }

    }

}

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

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

/**
 * A matcher for local assignments.
 */
public class StaticFieldAssignmentMatcher implements StatementMatcher<StaticFieldAssignment> {

    private InstructionMatcher<?> value;
    private boolean unwrap;
    private String owner;
    private String name;
    private TypeSignature type;

    StaticFieldAssignmentMatcher(InstructionMatcher<?> value, String owner, String name, TypeSignature type, boolean unwrap) {
        this.value = value == null ? InstructionMatcher.ANY : value;
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.unwrap = unwrap;
    }

    @Override
    public StaticFieldAssignment match(MatchContext ctx, Statement stmt) {
        if (!(stmt instanceof StaticFieldAssignment)) {
            return null;
        }
        StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
        Instruction val = assign.getValue();
        if (this.unwrap && val instanceof Cast) {
            val = ((Cast) val).getValue();
        }
        if (!this.value.matches(ctx, val)) {
            return null;
        }
        if (this.owner != null && !this.owner.equals(assign.getOwnerType())) {
            return null;
        }
        if (this.name != null && !this.name.equals(assign.getFieldName())) {
            return null;
        }
        if (this.type != null && !this.type.equals(assign.getFieldDescription())) {
            return null;
        }
        return assign;
    }

    /**
     * A matcher builder.
     */
    public static class Builder {

        private InstructionMatcher<?> value;
        private String owner;
        private String name;
        private TypeSignature type;
        private boolean unwrap;

        public Builder() {
            reset();
        }

        public Builder value(InstructionMatcher<?> val) {
            this.value = val;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
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

        /**
         * Resets this builder.
         */
        public Builder reset() {
            this.value = null;
            this.type = null;
            this.unwrap = false;
            this.owner = null;
            this.name = null;
            return this;
        }

        public StaticFieldAssignmentMatcher build() {
            return new StaticFieldAssignmentMatcher(this.value, this.owner, this.name, this.type, this.unwrap);
        }

    }

}

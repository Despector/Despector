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
package org.spongepowered.despector.ast.members.insn.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.List;

/**
 * A switch statement.
 */
public class Switch implements Statement {

    private Instruction variable;
    final List<Case> cases = Lists.newArrayList();

    public Switch(Instruction var) {
        this.variable = checkNotNull(var, "var");
    }

    /**
     * Gets the variable that the switch is acting upon.
     */
    public Instruction getSwitchVar() {
        return this.variable;
    }

    /**
     * Sets the variable that the switch is acting upon.
     */
    public void setSwitchVar(Instruction var) {
        this.variable = checkNotNull(var, "var");
    }

    /**
     * Gets all cases of this switch.
     */
    public List<Case> getCases() {
        return this.cases;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitSwitch(this);
        this.variable.accept(visitor);
        for (Case cs : this.cases) {
            cs.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(3);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_SWITCH);
        pack.writeString("var");
        this.variable.writeTo(pack);
        pack.writeString("cases").startArray(this.cases.size());
        for (Case cs : this.cases) {
            pack.startMap(4);
            pack.writeString("body");
            cs.getBody().writeTo(pack);
            pack.writeString("breaks").writeBool(cs.doesBreak());
            pack.writeString("default").writeBool(cs.isDefault());
            pack.writeString("indices").startArray(cs.getIndices().size());
            for (Integer index : cs.getIndices()) {
                pack.writeInt(index);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Switch)) {
            return false;
        }
        Switch insn = (Switch) obj;
        return this.variable.equals(insn.variable) && this.cases.equals(insn.cases);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.variable.hashCode();
        for (Case cs : this.cases) {
            h = h * 37 + cs.hashCode();
        }
        return h;
    }

    /**
     * A switch case.
     */
    public class Case {

        private StatementBlock body;
        private boolean breaks;
        private boolean is_default;
        private final List<Integer> indices;

        public Case(StatementBlock block, boolean br, boolean def, List<Integer> indices) {
            this.body = checkNotNull(block, "block");
            this.breaks = br;
            this.is_default = def;
            this.indices = indices == null ? Lists.newArrayList() : indices;
            Switch.this.cases.add(this);
        }

        /**
         * Gets the body of this case.
         */
        public StatementBlock getBody() {
            return this.body;
        }

        /**
         * Sets the body of this case.
         */
        public void setBody(StatementBlock block) {
            this.body = checkNotNull(block, "block");
        }

        /**
         * Gets if this case breaks at the end.
         */
        public boolean doesBreak() {
            return this.breaks;
        }

        /**
         * Sets if this case breaks at the end.
         */
        public void setBreak(boolean state) {
            this.breaks = state;
        }

        /**
         * Gets if this is the default case.
         */
        public boolean isDefault() {
            return this.is_default;
        }

        /**
         * Sets if this is the default case.
         */
        public void setDefault(boolean state) {
            this.is_default = state;
        }

        /**
         * Gets the indices targeting this case.
         */
        public List<Integer> getIndices() {
            return this.indices;
        }

        /**
         * Accepts the given visitor.
         */
        public void accept(InstructionVisitor visitor) {
            visitor.visitSwitchCase(this);
            for (Statement stmt : this.body.getStatements()) {
                stmt.accept(visitor);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Case)) {
                return false;
            }
            Case insn = (Case) obj;
            return this.body.equals(insn.body) && this.breaks == insn.breaks && this.is_default == insn.is_default
                    && this.indices.equals(insn.indices);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h = h * 37 + this.body.hashCode();
            h = h * 37 + this.indices.hashCode();
            h = h * 37 + (this.breaks ? 1 : 0);
            h = h * 37 + (this.is_default ? 1 : 0);
            return h;
        }

    }

}

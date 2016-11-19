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
package org.spongepowered.despector.ast.members.insn.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

import java.util.List;

public class TableSwitch implements Statement {

    private Instruction variable;
    private final List<Case> cases = Lists.newArrayList();

    public TableSwitch(Instruction var) {
        this.variable = checkNotNull(var, "var");
    }

    public Instruction getSwitchVar() {
        return this.variable;
    }

    public void setSwitchVar(Instruction var) {
        this.variable = checkNotNull(var, "var");
    }

    public List<Case> getCases() {
        return this.cases;
    }

    public void addCase(Case cs) {
        this.cases.add(cs);
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitSwitch(this);
        this.variable.accept(visitor);
        for (Case cs : this.cases) {
            cs.accept(visitor);
        }
    }

    public static class Case {

        private StatementBlock body;
        private boolean breaks;
        private boolean is_default;
        private final List<Integer> indices;

        public Case(StatementBlock block, boolean br, boolean def, List<Integer> indices) {
            this.body = checkNotNull(block, "block");
            this.breaks = br;
            this.is_default = def;
            this.indices = indices == null ? Lists.newArrayList() : indices;
        }

        public StatementBlock getBody() {
            return this.body;
        }

        public void setBody(StatementBlock block) {
            this.body = checkNotNull(block, "block");
        }

        public boolean doesBreak() {
            return this.breaks;
        }

        public void setBreak(boolean state) {
            this.breaks = state;
        }

        public boolean isDefault() {
            return this.is_default;
        }

        public void setDefault(boolean state) {
            this.is_default = state;
        }

        public List<Integer> getIndices() {
            return this.indices;
        }

        public void accept(InstructionVisitor visitor) {
            visitor.visitSwitchCase(this);
            for (Statement stmt : this.body.getStatements()) {
                stmt.accept(visitor);
            }
        }

    }

}

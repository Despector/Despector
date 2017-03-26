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
package org.spongepowered.despector.ast.kotlin;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

import java.util.ArrayList;
import java.util.List;

public class When implements Instruction {

    private LocalInstance local;
    private Instruction var;
    private final List<Case> cases = new ArrayList<>();
    private Case else_body;

    public When(LocalInstance local, Instruction var) {
        this.var = var;
    }

    public LocalInstance getLocal() {
        return this.local;
    }

    public Instruction getArg() {
        return this.var;
    }

    public void setArg(Instruction insn) {
        this.var = insn;
    }

    public List<Case> getCases() {
        return this.cases;
    }

    public StatementBlock getElseBody() {
        return this.else_body.getBody();
    }

    public Instruction getElseBodyLast() {
        return this.else_body.getLast();
    }

    public void setElseBody(StatementBlock body, Instruction last) {
        this.else_body = new Case(null, body, last);
    }

    @Override
    public String inferType() {
        return this.cases.get(0).getLast().inferType();
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        // TODO
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("when (").append(this.var.toString()).append(") {\n");
        for (Case cs : this.cases) {
            str.append("    ").append(cs.getCondition().toString()).append(" -> ");
            if (cs.getBody().getStatementCount() == 0) {
                str.append(cs.getLast().toString()).append("\n");
            } else {
                str.append("    {\n");
                for (Statement stmt : cs.getBody().getStatements()) {
                    str.append("        ").append(stmt.toString());
                }
                str.append("        ").append(cs.getLast().toString()).append("\n");
                str.append("    }\n");
            }
        }
        str.append("}");
        return str.toString();
    }

    public static class Case {

        private Condition condition;
        private StatementBlock body;
        private Instruction last;

        public Case(Condition cond, StatementBlock body, Instruction last) {
            this.condition = cond;
            this.body = body;
            this.last = last;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public StatementBlock getBody() {
            return this.body;
        }

        public Instruction getLast() {
            return this.last;
        }
    }

}

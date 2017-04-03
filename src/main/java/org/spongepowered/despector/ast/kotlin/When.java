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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A kotlin when statement.
 */
public class When implements Instruction {

    private LocalInstance local;
    private Instruction var;
    private final List<Case> cases = new ArrayList<>();
    private Case else_body;

    public When(LocalInstance local, Instruction var) {
        this.local = checkNotNull(local, "local");
        this.var = checkNotNull(var, "var");
        this.else_body = new Case(null, null, null);
    }

    /**
     * Gets the local used internally for each of the case checks.
     */
    public LocalInstance getLocal() {
        return this.local;
    }

    /**
     * Sets the local used internally for each of the case checks.
     */
    public void setLocal(LocalInstance local) {
        this.local = checkNotNull(local, "local");
    }

    /**
     * Gets the argument of the when statement.
     */
    public Instruction getArg() {
        return this.var;
    }

    /**
     * Sets the argument of the when statement.
     */
    public void setArg(Instruction insn) {
        this.var = checkNotNull(insn, "var");
    }

    /**
     * Gets the cases of the when statement.
     */
    public List<Case> getCases() {
        return this.cases;
    }

    /**
     * Gets the body of the else case.
     */
    @Nullable
    public StatementBlock getElseBody() {
        return this.else_body.getBody();
    }

    /**
     * Gets the last instruction of the else case which becomes the final value.
     */
    @Nullable
    public Instruction getElseBodyLast() {
        return this.else_body.getLast();
    }

    public void setElseBody(@Nullable StatementBlock body, @Nullable Instruction last) {
        this.else_body.setBody(body);
        this.else_body.setInstruction(last);
    }

    @Override
    public TypeSignature inferType() {
        // TODO gather the most specific type of all cases
        return this.cases.get(0).getLast().inferType();
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        // TODO
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        //TODO
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

    /**
     * A case of the when statement.
     */
    public static class Case {

        @Nullable private Condition condition;
        @Nullable private StatementBlock body;
        @Nullable private Instruction last;

        public Case(@Nullable Condition cond, StatementBlock body, Instruction last) {
            this.condition = cond;
            this.body = checkNotNull(body, "body");
            this.last = checkNotNull(last, "last");
        }

        /**
         * Gets the condition of this case, or null if this is the else case.
         */
        @Nullable
        public Condition getCondition() {
            return this.condition;
        }

        /**
         * Sets the condition of this case, may be null if this is the else
         * case.
         */
        public void setCondition(Condition cond) {
            this.condition = cond;
        }

        /**
         * Gets the body of this case, or null if this case is a single
         * instruction.
         */
        public StatementBlock getBody() {
            return this.body;
        }

        /**
         * Sets the body of this case, may be null if this case is a single
         * instruction.
         */
        public void setBody(StatementBlock body) {
            this.body = body;
        }

        /**
         * Gets the last instruction which becomes the value of the case. May be
         * null if this when statement does not return any value.
         */
        public Instruction getLast() {
            return this.last;
        }

        /**
         * Sets the last instruction which becomes the value of the case. May be
         * null if this when statement does not return any value.
         */
        public void setInstruction(Instruction insn) {
            this.last = insn;
        }
    }

}

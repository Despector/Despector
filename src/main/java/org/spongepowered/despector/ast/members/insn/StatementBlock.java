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
package org.spongepowered.despector.ast.members.insn;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.Locals;

import java.util.List;

/**
 * A block of statements.
 */
public class StatementBlock {

    /**
     * The type of a block.
     */
    public static enum Type {
        BLOCK,
        IF,
        WHILE,
        METHOD,
        SWITCH,
        TRY,
        CATCH,
        FINALLY
    }

    private Type type;
    private final Locals locals;
    private final List<Statement> statements;

    public StatementBlock(Type type, Locals locals) {
        this.type = checkNotNull(type, "type");
        this.locals = checkNotNull(locals, "locals");
        this.statements = Lists.newArrayList();
    }

    public StatementBlock(StatementBlock block) {
        this.type = block.type;
        this.locals = new Locals(block.locals);
        this.statements = Lists.newArrayList(block.statements);
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type t) {
        this.type = checkNotNull(t, "type");
    }

    public Locals getLocals() {
        return this.locals;
    }

    public List<Statement> getStatements() {
        return this.statements;
    }

    public void append(Statement insn) {
        this.statements.add(checkNotNull(insn, "insn"));
    }

    public Statement popStatement() {
        Statement last = this.statements.get(this.statements.size() - 1);
        this.statements.remove(this.statements.size() - 1);
        return last;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement insn : this.statements) {
            sb.append(insn.toString()).append("\n");
        }
        return sb.toString();
    }

}

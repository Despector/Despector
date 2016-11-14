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

import org.spongepowered.despector.ast.io.insn.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;

import java.util.List;

public class CatchBlock {

    private LocalInstance exception_local;
    private String dummy_name;
    private List<String> exceptions;
    private StatementBlock block;

    public CatchBlock(LocalInstance exception_local, List<String> ex, StatementBlock block) {
        this.exception_local = exception_local;
        this.dummy_name = null;
        this.exceptions = ex;
        this.block = block;
    }

    public CatchBlock(String dummy_name, List<String> ex, StatementBlock block) {
        this.exception_local = null;
        this.dummy_name = dummy_name;
        this.exceptions = ex;
        this.block = block;
    }

    public LocalInstance getExceptionLocal() {
        return this.exception_local;
    }

    public String getDummyName() {
        if (this.exception_local != null) {
            return this.exception_local.getName();
        }
        return this.dummy_name;
    }

    public void setDummyName(String name) {
        this.dummy_name = name;
    }

    public List<String> getExceptions() {
        return this.exceptions;
    }

    public StatementBlock getBlock() {
        return this.block;
    }

    public void accept(InstructionVisitor visitor) {
        visitor.visitCatchBlock(this);
        for (Statement stmt : this.block.getStatements()) {
            stmt.accept(visitor);
        }
    }

}

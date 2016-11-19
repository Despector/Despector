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

import org.spongepowered.despector.ast.io.insn.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;

import java.util.List;

import javax.annotation.Nullable;

public class CatchBlock {

    private final List<String> exceptions;
    private StatementBlock block;
    private LocalInstance exception_local;
    private String dummy_name;

    public CatchBlock(LocalInstance exception_local, List<String> ex, StatementBlock block) {
        this.exception_local = checkNotNull(exception_local, "local");
        this.dummy_name = null;
        this.exceptions = ex;
        this.block = block;
    }

    public CatchBlock(String dummy_name, List<String> ex, StatementBlock block) {
        this.exception_local = null;
        this.dummy_name = checkNotNull(dummy_name, "name");
        this.exceptions = ex;
        this.block = block;
    }

    @Nullable
    public LocalInstance getExceptionLocal() {
        return this.exception_local;
    }

    public void setExceptionLocal(@Nullable LocalInstance local) {
        if (local == null && this.dummy_name == null) {
            throw new IllegalStateException("Cannot have both a null exception local and dummy name in catch block.");
        }
        this.exception_local = local;
    }

    public String getDummyName() {
        if (this.exception_local != null) {
            return this.exception_local.getName();
        }
        return this.dummy_name;
    }

    /**
     * Sets the dummy name for this variable. This name is ignored if the
     * {@link #getExceptionLocal()} is not null.
     */
    public void setDummyName(String name) {
        if (name == null && this.exception_local == null) {
            throw new IllegalStateException("Cannot have both a null exception local and dummy name in catch block.");
        }
        this.dummy_name = name;
    }

    public List<String> getExceptions() {
        return this.exceptions;
    }

    public StatementBlock getBlock() {
        return this.block;
    }

    public void setBlock(StatementBlock block) {
        this.block = checkNotNull(block, "block");
    }

    public void accept(InstructionVisitor visitor) {
        visitor.visitCatchBlock(this);
        for (Statement stmt : this.block.getStatements()) {
            stmt.accept(visitor);
        }
    }

}

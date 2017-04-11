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
package com.voxelgenesis.despector.core.emitter;

import com.voxelgenesis.despector.core.ast.method.Local;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.method.stmt.Statement;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.ast.type.MethodEntry;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEmitterContext implements EmitterContext {

    private final EmitterSet set;

    private final Set<Local> defined_locals = new HashSet<>();
    private MethodEntry current_method;

    public AbstractEmitterContext(EmitterSet set) {
        this.set = set;
    }

    @Override
    public MethodEntry getCurrentMethod() {
        return this.current_method;
    }

    @Override
    public void setCurrentMethod(MethodEntry method) {
        this.current_method = method;
    }

    @Override
    public boolean isDefined(Local local) {
        return this.defined_locals.contains(local);
    }

    @Override
    public void markDefined(Local local) {
        this.defined_locals.add(local);
    }

    @Override
    public void clearDefinedLocals() {
        this.defined_locals.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Statement> void emitStatement(T stmt, boolean semicolon) {
        StatementEmitter<T> emitter = (StatementEmitter<T>) this.set.getStatementEmitter(stmt.getClass());
        if (emitter == null) {
            throw new IllegalStateException("No statement emitter for " + stmt.getClass().getName());
        }
        emitter.emit(this, stmt, semicolon);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Instruction> void emitInstruction(T stmt, TypeSignature type) {
        InstructionEmitter<T> emitter = (InstructionEmitter<T>) this.set.getInstructionEmitter(stmt.getClass());
        if (emitter == null) {
            throw new IllegalStateException("No statement emitter for " + stmt.getClass().getName());
        }
        emitter.emit(this, stmt, type);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void emitTypeSignature(TypeSignature sig) {
        TypeSignatureEmitter emitter = this.set.getTypeSignatureEmitter(sig.getClass());
        if (emitter == null) {
            throw new IllegalStateException("No signature emitter for " + sig.getClass().getName());
        }
        emitter.emit(sig);
    }
}

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
package org.spongepowered.despector.emitter.output;

import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterSet;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.StatementEmitter;

import java.util.ArrayList;
import java.util.List;

public class EmitterOutput {

    private final EmitterSet set;
    private final List<EmitterToken> tokens = new ArrayList<>(512);

    private TypeEntry outer_type;
    private TypeEntry type;
    private MethodEntry method;
    private FieldEntry field;

    public EmitterOutput(EmitterSet set) {
        this.set = set;
    }

    public List<EmitterToken> getTokens() {
        return this.tokens;
    }

    public void append(EmitterToken token) {
        this.tokens.add(token);
    }

    public void emitOuterType(TypeEntry type) {
        this.outer_type = type;

        emitType(type);

        this.outer_type = null;
    }

    public void emit(Annotation anno) {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void emitType(TypeEntry entry) {
        this.type = entry;

        AstEmitter emitter = this.set.getAstEmitter(entry.getClass());
        emitter.emit(this, entry);

        this.type = null;
    }

    public void emitField(FieldEntry fld) {
        this.field = fld;

        AstEmitter<FieldEntry> emitter = this.set.getAstEmitter(FieldEntry.class);
        emitter.emit(this, fld);

        this.field = null;
    }

    public void emitMethod(MethodEntry mth) {
        this.method = mth;

        AstEmitter<MethodEntry> emitter = this.set.getAstEmitter(MethodEntry.class);
        emitter.emit(this, mth);

        this.method = null;
    }

    public void emitBody(StatementBlock block, int start) {
        for (int i = start; i < block.getStatementCount(); i++) {
            emitStatement(block.getStatement(i));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Statement> void emitStatement(T stmt) {

        StatementEmitter<T> emitter = (StatementEmitter<T>) this.set.getStatementEmitter(stmt.getClass());
        emitter.emit(this, stmt);

    }

    @SuppressWarnings("unchecked")
    public <T extends Instruction> void emitInstruction(T insn, TypeSignature type) {

        InstructionEmitter<T> emitter = (InstructionEmitter<T>) this.set.getInstructionEmitter(insn.getClass());
        emitter.emit(this, insn, type);

    }

    public TypeEntry getOuterType() {
        return this.outer_type;
    }

    public TypeEntry getType() {
        return this.type;
    }

    public MethodEntry getMethod() {
        return this.method;
    }

    public FieldEntry getField() {
        return this.field;
    }

}

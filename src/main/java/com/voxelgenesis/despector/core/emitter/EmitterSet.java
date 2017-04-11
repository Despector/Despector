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

import com.voxelgenesis.despector.core.ast.method.condition.Condition;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.method.stmt.Statement;
import com.voxelgenesis.despector.core.ast.type.SourceEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of emitter operations.
 */
public class EmitterSet {

    private final Map<Class<?>, TypeEmitter<?>> emitters = new HashMap<>();
    private final Map<Class<?>, StatementEmitter<?>> stmt_emitters = new HashMap<>();
    private final Map<Class<?>, InstructionEmitter<?>> insn_emitters = new HashMap<>();
    private final Map<Class<?>, ConditionEmitter<?>> cond_emitters = new HashMap<>();
    private final Map<Class<?>, SpecialEmitter> special_emitters = new HashMap<>();
    private final Map<Class<?>, TypeSignatureEmitter<?>> signature_emitters = new HashMap<>();

    public EmitterSet() {

    }

    /**
     * Gets the given ast emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends SourceEntry> TypeEmitter<T> getAstEmitter(Class<T> type) {
        return (TypeEmitter<T>) this.emitters.get(type);
    }

    /**
     * Sets the given ast emitter for the given type.
     */
    public <T extends SourceEntry> void setAstEmitter(Class<? extends T> type, TypeEmitter<T> emitter) {
        this.emitters.put(type, emitter);
    }

    /**
     * Gets the given statement emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Statement> StatementEmitter<T> getStatementEmitter(Class<T> type) {
        return (StatementEmitter<T>) this.stmt_emitters.get(type);
    }

    /**
     * Sets the given statement emitter for the given type.
     */
    public <T extends Statement> void setStatementEmitter(Class<? extends T> type, StatementEmitter<T> emitter) {
        this.stmt_emitters.put(type, emitter);
    }

    /**
     * Gets the given instruction emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Instruction> InstructionEmitter<T> getInstructionEmitter(Class<T> type) {
        return (InstructionEmitter<T>) this.insn_emitters.get(type);
    }

    /**
     * Sets the given instruction emitter for the given type.
     */
    public <T extends Instruction> void setInstructionEmitter(Class<? extends T> type, InstructionEmitter<T> emitter) {
        this.insn_emitters.put(type, emitter);
    }

    /**
     * Gets the given condition emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Condition> ConditionEmitter<T> getConditionEmitter(Class<T> type) {
        return (ConditionEmitter<T>) this.cond_emitters.get(type);
    }

    /**
     * Sets the given condition emitter for the given type.
     */
    public <T extends Condition> void setConditionEmitter(Class<T> type, ConditionEmitter<T> emitter) {
        this.cond_emitters.put(type, emitter);
    }

    /**
     * Gets the given special emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends SpecialEmitter> T getSpecialEmitter(Class<T> type) {
        return (T) this.special_emitters.get(type);
    }

    /**
     * Sets the given special emitter for the given type.
     */
    public <T extends SpecialEmitter> void setSpecialEmitter(Class<T> type, T emitter) {
        this.special_emitters.put(type, emitter);
    }

    public TypeSignatureEmitter<?> getTypeSignatureEmitter(Class<?> type) {
        return this.signature_emitters.get(type);
    }

    public void setTypeSignatureEmitter(Class<?> type, TypeSignatureEmitter<?> emitter) {
        this.signature_emitters.put(type, emitter);
    }

    /**
     * Clones the given {@link EmitterSet} into this set.
     */
    public void clone(EmitterSet other) {
        this.emitters.putAll(other.emitters);
        this.cond_emitters.putAll(other.cond_emitters);
        this.insn_emitters.putAll(other.insn_emitters);
        this.special_emitters.putAll(other.special_emitters);
        this.stmt_emitters.putAll(other.stmt_emitters);
        this.signature_emitters.putAll(other.signature_emitters);
    }

}

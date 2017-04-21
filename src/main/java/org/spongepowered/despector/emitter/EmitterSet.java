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
package org.spongepowered.despector.emitter;

import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.emitter.format.EmitterFormat;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of emitter operations.
 */
public class EmitterSet {

    private final Map<Class<?>, AstEmitter<?,?>> emitters = new HashMap<>();
    private final Map<Class<?>, StatementEmitter<?,?>> stmt_emitters = new HashMap<>();
    private final Map<Class<?>, InstructionEmitter<?,?>> insn_emitters = new HashMap<>();
    private final Map<Class<?>, ConditionEmitter<?,?>> cond_emitters = new HashMap<>();
    private final Map<Class<?>, SpecialEmitter> special_emitters = new HashMap<>();

    public EmitterSet() {

    }

    /**
     * Gets the given ast emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <C extends AbstractEmitterContext, T extends AstEntry> AstEmitter<C, T> getAstEmitter(Class<T> type) {
        return (AstEmitter<C, T>) this.emitters.get(type);
    }

    /**
     * Sets the given ast emitter for the given type.
     */
    public <T extends AstEntry> void setAstEmitter(Class<? extends T> type, AstEmitter<?, T> emitter) {
        this.emitters.put(type, emitter);
    }

    /**
     * Gets the given statement emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <C extends AbstractEmitterContext, T extends Statement> StatementEmitter<C, T> getStatementEmitter(Class<T> type) {
        return (StatementEmitter<C, T>) this.stmt_emitters.get(type);
    }

    /**
     * Sets the given statement emitter for the given type.
     */
    public <T extends Statement> void setStatementEmitter(Class<? extends T> type, StatementEmitter<?, T> emitter) {
        this.stmt_emitters.put(type, emitter);
    }

    /**
     * Gets the given instruction emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <C extends AbstractEmitterContext, T extends Instruction> InstructionEmitter<C, T> getInstructionEmitter(Class<T> type) {
        return (InstructionEmitter<C, T>) this.insn_emitters.get(type);
    }

    /**
     * Sets the given instruction emitter for the given type.
     */
    public <T extends Instruction> void setInstructionEmitter(Class<? extends T> type, InstructionEmitter<?, T> emitter) {
        this.insn_emitters.put(type, emitter);
    }

    /**
     * Gets the given condition emitter for the given type.
     */
    @SuppressWarnings("unchecked")
    public <C extends AbstractEmitterContext, T extends Condition> ConditionEmitter<C, T> getConditionEmitter(Class<T> type) {
        return (ConditionEmitter<C, T>) this.cond_emitters.get(type);
    }

    /**
     * Sets the given condition emitter for the given type.
     */
    public <T extends Condition> void setConditionEmitter(Class<T> type, ConditionEmitter<?, T> emitter) {
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

    /**
     * Clones the given {@link EmitterFormat} into this emitter set.
     */
    public void clone(EmitterSet other) {
        this.emitters.putAll(other.emitters);
        this.cond_emitters.putAll(other.cond_emitters);
        this.insn_emitters.putAll(other.insn_emitters);
        this.special_emitters.putAll(other.special_emitters);
        this.stmt_emitters.putAll(other.stmt_emitters);
    }

}

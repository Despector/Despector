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
package org.spongepowered.despector.emitter;

import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.emitter.special.SpecialEmitter;

import java.util.HashMap;
import java.util.Map;

public class EmitterSet {

    private final Map<Class<?>, AstEmitter<?>> emitters = new HashMap<>();
    private final Map<Class<?>, StatementEmitter<?>> stmt_emitters = new HashMap<>();
    private final Map<Class<?>, InstructionEmitter<?>> insn_emitters = new HashMap<>();
    private final Map<Class<?>, ConditionEmitter<?>> cond_emitters = new HashMap<>();
    private final Map<Class<?>, SpecialEmitter> special_emitters = new HashMap<>();

    public EmitterSet() {

    }

    public EmitterSet(EmitterSet set) {
        this.emitters.putAll(set.emitters);
    }

    @SuppressWarnings("unchecked")
    public <T extends AstEntry> AstEmitter<T> getAstEmitter(Class<T> type) {
        return (AstEmitter<T>) this.emitters.get(type);
    }

    public <T extends AstEntry> void setAstEmitter(Class<? extends T> type, AstEmitter<T> emitter) {
        this.emitters.put(type, emitter);
    }

    @SuppressWarnings("unchecked")
    public <T extends Statement> StatementEmitter<T> getStatementEmitter(Class<T> type) {
        return (StatementEmitter<T>) this.stmt_emitters.get(type);
    }

    public <T extends Statement> void setStatementEmitter(Class<? extends T> type, StatementEmitter<T> emitter) {
        this.stmt_emitters.put(type, emitter);
    }

    @SuppressWarnings("unchecked")
    public <T extends Instruction> InstructionEmitter<T> getInstructionEmitter(Class<T> type) {
        return (InstructionEmitter<T>) this.insn_emitters.get(type);
    }

    public <T extends Instruction> void setInstructionEmitter(Class<? extends T> type, InstructionEmitter<T> emitter) {
        this.insn_emitters.put(type, emitter);
    }

    @SuppressWarnings("unchecked")
    public <T extends Condition> ConditionEmitter<T> getConditionEmitter(Class<T> type) {
        return (ConditionEmitter<T>) this.cond_emitters.get(type);
    }

    public <T extends Condition> void setConditionEmitter(Class<T> type, ConditionEmitter<T> emitter) {
        this.cond_emitters.put(type, emitter);
    }

    @SuppressWarnings("unchecked")
    public <T extends SpecialEmitter> T getSpecialEmitter(Class<T> type) {
        return (T) this.special_emitters.get(type);
    }

    public <T extends SpecialEmitter> void setSpecialEmitter(Class<T> type, T emitter) {
        this.special_emitters.put(type, emitter);
    }

}

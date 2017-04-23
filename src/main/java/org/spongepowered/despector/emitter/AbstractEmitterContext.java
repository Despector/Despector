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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class AbstractEmitterContext {

    protected EmitterSet set;

    protected TypeEntry type = null;
    protected TypeEntry outer_type = null;
    protected MethodEntry method;
    protected FieldEntry field;
    protected Statement statement;
    protected Deque<Instruction> insn_stack = new ArrayDeque<>();

    /**
     * Gets the current {@link EmitterSet}.
     */
    public EmitterSet getEmitterSet() {
        return this.set;
    }

    /**
     * Sets the current {@link EmitterSet}.
     */
    public void setEmitterSet(EmitterSet set) {
        this.set = checkNotNull(set, "set");
    }

    /**
     * Gets the current type being emitted.
     */
    @Nullable
    public TypeEntry getType() {
        return this.type;
    }

    /**
     * Sets the current type being emitted.
     */
    public void setType(@Nullable TypeEntry type) {
        this.type = type;
    }

    /**
     * Gets the current outer type being emitted.
     */
    @Nullable
    public TypeEntry getOuterType() {
        return this.outer_type;
    }

    public void setOuterType(@Nullable TypeEntry type) {
        this.outer_type = type;
    }

    /**
     * Gets the current method being emitted.
     */
    @Nullable
    public MethodEntry getMethod() {
        return this.method;
    }

    /**
     * Sets the current method being emitted.
     */
    public void setMethod(@Nullable MethodEntry mth) {
        this.method = mth;
    }

    /**
     * Gets the current field being emitted.
     */
    @Nullable
    public FieldEntry getField() {
        return this.field;
    }

    /**
     * Sets the current field being emitted.
     */
    public void setField(@Nullable FieldEntry fld) {
        this.field = fld;
    }

    /**
     * Gets the current statement being emitted.
     */
    public Statement getStatement() {
        return this.statement;
    }

    /**
     * Sets the current statement being emitted.
     */
    public void setStatement(Statement stmt) {
        this.statement = stmt;
    }

    /**
     * Gets the current instruction stack.
     */
    public Deque<Instruction> getCurrentInstructionStack() {
        return this.insn_stack;
    }

}

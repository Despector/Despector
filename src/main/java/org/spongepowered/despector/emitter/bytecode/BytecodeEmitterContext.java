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
package org.spongepowered.despector.emitter.bytecode;

import com.google.common.base.Throwables;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.AbstractEmitterContext;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.ConditionEmitter;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.StatementEmitter;

import java.io.IOException;
import java.io.OutputStream;

public class BytecodeEmitterContext extends AbstractEmitterContext {

    private final OutputStream out;

    private ClassWriter cw;
    private MethodVisitor mv;

    public BytecodeEmitterContext(OutputStream out) {
        this.out = out;
    }

    @SuppressWarnings("unchecked")
    public <T extends TypeEntry> void emitOuterType(T ast) {
        this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        AstEmitter<AbstractEmitterContext, T> emitter = (AstEmitter<AbstractEmitterContext, T>) this.set.getAstEmitter(ast.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for ast entry " + ast.getClass().getName());
        }
        emitter.emit(this, ast);
        this.cw.visitEnd();
        try {
            this.out.write(this.cw.toByteArray());
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AstEntry> void emitAst(T ast) {
        AstEmitter<AbstractEmitterContext, T> emitter =
                (AstEmitter<AbstractEmitterContext, T>) this.set.getAstEmitter(ast.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for ast entry " + ast.getClass().getName());
        }
        emitter.emit(this, ast);
    }

    /**
     * Emits the given statement.
     */
    @SuppressWarnings("unchecked")
    public <T extends Statement> BytecodeEmitterContext emitStatement(T obj) {
        StatementEmitter<AbstractEmitterContext, T> emitter =
                (StatementEmitter<AbstractEmitterContext, T>) this.set.getStatementEmitter(obj.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for statement " + obj.getClass().getName());
        }
        Statement last = getStatement();
        setStatement(obj);
        emitter.emit(this, obj, false);
        setStatement(last);
        return this;
    }

    /**
     * Emits the given instruction.
     */
    @SuppressWarnings("unchecked")
    public <T extends Instruction> BytecodeEmitterContext emitInstruction(T obj, TypeSignature type) {
        InstructionEmitter<AbstractEmitterContext, T> emitter =
                (InstructionEmitter<AbstractEmitterContext, T>) this.set.getInstructionEmitter(obj.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for instruction " + obj.getClass().getName());
        }
        this.insn_stack.push(obj);
        if (type == null) {
            type = obj.inferType();
        }
        emitter.emit(this, obj, type);
        this.insn_stack.pop();
        return this;
    }

    /**
     * Emits the given condition.
     */
    @SuppressWarnings("unchecked")
    public <T extends Condition> BytecodeEmitterContext emitCondition(T condition) {
        ConditionEmitter<AbstractEmitterContext, T> emitter =
                (ConditionEmitter<AbstractEmitterContext, T>) this.set.getConditionEmitter(condition.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for condition " + condition.getClass().getName());
        }
        emitter.emit(this, condition);
        return this;
    }

    public ClassWriter getClassWriter() {
        return this.cw;
    }

    public MethodVisitor getMethodVisitor() {
        return this.mv;
    }

    public void setMethodVisitor(MethodVisitor mv) {
        this.mv = mv;
    }

}

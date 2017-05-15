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
package org.spongepowered.despector.emitter.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.despector.ast.insn.condition.AndCondition;
import org.spongepowered.despector.ast.insn.condition.BooleanCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeIfEmitter implements StatementEmitter<BytecodeEmitterContext, If> {

    private Label start;
    private Label end;

    @Override
    public void emit(BytecodeEmitterContext ctx, If stmt, boolean semicolon) {
        MethodVisitor mv = ctx.getMethodVisitor();
        this.start = new Label();
        this.end = new Label();
        emit(ctx, stmt.getCondition());
        mv.visitLabel(this.start);
        for (Statement b : stmt.getBody()) {
            ctx.emitStatement(b);
        }
        mv.visitLabel(this.end);
    }

    private void emit(BytecodeEmitterContext ctx, Condition cond) {
        if (cond instanceof AndCondition) {
            AndCondition and = (AndCondition) cond;
            for (Condition op : and.getOperands()) {
                emitInverse(ctx, op);
            }
        }
    }

    private void emitInverse(BytecodeEmitterContext ctx, Condition cond) {
        MethodVisitor mv = ctx.getMethodVisitor();
        if (cond instanceof CompareCondition) {
            CompareCondition cmp = (CompareCondition) cond;
            ctx.emitInstruction(cmp.getLeft(), null);
            ctx.emitInstruction(cmp.getRight(), null);
            switch (cmp.getOperator()) {
            case NOT_EQUAL:
                mv.visitJumpInsn(Opcodes.IF_ACMPEQ, this.end);
                break;
            case EQUAL:
                mv.visitJumpInsn(Opcodes.IF_ACMPNE, this.end);
                break;
            default:
                throw new IllegalStateException("Unsupported compare operator: " + cmp.getOperator().name());
            }
        } else if(cond instanceof BooleanCondition) {
            BooleanCondition bool = (BooleanCondition) cond;
            ctx.emitInstruction(bool.getConditionValue(), null);
            if(bool.isInverse()) {
                mv.visitJumpInsn(Opcodes.IFNE, this.end);
            } else {
                mv.visitJumpInsn(Opcodes.IFEQ, this.end);
            }
        } else {
            throw new IllegalStateException();
        }
    }

}

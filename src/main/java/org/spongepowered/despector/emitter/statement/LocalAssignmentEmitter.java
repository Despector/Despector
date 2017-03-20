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
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.arg.operator.AddOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractOperator;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;

public class LocalAssignmentEmitter implements StatementEmitter<LocalAssignment> {

    @Override
    public void emit(EmitterContext ctx, LocalAssignment insn, boolean semicolon) {
        if (!insn.getLocal().getLocal().isParameter() && !ctx.isDefined(insn.getLocal())) {
            LocalInstance local = insn.getLocal();
            if (local.getSignature() != null) {
                GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
                generics.emitTypeSignature(ctx, local.getSignature());
            } else {
                ctx.emitTypeName(local.getTypeName());
            }
            ctx.printString(" ");
            ctx.markDefined(insn.getLocal());
        } else {
            // TODO replace with more generic handling from FieldAssign
            Instruction val = insn.getValue();
            if (val instanceof Cast) {
                val = ((Cast) val).getValue();
            } else if (val instanceof AddOperator) {
                AddOperator add = (AddOperator) val;
                if (add.getLeftOperand() instanceof LocalAccess) {
                    LocalAccess local = (LocalAccess) add.getLeftOperand();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        ctx.printString(insn.getLocal().getName());
                        if (add.getRightOperand() instanceof IntConstant) {
                            IntConstant right = (IntConstant) add.getRightOperand();
                            if (right.getConstant() == 1) {
                                ctx.printString("++");
                                if (semicolon)
                                    ctx.printString(";");
                                return;
                            } else if (right.getConstant() == -1) {
                                ctx.printString("--");
                                if (semicolon)
                                    ctx.printString(";");
                                return;
                            }
                        }
                        ctx.printString(" += ");
                        ctx.emit(add.getRightOperand(), null);
                        if (semicolon)
                            ctx.printString(";");
                        return;
                    }
                }
            } else if (val instanceof SubtractOperator) {
                SubtractOperator sub = (SubtractOperator) val;
                if (sub.getLeftOperand() instanceof LocalAccess) {
                    LocalAccess local = (LocalAccess) sub.getLeftOperand();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        ctx.printString(insn.getLocal().getName());
                        if (sub.getRightOperand() instanceof IntConstant) {
                            IntConstant right = (IntConstant) sub.getRightOperand();
                            if (right.getConstant() == 1) {
                                ctx.printString("--");
                                if (semicolon)
                                    ctx.printString(";");
                                return;
                            } else if (right.getConstant() == -1) {
                                ctx.printString("++");
                                if (semicolon)
                                    ctx.printString(";");
                                return;
                            }
                        }
                        ctx.printString(" += ");
                        ctx.emit(sub.getRightOperand(), local.getLocal().getType());
                        if (semicolon)
                            ctx.printString(";");
                        return;
                    }
                }
            }
        }
        ctx.printString(insn.getLocal().getName());
        ctx.printString(" = ");
        ctx.emit(insn.getValue(), insn.getLocal().getType());
        if (semicolon)
            ctx.printString(";");
    }

}

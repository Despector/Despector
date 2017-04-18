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
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;

/**
 * An emitter for local assignments.
 */
public class LocalAssignmentEmitter implements StatementEmitter<LocalAssignment> {

    @Override
    public void emit(EmitterContext ctx, LocalAssignment insn, boolean semicolon) {
        if (!insn.getLocal().getLocal().isParameter() && !ctx.isDefined(insn.getLocal())) {
            LocalInstance local = insn.getLocal();
            GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
            generics.emitTypeSignature(ctx, local.getType());
            ctx.printString(" ");
            ctx.markDefined(insn.getLocal());
        } else {
            Instruction val = insn.getValue();
            if (checkOperator(ctx, insn, val, semicolon)) {
                return;
            }
        }
        ctx.printString(insn.getLocal().getName());
        ctx.printString(" = ");
        ctx.emit(insn.getValue(), insn.getLocal().getType());
        if (semicolon) {
            ctx.printString(";");
        }
    }

    protected boolean checkOperator(EmitterContext ctx, LocalAssignment insn, Instruction val, boolean semicolon) {
        if (val instanceof Operator) {
            Instruction left = ((Operator) val).getLeftOperand();
            Instruction right = ((Operator) val).getRightOperand();
            String op = " " + ((Operator) val).getOperator().getSymbol() + "= ";
            if (left instanceof LocalAccess) {
                LocalAccess left_field = (LocalAccess) left;
                // If the field assign is of the form 'field = field + x'
                // where + is any operator then we collapse it to the '+='
                // form of the assignment.
                if (left_field.getLocal().getIndex() == insn.getLocal().getIndex()) {
                    ctx.printString(insn.getLocal().getName());
                    ctx.printString(op);
                    if ("Z".equals(insn.getLocal().getType())) {
                        if (val instanceof IntConstant) {
                            IntConstant cst = (IntConstant) insn.getValue();
                            if (cst.getConstant() == 1) {
                                ctx.printString("true");
                            } else {
                                ctx.printString("false");
                            }
                            if (semicolon) {
                                ctx.printString(";");
                            }
                            return true;
                        }
                    }
                    ctx.emit(right, insn.getLocal().getType());
                    if (semicolon) {
                        ctx.printString(";");
                    }
                    return true;
                }
            }
        }
        return false;
    }

}

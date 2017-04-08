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

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;

/**
 * An emitter for field assignments.
 */
public class FieldAssignmentEmitter implements StatementEmitter<FieldAssignment> {

    @Override
    public void emit(EmitterContext ctx, FieldAssignment insn, boolean semicolon) {
        if (insn.isInitializer()) {
            return;
        }
        if (insn instanceof StaticFieldAssignment) {
            if (ctx.getType() != null && !((StaticFieldAssignment) insn).getOwnerType().equals(ctx.getType().getDescriptor())) {
                ctx.emitTypeName(((StaticFieldAssignment) insn).getOwnerName());
                ctx.printString(".");
            }
        } else if (insn instanceof InstanceFieldAssignment) {
            ctx.emit(((InstanceFieldAssignment) insn).getOwner(), ClassTypeSignature.of(insn.getOwnerType()));
            ctx.printString(".");
        }

        ctx.printString(insn.getFieldName());
        Instruction val = insn.getValue();
        if (checkOperator(ctx, insn, val, semicolon)) {
            return;
        }
        ctx.printString(" = ");
        ctx.emit(val, ClassTypeSignature.of(insn.getFieldDescription()));
        if (semicolon) {
            ctx.printString(";");
        }
    }

    protected boolean checkOperator(EmitterContext ctx, FieldAssignment insn, Instruction val, boolean semicolon) {
        if (val instanceof Operator) {
            Instruction left = ((Operator) val).getLeftOperand();
            Instruction right = ((Operator) val).getRightOperand();
            String op = " " + ((Operator) val).getOperator() + "= ";
            if (left instanceof InstanceFieldAccess) {
                InstanceFieldAccess left_field = (InstanceFieldAccess) left;
                Instruction owner = left_field.getFieldOwner();
                if (owner instanceof LocalAccess && ((LocalAccess) owner).getLocal().getIndex() == 0) {
                    // If the field assign is of the form 'field = field + x'
                    // where + is any operator then we collapse it to the '+='
                    // form of the assignment.
                    if (left_field.getFieldName().equals(insn.getFieldName())) {
                        ctx.printString(op);
                        if (insn.getFieldDescription().equals("Z")) {
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
                        ctx.emit(right, ClassTypeSignature.of(insn.getFieldDescription()));
                        if (semicolon) {
                            ctx.printString(";");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

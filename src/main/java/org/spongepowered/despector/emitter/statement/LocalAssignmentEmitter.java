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
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class LocalAssignmentEmitter implements StatementEmitter<LocalAssignment> {

    @Override
    public void emit(EmitterOutput ctx, LocalAssignment insn) {
        ctx.append(new EmitterToken(TokenType.BEGIN_STATEMENT, insn));
        if (!insn.getLocal().getLocal().isParameter() && !ctx.isDefined(insn.getLocal())) {
            LocalInstance local = insn.getLocal();
            ctx.append(new EmitterToken(TokenType.TYPE, local.getType()));
            ctx.markDefined(insn.getLocal());
        } else {
            Instruction val = insn.getValue();
            if (checkOperator(ctx, insn, val)) {
                return;
            }
        }
        ctx.append(new EmitterToken(TokenType.NAME, insn.getLocal().getName()));
        ctx.append(new EmitterToken(TokenType.EQUALS, "="));
        ctx.emitInstruction(insn.getValue(), insn.getLocal().getType());
        ctx.append(new EmitterToken(TokenType.SPECIAL, ";"));
        ctx.append(new EmitterToken(TokenType.END_STATEMENT, insn));
    }

    protected boolean checkOperator(EmitterOutput ctx, LocalAssignment insn, Instruction val) {
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
                    ctx.append(new EmitterToken(TokenType.NAME, insn.getLocal().getName()));
                    ctx.append(new EmitterToken(TokenType.OPERATOR_EQUALS, op));
                    if ("Z".equals(insn.getLocal().getType())) {
                        if (val instanceof IntConstant) {
                            IntConstant cst = (IntConstant) insn.getValue();
                            if (cst.getConstant() == 1) {
                                ctx.append(new EmitterToken(TokenType.BOOLEAN, true));
                            } else {
                                ctx.append(new EmitterToken(TokenType.BOOLEAN, false));
                            }
                            ctx.append(new EmitterToken(TokenType.SPECIAL, ";"));
                            ctx.append(new EmitterToken(TokenType.END_STATEMENT, insn));
                            return true;
                        }
                    }
                    ctx.emitInstruction(right, insn.getLocal().getType());
                    ctx.append(new EmitterToken(TokenType.SPECIAL, ";"));
                    ctx.append(new EmitterToken(TokenType.END_STATEMENT, insn));
                    return true;
                }
            }
        }
        return false;
    }

}

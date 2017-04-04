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
package org.spongepowered.despector.emitter.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class TernaryEmitter implements InstructionEmitter<Ternary> {

    @Override
    public void emit(EmitterOutput ctx, Ternary ternary, TypeSignature type) {
        if (type == ClassTypeSignature.BOOLEAN && checkBooleanExpression(ctx, ternary)) {
            return;
        }
        if (ternary.getCondition() instanceof CompareCondition) {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            ctx.emitCondition(ternary.getCondition());
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        } else {
            ctx.emitCondition(ternary.getCondition());
        }
        ctx.append(new EmitterToken(TokenType.TERNARY_IF, "?"));
        ctx.emitInstruction(ternary.getTrueValue(), type);
        ctx.append(new EmitterToken(TokenType.TERNARY_ELSE, ":"));
        ctx.emitInstruction(ternary.getFalseValue(), type);
    }

    public boolean checkBooleanExpression(EmitterOutput ctx, Ternary ternary) {
        if (ternary.getTrueValue() instanceof IntConstant && ternary.getFalseValue() instanceof IntConstant) {
            int tr = ((IntConstant) ternary.getTrueValue()).getConstant();
            int fl = ((IntConstant) ternary.getFalseValue()).getConstant();
            if (tr == 0 && fl == 0) {
                ctx.append(new EmitterToken(TokenType.BOOLEAN, false));
            } else if (tr == 1 && fl == 1) {
                ctx.append(new EmitterToken(TokenType.BOOLEAN, true));
            } else if (tr == 0) {
                ctx.append(new EmitterToken(TokenType.OPERATOR, "!"));
                ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                ctx.emitCondition(ternary.getCondition());
                ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
            } else if (fl == 0) {
                ctx.emitCondition(ternary.getCondition());
            } else {
                return false;
            }
            return true;
        } else if (ternary.getTrueValue() instanceof IntConstant) {
            if (((IntConstant) ternary.getTrueValue()).getConstant() == 0) {
                // !a && b
                ctx.append(new EmitterToken(TokenType.OPERATOR, "!"));
                ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                ctx.emitCondition(ternary.getCondition());
                ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                ctx.append(new EmitterToken(TokenType.OPERATOR, "&&"));
                if(ternary.getFalseValue() instanceof Ternary) {
                    ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                    ctx.emitInstruction(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                    ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                } else {
                    ctx.emitInstruction(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                }
            } else {
                ctx.emitCondition(ternary.getCondition());
                ctx.append(new EmitterToken(TokenType.OPERATOR, "||"));
                if(ternary.getFalseValue() instanceof Ternary) {
                    ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                    ctx.emitInstruction(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                    ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                } else {
                    ctx.emitInstruction(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                }
            }
            return true;
        } else if (ternary.getFalseValue() instanceof IntConstant) {
            if (((IntConstant) ternary.getFalseValue()).getConstant() == 0) {
                // !a && b
                ctx.append(new EmitterToken(TokenType.OPERATOR, "!"));
                ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                ctx.emitCondition(ternary.getCondition());
                ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                ctx.append(new EmitterToken(TokenType.OPERATOR, "&&"));
                if(ternary.getTrueValue() instanceof Ternary) {
                    ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                    ctx.emitInstruction(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                    ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                } else {
                    ctx.emitInstruction(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                }
            } else {
                ctx.emitCondition(ternary.getCondition());
                ctx.append(new EmitterToken(TokenType.OPERATOR, "||"));
                if(ternary.getTrueValue() instanceof Ternary) {
                    ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                    ctx.emitInstruction(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                    ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                } else {
                    ctx.emitInstruction(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                }
            }
            return true;
        }
        return false;
    }

}

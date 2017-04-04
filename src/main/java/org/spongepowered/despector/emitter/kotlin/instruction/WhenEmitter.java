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
package org.spongepowered.despector.emitter.kotlin.instruction;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.kotlin.When;
import org.spongepowered.despector.ast.kotlin.When.Case;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOf;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

import java.util.List;

public class WhenEmitter implements InstructionEmitter<When> {

    private void emit(EmitterOutput ctx, Condition cond, LocalInstance local) {
        if (cond instanceof BooleanCondition) {
            Instruction val = ((BooleanCondition) cond).getConditionValue();
            if (val instanceof StaticMethodInvoke) {
                StaticMethodInvoke mth = (StaticMethodInvoke) val;
                if (mth.getMethodName().equals("areEqual") && mth.getOwner().equals("Lkotlin/jvm/internal/Intrinsics;")) {
                    ctx.emitInstruction(mth.getParams()[1], null);
                }
            } else if (val instanceof InstanceOf) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "is"));
                ctx.append(new EmitterToken(TokenType.TYPE, ((InstanceOf) val).getType()));
            } else {
                ctx.emitInstruction(val, ClassTypeSignature.BOOLEAN);
            }
        } else if (cond instanceof OrCondition) {
            List<Condition> operands = ((OrCondition) cond).getOperands();
            for (int i = 0; i < operands.size(); i++) {
                Condition arg = operands.get(i);
                ctx.append(new EmitterToken(TokenType.ARG_START, null));
                emit(ctx, arg, local);
            }
        } else {
            ctx.emitCondition(cond);
        }
    }

    @Override
    public void emit(EmitterOutput ctx, When arg, TypeSignature type) {
        ctx.append(new EmitterToken(TokenType.SPECIAL, "when"));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        ctx.emitInstruction(arg.getArg(), null);
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        for (Case cs : arg.getCases()) {
            emit(ctx, cs.getCondition(), arg.getLocal());
            ctx.append(new EmitterToken(TokenType.WHEN_CASE, "->"));
            if (cs.getBody().getStatementCount() == 0) {
                ctx.emitInstruction(cs.getLast(), arg.inferType());
            } else {
                ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
                ctx.emitBody(cs.getBody(), 0);
                ctx.emitInstruction(cs.getLast(), arg.inferType());
                ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
            }
        }
        ctx.append(new EmitterToken(TokenType.SPECIAL, "else"));
        ctx.append(new EmitterToken(TokenType.WHEN_CASE, "->"));
        if (arg.getElseBody().getStatementCount() == 0) {
            ctx.emitInstruction(arg.getElseBodyLast(), arg.inferType());
        } else {
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            ctx.emitBody(arg.getElseBody(), 0);
            ctx.emitInstruction(arg.getElseBodyLast(), arg.inferType());
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
    }

}

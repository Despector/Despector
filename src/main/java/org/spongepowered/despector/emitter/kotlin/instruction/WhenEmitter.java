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
import org.spongepowered.despector.ast.kotlin.When;
import org.spongepowered.despector.ast.kotlin.When.Case;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOf;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;

import java.util.List;

public class WhenEmitter implements InstructionEmitter<When> {

    private void emit(EmitterContext ctx, Condition cond, LocalInstance local) {
        if (cond instanceof BooleanCondition) {
            Instruction val = ((BooleanCondition) cond).getConditionValue();
            if (val instanceof StaticMethodInvoke) {
                StaticMethodInvoke mth = (StaticMethodInvoke) val;
                if (mth.getMethodName().equals("areEqual") && mth.getOwner().equals("Lkotlin/jvm/internal/Intrinsics;")) {
                    ctx.emit(mth.getParams()[1], null);
                }
            } else if(val instanceof InstanceOf) {
                ctx.printString("is ");
                KotlinEmitterUtil.emitType(ctx, ((InstanceOf) val).getType());
            } else {
                ctx.emit(val, "Z");
            }
        } else if (cond instanceof OrCondition) {
            List<Condition> operands = ((OrCondition) cond).getOperands();
            for (int i = 0; i < operands.size(); i++) {
                Condition arg = operands.get(i);
                emit(ctx, arg, local);
                if (i < operands.size() - 1) {
                    ctx.printString(", ");
                }
            }
        } else {
            ctx.emit(cond);
        }
    }

    @Override
    public void emit(EmitterContext ctx, When arg, String type) {
        ctx.printString("when (");
        ctx.emit(arg.getArg(), null);
        ctx.printString(") {");
        ctx.newLine();
        ctx.indent();
        ctx.printIndentation();
        for (Case cs : arg.getCases()) {
            emit(ctx, cs.getCondition(), arg.getLocal());
            ctx.printString(" -> ");
            if (cs.getBody().getStatementCount() == 0) {
                ctx.emit(cs.getLast(), arg.inferType());
                ctx.newLine();
                ctx.printIndentation();
            } else {
                ctx.printString("{");
                ctx.newLine();
                ctx.indent();
                ctx.printIndentation();
                ctx.emitBody(cs.getBody());
                ctx.emit(cs.getLast(), arg.inferType());
                ctx.newLine();
                ctx.dedent();
                ctx.printIndentation();
                ctx.printString("}");
                ctx.newLine();
                ctx.printIndentation();
            }
        }
        ctx.printString("else -> ");
        if (arg.getElseBody().getStatementCount() == 0) {
            ctx.emit(arg.getElseBodyLast(), arg.inferType());
            ctx.newLine();
        } else {
            ctx.printString("{");
            ctx.newLine();
            ctx.indent();
            ctx.printIndentation();
            ctx.emitBody(arg.getElseBody());
            ctx.emit(arg.getElseBodyLast(), arg.inferType());
            ctx.newLine();
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
            ctx.newLine();
        }
        ctx.dedent();
        ctx.printIndentation();
        ctx.printString("}");
    }

}

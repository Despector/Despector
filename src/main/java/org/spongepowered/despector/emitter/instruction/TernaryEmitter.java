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

import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstantArg;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.util.ConditionUtil;

public class TernaryEmitter implements InstructionEmitter<Ternary> {

    @Override
    public void emit(EmitterContext ctx, Ternary ternary, String type) {
        if ("Z".equals(type) && ternary.getTrueValue() instanceof IntConstantArg && ternary.getFalseValue() instanceof IntConstantArg) {
            // if the ternary contains simple boolean constants on both sides
            // then we can simplify it to simply be the condition
            IntConstantArg true_value = (IntConstantArg) ternary.getTrueValue();
            IntConstantArg false_value = (IntConstantArg) ternary.getFalseValue();
            if (true_value.getConstant() == 1 && false_value.getConstant() == 0) {
                ctx.emit(ternary.getCondition());
                return;
            } else if (true_value.getConstant() == 0 && false_value.getConstant() == 1) {
                ctx.emit(ConditionUtil.inverse(ternary.getCondition()));
                return;
            }
        }
        if (ternary.getCondition() instanceof CompareCondition) {
            ctx.printString("(");
            ctx.emit(ternary.getCondition());
            ctx.printString(")");
        } else {
            ctx.emit(ternary.getCondition());
        }
        ctx.printString(" ? ");
        ctx.emit(ternary.getTrueValue(), type);
        ctx.printString(" : ");
        ctx.emit(ternary.getFalseValue(), type);
    }

}

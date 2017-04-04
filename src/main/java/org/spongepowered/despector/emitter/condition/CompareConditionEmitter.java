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
package org.spongepowered.despector.emitter.condition;

import org.spongepowered.despector.ast.members.insn.arg.NumberCompare;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.emitter.ConditionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class CompareConditionEmitter implements ConditionEmitter<CompareCondition> {

    @Override
    public void emit(EmitterOutput ctx, CompareCondition compare) {
        if (compare.getLeft() instanceof NumberCompare) {
            NumberCompare cmp = (NumberCompare) compare.getLeft();
            ctx.emitInstruction(cmp.getLeftOperand(), null);
            ctx.append(new EmitterToken(TokenType.OPERATOR, compare.getOperator().asString()));
            ctx.emitInstruction(cmp.getRightOperand(), null);
            return;
        }
        ctx.emitInstruction(compare.getLeft(), null);
        ctx.append(new EmitterToken(TokenType.OPERATOR, compare.getOperator().asString()));
        ctx.emitInstruction(compare.getRight(), null);
    }

}

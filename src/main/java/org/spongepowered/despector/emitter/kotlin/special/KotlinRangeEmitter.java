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
package org.spongepowered.despector.emitter.kotlin.special;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.condition.AndCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;

public class KotlinRangeEmitter {

    public static final ConditionMatcher<?> RANGE_MATCHER = ConditionMatcher.and()
            .operand(ConditionMatcher.compare()
                    .left(InstructionMatcher.intConstant().build())
                    .build())
            .operand(ConditionMatcher.compare()
                    .right(InstructionMatcher.intConstant().build())
                    .build())
            .build();

    public static boolean checkRange(JavaEmitterContext ctx, Condition cond, boolean inverse) {
        if (!RANGE_MATCHER.matches(cond)) {
            return false;
        }
        AndCondition range = (AndCondition) cond;
        CompareCondition low = (CompareCondition) range.getOperands().get(0);
        CompareCondition high = (CompareCondition) range.getOperands().get(1);
        if (!low.getRight().equals(high.getLeft())) {
            return false;
        }
        ctx.emit(low.getRight(), null);
        ctx.printString(" ");
        ctx.printString("!", inverse);
        ctx.printString("in ");
        TypeSignature type = low.getRight().inferType();
        ctx.emit(low.getLeft(), type);
        ctx.printString("..");
        ctx.emit(high.getRight(), type);
        return true;
    }

    public static boolean checkRangeTernary(JavaEmitterContext ctx, Ternary ternary) {
        if (!(ternary.getFalseValue() instanceof IntConstant) || !(ternary.getTrueValue() instanceof IntConstant)) {
            return false;
        }
        int tr = ((IntConstant) ternary.getTrueValue()).getConstant();
        int fl = ((IntConstant) ternary.getFalseValue()).getConstant();
        if (tr == 0 && fl == 1) {
            return checkRange(ctx, ternary.getCondition(), true);
        } else if (tr == 1 && fl == 0) {
            return checkRange(ctx, ternary.getCondition(), false);
        }
        return false;
    }
}

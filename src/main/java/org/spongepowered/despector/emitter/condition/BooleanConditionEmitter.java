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

import org.spongepowered.despector.ast.members.insn.arg.InstanceOf;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.emitter.ConditionEmitter;
import org.spongepowered.despector.emitter.EmitterContext;

public class BooleanConditionEmitter implements ConditionEmitter<BooleanCondition> {

    @Override
    public void emit(EmitterContext ctx, BooleanCondition bool) {
        if (checkConstant(ctx, bool)) {
            return;
        }
        if (bool.isInverse()) {
            ctx.printString("!");
        }
        emit(ctx, bool.getConditionValue(), bool.isInverse());
    }

    protected void emit(EmitterContext ctx, Instruction val, boolean parens_needed) {
        if (parens_needed && val instanceof InstanceOf) {
            ctx.printString("(");
            ctx.emit(val, "Z");
            ctx.printString(")");
        } else {
            ctx.emit(val, "Z");
        }
    }

    protected boolean checkConstant(EmitterContext ctx, BooleanCondition bool) {
        Instruction val = bool.getConditionValue();
        if (val.inferType().equals("I")) {
            if (val instanceof IntConstant) {
                IntConstant cst = (IntConstant) val;
                if (cst.getConstant() == 0) {
                    ctx.printString("false");
                } else {
                    ctx.printString("true");
                }
                return true;
            }
            if (val instanceof Ternary) {
                Ternary ternary = (Ternary) val;
                if (ternary.getFalseValue() instanceof IntConstant && ternary.getFalseValue() instanceof IntConstant) {
                    int tr = ((IntConstant) ternary.getTrueValue()).getConstant();
                    int fl = ((IntConstant) ternary.getFalseValue()).getConstant();
                    if (tr == 0 && fl == 0) {
                        ctx.printString("false");
                    } else if (tr == 0) {
                        ctx.printString("!(");
                        ctx.emit(ternary.getCondition());
                        ctx.printString(")");
                    } else if (fl == 0) {
                        ctx.emit(ternary.getCondition());
                    } else {
                        ctx.printString("true");
                    }
                    return true;
                } else if (ternary.getTrueValue() instanceof IntConstant) {
                    if (((IntConstant) ternary.getTrueValue()).getConstant() == 0) {
                        // !a && b
                        ctx.printString("!(");
                        ctx.emit(ternary.getCondition());
                        ctx.printString(") && ");
                        ctx.emit(ternary.getFalseValue(), "Z");
                    } else {
                        ctx.emit(ternary.getCondition());
                        ctx.markWrapPoint();
                        ctx.printString(" || ");
                        ctx.emit(ternary.getFalseValue(), "Z");
                    }
                    return true;
                } else if (ternary.getFalseValue() instanceof IntConstant) {
                    if (((IntConstant) ternary.getFalseValue()).getConstant() == 0) {
                        // !a && b
                        ctx.printString("!(");
                        ctx.emit(ternary.getCondition());
                        ctx.printString(") || ");
                        ctx.emit(ternary.getFalseValue(), "Z");
                    } else {
                        ctx.emit(ternary.getCondition());
                        ctx.markWrapPoint();
                        ctx.printString(" && ");
                        ctx.emit(ternary.getFalseValue(), "Z");
                    }
                    return true;
                }
            }
            if (val instanceof Operator) {
                ctx.printString("(");
                ctx.emit(val, "I");
                ctx.printString(")");
            } else {
                ctx.emit(val, "I");
            }
            if (bool.isInverse()) {
                ctx.printString(" == 0");
            } else {
                ctx.printString(" != 0");
            }
            return true;
        }
        return false;
    }

}

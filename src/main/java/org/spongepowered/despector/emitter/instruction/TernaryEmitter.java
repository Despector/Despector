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
package org.spongepowered.despector.emitter.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;

/**
 * An emitter for ternary instructions.
 */
public class TernaryEmitter implements InstructionEmitter<Ternary> {

    @Override
    public void emit(EmitterContext ctx, Ternary ternary, TypeSignature type) {
        if (type == ClassTypeSignature.BOOLEAN && checkBooleanExpression(ctx, ternary)) {
            return;
        }
        if (ternary.getCondition() instanceof CompareCondition) {
            ctx.printString("(");
            ctx.emit(ternary.getCondition());
            ctx.printString(")");
        } else {
            ctx.emit(ternary.getCondition());
        }
        ctx.markWrapPoint();
        ctx.printString(" ? ");
        ctx.emit(ternary.getTrueValue(), type);
        ctx.markWrapPoint();
        ctx.printString(" : ");
        ctx.emit(ternary.getFalseValue(), type);
    }

    /**
     * Checks if the given ternary can be simplified to a boolean expression.
     */
    public boolean checkBooleanExpression(EmitterContext ctx, Ternary ternary) {
        if (ternary.getTrueValue() instanceof IntConstant && ternary.getFalseValue() instanceof IntConstant) {
            int tr = ((IntConstant) ternary.getTrueValue()).getConstant();
            int fl = ((IntConstant) ternary.getFalseValue()).getConstant();
            if (tr == 0 && fl == 0) {
                ctx.printString("false");
            } else if (tr == 1 && fl == 1) {
                ctx.printString("true");
            } else if (tr == 0) {
                ctx.printString("!");
                ctx.printString(" ", ctx.getFormat().insert_space_after_unary_operator);
                ctx.printString("(");
                ctx.emit(ternary.getCondition());
                ctx.printString(")");
            } else if (fl == 0) {
                ctx.emit(ternary.getCondition());
            } else {
                return false;
            }
            return true;
        } else if (ternary.getTrueValue() instanceof IntConstant) {
            if (((IntConstant) ternary.getTrueValue()).getConstant() == 0) {
                // !a && b
                ctx.printString("!");
                ctx.printString(" ", ctx.getFormat().insert_space_after_unary_operator);
                ctx.printString("(");
                ctx.emit(ternary.getCondition());
                ctx.printString(") && ");
                if (ternary.getFalseValue() instanceof Ternary) {
                    ctx.printString("(");
                    ctx.emit(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                    ctx.printString(")");
                } else {
                    ctx.emit(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                }
            } else {
                ctx.emit(ternary.getCondition());
                ctx.markWrapPoint();
                ctx.printString(" || ");
                if (ternary.getFalseValue() instanceof Ternary) {
                    ctx.printString("(");
                    ctx.emit(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                    ctx.printString(")");
                } else {
                    ctx.emit(ternary.getFalseValue(), ClassTypeSignature.BOOLEAN);
                }
            }
            return true;
        } else if (ternary.getFalseValue() instanceof IntConstant) {
            if (((IntConstant) ternary.getFalseValue()).getConstant() == 0) {
                // !a && b
                ctx.printString("!");
                ctx.printString(" ", ctx.getFormat().insert_space_after_unary_operator);
                ctx.printString("(");
                ctx.emit(ternary.getCondition());
                ctx.printString(") && ");
                if (ternary.getTrueValue() instanceof Ternary) {
                    ctx.printString("(");
                    ctx.emit(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                    ctx.printString(")");
                } else {
                    ctx.emit(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                }
            } else {
                ctx.emit(ternary.getCondition());
                ctx.markWrapPoint();
                ctx.printString(" || ");
                if (ternary.getTrueValue() instanceof Ternary) {
                    ctx.printString("(");
                    ctx.emit(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                    ctx.printString(")");
                } else {
                    ctx.emit(ternary.getTrueValue(), ClassTypeSignature.BOOLEAN);
                }
            }
            return true;
        }
        return false;
    }

}

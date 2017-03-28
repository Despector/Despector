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

import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.arg.operator.OperatorType;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;

public class KotlinOperatorEmitter implements InstructionEmitter<Operator> {

    public boolean needParens(OperatorType op) {
        switch (op) {
        case AND:
        case OR:
        case XOR:
        case SHIFT_LEFT:
        case SHIFT_RIGHT:
        case UNSIGNED_SHIFT_RIGHT:
            return true;
        case ADD:
        case SUBTRACT:
        case MULTIPLY:
        case DIVIDE:
        case REMAINDER:
        default:
            return false;
        }
    }

    public void emitOp(EmitterContext ctx, OperatorType op) {
        switch (op) {
        case AND:
            ctx.printString(" and ");
            break;
        case OR:
            ctx.printString(" or ");
            break;
        case XOR:
            ctx.printString(" xor ");
            break;
        case SHIFT_LEFT:
            ctx.printString(" shl ");
            break;
        case SHIFT_RIGHT:
            ctx.printString(" shr ");
            break;
        case UNSIGNED_SHIFT_RIGHT:
            ctx.printString(" ushr ");
            break;
        default:
            ctx.printString(" " + op.getSymbol() + " ");
            break;
        }
    }

    @Override
    public void emit(EmitterContext ctx, Operator arg, String type) {
        if (arg.getLeftOperand() instanceof Operator) {
            Operator right = (Operator) arg.getLeftOperand();
            if (arg.getOperator().getPrecedence() > right.getOperator().getPrecedence()) {
                ctx.printString("(");
            }
            ctx.emit(arg.getLeftOperand(), null);
            if (arg.getOperator().getPrecedence() > right.getOperator().getPrecedence()) {
                ctx.printString(")");
            }
        } else {
            ctx.emit(arg.getLeftOperand(), null);
        }
        ctx.markWrapPoint();

        emitOp(ctx, arg.getOperator());
        if (arg.getRightOperand() instanceof Operator) {
            Operator right = (Operator) arg.getRightOperand();
            if (arg.getOperator().getPrecedence() > right.getOperator().getPrecedence()) {
                ctx.printString("(");
            }
            ctx.emit(arg.getRightOperand(), null);
            if (arg.getOperator().getPrecedence() > right.getOperator().getPrecedence()) {
                ctx.printString(")");
            }
        } else {
            ctx.emit(arg.getRightOperand(), null);
        }
    }
}

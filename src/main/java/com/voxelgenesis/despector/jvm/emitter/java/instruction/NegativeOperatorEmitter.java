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
package com.voxelgenesis.despector.jvm.emitter.java.instruction;

import com.voxelgenesis.despector.core.ast.method.insn.operator.NegativeOperator;
import com.voxelgenesis.despector.core.ast.method.insn.operator.Operator;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.emitter.EmitterContext;
import com.voxelgenesis.despector.core.emitter.InstructionEmitter;

/**
 * An emitter for a unary negative operator.
 */
public class NegativeOperatorEmitter implements InstructionEmitter<NegativeOperator> {

    @Override
    public void emit(EmitterContext ctx, NegativeOperator arg, TypeSignature type) {
        ctx.printString("-");
        //TODO ctx.printString(" ", ctx.getFormat().insert_space_after_unary_operator);
        if (arg.getOperand() instanceof Operator) {
            ctx.printString("(");
            ctx.emitInstruction(arg.getOperand(), null);
            ctx.printString(")");
        } else {
            ctx.emitInstruction(arg.getOperand(), null);
        }
    }

}

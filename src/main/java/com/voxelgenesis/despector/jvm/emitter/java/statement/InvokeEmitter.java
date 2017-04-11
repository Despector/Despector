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
package com.voxelgenesis.despector.jvm.emitter.java.statement;

import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.method.invoke.InstanceMethodInvoke;
import com.voxelgenesis.despector.core.ast.method.invoke.InvokeStatement;
import com.voxelgenesis.despector.core.emitter.EmitterContext;
import com.voxelgenesis.despector.core.emitter.StatementEmitter;

/**
 * An emitter for invoke statements.
 */
public class InvokeEmitter implements StatementEmitter<InvokeStatement> {

    @Override
    public void emit(EmitterContext ctx, InvokeStatement insn, boolean semicolon) {
        Instruction i = insn.getInstruction();
        if (i instanceof InstanceMethodInvoke) {
            InstanceMethodInvoke mth = (InstanceMethodInvoke) i;
            if (mth.getMethodName().equals("<init>") && (mth.getParams().length == 0 || mth.getOwner().equals("Ljava/lang/Enum;"))) {
                return;
            }
        }
        ctx.emitInstruction(i, null);
        if (semicolon) {
            ctx.printString(";");
        }
    }

}

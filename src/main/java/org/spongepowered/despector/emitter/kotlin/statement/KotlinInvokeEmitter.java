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
package org.spongepowered.despector.emitter.kotlin.statement;

import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.InvokeStatement;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;

public class KotlinInvokeEmitter implements StatementEmitter<InvokeStatement> {

    @Override
    public void emit(EmitterOutput ctx, InvokeStatement insn) {
        Instruction i = insn.getInstruction();
        if (i instanceof InstanceMethodInvoke) {
            InstanceMethodInvoke mth = (InstanceMethodInvoke) i;
            if (mth.getMethodName().equals("<init>") && mth.getParams().length == 0) {
                return;
            }
        } else if(i instanceof StaticMethodInvoke) {
            StaticMethodInvoke mth = (StaticMethodInvoke) i;
            if("Lkotlin/jvm/internal/Intrinsics;".equals(mth.getOwner())) {
                return;
            }
        }
        ctx.emitInstruction(i, null);
    }

}

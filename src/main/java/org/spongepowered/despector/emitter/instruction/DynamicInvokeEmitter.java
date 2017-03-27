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

import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.function.DynamicInvokeHandle;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;

public class DynamicInvokeEmitter implements InstructionEmitter<DynamicInvokeHandle> {

    @Override
    public void emit(EmitterContext ctx, DynamicInvokeHandle arg, String type) {

        TypeEntry owner = ctx.getType().getSource().get(arg.getLambdaOwner());
        MethodEntry method = owner.getStaticMethod(arg.getLambdaMethod());
        StatementBlock block = method.getInstructions();

        ctx.printString("(");

        for (int i = 0; i < method.getParamTypes().size(); i++) {
            if (block == null) {
                ctx.printString("local" + i);
            } else {
                Local local = block.getLocals().getLocal(i);
                LocalInstance insn = local.getParameterInstance();
                ctx.printString(insn.getName());
            }
            if (i < method.getParamTypes().size() - 1) {
                ctx.printString(", ");
            }
        }

        ctx.printString(") -> ");
        if (block.getStatementCount() == 1) {
            Return ret = (Return) block.getStatement(0);
            ctx.emit(ret.getValue().get(), method.getReturnType());
            return;
        } else if (block.getStatementCount() == 2) {
            if (block.getStatement(1) instanceof Return && !((Return) block.getStatement(1)).getValue().isPresent()) {
                ctx.emit(block.getStatement(0), false);
                return;
            }
        }
        ctx.printString("{");
        ctx.newLine().indent();
        ctx.emitBody(block);
        ctx.newLine().dedent().printIndentation();
        ctx.printString("}");

    }

}

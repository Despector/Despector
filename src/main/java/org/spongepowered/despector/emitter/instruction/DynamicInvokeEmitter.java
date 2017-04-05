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
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.function.DynamicInvokeHandle;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class DynamicInvokeEmitter implements InstructionEmitter<DynamicInvokeHandle> {

    @Override
    public void emit(EmitterOutput ctx, DynamicInvokeHandle arg, TypeSignature type) {

        TypeEntry owner = ctx.getType().getSource().get(arg.getLambdaOwner());
        MethodEntry method = owner.getStaticMethodSafe(arg.getLambdaMethod());
        if (method == null) {
            method = owner.getMethodSafe(arg.getLambdaMethod());
            if (method == null) {
                throw new IllegalStateException();
            }
        }
        StatementBlock block = method.getInstructions();

        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));

        for (int i = 0; i < method.getParamTypes().size(); i++) {
            if (i > 0) {
                ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
            }
            if (block == null) {
                ctx.append(new EmitterToken(TokenType.NAME, "local" + i));
            } else {
                Local local = block.getLocals().getLocal(i);
                LocalInstance insn = local.getParameterInstance();
                ctx.append(new EmitterToken(TokenType.NAME, insn.getName()));
            }
        }

        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.LAMBDA, "->"));
        if (block.getStatementCount() == 1) {
            Return ret = (Return) block.getStatement(0);
            ctx.emitInstruction(ret.getValue().get(), method.getReturnType());
            return;
        } else if (block.getStatementCount() == 2) {
            if (block.getStatement(1) instanceof Return && !((Return) block.getStatement(1)).getValue().isPresent()) {
                ctx.emitStatement(block.getStatement(0));
                return;
            }
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        ctx.emitBody(block, 0);
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));

    }

}

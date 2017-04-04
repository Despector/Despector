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

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.emitter.statement.LocalAssignmentEmitter;

public class KotlinLocalAssignmentEmitter extends LocalAssignmentEmitter {

    @Override
    public void emit(EmitterOutput ctx, LocalAssignment insn) {
        if (!insn.getLocal().getLocal().isParameter() && !ctx.isDefined(insn.getLocal())) {
            if (insn.getLocal().isEffectivelyFinal()) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "val"));
            } else {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "var"));
            }
            ctx.append(new EmitterToken(TokenType.NAME, insn.getLocal().getName()));
            ctx.append(new EmitterToken(TokenType.SPECIAL, ":"));
            LocalInstance local = insn.getLocal();
            ctx.append(new EmitterToken(TokenType.TYPE, local.getType()));
            ctx.markDefined(insn.getLocal());
        } else {
            ctx.append(new EmitterToken(TokenType.NAME, insn.getLocal().getName()));
            if (checkOperator(ctx, insn, insn.getValue())) {
                return;
            }
        }
        ctx.append(new EmitterToken(TokenType.EQUALS, "="));
        ctx.emitInstruction(insn.getValue(), insn.getLocal().getType());
    }

}

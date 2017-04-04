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
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class TryCatchEmitter implements StatementEmitter<TryCatch> {

    @Override
    public void emit(EmitterOutput ctx, TryCatch try_block) {
        ctx.append(new EmitterToken(TokenType.SPECIAL, "try"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        ctx.emitBody(try_block.getTryBlock(), 0);
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        for (CatchBlock c : try_block.getCatchBlocks()) {
            ctx.append(new EmitterToken(TokenType.SPECIAL, "catch"));
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            for (int i = 0; i < c.getExceptions().size(); i++) {
                ctx.append(new EmitterToken(TokenType.TYPE, c.getExceptions().get(i)));
                ctx.append(new EmitterToken(TokenType.OPERATOR, "|"));
            }
            if (c.getExceptionLocal() != null) {
                ctx.append(new EmitterToken(TokenType.NAME, c.getExceptionLocal().getName()));
                ctx.markDefined(c.getExceptionLocal());
            } else {
                ctx.append(new EmitterToken(TokenType.NAME, c.getDummyName()));
            }
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            ctx.emitBody(c.getBlock(), 0);
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
    }

}

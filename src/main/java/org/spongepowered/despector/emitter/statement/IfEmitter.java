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

import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.If.Else;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class IfEmitter implements StatementEmitter<If> {

    @Override
    public void emit(EmitterOutput ctx, If insn) {
        ctx.append(new EmitterToken(TokenType.BEGIN_STATEMENT, insn));
        ctx.append(new EmitterToken(TokenType.IF, "if"));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        ctx.emitCondition(insn.getCondition());
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        if (!insn.getIfBody().getStatements().isEmpty()) {
            ctx.emitBody(insn.getIfBody(), 0);
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        Else else_ = insn.getElseBlock();
        for (int i = 0; i < insn.getElifBlocks().size(); i++) {
            Elif elif = insn.getElifBlocks().get(i);
            ctx.append(new EmitterToken(TokenType.ELSE_IF, "else if"));
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            ctx.emitCondition(elif.getCondition());
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            ctx.emitBody(elif.getBody(), 0);
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
        if (else_ != null) {
            StatementBlock else_block = else_.getElseBody();
            ctx.append(new EmitterToken(TokenType.ELSE, "else"));
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            if (!else_block.getStatements().isEmpty()) {
                ctx.emitBody(else_block, 0);
            }
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
        ctx.append(new EmitterToken(TokenType.END_STATEMENT, insn));
    }

}

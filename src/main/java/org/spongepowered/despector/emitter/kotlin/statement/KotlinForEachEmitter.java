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
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

public class KotlinForEachEmitter implements StatementEmitter<ForEach> {

    private static final StatementMatcher<ForEach> MAP_ITERATOR = MatchContext.storeLocal("loop_value", StatementMatcher.foreach()
            .value(InstructionMatcher.instanceinvoke()
                    .name("entrySet")
                    .desc("()Ljava/lang/Set;")
                    .build())
            .body(0, StatementMatcher.localassign()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("getKey")
                            .desc("()Ljava/lang/Object;")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("loop_value")
                                    .build())
                            .autoUnwrap()
                            .build())
                    .build())
            .body(1, StatementMatcher.localassign()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("getValue")
                            .desc("()Ljava/lang/Object;")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("loop_value")
                                    .build())
                            .autoUnwrap()
                            .build())
                    .build())
            .build());

    @Override
    public void emit(EmitterOutput ctx, ForEach loop) {
        if (checkMapIterator(ctx, loop)) {
            return;
        }
        ctx.append(new EmitterToken(TokenType.SPECIAL, "for"));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        LocalInstance local = loop.getValueAssignment();
        ctx.append(new EmitterToken(TokenType.NAME, local.getName()));
        ctx.append(new EmitterToken(TokenType.SPECIAL, "in"));
        ctx.emitInstruction(loop.getCollectionValue(), null);
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        if (!loop.getBody().getStatements().isEmpty()) {
            ctx.emitBody(loop.getBody(), 0);
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
    }

    public boolean checkMapIterator(EmitterOutput ctx, ForEach loop) {
        if (!MAP_ITERATOR.matches(MatchContext.create(), loop)) {
            return false;
        }
        LocalAssignment key_assign = (LocalAssignment) loop.getBody().getStatement(0);
        LocalAssignment value_assign = (LocalAssignment) loop.getBody().getStatement(1);
        Instruction collection = ((InstanceMethodInvoke) loop.getCollectionValue()).getCallee();

        ctx.append(new EmitterToken(TokenType.SPECIAL, "for"));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        ctx.append(new EmitterToken(TokenType.NAME, key_assign.getLocal().getName()));
        ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
        ctx.append(new EmitterToken(TokenType.NAME, value_assign.getLocal().getName()));
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.SPECIAL, "in"));
        ctx.emitInstruction(collection, null);
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        if (!loop.getBody().getStatements().isEmpty()) {
            ctx.emitBody(loop.getBody(), 2);
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));

        return true;
    }

}

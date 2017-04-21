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
package org.spongepowered.despector.emitter.kotlin.statement;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.statement.ForEmitter;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

/**
 * An emitter for kotlin for loops.
 */
public class KotlinForEmitter extends ForEmitter {

    private static final StatementMatcher<For> CHAR_ITERATOR = StatementMatcher.forloop()
            .init(MatchContext.storeLocal("loop_iterator",
                    StatementMatcher.localassign()
                            .value(InstructionMatcher.staticinvoke()
                                    .owner("Lkotlin/text/StringsKt;")
                                    .name("iterator")
                                    .desc("(Ljava/lang/CharSequence;)Lkotlin/collections/CharIterator;")
                                    .build())
                            .build()))
            .condition(ConditionMatcher.bool()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("hasNext")
                            .desc("()Z")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("loop_iterator")
                                    .build())
                            .build())
                    .build())
            .incr(StatementMatcher.NONE)
            .body(0, StatementMatcher.localassign()
                    .value(InstructionMatcher.instanceinvoke()
                            .owner("Lkotlin/collections/CharIterator;")
                            .name("nextChar")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("loop_iterator")
                                    .build())
                            .build())
                    .build())
            .build();

    @Override
    public void emit(JavaEmitterContext ctx, For loop, boolean semicolon) {
        if (checkCharIterator(ctx, loop)) {
            return;
        }
        super.emit(ctx, loop, semicolon);
    }

    /**
     * Checks if the given for loop is an iterator over characters in a string.
     */
    public boolean checkCharIterator(JavaEmitterContext ctx, For loop) {
        if (!CHAR_ITERATOR.matches(MatchContext.create(), loop)) {
            return false;
        }
        LocalInstance local = ((LocalAssignment) loop.getBody().getStatement(0)).getLocal();
        Instruction str = ((StaticMethodInvoke) ((LocalAssignment) loop.getInit()).getValue()).getParams()[0];
        ctx.printString("for (");
        ctx.printString(local.getName());
        ctx.printString(" in ");
        ctx.emit(InstructionMatcher.unwrapCast(str), ClassTypeSignature.STRING);
        ctx.printString(") {");
        ctx.newLine();
        ctx.indent();
        ctx.emitBody(loop.getBody(), 1);
        ctx.dedent();
        ctx.newLine();
        ctx.printIndentation();
        ctx.printString("}");
        return true;
    }

}

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
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

/**
 * An emitter for kotlin for-each loops.
 */
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
    public void emit(EmitterContext ctx, ForEach loop, boolean semicolon) {
        if (checkMapIterator(ctx, loop)) {
            return;
        }
        ctx.printString("for (");
        LocalInstance local = loop.getValueAssignment();
        ctx.printString(local.getName());
        ctx.printString(" in ");
        ctx.emit(loop.getCollectionValue(), null);
        ctx.printString(") {");
        ctx.newLine();
        if (!loop.getBody().getStatements().isEmpty()) {
            ctx.indent();
            ctx.emitBody(loop.getBody());
            ctx.dedent();
            ctx.newLine();
        }
        ctx.printIndentation();
        ctx.printString("}");
    }

    /**
     * Checks if the given for-each loop is over a map iterator.
     */
    public boolean checkMapIterator(EmitterContext ctx, ForEach loop) {
        if (!MAP_ITERATOR.matches(MatchContext.create(), loop)) {
            return false;
        }
        LocalAssignment key_assign = (LocalAssignment) loop.getBody().getStatement(0);
        LocalAssignment value_assign = (LocalAssignment) loop.getBody().getStatement(1);
        Instruction collection = ((InstanceMethodInvoke) loop.getCollectionValue()).getCallee();

        ctx.printString("for ((");
        ctx.printString(key_assign.getLocal().getName());
        ctx.printString(", ");
        ctx.printString(value_assign.getLocal().getName());

        ctx.printString(") in ");
        ctx.emit(collection, null);
        ctx.printString(") {");
        ctx.newLine();
        if (!loop.getBody().getStatements().isEmpty()) {
            ctx.indent();
            ctx.emitBody(loop.getBody(), 2);
            ctx.dedent();
            ctx.newLine();
        }
        ctx.printIndentation();
        ctx.printString("}");

        return true;
    }

}

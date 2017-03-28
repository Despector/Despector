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
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.StatementMatcher;

public class KotlinForEachEmitter implements StatementEmitter<ForEach> {

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

    public boolean checkMapIterator(EmitterContext ctx, ForEach loop) {
        InstanceMethodInvoke map = InstructionMatcher.requireInstanceMethodInvoke(loop.getCollectionValue(), "entrySet", "()Ljava/util/Set;");
        if (map == null) {
            return false;
        }
        if (loop.getBody().getStatementCount() <= 2) {
            return false;
        }
        Statement key = loop.getBody().getStatement(0);
        Statement value = loop.getBody().getStatement(1);
        LocalAssignment key_assign = StatementMatcher.requireLocalAssignment(key);
        if (key_assign == null) {
            return false;
        }
        LocalAssignment value_assign = StatementMatcher.requireLocalAssignment(value);
        if (value_assign == null) {
            return false;
        }
        InstanceMethodInvoke key_get = InstructionMatcher.requireInstanceMethodInvoke(InstructionMatcher.unwrapCast(key_assign.getValue()), "getKey",
                "()Ljava/lang/Object;");
        if (key_get == null || !(key_get.getCallee() instanceof LocalAccess)
                || ((LocalAccess) key_get.getCallee()).getLocal() != loop.getValueAssignment()) {
            return false;
        }
        InstanceMethodInvoke value_get = InstructionMatcher.requireInstanceMethodInvoke(InstructionMatcher.unwrapCast(value_assign.getValue()),
                "getValue", "()Ljava/lang/Object;");
        if (value_get == null || !(value_get.getCallee() instanceof LocalAccess)
                || ((LocalAccess) value_get.getCallee()).getLocal() != loop.getValueAssignment()) {
            return false;
        }

        ctx.printString("for ((");
        ctx.printString(key_assign.getLocal().getName());
        ctx.printString(", ");
        ctx.printString(value_assign.getLocal().getName());

        ctx.printString(") in ");
        ctx.emit(map.getCallee(), null);
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

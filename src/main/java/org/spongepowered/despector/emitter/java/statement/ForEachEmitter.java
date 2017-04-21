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
package org.spongepowered.despector.emitter.java.statement;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;

/**
 * An emitter for for-each loops.
 */
public class ForEachEmitter implements StatementEmitter<JavaEmitterContext, ForEach> {

    @Override
    public void emit(JavaEmitterContext ctx, ForEach loop, boolean semicolon) {
        ctx.printString("for");
        ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_for);
        ctx.printString("(");
        ctx.printString(" ", ctx.getFormat().insert_space_after_opening_paren_in_for);
        LocalInstance local = loop.getValueAssignment();
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        generics.emitTypeSignature(ctx, local.getType());
        ctx.printString(" ");
        ctx.printString(local.getName());
        ctx.printString(" ", ctx.getFormat().insert_space_before_colon_in_for);
        ctx.printString(":");
        ctx.printString(" ", ctx.getFormat().insert_space_after_colon_in_for);
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

}

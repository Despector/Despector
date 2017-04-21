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

import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

/**
 * An emitter for comments.
 */
public class CommentEmitter implements StatementEmitter<JavaEmitterContext, Comment> {

    @Override
    public void emit(JavaEmitterContext ctx, Comment stmt, boolean semicolon) {
        if (stmt.getCommentText().isEmpty()) {
            return;
        }
        if (stmt.getCommentText().size() == 1) {
            ctx.printString("// ");
            ctx.printString(stmt.getCommentText().get(0));
        } else {
            ctx.printString("/*");
            ctx.newLine();
            for (String line : stmt.getCommentText()) {
                ctx.printIndentation();
                ctx.printString(" * ");
                ctx.printString(line);
                ctx.newLine();
            }
            ctx.printIndentation();
            ctx.printString(" */");
        }
    }

}

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

import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.If.Elif;
import org.spongepowered.despector.ast.stmt.branch.If.Else;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

/**
 * An emitter for if statements.
 */
public class IfEmitter implements StatementEmitter<JavaEmitterContext, If> {

    @Override
    public void emit(JavaEmitterContext ctx, If insn, boolean semicolon) {
        ctx.printString("if");
        ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_if);
        ctx.printString("(");
        ctx.printString(" ", ctx.getFormat().insert_space_after_opening_paren_in_if);
        ctx.emit(insn.getCondition());
        ctx.printString(" ", ctx.getFormat().insert_space_before_closing_paren_in_if);
        ctx.printString(") {");
        ctx.newLine();
        if (!insn.getBody().getStatements().isEmpty()) {
            ctx.indent();
            ctx.emitBody(insn.getBody());
            ctx.dedent();
            ctx.newLine();
        }
        ctx.printIndentation();
        Else else_ = insn.getElseBlock();
        for (int i = 0; i < insn.getElifBlocks().size(); i++) {
            Elif elif = insn.getElifBlocks().get(i);
            ctx.printString("} else if");
            ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_if);
            ctx.printString("(");
            ctx.emit(elif.getCondition());
            ctx.printString(") {");
            ctx.newLine();
            ctx.indent();
            ctx.emitBody(elif.getBody());
            ctx.dedent();
            ctx.newLine();
            ctx.printIndentation();
        }
        if (else_ == null) {
            ctx.printString("}");
        } else {
            StatementBlock else_block = else_.getBody();
            if (ctx.getFormat().insert_new_line_before_else_in_if_statement) {
                ctx.printString("}");
                ctx.newIndentedLine();
                ctx.printString("else {");
            } else {
                ctx.printString("} else {");
            }
            ctx.newLine();
            if (!else_block.getStatements().isEmpty()) {
                ctx.indent();
                ctx.emitBody(else_block);
                ctx.dedent();
                ctx.newLine();
            }
            ctx.printIndentation();
            ctx.printString("}");
        }
    }

}

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
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.If.Else;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;

public class IfEmitter implements StatementEmitter<If> {

    @Override
    public void emit(EmitterContext ctx, If insn, boolean semicolon) {
        ctx.printString("if (");
        ctx.emit(insn.getCondition());
        ctx.printString(") {");
        ctx.newLine();
        if (!insn.getIfBody().getStatements().isEmpty()) {
            ctx.indent();
            ctx.emitBody(insn.getIfBody());
            ctx.dedent();
            ctx.newLine();
        }
        ctx.printIndentation();
        Else else_ = insn.getElseBlock();
        for (int i = 0; i < insn.getElifBlocks().size(); i++) {
            Elif elif = insn.getElifBlocks().get(i);
            ctx.printString("} else if (");
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
            StatementBlock else_block = else_.getElseBody();
            ctx.printString("} else {");
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

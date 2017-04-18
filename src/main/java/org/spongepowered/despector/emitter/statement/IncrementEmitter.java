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

import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;

/**
 * An emitter for increment statements.
 */
public class IncrementEmitter implements StatementEmitter<Increment> {

    @Override
    public void emit(EmitterContext ctx, Increment insn, boolean semicolon) {
        ctx.printString(insn.getLocal().getName());
        if (insn.getIncrementValue() == 1) {
            ctx.printString("++");
            if (semicolon) {
                ctx.printString(";");
            }
            return;
        } else if (insn.getIncrementValue() == -1) {
            ctx.printString("--");
            if (semicolon) {
                ctx.printString(";");
            }
            return;
        }
        ctx.printString(" += ");
        ctx.printString(String.valueOf(insn.getIncrementValue()));
        if (semicolon) {
            ctx.printString(";");
        }
    }

}

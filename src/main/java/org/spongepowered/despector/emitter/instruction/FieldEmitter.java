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
package org.spongepowered.despector.emitter.instruction;

import org.spongepowered.despector.ast.members.insn.arg.field.FieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldArg;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;

public class FieldEmitter implements InstructionEmitter<FieldArg> {

    @Override
    public void emit(EmitterContext ctx, FieldArg arg, String type) {
        if (arg instanceof StaticFieldArg) {
            if (ctx.getType() == null || !((StaticFieldArg) arg).getOwnerName().equals(ctx.getType().getName())) {
                ctx.emitTypeName(((StaticFieldArg) arg).getOwnerName());
                ctx.printString(".");
            }
            ctx.printString(arg.getFieldName());
        } else if (arg instanceof InstanceFieldArg) {
            ctx.emit(((InstanceFieldArg) arg).getFieldOwner(), arg.getOwner());
            ctx.printString(".");
            ctx.printString(arg.getFieldName());
        } else {
            throw new UnsupportedOperationException();
        }
    }

}

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
package org.spongepowered.despector.emitter.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.field.FieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldAccess;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An emitter for instance field accesses.
 */
public class FieldEmitter implements InstructionEmitter<FieldAccess> {

    @Override
    public void emit(EmitterContext ctx, FieldAccess arg, TypeSignature type) {
        if (arg instanceof StaticFieldAccess) {
            if (ctx.getType() == null || !((StaticFieldAccess) arg).getOwnerName().equals(ctx.getType().getName())) {
                ctx.emitTypeName(((StaticFieldAccess) arg).getOwnerName());
                ctx.printString(".");
            }
            ctx.printString(arg.getFieldName());
        } else if (arg instanceof InstanceFieldAccess) {
            if (TypeHelper.isAnonClass(arg.getOwnerName()) && arg.getFieldName().startsWith("val$")) {
                ctx.printString(arg.getFieldName().substring(4));
                return;
            }
            ctx.emit(((InstanceFieldAccess) arg).getFieldOwner(), ClassTypeSignature.of(arg.getOwnerType()));
            ctx.printString(".");
            ctx.printString(arg.getFieldName());
        } else {
            throw new UnsupportedOperationException();
        }
    }

}

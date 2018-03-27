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
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.statement.LocalAssignmentEmitter;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;

/**
 * An emitter for kotlin local assignments.
 */
public class KotlinLocalAssignmentEmitter extends LocalAssignmentEmitter {

    @Override
    public void emit(JavaEmitterContext ctx, LocalAssignment insn, boolean semicolon) {
        TypeSignature type = insn.getLocal().getType();
        if (type == null) {
            type = insn.getValue().inferType();
            insn.getLocal().setType(type);
        }
        if (!insn.getLocal().getLocal().isParameter() && !ctx.isDefined(insn.getLocal())) {
            if (insn.getLocal().isEffectivelyFinal()) {
                ctx.printString("val ");
            } else {
                ctx.printString("var ");
            }
            ctx.printString(insn.getLocal().getName());
            ctx.printString(": ");
            LocalInstance local = insn.getLocal();
            KotlinEmitterUtil.emitType(ctx, local.getType());
            ctx.markDefined(insn.getLocal());
        } else {
            ctx.printString(insn.getLocal().getName());
            if (checkOperator(ctx, insn, insn.getValue(), semicolon)) {
                return;
            }
        }
        ctx.printString(" = ");
        ctx.markWrapPoint();
        ctx.emit(insn.getValue(), insn.getLocal().getType());
    }

}

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
package org.spongepowered.despector.emitter.kotlin.condition;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.insn.condition.BooleanCondition;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.condition.BooleanConditionEmitter;

/**
 * An emitter for kotlin boolean conditions.
 */
public class KotlinBooleanConditionEmitter extends BooleanConditionEmitter {

    @Override
    public void emit(JavaEmitterContext ctx, BooleanCondition bool) {
        if (checkConstant(ctx, bool)) {
            return;
        }
        if (bool.getConditionValue() instanceof InstanceOf) {
            InstanceOf arg = (InstanceOf) bool.getConditionValue();
            ctx.emit(arg.getCheckedValue(), null);
            ctx.printString(" !is ");
            ctx.emitType(arg.getType().getDescriptor());
            return;
        }
        if (bool.isInverse()) {
            ctx.printString("!");
        }
        if (bool.isInverse() && bool.getConditionValue() instanceof InstanceOf) {
            ctx.printString("(");
            ctx.emit(bool.getConditionValue(), ClassTypeSignature.BOOLEAN);
            ctx.printString(")");
        } else {
            ctx.emit(bool.getConditionValue(), ClassTypeSignature.BOOLEAN);
        }
    }

}

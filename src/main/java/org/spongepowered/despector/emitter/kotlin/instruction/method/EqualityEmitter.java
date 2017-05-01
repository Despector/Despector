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
package org.spongepowered.despector.emitter.kotlin.instruction.method;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

/**
 * An emitter for kotlin equals methods.
 */
public class EqualityEmitter implements SpecialMethodEmitter<StaticMethodInvoke> {

    @Override
    public boolean emit(JavaEmitterContext ctx, StaticMethodInvoke arg, TypeSignature type) {
        if (arg.getParameters().length != 2) {
            return false;
        }
        ctx.emit(arg.getParameters()[0], null);
        ctx.printString(" == ");
        ctx.emit(arg.getParameters()[1], null);
        return true;
    }

}

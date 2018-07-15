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
package org.spongepowered.despector.emitter.ir;

import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.Emitter;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.parallel.Timing;

/**
 * A java source emitter.
 */
public class IREmitter implements Emitter<JavaEmitterContext> {

    @Override
    public void setup(JavaEmitterContext ctx) {
        ctx.setSemicolons(true);
        ctx.setEmitterSet(Emitters.IR_SET);
    }

    @Override
    public void emit(JavaEmitterContext ctx, TypeEntry type) {
        setup(ctx);
        long emitting_start = System.nanoTime();
        ctx.emitOuterType(type);
        Timing.time_emitting += System.nanoTime() - emitting_start;
    }

}

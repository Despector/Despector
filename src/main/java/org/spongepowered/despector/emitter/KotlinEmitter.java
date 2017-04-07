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
package org.spongepowered.despector.emitter;

import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.output.ImportManager;

public class KotlinEmitter implements Emitter {

    @Override
    public void setup(EmitterContext ctx) {
        ctx.setSemicolons(false);
        ctx.setEmitterSet(Emitters.KOTLIN_SET);
        ImportManager imports = ctx.getImportManager();
        imports.addImplicitImport("kotlin/");
        imports.addImplicitImport("kotlin/annotation/");
        imports.addImplicitImport("kotlin/collections/");
        imports.addImplicitImport("kotlin/comparisons/");
        imports.addImplicitImport("kotlin/io/");
        imports.addImplicitImport("kotlin/ranges/");
        imports.addImplicitImport("kotlin/sequences/");
        imports.addImplicitImport("kotlin/text/");
        imports.addImplicitImport("kotlin/jvm/");
    }

    @Override
    public void emit(EmitterContext ctx, TypeEntry type) {
        setup(ctx);
        ctx.emitOuterType(type);
    }

}

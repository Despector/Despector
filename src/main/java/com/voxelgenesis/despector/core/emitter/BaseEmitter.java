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
package com.voxelgenesis.despector.core.emitter;

import com.voxelgenesis.despector.core.ast.method.stmt.Statement;
import com.voxelgenesis.despector.core.ast.method.stmt.misc.Return;
import com.voxelgenesis.despector.core.ast.type.MethodEntry;
import com.voxelgenesis.despector.core.ast.type.SourceEntry;

public class BaseEmitter {

    private final EmitterSet set;

    public BaseEmitter(EmitterSet set) {
        this.set = set;
    }

    public EmitterSet getSet() {
        return this.set;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void emit(EmitterContext ctx, SourceEntry entry) {
        TypeEmitter emitter = this.set.getAstEmitter(entry.getClass());
        emitter.emit(ctx, entry);
    }

    public void emitMethodBody(EmitterContext ctx, MethodEntry entry) {
        for (int i = 0; i < entry.getStatements().size(); i++) {
            Statement stmt = entry.getStatements().get(i);
            if (i == entry.getStatements().size() - 1 && stmt instanceof Return && !((Return) stmt).getValue().isPresent()) {
                return;
            }
            if (i > 0) {
                ctx.newLine();
            }
            ctx.emitStatement(stmt, true);
        }
    }

}

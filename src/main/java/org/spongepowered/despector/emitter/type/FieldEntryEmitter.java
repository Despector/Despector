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
package org.spongepowered.despector.emitter.type;

import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class FieldEntryEmitter implements AstEmitter<FieldEntry> {

    @Override
    public boolean emit(EmitterOutput ctx, FieldEntry ast) {

        for (Annotation anno : ast.getAnnotations()) {
            ctx.emit(anno);
        }

        ctx.append(new EmitterToken(TokenType.ACCESS, ast.getAccessModifier()));
        if (ast.isStatic()) {
            ctx.append(new EmitterToken(TokenType.MODIFIER, "static"));
        }
        if (ast.isFinal()) {
            ctx.append(new EmitterToken(TokenType.MODIFIER, "final"));
        }
        ctx.append(new EmitterToken(TokenType.TYPE, ast.getType()));
        ctx.append(new EmitterToken(TokenType.NAME, ast.getName()));
        if (ast.getInitializer() != null) {
            ctx.append(new EmitterToken(TokenType.FIELD_INITIALIZER, null));
            ctx.emitInstruction(ast.getInitializer(), ast.getType());
        }
        return true;
    }

}

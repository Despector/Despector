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
package org.spongepowered.despector.emitter.special;

import org.objectweb.asm.Type;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

public class PackageInfoEmitter implements SpecialEmitter {

    public void emit(EmitterOutput ctx, InterfaceEntry info) {

        for (Annotation anno : info.getAnnotations()) {
            emit(ctx, anno);
        }

        String pkg = info.getName();
        int last = pkg.lastIndexOf('/');
        if (last != -1) {
            pkg = pkg.substring(0, last).replace('/', '.');
            ctx.append(new EmitterToken(TokenType.SPECIAL, "package"));
            ctx.append(new EmitterToken(TokenType.NAME, pkg));
            ctx.append(new EmitterToken(TokenType.STATEMENT_END, ";"));
        }
    }

    // TODO add some settings to the annotation emitter / emitter context to
    // disable import tracking

    private void emitValue(EmitterOutput ctx, Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_START, "{"));
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_END, "}"));
            } else if (list.size() == 1) {
                emitValue(ctx, list.get(0));
            } else {
                for (int i = 0; i < list.size(); i++) {
                    ctx.append(new EmitterToken(TokenType.ARG_START, null));
                    emitValue(ctx, list.get(i));
                }
            }
        } else if (value instanceof Type) {
            ctx.append(new EmitterToken(TokenType.NAME, ((Type) value).getInternalName().replace('/', '.')));
        } else {
            AnnotationEmitter.emitValue(ctx, value);
        }
    }

    public void emit(EmitterOutput ctx, Annotation annotation) {
        ctx.append(new EmitterToken(TokenType.SPECIAL, "@"));
        ctx.append(new EmitterToken(TokenType.NAME, TypeHelper.descToType(annotation.getType().getName()).replace('/', '.')));
        if (annotation.getKeys().isEmpty()) {
            return;
        } else if (annotation.getKeys().size() == 1 && "value".equals(annotation.getKeys().iterator().next())) {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            emitValue(ctx, annotation.getValue("value"));
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        } else {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            for (String key : annotation.getKeys()) {
                ctx.append(new EmitterToken(TokenType.ARG_START, null));
                ctx.append(new EmitterToken(TokenType.NAME, key));
                ctx.append(new EmitterToken(TokenType.EQUALS, "="));
                Object value = annotation.getValue(key);
                emitValue(ctx, value);
            }
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        }
    }

}

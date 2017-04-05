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
import org.spongepowered.despector.emitter.instruction.StringConstantEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnnotationEmitter implements SpecialEmitter {

    private static final Map<Class<?>, BiConsumer<EmitterOutput, Object>> value_emitters = new HashMap<>();

    static {
        value_emitters.put(Boolean.class, (ctx, value) -> ctx.append(new EmitterToken(TokenType.BOOLEAN, value)));
        value_emitters.put(Integer.class, (ctx, value) -> ctx.append(new EmitterToken(TokenType.INT, value)));
        value_emitters.put(int[].class, (ctx, value) -> {
            int[] values = (int[]) value;
            ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_START, "{"));
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                }
                ctx.append(new EmitterToken(TokenType.INT, values[i]));
            }
            ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_END, "}"));
        });
        value_emitters.put(ArrayList.class, (ctx, value) -> {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_START, "{"));
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_END, "}"));
            } else if (list.size() == 1) {
                emitValue(ctx, list.get(0));
            } else {
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_START, "{"));
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) {
                        ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                    }
                    emitValue(ctx, list.get(i));
                }
                ctx.append(new EmitterToken(TokenType.ARRAY_INITIALIZER_END, "}"));
            }
        });
        value_emitters.put(String.class, (ctx, value) -> {
            ctx.append(new EmitterToken(TokenType.STRING, StringConstantEmitter.escape((String) value)));
        });
        value_emitters.put(Type.class, (ctx, value) -> ctx.append(new EmitterToken(TokenType.TYPE, ((Type) value).getDescriptor())));
    }

    public static void emitValue(EmitterOutput ctx, Object value) {
        BiConsumer<EmitterOutput, Object> emitter = value_emitters.get(value.getClass());
        if (emitter == null) {
            throw new IllegalStateException("Unknown annotation value type in emitter: " + value.getClass().getName());
        }
        emitter.accept(ctx, value);
    }

    public void emit(EmitterOutput ctx, Annotation annotation) {
        ctx.append(new EmitterToken(TokenType.SPECIAL, "@"));
        ctx.append(new EmitterToken(TokenType.TYPE, annotation.getType().getName()));
        if (annotation.getKeys().isEmpty()) {
            return;
        } else if (annotation.getKeys().size() == 1 && "value".equals(annotation.getKeys().iterator().next())) {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            emitValue(ctx, annotation.getValue("value"));
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        } else {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            boolean first = true;
            for (String key : annotation.getKeys()) {
                if (!first) {
                    ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                } else {
                    first = false;
                }
                ctx.append(new EmitterToken(TokenType.NAME, key));
                ctx.append(new EmitterToken(TokenType.EQUALS, "="));
                emitValue(ctx, annotation.getValue(key));
            }
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        }
    }

}

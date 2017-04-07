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
package org.spongepowered.despector.emitter.special;

import org.objectweb.asm.Type;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.emitter.EmitterContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnnotationEmitter implements SpecialEmitter {

    private static final Map<Class<?>, BiConsumer<EmitterContext, Object>> value_emitters = new HashMap<>();

    static {
        value_emitters.put(Boolean.class, (ctx, value) -> ctx.printString(String.valueOf(value)));
        value_emitters.put(Integer.class, (ctx, value) -> ctx.printString(String.valueOf(value)));
        value_emitters.put(int[].class, (ctx, value) -> {
            int[] values = (int[]) value;
            ctx.printString("{");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    ctx.printString(", ");
                }
                ctx.printString(String.valueOf(values[i]));
            }
            ctx.printString("}");
        });
        value_emitters.put(ArrayList.class, (ctx, value) -> {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                ctx.printString("{}");
            } else if (list.size() == 1) {
                emitValue(ctx, list.get(0));
            } else {
                ctx.printString("{");
                for (int i = 0; i < list.size(); i++) {
                    if (i != 0) {
                        ctx.printString(", ");
                    }
                    emitValue(ctx, list.get(i));
                }
                ctx.printString("}");
            }
        });
        value_emitters.put(String.class, (ctx, value) -> {
            ctx.printString("\"");
            // TODO escape string
            ctx.printString((String) value);
            ctx.printString("\"");
        });
        value_emitters.put(Type.class, (ctx, value) -> ctx.emitTypeName(((Type) value).getInternalName()));
    }

    private static void emitValue(EmitterContext ctx, Object value) {
        BiConsumer<EmitterContext, Object> emitter = value_emitters.get(value.getClass());
        if (emitter == null) {
            throw new IllegalStateException("Unknown annotation value type in emitter: " + value.getClass().getName());
        }
        emitter.accept(ctx, value);
    }

    public void emit(EmitterContext ctx, Annotation annotation) {
        ctx.printString("@");
        ctx.emitType(annotation.getType().getName());
        if (annotation.getKeys().isEmpty()) {
            return;
        } else if (annotation.getKeys().size() == 1 && "value".equals(annotation.getKeys().iterator().next())) {
            ctx.printString("(");
            emitValue(ctx, annotation.getValue("value"));
            ctx.printString(" ", ctx.getFormat().insert_space_before_closing_paren_in_annotation);
            ctx.printString(")");
        } else {
            ctx.printString("(");
            boolean first = true;
            for (String key : annotation.getKeys()) {
                if (!first) {
                    ctx.printString(", ");
                }
                first = false;
                ctx.printString(key);
                ctx.printString(" = ");
                Object value = annotation.getValue(key);
                emitValue(ctx, value);
            }
            ctx.printString(" ", ctx.getFormat().insert_space_before_closing_paren_in_annotation);
            ctx.printString(")");
        }
    }

}

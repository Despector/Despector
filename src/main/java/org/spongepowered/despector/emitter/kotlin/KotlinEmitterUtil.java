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
package org.spongepowered.despector.emitter.kotlin;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

/**
 * A utility for the kotlin emitter.
 */
public final class KotlinEmitterUtil {

    /**
     * Emits the given type.
     */
    public static void emitParamType(JavaEmitterContext ctx, TypeSignature type) {
        if (type.isArray()) {
            ctx.printString("Array<");
            emitParamType(ctx, TypeSignature.getArrayComponent(type));
            ctx.printString(">");
        } else if (type == ClassTypeSignature.OBJECT) {
            ctx.printString("Any");
        } else {
            emitType(ctx, type);
        }
    }

    /**
     * Emits the given parameter type.
     */
    public static void emitParamType(JavaEmitterContext ctx, String type) {
        if ("Ljava/lang/Object;".equals(type)) {
            ctx.printString("Any");
        } else {
            emitType(ctx, type);
        }
    }

    /**
     * Emits the given type.
     */
    public static void emitType(JavaEmitterContext ctx, TypeSignature type) {
        if (type.isArray()) {
            ctx.printString("Array<");
            emitType(ctx, TypeSignature.getArrayComponent(type));
            ctx.printString(">");
        } else if (type == ClassTypeSignature.BYTE) {
            ctx.printString("Byte");
        } else if (type == ClassTypeSignature.SHORT) {
            ctx.printString("Short");
        } else if (type == ClassTypeSignature.INT || type == ClassTypeSignature.INTEGER_OBJECT) {
            ctx.printString("Int");
        } else if (type == ClassTypeSignature.LONG) {
            ctx.printString("Long");
        } else if (type == ClassTypeSignature.FLOAT) {
            ctx.printString("Float");
        } else if (type == ClassTypeSignature.DOUBLE) {
            ctx.printString("Double");
        } else if (type == ClassTypeSignature.BOOLEAN) {
            ctx.printString("Boolean");
        } else if (type == ClassTypeSignature.CHAR || type == ClassTypeSignature.CHARACTER_OBJECT) {
            ctx.printString("Char");
        } else {
            ctx.emitType(type);
        }
    }

    /**
     * Emits the given type.
     */
    public static void emitType(JavaEmitterContext ctx, String type) {
        if (type.startsWith("[")) {
            ctx.printString("Array<");
            emitType(ctx, type.substring(1));
            ctx.printString(">");
        } else if ("B".equals(type)) {
            ctx.printString("Byte");
        } else if ("S".equals(type)) {
            ctx.printString("Short");
        } else if ("I".equals(type) || "Ljava/lang/Integer;".equals(type)) {
            ctx.printString("Int");
        } else if ("J".equals(type)) {
            ctx.printString("Long");
        } else if ("F".equals(type)) {
            ctx.printString("Float");
        } else if ("D".equals(type)) {
            ctx.printString("Double");
        } else if ("Z".equals(type)) {
            ctx.printString("Boolean");
        } else if ("C".equals(type) || "Ljava/lang/Character;".equals(type)) {
            ctx.printString("Char");
        } else {
            ctx.emitType(type);
        }
    }

    private KotlinEmitterUtil() {
    }

}

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
package org.spongepowered.despector.ast.generic;

import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * A type signature which can be either a type variable or a concrete class or
 * void.
 */
public abstract class TypeSignature {

    /**
     * Gets if this signature has any type arguments.
     */
    public abstract boolean hasArguments();

    public abstract String getName();

    public abstract String getDescriptor();

    public boolean isArray() {
        return false;
    }

    public abstract void writeTo(MessagePacker pack) throws IOException;

    public static TypeSignature arrayOf(TypeSignature type) {
        if (type instanceof ClassTypeSignature) {
            ClassTypeSignature sig = (ClassTypeSignature) type;
            ClassTypeSignature array = ClassTypeSignature.of("[" + sig.getType());
            array.getArguments().addAll(sig.getArguments());
            return array;
        } else if (type instanceof TypeVariableSignature) {
            TypeVariableSignature sig = (TypeVariableSignature) type;
            TypeVariableSignature array = new TypeVariableSignature("[" + sig.getIdentifier());
            return array;
        }
        throw new IllegalStateException();
    }

    public static TypeSignature getArrayComponent(TypeSignature type) {
        if (type instanceof ClassTypeSignature) {
            ClassTypeSignature sig = (ClassTypeSignature) type;
            ClassTypeSignature array = ClassTypeSignature.of(sig.getType().substring(1), true);
            array.getArguments().addAll(sig.getArguments());
            return array;
        } else if (type instanceof TypeVariableSignature) {
            TypeVariableSignature sig = (TypeVariableSignature) type;
            TypeVariableSignature array = new TypeVariableSignature(sig.getIdentifier().substring(1));
            return array;
        }
        throw new IllegalStateException();
    }

}

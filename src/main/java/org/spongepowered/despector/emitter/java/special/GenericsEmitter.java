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
package org.spongepowered.despector.emitter.java.special;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.emitter.SpecialEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

import java.util.List;

/**
 * An emitter for various generic signatures.
 */
public class GenericsEmitter implements SpecialEmitter {

    /**
     * Emits the given {@link TypeParameter}.
     */
    public void emitTypeParameter(JavaEmitterContext ctx, TypeParameter param) {
        ctx.printString(param.getIdentifier());
        boolean had_superclass = false;
        superclass: if (param.getClassBound() != null) {
            if (param.getClassBound() instanceof ClassTypeSignature) {
                ClassTypeSignature cls = (ClassTypeSignature) param.getClassBound();
                if (cls.getDescriptor().equals("Ljava/lang/Object;")) {
                    break superclass;
                }
            } else if (param.getClassBound() instanceof GenericClassTypeSignature) {
                GenericClassTypeSignature cls = (GenericClassTypeSignature) param.getClassBound();
                if (cls.getDescriptor().equals("Ljava/lang/Object;")) {
                    break superclass;
                }
            }
            ctx.printString(" extends ");
            emitTypeSignature(ctx, param.getClassBound(), false);
            had_superclass = true;
        }
        for (TypeSignature ibound : param.getInterfaceBounds()) {
            if (!had_superclass) {
                ctx.printString(" extends ");
            } else {
                ctx.printString(" & ");
            }
            emitTypeSignature(ctx, ibound, false);
        }
    }

    /**
     * Emits the given type.
     */
    public void emitTypeSignature(JavaEmitterContext ctx, TypeSignature sig, boolean is_varargs) {
        if (sig instanceof TypeVariableSignature) {
            int array_depth = 0;
            String type = ((TypeVariableSignature) sig).getIdentifier();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
            }
            ctx.printString(type.substring(1, type.length() - 1));
            if (is_varargs && array_depth > 0) {
                array_depth--;
            }
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_opening_bracket_in_array_type_reference);
                ctx.printString("[");
                ctx.printString(" ", ctx.getFormat().insert_space_between_brackets_in_array_type_reference);
                ctx.printString("]");
            }
            if (is_varargs && array_depth >= 0) {
                ctx.printString("...");
            }
        } else if (sig instanceof ClassTypeSignature) {
            ClassTypeSignature cls = (ClassTypeSignature) sig;
            int array_depth = 0;
            String type = cls.getType();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
            }
            ctx.emitType(type);
            if (is_varargs && array_depth > 0) {
                array_depth--;
            }
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_opening_bracket_in_array_type_reference);
                ctx.printString("[");
                ctx.printString(" ", ctx.getFormat().insert_space_between_brackets_in_array_type_reference);
                ctx.printString("]");
            }
            if (is_varargs && array_depth >= 0) {
                ctx.printString("...");
            }
        } else if (sig instanceof GenericClassTypeSignature) {
            GenericClassTypeSignature cls = (GenericClassTypeSignature) sig;
            int array_depth = 0;
            String type = cls.getType();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
            }
            ctx.emitType(type);
            emitTypeArguments(ctx, cls.getArguments());
            if (is_varargs && array_depth > 0) {
                array_depth--;
            }
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_opening_bracket_in_array_type_reference);
                ctx.printString("[");
                ctx.printString(" ", ctx.getFormat().insert_space_between_brackets_in_array_type_reference);
                ctx.printString("]");
            }
            if (is_varargs && array_depth >= 0) {
                ctx.printString("...");
            }
        } else if (sig instanceof VoidTypeSignature) {
            ctx.printString("void");
        }
    }

    /**
     * Emits the given set of {@link TypeParameter}s.
     */
    public void emitTypeParameters(JavaEmitterContext ctx, List<TypeParameter> parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        ctx.printString("<");
        boolean first = true;
        for (TypeParameter param : parameters) {
            if (!first) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_method_invocation_arguments);
                ctx.printString(",");
                ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_method_invocation_arguments);
            }
            first = false;
            emitTypeParameter(ctx, param);
        }
        ctx.printString(">");

    }

    /**
     * Emits the given {@link TypeArgument}.
     */
    public void emitTypeArgument(JavaEmitterContext ctx, TypeArgument arg) {
        switch (arg.getWildcard()) {
        case NONE:
            emitTypeSignature(ctx, arg.getSignature(), false);
            break;
        case EXTENDS:
            ctx.printString("? extends ");
            emitTypeSignature(ctx, arg.getSignature(), false);
            break;
        case SUPER:
            ctx.printString("? super ");
            emitTypeSignature(ctx, arg.getSignature(), false);
            break;
        case STAR:
            ctx.printString("?");
            break;
        default:
            break;
        }
    }

    /**
     * Emits the given set of {@link TypeArgument}s.
     */
    public void emitTypeArguments(JavaEmitterContext ctx, List<TypeArgument> arguments) {
        if (arguments.isEmpty()) {
            return;
        }
        ctx.printString("<");
        boolean first = true;
        for (TypeArgument param : arguments) {
            if (!first) {
                ctx.printString(", ");
            }
            first = false;
            emitTypeArgument(ctx, param);
        }
        ctx.printString(">");
    }

    /**
     * Gets the length of the given {@link TypeSignature} when its emitted.
     */
    public static int getLength(JavaEmitterContext ctx, TypeSignature type) {
        if (type instanceof VoidTypeSignature) {
            return 4;
        }
        if (type instanceof ClassTypeSignature) {
            ClassTypeSignature cls = (ClassTypeSignature) type;
            return ctx.getType(cls.getDescriptor()).length();
        }
        if (type instanceof GenericClassTypeSignature) {
            GenericClassTypeSignature cls = (GenericClassTypeSignature) type;
            int length = ctx.getType(cls.getDescriptor()).length();
            if (!cls.getArguments().isEmpty()) {
                for (TypeArgument arg : cls.getArguments()) {
                    length += 2;
                    switch (arg.getWildcard()) {
                    case NONE:
                        length += getLength(ctx, arg.getSignature());
                        break;
                    case EXTENDS:
                        length += 10;
                        length += getLength(ctx, arg.getSignature());
                        break;
                    case SUPER:
                        length += 8;
                        length += getLength(ctx, arg.getSignature());
                        break;
                    case STAR:
                        length++;
                        break;
                    default:
                        break;
                    }
                }
            }
            return length;
        }
        if (type instanceof TypeVariableSignature) {
            return ((TypeVariableSignature) type).getIdentifierName().length();
        }
        return 0;
    }

}

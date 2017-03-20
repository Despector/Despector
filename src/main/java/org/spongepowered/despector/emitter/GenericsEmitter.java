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
package org.spongepowered.despector.emitter;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;

import java.util.List;

public class GenericsEmitter {

    public void emitTypeParameter(EmitterContext ctx, TypeParameter param) {
        ctx.printString(param.getIdentifier());
        superclass: if (param.getClassBound() != null) {
            if (param.getClassBound() instanceof ClassTypeSignature) {
                ClassTypeSignature cls = (ClassTypeSignature) param.getClassBound();
                if (cls.getTypeName().equals("java/lang/Object")) {
                    break superclass;
                }
            }
            ctx.printString(" extends ");
            emitTypeSignature(ctx, param.getClassBound());
        }
    }

    public void emitTypeSignature(EmitterContext ctx, TypeSignature sig) {
        if (sig instanceof TypeVariableSignature) {
            String desc = ((TypeVariableSignature) sig).getIdentifier();
            ctx.printString(desc.substring(1, desc.length() - 1));
        } else if (sig instanceof ClassTypeSignature) {
            ClassTypeSignature cls = (ClassTypeSignature) sig;
            ctx.emitTypeName(cls.getTypeName());
            emitTypeArguments(ctx, cls.getArguments());
        } else if(sig instanceof VoidTypeSignature) {
            ctx.printString("void");
        }
    }

    public void emitTypeParameters(EmitterContext ctx, List<TypeParameter> parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        ctx.printString("<");
        boolean first = true;
        for (TypeParameter param : parameters) {
            if (!first) {
                ctx.printString(", ");
            }
            first = false;
            emitTypeParameter(ctx, param);
        }
        ctx.printString(">");

    }

    public void emitTypeArgument(EmitterContext ctx, TypeArgument arg) {
        switch (arg.getWildcard()) {
        case NONE:
            emitTypeSignature(ctx, arg.getSignature());
            break;
        case EXTENDS:
            ctx.printString("? extends ");
            emitTypeSignature(ctx, arg.getSignature());
            break;
        case SUPER:
            ctx.printString("? super ");
            emitTypeSignature(ctx, arg.getSignature());
            break;
        case STAR:
            ctx.printString("?");
            break;
        default:
            break;
        }
    }

    public void emitTypeArguments(EmitterContext ctx, List<TypeArgument> arguments) {
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

}

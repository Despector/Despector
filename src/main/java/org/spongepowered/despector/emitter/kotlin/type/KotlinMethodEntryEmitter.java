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
package org.spongepowered.despector.emitter.kotlin.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.special.GenericsEmitter;

public class KotlinMethodEntryEmitter implements AstEmitter<MethodEntry> {

    public static boolean isOverriden(MethodEntry method) {
        if (method.getName().equals("toString") && method.getSignature().equals("()Ljava/lang/String;")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean emit(EmitterContext ctx, MethodEntry method) {

        boolean non_null = false;

        for (Annotation anno : method.getAnnotations()) {
            if ("Lorg/jetbrains/annotations/NotNull;".equals(anno.getType().getName())) {
                non_null = true;
                continue;
            }
            ctx.printIndentation();
            ctx.emit(anno);
            ctx.printString("\n");
        }

        ctx.setMethod(method);
        if (method.getName().equals("<clinit>")) {
            int start = 0;
            if (method.getInstructions() != null) {
                for (Statement stmt : method.getInstructions().getStatements()) {
                    if (!(stmt instanceof StaticFieldAssignment)) {
                        break;
                    }
                    StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
                    if (!assign.getOwnerName().equals(method.getOwner())) {
                        break;
                    }
                    start++;
                }
                // only need one less as we can ignore the return at the end
                if (start == method.getInstructions().getStatements().size() - 1) {
                    return false;
                }
            }
            ctx.printIndentation();
            ctx.printString("static {\n");
            ctx.indent();
            if (method.getInstructions() == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(method.getInstructions(), start);
            }
            ctx.printString("\n");
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2) {
            return false;
        }
        ctx.printIndentation();
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        MethodSignature sig = method.getMethodSignature();
        if ("<init>".equals(method.getName())) {
            String name = method.getOwnerName();
            name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('$')) + 1);
            ctx.printString(name);
        } else {
            if (isOverriden(method)) {
                ctx.printString("override ");
            }
            ctx.printString("fun ");
            ctx.printString(method.getName());
        }
        ctx.printString("(");
        StatementBlock block = method.getInstructions();
        for (int i = 0; i < method.getParamTypes().size(); i++) {
            int param_index = i;
            if (!method.isStatic()) {
                param_index++;
            }
            if (block == null) {
                if (sig != null) {
                    // interfaces have no lvt for parameters, need to get
                    // generic types from the method signature
                    generics.emitTypeSignature(ctx, sig.getParameters().get(i));
                } else {
                    ctx.emitType(method.getParamTypes().get(i));
                }
                ctx.printString(" ");
                ctx.printString("local" + param_index);
            } else {
                Local local = block.getLocals().getLocal(param_index);
                LocalInstance insn = local.getParameterInstance();
                if (insn.getSignature() != null) {
                    generics.emitTypeSignature(ctx, insn.getSignature());
                } else {
                    ctx.emitType(method.getParamTypes().get(i));
                }
                ctx.printString(" ");
                ctx.printString(insn.getName());
            }
            if (i < method.getParamTypes().size() - 1) {
                ctx.printString(", ");
            }
        }
        ctx.printString(")");
        if (!method.getReturnType().equals("V")) {
            ctx.printString(": ");
            if (sig != null) {
                if (!sig.getTypeParameters().isEmpty()) {
                    generics.emitTypeParameters(ctx, sig.getTypeParameters());
                    ctx.printString(" ");
                }
                generics.emitTypeSignature(ctx, sig.getReturnType());
            } else {
                ctx.emitType(method.getReturnType());
            }
        }
        if (!method.isAbstract()) {
            ctx.printString(" {\n");
            ctx.indent();
            if (block == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(block);
            }
            ctx.printString("\n");
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
        } else {
            ctx.printString(";");
        }
        ctx.setMethod(null);
        return true;
    }

    private static void printReturn(EmitterContext ctx, String type) {
        char f = type.charAt(0);
        if (f == 'V') {
            return;
        }
        ctx.printString("\n");
        ctx.printIndentation();
        switch (f) {
        case 'I':
        case 'S':
        case 'B':
        case 'C':
            ctx.printString("return 0;");
            break;
        case 'J':
            ctx.printString("return 0L;");
            break;
        case 'F':
            ctx.printString("return 0.0f;");
            break;
        case 'D':
            ctx.printString("return 0.0;");
            break;
        case 'Z':
            ctx.printString("return false;");
            break;
        case 'L':
            ctx.printString("return null;");
            break;
        default:
            throw new IllegalStateException("Malformed return type " + type);
        }
    }

}

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

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class MethodEntryEmitter implements AstEmitter<MethodEntry> {

    @Override
    public boolean emit(EmitterOutput ctx, MethodEntry method) {

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
            for (Annotation anno : method.getAnnotations()) {
                ctx.emit(anno);
            }
            ctx.append(new EmitterToken(TokenType.BEGIN_METHOD, "<clinit>"));
            ctx.append(new EmitterToken(TokenType.SPECIAL, "static"));
            if (method.getInstructions() == null) {
                ctx.append(new EmitterToken(TokenType.COMMENT, "Error decompiling block"));
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(method.getInstructions(), start);
            }
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
            ctx.append(new EmitterToken(TokenType.END_METHOD, "<clinit>"));
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2) {
            // TODO this could omit somewhere the ctor is purely passing
            // constants to the super ctor
            return false;
        }
        ctx.append(new EmitterToken(TokenType.BEGIN_METHOD, method));
        for (Annotation anno : method.getAnnotations()) {
            ctx.emit(anno);
        }
        if (!(ctx.getType() instanceof InterfaceEntry) && !(ctx.getType() instanceof EnumEntry && method.getName().equals("<init>"))) {
            ctx.append(new EmitterToken(TokenType.ACCESS, method.getAccessModifier()));
        }
        MethodSignature sig = method.getMethodSignature();
        if ("<init>".equals(method.getName())) {
            String name = method.getOwnerName();
            name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('$')) + 1);
            ctx.append(new EmitterToken(TokenType.NAME, name));
        } else {
            if (method.isStatic()) {
                ctx.append(new EmitterToken(TokenType.MODIFIER, "static"));
            }
            if (method.isFinal()) {
                ctx.append(new EmitterToken(TokenType.MODIFIER, "final"));
            }
            if (method.isAbstract() && !(ctx.getType() instanceof InterfaceEntry)) {
                ctx.append(new EmitterToken(TokenType.MODIFIER, "abstract"));
            }

            if (sig != null) {
                if (!sig.getTypeParameters().isEmpty()) {
                    ctx.append(new EmitterToken(TokenType.GENERIC_PARAMS, sig.getTypeParameters()));
                }
                ctx.append(new EmitterToken(TokenType.TYPE, sig.getReturnType()));
            } else {
                ctx.append(new EmitterToken(TokenType.TYPE, method.getReturnType()));
            }

            ctx.append(new EmitterToken(TokenType.NAME, method.getName()));
        }
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        StatementBlock block = method.getInstructions();
        int start = 0;
        if ("<init>".equals(method.getName()) && ctx.getType() instanceof EnumEntry) {
            // If this is an enum type then we skip the first two ctor
            // parameters
            // (which are the index and name of the enum constant)
            start += 2;
        }
        for (int i = start; i < method.getParamTypes().size(); i++) {
            int param_index = i;
            if (!method.isStatic()) {
                param_index++;
            }
            if (i > start) {
                ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
            }
            if (block == null) {
                if (sig != null) {
                    // interfaces have no lvt for parameters, need to get
                    // generic types from the method signature
                    ctx.append(new EmitterToken(TokenType.TYPE, sig.getParameters().get(i)));
                } else {
                    ctx.append(new EmitterToken(TokenType.TYPE, method.getParamTypes().get(i)));
                }
                ctx.append(new EmitterToken(TokenType.NAME, "local" + param_index));
            } else {
                Local local = block.getLocals().getLocal(param_index);
                LocalInstance insn = local.getParameterInstance();
                for (Annotation anno : insn.getAnnotations()) {
                    ctx.emit(anno);
                }
                ctx.append(new EmitterToken(TokenType.TYPE, insn.getType()));
                ctx.append(new EmitterToken(TokenType.NAME, insn.getName()));
            }
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        if (!method.isAbstract()) {
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            if (block == null) {
                ctx.append(new EmitterToken(TokenType.COMMENT, "Error decompiling block"));
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(block, 0);
            }
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
        ctx.append(new EmitterToken(TokenType.END_METHOD, method));
        return true;
    }

    protected static void printReturn(EmitterOutput ctx, TypeSignature type) {
        char f = type.getDescriptor().charAt(0);
        if (f == 'V') {
            return;
        }
        switch (f) {
        case 'I':
        case 'S':
        case 'B':
        case 'C':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.INT, 0));
            break;
        case 'J':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.LONG, 0));
            break;
        case 'F':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.FLOAT, 0));
            break;
        case 'D':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.DOUBLE, 0));
            break;
        case 'Z':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.BOOLEAN, false));
            break;
        case 'L':
            ctx.append(new EmitterToken(TokenType.RETURN, "return"));
            ctx.append(new EmitterToken(TokenType.RAW, "null"));
            break;
        default:
            throw new IllegalStateException("Malformed return type " + type);
        }
    }

}

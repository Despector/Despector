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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.emitter.type.ClassEntryEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

public class AnonymousClassEmitter implements SpecialEmitter {

    public void emit(EmitterOutput ctx, ClassEntry type, New new_insn) {
        checkArgument(type.isAnonType());

        TypeSignature actual_type = null;
        if (type.getSuperclassName().equals("java/lang/Object")) {
            if (type.getSignature() != null) {
                actual_type = type.getSignature().getInterfaceSignatures().get(0);
            } else {
                actual_type = ClassTypeSignature.of(type.getInterfaces().get(0));
            }
        } else {
            if (type.getSignature() != null) {
                actual_type = type.getSignature().getSuperclassSignature();
            } else {
                actual_type = ClassTypeSignature.of(type.getSuperclass());
            }
        }

        ctx.append(new EmitterToken(TokenType.SPECIAL, "new"));
        ctx.append(new EmitterToken(TokenType.TYPE, actual_type));

        int syn_field_count = 0;
        for (FieldEntry fld : type.getFields()) {
            if ((fld.getName().startsWith("val$") || fld.getName().startsWith("this$")) && fld.isSynthetic()) {
                syn_field_count++;
            }
        }

        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        List<String> param_types = TypeHelper.splitSig(new_insn.getCtorDescription());
        for (int i = 0; i < new_insn.getParameters().length - syn_field_count; i++) {
            if (i > 0) {
                ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
            }
            Instruction param = new_insn.getParameters()[i];
            ctx.emitInstruction(param, ClassTypeSignature.of(param_types.get(i)));
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));

        ClassEntryEmitter emitter = (ClassEntryEmitter) ctx.getEmitterSet().getAstEmitter(ClassEntry.class);
        emitter.emitStaticFields(ctx, type);
        emitter.emitStaticMethods(ctx, type);
        emitter.emitFields(ctx, type);
        emitMethods(ctx, type);

        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
    }

    public void emitMethods(EmitterOutput ctx, ClassEntry type) {
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                if (mth.getName().equals("<init>")) {
                    // TODO need to check if ctor has extra things that still
                    // need to be emitted?
                    continue;
                }
                ctx.emitMethod(mth);
            }
        }
    }

}

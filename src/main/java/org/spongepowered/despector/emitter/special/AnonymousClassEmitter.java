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
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.type.ClassEntryEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

public class AnonymousClassEmitter implements SpecialEmitter {

    public void emit(EmitterContext ctx, ClassEntry type, New new_insn) {
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

        ctx.printString("new ");
        ctx.emitType(actual_type);

        int syn_field_count = 0;
        for (FieldEntry fld : type.getFields()) {
            if (fld.getName().startsWith("val$") && fld.isSynthetic()) {
                syn_field_count++;
            }
        }

        ctx.printString("(");
        List<String> param_types = TypeHelper.splitSig(new_insn.getCtorDescription());
        for (int i = 0; i < new_insn.getParameters().length - syn_field_count; i++) {
            Instruction param = new_insn.getParameters()[i];
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
            if (i < new_insn.getParameters().length - 1) {
                ctx.printString(", ");
            }
        }
        ctx.printString(") {");
        ctx.newLine();
        ctx.indent();
        ctx.indent();

        ClassEntryEmitter emitter = (ClassEntryEmitter) ctx.getEmitterSet().getAstEmitter(ClassEntry.class);
        emitter.emitStaticFields(ctx, type);
        emitter.emitStaticMethods(ctx, type);
        emitter.emitFields(ctx, type);
        emitMethods(ctx, type);

        ctx.dedent();
        ctx.dedent();
        ctx.printIndentation();
        ctx.printString("}");
    }

    public void emitMethods(EmitterContext ctx, ClassEntry type) {
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
                if (ctx.emit(mth)) {
                    ctx.newLine();
                    ctx.newLine();
                }
            }
        }
    }

}

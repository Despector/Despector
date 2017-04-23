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
package org.spongepowered.despector.emitter.java.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.AnonymousClassEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * An emitter for a new instruction.
 */
public class NewEmitter implements InstructionEmitter<JavaEmitterContext, New> {

    @Override
    public void emit(JavaEmitterContext ctx, New arg, TypeSignature type) {

        if (arg.getType().getName().contains("$")) {
            String last = arg.getType().getName();
            int last_inner_class = last.lastIndexOf('$');
            last = last.substring(last_inner_class + 1);
            if (last.matches("[0-9]+")) {
                TypeEntry anon_type = ctx.getType().getSource().get(arg.getType().getName());
                if (anon_type != null) {
                    AnonymousClassEmitter emitter = ctx.getEmitterSet().getSpecialEmitter(AnonymousClassEmitter.class);
                    emitter.emit(ctx, (ClassEntry) anon_type, arg);
                    return;
                }
                System.err.println("Missing TypeEntry for anon type " + arg.getType());
            }
        }

        ctx.printString("new ");
        ctx.emitType(arg.getType(), false);

        if (ctx.getField() != null && ctx.getField().getType().hasArguments()) {
            ctx.printString("<>");
        } else if (ctx.getStatement() != null && ctx.getStatement() instanceof LocalAssignment) {
            LocalAssignment assign = (LocalAssignment) ctx.getStatement();
            TypeSignature sig = assign.getLocal().getType();
            if (sig != null && sig instanceof GenericClassTypeSignature && !((GenericClassTypeSignature) sig).getArguments().isEmpty()) {
                ctx.printString("<>");
            }
        }

        ctx.printString("(");
        List<String> args = TypeHelper.splitSig(arg.getCtorDescription());
        for (int i = 0; i < arg.getParameters().length; i++) {
            Instruction param = arg.getParameters()[i];
            ctx.markWrapPoint();
            ctx.emit(param, ClassTypeSignature.of(args.get(i)));
            if (i < arg.getParameters().length - 1) {
                ctx.printString(", ");
            }
        }
        ctx.printString(")");
    }

}

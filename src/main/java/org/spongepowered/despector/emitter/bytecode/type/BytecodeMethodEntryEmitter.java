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
package org.spongepowered.despector.emitter.bytecode.type;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeMethodEntryEmitter implements AstEmitter<BytecodeEmitterContext, MethodEntry> {

    @Override
    public boolean emit(BytecodeEmitterContext ctx, MethodEntry ast) {
        int acc = 0;
        switch (ast.getAccessModifier()) {
        case PUBLIC:
            acc |= ACC_PUBLIC;
            break;
        case PRIVATE:
            acc |= ACC_PRIVATE;
            break;
        case PROTECTED:
            acc |= ACC_PROTECTED;
            break;
        default:
        }
        if (ast.isStatic()) {
            acc |= ACC_STATIC;
        }
        if (ast.isAbstract()) {
            acc |= ACC_ABSTRACT;
        }
        if (ast.isBridge()) {
            acc |= ACC_BRIDGE;
        }
        if (ast.isDeprecated()) {
            acc |= ACC_DEPRECATED;
        }
        if (ast.isFinal()) {
            acc |= ACC_FINAL;
        }
        if (ast.isNative()) {
            acc |= ACC_NATIVE;
        }
        if (ast.isStrictFp()) {
            acc |= ACC_STRICT;
        }
        if (ast.isSynchronized()) {
            acc |= ACC_SYNCHRONIZED;
        }
        if (ast.isSynthetic()) {
            acc |= ACC_SYNTHETIC;
        }
        if (ast.isVarargs()) {
            acc |= ACC_VARARGS;
        }
        MethodVisitor mv = ctx.getClassWriter().visitMethod(acc, ast.getName(), ast.getDescription(), null, null);
        ctx.setMethodVisitor(mv);
        mv.visitCode();
        Label start = new Label();
        mv.visitLabel(start);
        for (Statement stmt : ast.getInstructions().getStatements()) {
            ctx.emitStatement(stmt);
        }
        Label end = new Label();
        mv.visitLabel(end);

        // TODO the indices we have for locals are in terms of the ir list and
        // are therefore useless now
        for (LocalInstance local : ast.getLocals().getAllInstances()) {
            if (local.getType().getDescriptor().startsWith("L")) {
                mv.visitLocalVariable(local.getName(), local.getTypeName(), local.getType().toString(), start, end, local.getIndex());
            } else {
                mv.visitLocalVariable(local.getName(), local.getTypeName(), null, start, end, local.getIndex());
            }
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return true;
    }

}

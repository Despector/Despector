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
package org.spongepowered.despector.emitter.bytecode.instruction;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeLocalAccessEmitter implements InstructionEmitter<BytecodeEmitterContext, LocalAccess> {

    @Override
    public void emit(BytecodeEmitterContext ctx, LocalAccess arg, TypeSignature t) {
        MethodVisitor mv = ctx.getMethodVisitor();
        TypeSignature type = arg.getLocal().getType();
        if (type == ClassTypeSignature.INT || type == ClassTypeSignature.BOOLEAN || type == ClassTypeSignature.BYTE
                || type == ClassTypeSignature.SHORT || type == ClassTypeSignature.CHAR) {
            mv.visitVarInsn(Opcodes.ILOAD, arg.getLocal().getIndex());
        } else if (type == ClassTypeSignature.LONG) {
            mv.visitVarInsn(Opcodes.LLOAD, arg.getLocal().getIndex());
        } else if (type == ClassTypeSignature.FLOAT) {
            mv.visitVarInsn(Opcodes.FLOAD, arg.getLocal().getIndex());
        } else if (type == ClassTypeSignature.DOUBLE) {
            mv.visitVarInsn(Opcodes.DLOAD, arg.getLocal().getIndex());
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, arg.getLocal().getIndex());
        }
        ctx.updateStack(1);
    }

}

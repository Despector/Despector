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
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeCastEmitter implements InstructionEmitter<BytecodeEmitterContext, Cast> {

    @Override
    public void emit(BytecodeEmitterContext ctx, Cast arg, TypeSignature type) {
        MethodVisitor mv = ctx.getMethodVisitor();
        ctx.emitInstruction(arg.getValue(), null);
        TypeSignature to = arg.getType();
        TypeSignature from = arg.getValue().inferType();
        if (to == ClassTypeSignature.INT) {
            if (from == ClassTypeSignature.LONG) {
                mv.visitInsn(Opcodes.L2I);
            } else if (from == ClassTypeSignature.FLOAT) {
                mv.visitInsn(Opcodes.F2I);
            } else if (from == ClassTypeSignature.DOUBLE) {
                mv.visitInsn(Opcodes.D2I);
            }
        } else if (to == ClassTypeSignature.LONG) {
            if (from == ClassTypeSignature.INT) {
                mv.visitInsn(Opcodes.I2L);
            } else if (from == ClassTypeSignature.FLOAT) {
                mv.visitInsn(Opcodes.F2L);
            } else if (from == ClassTypeSignature.DOUBLE) {
                mv.visitInsn(Opcodes.D2L);
            }
        } else if (to == ClassTypeSignature.FLOAT) {
            if (from == ClassTypeSignature.LONG) {
                mv.visitInsn(Opcodes.L2F);
            } else if (from == ClassTypeSignature.INT) {
                mv.visitInsn(Opcodes.I2F);
            } else if (from == ClassTypeSignature.DOUBLE) {
                mv.visitInsn(Opcodes.D2F);
            }
        } else if (to == ClassTypeSignature.DOUBLE) {
            if (from == ClassTypeSignature.LONG) {
                mv.visitInsn(Opcodes.L2D);
            } else if (from == ClassTypeSignature.FLOAT) {
                mv.visitInsn(Opcodes.F2D);
            } else if (from == ClassTypeSignature.INT) {
                mv.visitInsn(Opcodes.I2D);
            }
        }
        mv.visitTypeInsn(Opcodes.CHECKCAST, to.getDescriptor());
    }

}

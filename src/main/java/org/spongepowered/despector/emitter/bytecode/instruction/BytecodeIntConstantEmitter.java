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
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeIntConstantEmitter implements InstructionEmitter<BytecodeEmitterContext, IntConstant> {

    @Override
    public void emit(BytecodeEmitterContext ctx, IntConstant arg, TypeSignature type) {
        MethodVisitor mv = ctx.getMethodVisitor();
        int val = arg.getConstant();
        if (val == -1) {
            mv.visitInsn(Opcodes.ICONST_M1);
        } else if (val == 0) {
            mv.visitInsn(Opcodes.ICONST_0);
        } else if (val == 1) {
            mv.visitInsn(Opcodes.ICONST_1);
        } else if (val == 2) {
            mv.visitInsn(Opcodes.ICONST_2);
        } else if (val == 3) {
            mv.visitInsn(Opcodes.ICONST_3);
        } else if (val == 4) {
            mv.visitInsn(Opcodes.ICONST_4);
        } else if (val == 5) {
            mv.visitInsn(Opcodes.ICONST_5);
        } else if (val <= Byte.MAX_VALUE && val >= Byte.MIN_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, val);
        } else if (val <= Short.MAX_VALUE && val >= Short.MIN_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, val);
        } else {
            mv.visitLdcInsn(val);
        }

    }

}

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
package org.spongepowered.test.kotlin;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.test.util.KotlinTestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class RangeTests {


    @Test
    public void testIfRange() {
        TestMethodBuilder builder = new TestMethodBuilder("decimalDigitValue", "(C)I");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(BIPUSH, 48);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IF_ICMPGT, l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitIntInsn(BIPUSH, 57);
        mv.visitJumpInsn(IF_ICMPGT, l1);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(l2);
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Out of range");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Throwable");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l3);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitIntInsn(BIPUSH, 48);
        mv.visitInsn(ISUB);
        mv.visitLabel(end);
        mv.visitInsn(IRETURN);
        mv.visitLocalVariable("c", "C", null, start, end, 0);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "decimalDigitValue");
        String good = "fun decimalDigitValue(c: Char): Int {\n"
                + "    if (c !in '0'..'9') {\n"
                + "        throw IllegalArgumentException(\"Out of range\")\n"
                + "    }\n\n"
                + "    return c - 48\n"
                + "}";
        Assert.assertEquals(good, insn);
    }
}

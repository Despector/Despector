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

public class StringTests {

    @Test
    public void testSimpleStringTemplate() {
        TestMethodBuilder builder = new TestMethodBuilder("main", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitInsn(ICONST_5);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("a is ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("s", "Ljava/lang/String;", null, start, end, 2);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "main");
        String good = "fun main() {\n"
                + "    val a: Int = 5\n"
                + "    val s: String = \"a is $a\"\n"
                + "}";
        Assert.assertEquals(good, insn);
    }
}

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
package org.spongepowered.test.decompile;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class MethodTests {

    @BeforeClass
    public static void setup() {
        LibraryConfiguration.quiet = false;
        LibraryConfiguration.parallel = false;
    }

    public static void test_mth() {
        int[][] a = new int[5][6];
    }

    @Test
    public void testStaticInvoke() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        Label end = new Label();
        mv.visitLabel(start);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Hello World!");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "System.out.println(\"Hello World!\");";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testMultiNewArray() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        Label l1 = new Label();
        Label end = new Label();
        mv.visitLabel(start);
        mv.visitInsn(ICONST_5);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitMultiANewArrayInsn("[[I", 2);
        mv.visitVarInsn(ASTORE, 0);
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        mv.visitLabel(end);
        mv.visitLocalVariable("a", "[[I", null, l1, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "int[][] a = new int[5][6];";
        Assert.assertEquals(good, insn);
    }
}

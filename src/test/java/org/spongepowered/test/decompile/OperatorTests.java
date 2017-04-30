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
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class OperatorTests {

    @Test
    public void testIntConstant() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(BIPUSH, 65);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = 65;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTypeConstant() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
        mv.visitIntInsn(ASTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "Ljava/lang/Class;", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = String.class;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNegative() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(II)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitInsn(INEG);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = -a;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testAdd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(IADD);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a + b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testSub() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(ISUB);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a - b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testMultiply() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(IMUL);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a * b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testDiv() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(IDIV);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a / b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testRem() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(IREM);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a % b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testShr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(ISHR);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a >> b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testUshr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(IUSHR);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a >>> b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testShl() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(III)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitIntInsn(ILOAD, 1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitInsn(ISHL);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "I", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a << b;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testFloatCompare() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZFF)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitIntInsn(FLOAD, 1);
        mv.visitIntInsn(FLOAD, 2);
        mv.visitInsn(FCMPG);
        mv.visitJumpInsn(IFGT, l1);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l2);
        mv.visitIntInsn(ISTORE, 0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "Z", null, start, end, 0);
        mv.visitLocalVariable("a", "F", null, start, end, 1);
        mv.visitLocalVariable("b", "F", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a <= b;";
        Assert.assertEquals(good, insn);
    }
}

/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.test.kotlin;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.test.util.KotlinTestHelper;
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int) {\n"
                + "    i = 65\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int) {\n"
                + "    i = -a\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a + b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a - b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a * b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a / b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a % b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a shr b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a ushr b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Int, a: Int, b: Int) {\n"
                + "    i = a shl b\n"
                + "}";
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

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "test_mth");
        String good = "fun test_mth(i: Boolean, a: Float, b: Float) {\n"
                + "    i = a <= b\n"
                + "}";
        Assert.assertEquals(good, insn);
    }
}

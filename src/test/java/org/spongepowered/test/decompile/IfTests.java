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

public class IfTests {

    private static final Type THIS_TYPE = Type.getType(IfTests.class);

    public static void body() {

    }

    @Test
    public void testSimple() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(Z)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testComparisonAndNull() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ILjava/lang/Object;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_3);
        mv.visitJumpInsn(IF_ICMPLT, body);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNULL, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "I", null, start, ret, 0);
        mv.visitLocalVariable("b", "Ljava/lang/Object;", null, start, ret, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a < 3 || b != null) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testOr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, body);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a || b) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a && b) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testMultipleAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a && b && c) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testMultipleOr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, body);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, body);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a || b || c) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testOrAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, body);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a || b && c) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testAndOr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        Label l1 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, body);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a && b || c) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testPOS() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        Label l1 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFNE, body);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);
        mv.visitLocalVariable("d", "Z", null, start, ret, 3);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if ((a || b) && (c || d)) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testSOP() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        Label l1 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, body);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);
        mv.visitLocalVariable("d", "Z", null, start, ret, 3);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a && b || c && d) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testCommonFactor() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label ret = new Label();
        Label body = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFNE, body);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitJumpInsn(IFEQ, ret);
        mv.visitLabel(body);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(ret);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, ret, 0);
        mv.visitLocalVariable("b", "Z", null, start, ret, 1);
        mv.visitLocalVariable("c", "Z", null, start, ret, 2);
        mv.visitLocalVariable("d", "Z", null, start, ret, 3);
        mv.visitLocalVariable("e", "Z", null, start, ret, 4);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if ((a || b) && c || d && e) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testElse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(Z)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label else_ = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedIf() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label else_ = new Label();
        Label inner_else = new Label();
        Label inner_end = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, else_);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, inner_else);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, inner_end);
        mv.visitLabel(inner_else);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(inner_end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    } else {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedIf2() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label else_ = new Label();
        Label inner_else = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, else_);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, inner_else);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(inner_else);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    }\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedIf2Optimized() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label else_ = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, else_);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    }\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedIf3() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l3);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);
        mv.visitLocalVariable("c", "Z", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        if (c) {\n"
                + "            org.spongepowered.test.decompile.IfTests.body();\n"
                + "        } else {\n"
                + "            org.spongepowered.test.decompile.IfTests.body();\n"
                + "        }\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedIf3Optimized() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);
        mv.visitLocalVariable("c", "Z", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        if (c) {\n"
                + "            org.spongepowered.test.decompile.IfTests.body();\n"
                + "        } else {\n"
                + "            org.spongepowered.test.decompile.IfTests.body();\n"
                + "        }\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testElif() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label else_ = new Label();
        Label else2 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, else_);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else_);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, else2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(else2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "} else if (b) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedInstanceOf() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZLjava/lang/Object;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, THIS_TYPE.getInternalName());
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Ljava/lang/Object;", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "    if (b instanceof org.spongepowered.test.decompile.IfTests) {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNestedInstanceOfOptimized() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZLjava/lang/Object;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, THIS_TYPE.getInternalName());
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Ljava/lang/Object;", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "    if (b instanceof org.spongepowered.test.decompile.IfTests) {\n"
                + "        org.spongepowered.test.decompile.IfTests.body();\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testIfReturns() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitLabel(end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "    return;\n"
                + "}\n\n"
                + "if (b) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "    return;\n"
                + "}\n\n"
                + "org.spongepowered.test.decompile.IfTests.body();";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testIfElseReturns() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "    if (b) {\n"
                + "        return;\n"
                + "    }\n"
                + "} else {\n"
                + "    org.spongepowered.test.decompile.IfTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

}

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
import org.objectweb.asm.Type;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class WhileTests {

    @BeforeClass
    public static void setup() {
        LibraryConfiguration.quiet = false;
        LibraryConfiguration.parallel = false;
    }

    private static final Type THIS_TYPE = Type.getType(WhileTests.class);

    public static void body() {

    }

    @Test
    public void testWhile() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPLT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testFor() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitIincInsn(0, 1);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testForInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(I)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitIincInsn(0, 1);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPLT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a && b) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileAndInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a && b) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileNestedIf() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a) {\n"
                + "    if (b) {\n"
                + "        org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileNestedIfInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l3);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(IFNE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a) {\n"
                + "    if (b) {\n"
                + "        org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileBreak() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(l2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    if (a) {\n"
                + "        break;\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileBreakInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPLT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    if (a) {\n"
                + "        break;\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileContinue() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    if (a) {\n"
                + "        continue;\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileContinueInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l3);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPLT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    if (a) {\n"
                + "        continue;\n"
                + "    }\n\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testIfThenWhileInverse() {
        // the target of an if preceeding a while which has been made in the
        // inverse manner will be optimized to point to the condition of the
        // while loop rather than the start of the while loop
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l2);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPLT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "if (a) {\n"
                + "    return;\n"
                + "}\n\n"
                + "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileDirectBreak() {
        // if the break is the only thing in a condition it will sometimes be
        // optimized so that the inverse of the condition targets the outside of
        // the loop rather than having a goto inside the condition
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(IZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, end);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, end);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    org.spongepowered.test.decompile.WhileTests.body();\n"
                + "    if (a) {\n"
                + "        break;\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

}

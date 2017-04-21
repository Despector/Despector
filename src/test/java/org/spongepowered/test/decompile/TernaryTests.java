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

import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class TernaryTests {

    private static final Type THIS_TYPE = Type.getType(TernaryTests.class);

    @Test
    public void testSimple() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZI)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitIntInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_3);
        mv.visitLabel(l2);
        mv.visitIntInsn(ISTORE, 1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("i", "I", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a ? 6 : 3;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testToField() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZI)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitIntInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_3);
        mv.visitLabel(l2);
        mv.visitFieldInsn(PUTSTATIC, THIS_TYPE.getInternalName(), "afield", "I");
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("i", "I", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "org.spongepowered.test.decompile.TernaryTests.afield = a ? 6 : 3;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNested() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZIZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitIntInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitInsn(ICONST_4);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l3);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_3);
        mv.visitLabel(l2);
        mv.visitIntInsn(ISTORE, 1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("i", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "Z", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a ? b ? 4 : 5 : 3;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNested2() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZIZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        mv.visitIntInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(ICONST_3);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitIntInsn(ILOAD, 2);
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitInsn(ICONST_4);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l3);
        mv.visitInsn(ICONST_5);
        mv.visitLabel(l2);
        mv.visitIntInsn(ISTORE, 1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("i", "I", null, start, end, 1);
        mv.visitLocalVariable("b", "Z", null, start, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = a ? 3 : b ? 4 : 5;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testReturned() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(ZI)I");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        Label l1 = new Label();
        mv.visitIntInsn(ILOAD, 0);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_3);
        mv.visitLabel(end);
        mv.visitInsn(IRETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("i", "I", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "return a ? 6 : 3;";
        Assert.assertEquals(good, insn);
    }

}

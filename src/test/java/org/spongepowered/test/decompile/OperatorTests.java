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
import static org.objectweb.asm.commons.GeneratorAdapter.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class OperatorTests {

    @Test
    public void testIntConstant() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.push(65);
        mv.storeArg(0);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "i = 65;";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNegative() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.math(NEG, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(ADD, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(SUB, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(MUL, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(DIV, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(REM, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(SHR, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(USHR, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, int, int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.math(SHL, Type.INT_TYPE);
        mv.storeArg(0);
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, float, float)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        mv.loadArg(1);
        mv.loadArg(2);
        mv.visitInsn(FCMPG);
        mv.ifZCmp(GT, l1);
        mv.push(true);
        mv.goTo(l2);
        mv.visitLabel(l1);
        mv.push(false);
        mv.visitLabel(l2);
        mv.storeArg(0);
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

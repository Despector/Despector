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
import org.objectweb.asm.commons.Method;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class IfTests {

    private static final Type THIS_TYPE = Type.getType(IfTests.class);

    public static void body() {

    }

    @Test
    public void testSimple() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, Object)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.push(3);
        mv.ifICmp(LT, body);
        mv.loadArg(1);
        mv.ifNull(ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(NE, body);
        mv.loadArg(1);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(1);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(1);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(2);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(NE, body);
        mv.loadArg(1);
        mv.ifZCmp(NE, body);
        mv.loadArg(2);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(NE, body);
        mv.loadArg(1);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(2);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        Label l1 = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, l1);
        mv.loadArg(1);
        mv.ifZCmp(NE, body);
        mv.visitLabel(l1);
        mv.loadArg(2);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        Label l1 = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(NE, l1);
        mv.loadArg(1);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(l1);
        mv.loadArg(2);
        mv.ifZCmp(NE, body);
        mv.loadArg(3);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        Label l1 = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, l1);
        mv.loadArg(1);
        mv.ifZCmp(NE, body);
        mv.visitLabel(l1);
        mv.loadArg(2);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(3);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label ret = mv.newLabel();
        Label body = mv.newLabel();
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(NE, l1);
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.visitLabel(l1);
        mv.loadArg(2);
        mv.ifZCmp(NE, body);
        mv.visitLabel(l2);
        mv.loadArg(3);
        mv.ifZCmp(EQ, ret);
        mv.loadArg(4);
        mv.ifZCmp(EQ, ret);
        mv.visitLabel(body);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label else_ = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(end);
        mv.visitLabel(else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label else_ = mv.newLabel();
        Label inner_else = mv.newLabel();
        Label inner_end = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, else_);
        mv.loadArg(1);
        mv.ifZCmp(EQ, inner_else);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(inner_end);
        mv.visitLabel(inner_else);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(inner_end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(end);
        mv.visitLabel(else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label else_ = mv.newLabel();
        Label inner_else = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, else_);
        mv.loadArg(1);
        mv.ifZCmp(EQ, inner_else);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(inner_else);
        mv.goTo(end);
        mv.visitLabel(else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label else_ = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, else_);
        mv.loadArg(1);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(end);
        mv.visitLabel(else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label l3 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(EQ, l1);
        mv.loadArg(2);
        mv.ifZCmp(EQ, l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(l3);
        mv.visitLabel(l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l3);
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(EQ, l1);
        mv.loadArg(2);
        mv.ifZCmp(EQ, l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(l1);
        mv.visitLabel(l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label else_ = mv.newLabel();
        Label else2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, else_);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(end);
        mv.visitLabel(else_);
        mv.loadArg(1);
        mv.ifZCmp(EQ, else2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(end);
        mv.visitLabel(else2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, Object)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(1);
        mv.instanceOf(THIS_TYPE);
        mv.ifZCmp(EQ, l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, Object)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(1);
        mv.instanceOf(THIS_TYPE);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.loadArg(1);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitInsn(RETURN);
        mv.visitLabel(end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.loadArg(0);
        mv.ifZCmp(EQ, l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
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

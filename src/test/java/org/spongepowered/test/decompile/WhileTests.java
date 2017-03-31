/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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

public class WhileTests {

    private static final Type THIS_TYPE = Type.getType(WhileTests.class);

    public static void body() {

    }

    @Test
    public void testWhile() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(GE, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.goTo(l2);
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l2);
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(LT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (i < 5) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testFor() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.push(0);
        mv.storeArg(0);
        mv.visitLabel(l1);
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(GE, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.iinc(0, 1);
        mv.goTo(l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testForInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.push(0);
        mv.storeArg(0);
        mv.goTo(l2);
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.iinc(0, 1);
        mv.visitLabel(l2);
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(LT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(EQ, end);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a && b) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileAndInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.goTo(l2);
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l2);
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(NE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a && b) {\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileNestedIf() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.goTo(l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a) {\n"
                + "    if (b) {\n"
                + "        LoopTests.body();\n"
                + "    }\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testWhileNestedIfInverse() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label l3 = mv.newLabel();
        Label end = mv.newLabel();
        mv.goTo(l3);
        mv.visitLabel(l1);
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.visitLabel(l3);
        mv.loadArg(0);
        mv.ifZCmp(NE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "while (a) {\n"
                + "    if (b) {\n"
                + "        LoopTests.body();\n"
                + "    }\n"
                + "    LoopTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

}

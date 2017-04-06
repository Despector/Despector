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

import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.commons.GeneratorAdapter.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class DoWhileTests {

    private static final Type THIS_TYPE = Type.getType(DoWhileTests.class);

    public static void body() {

    }

    @Test
    public void testDoWhile() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(LT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "do {\n"
                + "    org.spongepowered.test.decompile.DoWhileTests.body();\n"
                + "} while (i < 5);";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testDoWhileAnd() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(0);
        mv.ifZCmp(EQ, end);
        mv.loadArg(1);
        mv.ifZCmp(NE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "do {\n"
                + "    org.spongepowered.test.decompile.DoWhileTests.body();\n"
                + "} while (a && b);";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testDoWhileOr() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (boolean, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(0);
        mv.ifZCmp(NE, l1);
        mv.loadArg(1);
        mv.ifZCmp(NE, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("a", "Z", null, start, end, 0);
        mv.visitLocalVariable("b", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "do {\n"
                + "    org.spongepowered.test.decompile.DoWhileTests.body();\n"
                + "} while (a || b);";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testDoWhileBreak() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.goTo(end);
        mv.visitLabel(l2);
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(LT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "do {\n"
                + "    org.spongepowered.test.decompile.DoWhileTests.body();\n"
                + "    if (a) {\n"
                + "        break;\n"
                + "    }\n"
                + "} while (i < 5);";
        Assert.assertEquals(good, insn);
    }

    //@Test
    public void testDoWhileContinue() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "void test_mth (int, boolean)");
        GeneratorAdapter mv = builder.getGenerator();
        Label start = mv.newLabel();
        mv.visitLabel(start);
        Label l1 = mv.newLabel();
        Label l2 = mv.newLabel();
        Label end = mv.newLabel();
        mv.visitLabel(l1);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(1);
        mv.ifZCmp(EQ, l2);
        mv.goTo(l1);
        mv.visitLabel(l2);
        mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
        mv.loadArg(0);
        mv.push(5);
        mv.ifICmp(LT, l1);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("i", "I", null, start, end, 0);
        mv.visitLocalVariable("a", "Z", null, start, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "do {\n"
                + "    DoWhileTests.body();\n"
                + "    if (a) {\n"
                + "        continue;\n"
                + "    }\n"
                + "    DoWhileTests.body();\n"
                + "} while (i < 5);";
        Assert.assertEquals(good, insn);
    }
}

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
package org.spongepowered.test.ast;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.*;

import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

import java.io.IOException;
import java.io.PrintStream;

@SuppressWarnings("unused")
public class BranchTest {

    private void mth_simpleif(boolean a, boolean b) {
        if (a) {
            System.out.println(a);
        }
    }

    @Test
    public void testSimpleIf() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_simpleif");
        String good = "if (a) {\n    System.out.println(a);\n}";
        assertEquals(good, insn);
    }

    public void mth_if1(Object o, int i) {
        if (i < 3 || o != null) {
            i++;
        }
    }

    @Test
    public void testIfWithComparisonAndNullCheck() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if1");
        String good = "if (i < 3 || o != null) {\n"
                + "    i++;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if3(int i, boolean a, boolean b) {
        if (a || b) {
            i += 5;
        }
    }

    @Test
    public void testIfWithOr() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if3");
        String good = "if (a || b) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if4(int i, boolean a, boolean b) {
        if (a && b) {
            i += 5;
        }
    }

    @Test
    public void testIfWithAnd() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if4");
        String good = "if (a && b) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if5(int i, boolean a, boolean b, boolean c) {
        if (a && b && c) {
            i += 5;
        }
    }

    @Test
    public void testIfWithMultipleAnd() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if5");
        String good = "if (a && b && c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if6(int i, boolean a, boolean b, boolean c) {
        if (a || b || c) {
            i += 5;
        }
    }

    @Test
    public void testIfWithMultipleOr() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if6");
        String good = "if (a || b || c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if7(int i, boolean a, boolean b, boolean c) {
        if (a || b && c) {
            i += 5;
        }
    }

    @Test
    public void testIfWithOrandAnd() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if7");
        String good = "if (a || b && c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if8(int i, boolean a, boolean b, boolean c) {
        if (a && b || c) {
            i += 5;
        }
    }

    @Test
    public void testIfWithAndandOr() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if8");
        String good = "if (a && b || c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if9(int i, boolean a, boolean b, boolean c, boolean d) {
        if ((a || d) && (b || c)) {
            i += 5;
        }
    }

    @Test
    public void testIfWithPoS() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if9");
        String good = "if ((a || d) && (b || c)) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if10(int i, boolean a, boolean b, boolean c, boolean d) {
        if ((a && d) || (b && c)) {
            i += 5;
        }
    }

    @Test
    public void testIfWithSoP() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if10");
        String good = "if (a && d || b && c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if11(int i, boolean a, boolean b, boolean c, boolean d, boolean e) {
        if (((a || e) && d) || (b && c)) {
            i += 5;
        }
    }

    @Test
    public void testIfWithCommonFactor() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if11");
        String good = "if ((a || e) && d || b && c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_ifelse(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a) {
            System.out.println(c);
        } else {
            System.out.println(d);
        }
    }

    @Test
    public void testIfWithElse() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_ifelse");
        String good = "if (a) {\n"
                + "    System.out.println(c);\n"
                + "} else {\n"
                + "    System.out.println(d);\n"
                + "}";
        assertEquals(good, insn);
    }

    public boolean mth_ifelseret(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a) {
            System.out.println(c);
            return c;
        }
        System.out.println(d);
        return d;
    }

    @Test
    public void testIfWithElseReturn() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_ifelseret");
        String good = "if (a) {\n"
                + "    System.out.println(c);\n"
                + "    return c;\n"
                + "}\n"
                + "System.out.println(d);\n"
                + "return d;";
        assertEquals(good, insn);
    }

    public void mth_if13(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a) {
            if (b) {
                System.out.println(e);
            } else {
                System.out.println(a);
            }
            System.out.println(c);
        } else {
            System.out.println(d);
        }
    }

    @Test
    public void testIfWithNestingAndElse() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if13");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        System.out.println(e);\n"
                + "    } else {\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "    System.out.println(c);\n"
                + "} else {\n"
                + "    System.out.println(d);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if15(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a) {
            System.out.println(a);
        } else if (b) {
            System.out.println(b);
        } else {
            System.out.println(f);
        }
    }

    @Test
    public void testIfWithElif() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if15");
        String good = "if (a) {\n"
                + "    System.out.println(a);\n"
                + "} else if (b) {\n"
                + "    System.out.println(b);\n"
                + "} else {\n"
                + "    System.out.println(f);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_for(int i, boolean c) {
        for (i = 0; i < 5; i++) {
            System.out.println(c);
        }
    }

    @Test
    public void testBasicFor() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_for");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    System.out.println(c);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_for2(int i, boolean d, boolean e, boolean f) {
        for (; e && d;) {
            System.out.println(f);
        }
    }

    @Test
    public void testForThatLooksLikeAWhile() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_for2");
        String good = "while (e && d) {\n"
                + "    System.out.println(f);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_while(int i, boolean a, boolean d, boolean e) {
        while (a ^ d) {
            System.out.println(e);
        }
    }

    @Test
    public void testBasicWhile() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_while");
        String good = "while (a ^ d) {\n"
                + "    System.out.println(e);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_while2(int i, boolean a, boolean d, boolean e) {
        while (a ^ d) {
            if (e) {
                System.out.println(e);
            }
        }
    }

    @Test
    public void testBasicWhileWithNesting() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_while2");
        String good = "while (a ^ d) {\n"
                + "    if (e) {\n"
                + "        System.out.println(e);\n"
                + "    }\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_dowhile(int i, boolean d, boolean e, boolean f) {
        do {
            System.out.println(d);
        } while (d || e || f);
    }

    @Test
    public void testDoWhile() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_dowhile");
        String good = "do {\n"
                + "    System.out.println(d);\n"
                + "} while (d || e || f);";
        assertEquals(good, insn);
    }

    private static enum TestEnum {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
    }

    public void mth_lookupswitch(int i, TestEnum ie, boolean a, boolean c, boolean d) {
        switch (ie) {
        case ONE:
            System.out.println(a);
            break;
        case EIGHT:
            System.out.println(c);
            break;
        default:
            System.out.println(d);
        }
    }

    @Test
    public void testLookupSwitch() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_lookupswitch");
        String good =
                "switch (BranchTest.$SWITCH_TABLE$org$spongepowered$test$ast$BranchTest$TestEnum()[ie.ordinal()]) {\n"
                        + "case 1:\n"
                        + "    System.out.println(a);\n"
                        + "    break;\n"
                        + "case 8:\n"
                        + "    System.out.println(c);\n"
                        + "    break;\n"
                        + "default:\n"
                        + "    System.out.println(d);\n"
                        + "}";
        assertEquals(good, insn);
    }

    public void mth_tableswitch(int i, TestEnum ie, boolean a, boolean c, boolean d) {
        switch (ie) {
        case ONE:
            System.out.println(a);
            break;
        case TWO:
            System.out.println(c);
            break;
        default:
            System.out.println(d);
        }

    }

    @Test
    public void testTableSwitch() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_tableswitch");
        // this test produces the raw switch because the test helper decompiles
        // single methods and therefore the emitter lacks the information to
        // cleanup the switch condition and cases.
        String good =
                "switch (BranchTest.$SWITCH_TABLE$org$spongepowered$test$ast$BranchTest$TestEnum()[ie.ordinal()]) {\n"
                        + "case 1:\n"
                        + "    System.out.println(a);\n"
                        + "    break;\n"
                        + "case 2:\n"
                        + "    System.out.println(c);\n"
                        + "    break;\n"
                        + "default:\n"
                        + "    System.out.println(d);\n"
                        + "}";
        assertEquals(good, insn);
    }

    private void mth_ifnestedinstanceof(boolean a, Object b) {
        if (!a) {
            System.out.println(a);
            if (b instanceof String) {
                System.out.println(b);
            }
        }
    }

    @Test
    public void testIfWithNestedInstanceof() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_ifnestedinstanceof");
        String good = "if (!a) {\n"
                + "    System.out.println(a);\n"
                + "    if (b instanceof String) {\n"
                + "        System.out.println(b);\n"
                + "    }\n"
                + "}";
        assertEquals(good, insn);
    }

    public boolean mth_ifred(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a != b) {
            return false;
        }
        if (d && e) {
            return !c;
        }
        return false;
    }

    @Test
    public void testIfWithReturns() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_ifred");
        // This output looks wrong because there is no type information to
        // convert the ints to bools
        String good = "if (a != b) {\n"
                + "    return 0;\n"
                + "}\n"
                + "if (d && e) {\n"
                + "    return c ? 0 : 1;\n"
                + "}\n"
                + "return 0;";
        assertEquals(good, insn);
    }

    public void mth_ifnestedelse(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if (a) {
            if (b) {
                System.out.println(a);
            } else {
                System.out.println(b);
            }
        } else {
            System.out.println(c);
        }
    }

    @Test
    public void testIfWithNestedElse() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_ifnestedelse");
        String good = "if (a) {\n"
                + "    if (b) {\n"
                + "        System.out.println(a);\n"
                + "    } else {\n"
                + "        System.out.println(b);\n"
                + "    }\n"
                + "} else {\n"
                + "    System.out.println(c);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_whilebreak(int i, boolean a, boolean d, boolean e) {
        while (a) {
            if (d) {
                break;
            }
            System.out.println(e);
        }
    }

    @Test
    public void testBasicWhileBreak() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_whilebreak");
        String good = "while (a) {\n"
                + "    if (d) {\n"
                + "        break;\n"
                + "    }\n"
                + "    System.out.println(e);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_whilecontinue(int i, boolean a, boolean d, boolean e) {
        while (a) {
            if (d) {
                continue;
            }
            System.out.println(e);
        }
    }

    @Test
    public void testBasicWhileContinue() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_whilecontinue");
        String good = "while (a) {\n"
                + "    if (d) {\n"
                + "        continue;\n"
                + "    }\n"
                + "    System.out.println(e);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_dowhilebreak(int i, boolean a, boolean d, boolean e) {
        do {
            System.out.println(i);
            if (d) {
                break;
            }
        } while (a);
    }

    @Test
    public void testBasicDoWhileBreak() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_dowhilebreak");
        String good = "do {\n"
                + "    System.out.println(i);\n"
                + "    if (d) {\n"
                + "        break;\n"
                + "    }\n"
                + "} while (a);";
        assertEquals(good, insn);
    }

    @Test
    public void testInverseFor() throws IOException {
        TestMethodBuilder builder = new TestMethodBuilder("mth_inversefor", "(ILjava/lang/String;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start_label = new Label();
        mv.visitLabel(start_label);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        Label loop_condition = new Label();
        Label loop_body_start = new Label();
        mv.visitLabel(loop_body_start);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, loop_condition);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
        mv.visitIincInsn(1, 1);
        mv.visitJumpInsn(GOTO, loop_body_start);
        mv.visitLabel(loop_condition);
        mv.visitInsn(RETURN);
        Label end_label = new Label();
        mv.visitLabel(end_label);
        mv.visitLocalVariable("i", "I", null, start_label, end_label, 1);
        mv.visitLocalVariable("c", "Z", null, start_label, end_label, 2);

        String insn = TestHelper.getAsString(builder.finish(), "mth_inversefor");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    System.out.println(c);\n"
                + "}";
        assertEquals(good, insn);
    }

    @Test
    public void testInverseForWithNesting() throws IOException {
        TestMethodBuilder builder = new TestMethodBuilder("mth_inversefor", "(IZZ)V");
        MethodVisitor mv = builder.getGenerator();
        Label start_label = new Label();
        mv.visitLabel(start_label);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        Label loop_condition = new Label();
        Label loop_continue = new Label();
        Label loop_body_start = new Label();
        mv.visitLabel(loop_body_start);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(ICONST_5);
        mv.visitJumpInsn(IF_ICMPGE, loop_condition);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitJumpInsn(IFEQ, loop_continue);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
        mv.visitLabel(loop_continue);
        mv.visitIincInsn(1, 1);
        mv.visitJumpInsn(GOTO, loop_body_start);
        mv.visitLabel(loop_condition);
        mv.visitInsn(RETURN);
        Label end_label = new Label();
        mv.visitLabel(end_label);
        mv.visitLocalVariable("i", "I", null, start_label, end_label, 1);
        mv.visitLocalVariable("c", "Z", null, start_label, end_label, 2);
        mv.visitLocalVariable("e", "Z", null, start_label, end_label, 3);

        String insn = TestHelper.getAsString(builder.finish(), "mth_inversefor");
        String good = "for (i = 0; i < 5; i++) {\n"
                + "    if (e) {\n"
                + "        System.out.println(c);\n"
                + "    }\n"
                + "}";
        assertEquals(good, insn);
    }

}

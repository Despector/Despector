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

import org.junit.Test;

import java.io.IOException;

@SuppressWarnings("unused")
public class BranchTest {

    private void mth_simpleif(boolean a) {
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
    public void testmth_if1() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if1");
        String good = "if (i < 3 || o != null) {\n"
                + "    i++;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if2(int i, boolean a) {
        if (a) {
            i += 5;
        }
    }

    @Test
    public void testmth_if2() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if2");
        String good = "if (a) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if3(int i, boolean a, boolean b) {
        if (a || b) {
            i += 5;
        }
    }

    @Test
    public void testmth_if3() throws IOException {
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
    public void testmth_if4() throws IOException {
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
    public void testmth_if5() throws IOException {
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
    public void testmth_if6() throws IOException {
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
    public void testmth_if7() throws IOException {
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
    public void testmth_if8() throws IOException {
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
    public void testmth_if9() throws IOException {
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
    public void testmth_if10() throws IOException {
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
    public void testmth_if11() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if11");
        String good = "if ((a || e) && d || b && c) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if12(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if ((a && (c || e)) || (b || (d && f))) {
            i += 5;
        }
    }

    @Test
    public void testmth_if12() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if12");
        String good = "if (a && (c || e) || b || d && f) {\n"
                + "    i += 5;\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_if13(int i, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        if ((a && (c || e))) {
            if (b || (d && f)) {
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
    public void testmth_if13() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if13");
        String good = "if (a && (c || e)) {\n"
                + "    if (b || d && f) {\n"
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
        if (a && b) {
            System.out.println(a);
        } else if (c && d) {
            System.out.println(b);
        } else if (e ^ f) {
            System.out.println(c);
        } else {
            System.out.println(f);
        }
    }

    @Test
    public void testmth_if15() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_if15");
        String good = "if (a && b) {\n"
                + "    System.out.println(a);\n"
                + "} else if (c && d) {\n"
                + "    System.out.println(b);\n"
                + "} else if (e ^ f) {\n"
                + "    System.out.println(c);\n"
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
    public void testmth_for() throws IOException {
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
    public void testmth_for2() throws IOException {
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
    public void testmth_while() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_while");
        String good = "while (a ^ d) {\n"
                + "    System.out.println(e);\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_dowhile(int i, boolean d, boolean e, boolean f) {
        do {
            System.out.println(d);
        } while (d || e || f);
    }

    @Test
    public void testmth_dowhile() throws IOException {
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
    public void testmth_lookupswitch() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_lookupswitch");
        String good = "switch (ie) {\n"
                + "case ONE:\n"
                + "    System.out.println(a);\n"
                + "    break;\n"
                + "case EIGHT:\n"
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
    public void testmth_tableswitch() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_tableswitch");
        String good = "switch (ie) {\n"
                + "case ONE:\n"
                + "    System.out.println(a);\n"
                + "    break;\n"
                + "case TWO:\n"
                + "    System.out.println(c);\n"
                + "    break;\n"
                + "default:\n"
                + "    System.out.println(d);\n"
                + "}";
        assertEquals(good, insn);
    }

}

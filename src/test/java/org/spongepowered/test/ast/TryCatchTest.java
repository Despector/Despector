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
package org.spongepowered.test.ast;

import static org.junit.Assert.assertEquals;

import org.spongepowered.test.util.TestHelper;

import org.junit.Test;

public class TryCatchTest {

    public void mth_trycatch(int i, Object o) {
        try {
            i = o.hashCode();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testmth_trycatch() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatch");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "} catch (NullPointerException e) {\n"
                    + "    e.printStackTrace();\n"
                    + "}";
        assertEquals(good, insn);
    }

    public void mth_trycatch2(int i, Object o) {
        try {
            i = o.hashCode();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testmth_trycatch2() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatch2");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "} catch (NullPointerException e) {\n"
                    + "    e.printStackTrace();\n"
                    + "} catch (OutOfMemoryError e) {\n"
                    + "    e.printStackTrace();\n"
                    + "}";
        assertEquals(good, insn);
    }

    public void mth_trycatch3(int i, Object o) {
        try {
            i = o.hashCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testmth_trycatch3() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatch3");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "} catch (NullPointerException | IllegalArgumentException e) {\n"
                    + "    e.printStackTrace();\n"
                    + "}";
        assertEquals(good, insn);
    }

    public int mth_trycatchreturn(int i, Object o) {
        try {
            i = o.hashCode();
            return i;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Test
    public void testmth_trycatchreturn() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatchreturn");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "    return i;\n"
                    + "} catch (NullPointerException e) {\n"
                    + "    e.printStackTrace();\n"
                    + "    return 0;\n"
                    + "}";
        assertEquals(good, insn);
    }

    public int mth_trycatchreturn2(int i, Object o) {
        try {
            i = o.hashCode();
            return i;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Test
    public void testmth_trycatchreturn2() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatchreturn2");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "    return i;\n"
                    + "} catch (NullPointerException e) {\n"
                    + "    e.printStackTrace();\n"
                    + "}\n\n"
                    + "return 0;";
        assertEquals(good, insn);
    }

    public void mth_nestedtrycatch(int i, Object o) {
        try {
            try {
                i = o.hashCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testmth_nestedtrycatch() {
        String insn = TestHelper.getAsString(getClass(), "mth_nestedtrycatch");
        String good = "try {\n"
                + "    try {\n"
                + "        i = o.hashCode();\n"
                + "    } catch (Exception e) {\n"
                + "        e.printStackTrace();\n"
                + "    }\n"
                + "} catch (NullPointerException e) {\n"
                + "    e.printStackTrace();\n"
                + "}";
        assertEquals(good, insn);
    }

    public void mth_trycatchWithControlFlow(int i, Object o) {
        try {
            i = o.hashCode();
        } catch (NullPointerException e) {
            if(e != null) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testTryCatchWithControlFlow() {
        String insn = TestHelper.getAsString(getClass(), "mth_trycatchWithControlFlow");
        String good = "try {\n"
                    + "    i = o.hashCode();\n"
                    + "} catch (NullPointerException e) {\n"
                    + "    if (e != null) {\n"
                    + "        e.printStackTrace();\n"
                    + "    }\n"
                    + "}";
        assertEquals(good, insn);
    }

}

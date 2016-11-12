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

public class BranchTest {

    private void mth_simpleif(boolean a) {
        if (a) {
            System.out.println(a);
        }
    }

    @Test
    public void testSimpleIf() throws IOException {
        String insn = TestHelper.getAsString(BranchTest.class, "mth_simpleif");
        String good = "if (a) {\n    System.out.println(a);\n}";
        assertEquals(good, insn);
    }

    public void mth_basicternary(boolean a) {
        int r = a ? 6 : 3;
    }

    @Test
    public void testBasicTernary() throws IOException {
        String insn = TestHelper.getAsString(BranchTest.class, "mth_basicternary");
        String good = "int r = a ? 6 : 3;";
        assertEquals(good, insn);
    }
}

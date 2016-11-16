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
public class TernaryTest {

    public void mth_basicTernary(boolean a) {
        int r = a ? 6 : 3;
    }

    @Test
    public void testBasicTernary() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_basicTernary");
        String good = "int r = a ? 6 : 3;";
        assertEquals(good, insn);
    }

    public void mth_moreComplexTernary(boolean a, boolean b, int j) {
        int i = a || b ? 0 - j * 24 + Integer.MAX_VALUE : 1 + j;
    }

    @Test
    public void testMoreComplexTernary() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_moreComplexTernary");
        String good = "int i = a || b ? 0 - j * 24 + Integer.MAX_VALUE : 1 + j;";
        assertEquals(good, insn);
    }

    private int field;
    
    public void mth_basicTernaryToField(boolean a) {
        this.field = a ? 255 : 0;
    }

    @Test
    public void testBasicTernaryToField() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_basicTernaryToField");
        String good = "this.field = a ? 255 : 0;";
        assertEquals(good, insn);
    }

}

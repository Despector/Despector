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

import static org.spongepowered.test.util.TestHelper.check;

import org.junit.Test;
import org.spongepowered.test.util.TestHelper;

import java.io.IOException;

@SuppressWarnings("unused")
public class OperatorsTest {

    public OperatorsTest areallylongmethodnametotestwrapping() {
        return this;
    }

    public void mth_wrapped() {
        areallylongmethodnametotestwrapping().areallylongmethodnametotestwrapping().areallylongmethodnametotestwrapping()
                .areallylongmethodnametotestwrapping();
        areallylongmethodnametotestwrapping();
    }

    //@Test
    public void testWrapping() throws IOException {
        String expected = "areallylongmethodnametotestwrapping().areallylongmethodnametotestwrapping().areallylongmethodnametotestwrapping()\n" + 
                "        .areallylongmethodnametotestwrapping();\n"
                + "areallylongmethodnametotestwrapping();";
        check(getClass(), "mth_wrapped", expected);
    }


    private void mth_intconstant() {
        int i = 65;
    }

    @Test
    public void testIntConstant() throws IOException {
        check(getClass(), "mth_intconstant", "int i = 65;");
    }

    private void mth_neg(int a, int b) {
        int i = -(a + b);
    }

    @Test
    public void testUnaryNegative() throws IOException {
        check(getClass(), "mth_neg", "int i = -(a + b);");
    }

    private void mth_floatcmp(float a, float b) {
        boolean z = a <= b;
    }

    @Test
    public void testFloatCompare() throws IOException {
        check(getClass(), "mth_floatcmp", "boolean z = a <= b;");
    }

}

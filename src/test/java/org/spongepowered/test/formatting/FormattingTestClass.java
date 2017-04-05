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
package org.spongepowered.test.formatting;

import java.util.ArrayList;
import java.util.List;

public class FormattingTestClass {

    public int afield;
    public String anotherField;
    private List<String> alist = new ArrayList<>();

    public int foo() {
        return 0;
    }

    public void bar(String... args) {
        this.alist.add(args[0]);
    }

    public static void main(String[] args) {
        FormattingTestClass insn = new FormattingTestClass();
        insn.foo();
        insn.bar("This is a very long line", "with a lot of args that should form breakpoints and test line",
                "wrapping foo bar bazz wheee alright this is over 150 characters");
    }

    public static class AnotherClass extends Foo implements Baz {

    }

    public static class Bar implements Baz, Noo {

    }

    public static interface Baz {

    }

    public static interface Noo {

    }

    public static class Foo {

    }

}

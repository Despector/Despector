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

import org.junit.BeforeClass;
import org.junit.Test;
import org.spongepowered.despector.config.LibraryConfiguration;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LambdaTest {

    @BeforeClass
    public static void setup() {
        LibraryConfiguration.quiet = false;
        LibraryConfiguration.parallel = false;
    }

    public void test_lambda() {
        Runnable r = () -> System.out.println("Hello World");
        r.run();
    }

    @Test
    public void testLambda() {
        String good = "Runnable r = () -> System.out.println(\"Hello World\");\n"
                + "r.run();";
        check(getClass(), "test_lambda", good);
    }

    public void test_consumer() {
        Consumer<Object> r = (obj) -> System.out.println("Hello World");
        r.accept(null);
    }

    @Test
    public void testConsumer() {
        String good = "java.util.function.Consumer<Object> r = (obj) -> System.out.println(\"Hello World\");\n"
                + "r.accept(null);";
        check(getClass(), "test_consumer", good);
    }

    public void test_producer() throws Exception {
        Callable<Object> r = () -> null;
        r.call();
    }

    @Test
    public void testProducer() {
        String good = "java.util.concurrent.Callable<Object> r = () -> null;\n"
                + "r.call();";
        check(getClass(), "test_producer", good);
    }

    public void test_method_ref(Runnable r) {
        Runnable a = r::run;
        a.run();
    }

    @Test
    public void testMethodRef() {
        String good = "Runnable a = r::run;\n"
                + "a.run();";
        check(getClass(), "test_method_ref", good);
    }

}

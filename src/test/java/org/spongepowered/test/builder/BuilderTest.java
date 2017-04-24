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
package org.spongepowered.test.builder;

import static org.spongepowered.despector.transform.builder.Builders.createClass;

import org.junit.Test;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.transform.builder.ClassTypeBuilder;

public class BuilderTest {

    private static final DefineableClassLoader cl = new DefineableClassLoader();

    @Test
    public void testSimple() {
        SourceSet set = new SourceSet();
        ClassTypeBuilder builder = createClass("test.TestType");
        builder.method().build(set);
        ClassEntry entry = builder.build(set);
        
        // TODO bytecode emitter
    }

    public static interface Built {

        Object call();
    }

    private static class DefineableClassLoader extends ClassLoader {

        public Class<?> defineClass(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length);
        }

    }

}

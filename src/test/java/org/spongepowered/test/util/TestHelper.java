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
package org.spongepowered.test.util;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.reference.ReferenceSourceSet;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

public class TestHelper {

    public static final ReferenceSourceSet TEST_REFERENCE = new ReferenceSourceSet(Thread.currentThread().getContextClassLoader());

    private static final Map<Class<?>, TypeEntry> CACHED_TYPES = Maps.newHashMap();
    private static final SourceSet DUMMY_SOURCE_SET = new SourceSet(TEST_REFERENCE);

    static {
        DUMMY_SOURCE_SET.setLoader(new TestLoader(TestHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
    }

    private static class TestLoader implements SourceSet.Loader {

        private final String path;

        public TestLoader(String path) {
            this.path = path;
        }

        @Override
        public InputStream find(String name) {
            File file = new File(this.path, name + ".class");
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static TypeEntry get(Class<?> cls) {
        TypeEntry type = CACHED_TYPES.get(cls);
        if(type != null) {
            return type;
        }
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
        File file = new File(path, cls.getName().replace('.', '/') + ".class");
        try {
            type = Decompilers.WILD.decompile(file, DUMMY_SOURCE_SET);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CACHED_TYPES.put(cls, type);
        return type;
    }

    public static String getAsString(byte[] data, String method_name) {
        ClassReader cr = new ClassReader(data);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        TypeEntry type = Decompilers.WILD.decompile(cn, DUMMY_SOURCE_SET);
        MethodEntry method = type.getMethodSafe(method_name);
        return getAsString(type, method);
    }

    public static String getAsString(TypeEntry type, MethodEntry method) {
        StringWriter writer = new StringWriter();
        EmitterContext emitter = new EmitterContext(writer, EmitterFormat.defaults());
        emitter.setEmitterSet(Emitters.JAVA_SET);
        emitter.setMethod(method);
        emitter.setType(type);
        emitter.emitBody(method.getInstructions());
        emitter.flush();
        return writer.toString();
    }
    public static String getAsString(Class<?> cls, String method_name) {
        TypeEntry type = get(cls);
        MethodEntry method = type.getMethodSafe(method_name);
        return getAsString(type, method);
    }

    public static void check(Class<?> cls, String method_name, String expected) {
        TypeEntry type = get(cls);
        MethodEntry method = type.getMethodSafe(method_name);
        String actual = getAsString(type, method);
        if (!actual.equals(expected)) {
            System.err.println("Test " + method_name + " failed!");
            System.err.println("Expected:");
            System.err.println(expected);
            System.err.println("Found:");
            System.err.println(actual);
            Assert.assertEquals(expected, actual);
        }
    }

}

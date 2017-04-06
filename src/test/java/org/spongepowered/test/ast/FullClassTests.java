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

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FullClassTests {

    @Test
    public void testGenerics() throws Exception {
        compare("javaclasses/GenericsTestClass", Language.JAVA);
    }

    public static void compare(String classname, Language lang) throws IOException, URISyntaxException {
        URL source = Thread.currentThread().getContextClassLoader().getResource(classname + ".java.test");
        InputStream compiled = Thread.currentThread().getContextClassLoader().getResourceAsStream(classname + ".class.test");

        if (source == null) {
            Assert.fail("Resource not found " + classname + ".java.test");
        }
        if (compiled == null) {
            Assert.fail("Resource not found " + classname + ".class.test");
        }

        ClassReader cr = new ClassReader(compiled);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        SourceSet src = new SourceSet();
        TypeEntry type = Decompilers.get(lang).decompile(cn, src);
        StringWriter writer = new StringWriter();
        EmitterContext ctx = new EmitterContext(writer, EmitterFormat.defaults());
        Emitters.get(lang).emit(ctx, type);
        String source_str = new String(Files.readAllBytes(Paths.get(source.toURI()))).replaceAll("\r\n", "\n");
        Assert.assertEquals(source_str, writer.toString());
    }

}

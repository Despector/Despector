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

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.ast.io.SingularClassLoader;
import org.spongepowered.despector.ast.io.emitter.SourceEmitter;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.type.TypeEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class TestHelper {

    private static final Map<Class<?>, TypeEntry> cached_types = Maps.newHashMap();

    public static StatementBlock get(Class<?> cls, String method_name) throws IOException {
        TypeEntry type = cached_types.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            type = SingularClassLoader.instance.load(cn, null);
            cached_types.put(cls, type);
        }
        return type.getMethod(method_name).getInstructions();
    }

    public static String getAsString(Class<?> cls, String method_name) throws IOException {
        TypeEntry type = cached_types.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            type = SingularClassLoader.instance.load(cn, null);
            cached_types.put(cls, type);
        }
        MethodEntry method = type.getMethod(method_name);
        StringWriter writer = new StringWriter();
        SourceEmitter emitter = new SourceEmitter(writer);
        emitter.emitBody(method, type);
        return writer.toString();
    }

}

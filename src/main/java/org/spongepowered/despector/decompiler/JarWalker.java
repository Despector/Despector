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
package org.spongepowered.despector.decompiler;

import com.google.common.collect.Sets;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.ast.SourceSet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Walks a jar file to produce an ast. Steps such as associating overriding
 * methods and finding string constants are also during this traversal.
 */
public class JarWalker {

    // TODO move to conf
    private static final Set<String> EXCLUDES = Sets.newHashSet();
    private static final Set<String> NON_OBF_NAMES = Sets.newHashSet();

    static {
        // shaded libs are excluded from our obfuscated source set because they
        // are directly in the classpath
        EXCLUDES.add("org/apache");
        EXCLUDES.add("it/unimi");
        EXCLUDES.add("io/netty");
        EXCLUDES.add("com/mojang");
        EXCLUDES.add("com/google");
        EXCLUDES.add("javax/annotation");

        // TODO move to a common location
        NON_OBF_NAMES.add("call");
        NON_OBF_NAMES.add("clone");
        NON_OBF_NAMES.add("name");
        NON_OBF_NAMES.add("ordinal");
        NON_OBF_NAMES.add("run");
        NON_OBF_NAMES.add("setPreferredSize");
        NON_OBF_NAMES.add("setMinimumSize");
        NON_OBF_NAMES.add("setMaximumSize");
        NON_OBF_NAMES.add("setLayout");
        NON_OBF_NAMES.add("add");
        NON_OBF_NAMES.add("setBackground");
        NON_OBF_NAMES.add("apply");
        NON_OBF_NAMES.add("compare");
    }

    private final Path jar;

    /**
     * Creates a new jar walker.
     */
    public JarWalker(Path jar) {
        this.jar = jar;
    }

    /**
     * Produces a new obfuscated source set for this version.
     */
    public void walk(SourceSet sources, Decompiler decomp) {
        scanJar(this.jar, sources, decomp);
    }

    private void scanJar(Path path, SourceSet src, Decompiler decomp) {
        try (JarInputStream jar = new JarInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            ZipEntry entry = jar.getNextEntry();
            if (entry == null) {
                return;
            }
            do {
                if (entry.isDirectory()) {
                    continue;
                }
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                scanClassFile(jar, src, decomp);
            } while ((entry = jar.getNextEntry()) != null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void scanClassFile(JarInputStream input, SourceSet src, Decompiler decomp) throws IOException {
        ClassReader reader = new ClassReader(input);
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        for (String ex : EXCLUDES) {
            if (cn.name.startsWith(ex)) {
                return;
            }
        }
        decomp.decompile(cn, src);
    }

}

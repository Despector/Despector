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
package org.spongepowered.despector.decompiler;

import org.spongepowered.despector.ast.SourceSet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Walks a jar file to produce an ast. Steps such as associating overriding
 * methods and finding string constants are also during this traversal.
 */
public class JarWalker {

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
        decomp.decompile(input, src);
    }

}

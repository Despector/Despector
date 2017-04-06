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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A directory walker which walks a directory and visits all child files and
 * directories.
 */
public class DirectoryWalker {

    private final Path directory;

    public DirectoryWalker(Path dir) {
        this.directory = dir;
    }

    /**
     * Walks this directory and visits all class files in it or any child
     * directory and loads them into the given {@link SourceSet}.
     */
    public void walk(SourceSet src, Decompiler decomp) throws IOException {
        File dir = this.directory.toFile();
        visit(dir, src, decomp);
    }

    private void visit(File file, SourceSet src, Decompiler decomp) throws IOException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                visit(f, src, decomp);
            }
        } else {
            if (file.getName().endsWith(".class")) {
                decomp.decompile(file, src);
            }
        }
    }

}

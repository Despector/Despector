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
package org.spongepowered.test.generate;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class TestGenerator {

    public static void main(String[] args) {
        File tests_dir = new File("src/test/java/org/spongepowered/test/generate");
        if (!tests_dir.exists() || !tests_dir.isDirectory()) {
            System.err.println("Tests directory " + tests_dir.getAbsolutePath() + " does not exist or is not a directory.");
            return;
        }
        File tests_bin_dir = new File("build/classes/java/test/org/spongepowered/test/generate");
        if (!tests_bin_dir.exists() || !tests_bin_dir.isDirectory()) {
            System.err.println("Tests directory " + tests_dir.getAbsolutePath()
                    + " does not exist or is not a directory. (have you run `./gradlew build` yet?)");
            return;
        }
        File tests_out_dir = new File("src/test/resources/javaclasses");
        if (!tests_out_dir.exists()) {
            tests_out_dir.mkdirs();
        }
        for (File f : tests_dir.listFiles()) {
            if (f.getName().equals("TestGenerator.java")) {
                continue;
            }
            if (!f.getName().endsWith(".java")) {
                continue;
            }
            String name = f.getName().substring(0, f.getName().length() - 5);
            File compiled = new File(tests_bin_dir, name + ".class");
            if (!compiled.exists()) {
                System.err.println("Test file " + name + " does not exist in the compiled output. (have you run `./gradlew build` yet?)");
                continue;
            }
            System.out.println("Adding test for " + name);
            try {
                Files.copy(f, new File(tests_out_dir, name + ".java.test"));
                Files.copy(compiled, new File(tests_out_dir, name + ".class.test"));
                f.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

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
package org.spongepowered.despector.ast.io.emitter.format;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class EclipseFormatLoader implements FormatLoader {

    public static final EclipseFormatLoader instance = new EclipseFormatLoader();

    private EclipseFormatLoader() {
    }

    @Override
    public EmitterFormat load(Path formatter, Path import_order) throws IOException {
        EmitterFormat format = new EmitterFormat();

        try (BufferedReader reader = new BufferedReader(new FileReader(import_order.toFile()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=");
                if (parts.length != 2) {
                    System.err.println("Malformed importorder file");
                    continue;
                }
                int part = Integer.parseInt(parts[0]);
                String path = parts[1];
                if (part < format.import_order.size()) {
                    format.import_order.set(part, path);
                } else {
                    while (part > format.import_order.size()) {
                        format.import_order.add(null);
                    }
                    format.import_order.add(path);
                }
            }
        }

        return format;
    }

}

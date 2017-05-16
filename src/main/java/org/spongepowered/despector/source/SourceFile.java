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
package org.spongepowered.despector.source;

import org.spongepowered.despector.ast.type.TypeEntry;

import java.util.ArrayList;
import java.util.List;

public class SourceFile {

    private final String name;

    private final List<TypeEntry> top_types = new ArrayList<>();
    private final List<TypeEntry> all_types = new ArrayList<>();

    private List<String> header;
    private String pkg = "";
    private final List<String> imports = new ArrayList<>();

    public SourceFile(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<TypeEntry> getTopLevelTypes() {
        return this.top_types;
    }

    public void addTopLevelType(TypeEntry type) {
        this.top_types.add(type);
        this.all_types.add(type);
    }

    public List<TypeEntry> getAllTypes() {
        return this.all_types;
    }

    public void addInnerType(TypeEntry type) {
        this.all_types.add(type);
    }

    public List<String> getHeader() {
        return this.header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public String getPackage() {
        return this.pkg;
    }

    public void setPackage(String pkg) {
        this.pkg = pkg;
    }

    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String im) {
        this.imports.add(im);
    }

}

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
package com.voxelgenesis.despector.core.ast;

import com.voxelgenesis.despector.core.ast.type.SourceEntry;
import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SourceSet {

    private final Map<String, SourceEntry> all_sources = new HashMap<>();

    public SourceSet() {

    }

    public void add(SourceEntry source) {
        this.all_sources.put(source.getName(), source);
    }

    public SourceEntry find(String name) {
        return this.all_sources.get(name);
    }

    public Collection<SourceEntry> getAllSources() {
        return this.all_sources.values();
    }

    public void accept(AstVisitor visitor) {
        for (SourceEntry entry : this.all_sources.values()) {
            entry.accept(visitor);
        }
    }

}

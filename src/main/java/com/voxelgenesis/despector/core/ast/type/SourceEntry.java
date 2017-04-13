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
package com.voxelgenesis.despector.core.ast.type;

import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;
import com.voxelgenesis.despector.core.ast.visitor.TypeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceEntry {

    private final String name;

    private final Map<String, Map<String, MethodEntry>> methods = new HashMap<>();
    private final List<MethodEntry> all_methods = new ArrayList<>();

    public SourceEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addMethod(MethodEntry method) {
        Map<String, MethodEntry> mth = this.methods.get(method.getName());
        if (mth == null) {
            mth = new HashMap<>();
            this.methods.put(method.getName(), mth);
        }
        mth.put(method.getSignature(), method);
        this.all_methods.add(method);
    }

    public MethodEntry findMethod(String name) {
        Map<String, MethodEntry> mth = this.methods.get(name);
        if (mth != null) {
            if (mth.size() != 1) {
                throw new IllegalArgumentException("Attempted to find ambiguous method: " + name);
            }
            return mth.values().iterator().next();
        }
        return null;
    }

    public Collection<MethodEntry> getMethods() {
        return this.all_methods;
    }

    public void accept(AstVisitor visitor) {
        if(visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitSourceEntry(this);
        }
        for (MethodEntry method : this.all_methods) {
            method.accept(visitor);
        }
    }

}

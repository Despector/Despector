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
package org.spongepowered.despector.transform.builder;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class TypeBuilder<B extends TypeBuilder<B>> {

    protected final String name;
    private AccessModifier access;

    private final List<String> interfaces = new ArrayList<>();

    private final List<FieldEntry> fields = new ArrayList<>();
    private final List<MethodEntry> methods = new ArrayList<>();

    public TypeBuilder(String n) {
        this.name = n;
    }

    public B implement(String inter) {
        this.interfaces.add(inter);
        return (B) this;
    }

    public B access(AccessModifier acc) {
        this.access = acc;
        return (B) this;
    }

    public MethodBuilder method() {
        return new MethodBuilder((m) -> {
            this.methods.add(m);
            m.setOwner(this.name);
        });
    }

    public FieldBuilder field() {
        return new FieldBuilder((m) -> {
            this.fields.add(m);
            m.setOwner(this.name);
        });
    }

    public B reset() {
        this.interfaces.clear();
        this.methods.clear();
        return (B) this;
    }

    protected void apply(TypeEntry type) {
        type.setAccessModifier(this.access);
        type.getInterfaces().addAll(this.interfaces);
        for (MethodEntry m : this.methods) {
            type.addMethod(m);
        }
        for (FieldEntry f : this.fields) {
            type.addField(f);
        }
    }

}

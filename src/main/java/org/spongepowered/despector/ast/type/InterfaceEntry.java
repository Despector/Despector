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
package org.spongepowered.despector.ast.type;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * Represents an interface type.
 */
public class InterfaceEntry extends TypeEntry {

    public InterfaceEntry(SourceSet src, Language lang, String name) {
        super(src, lang, name);
    }

    @Override
    public String toString() {
        return "Interface " + this.name;
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        super.writeTo(pack, 0, AstSerializer.ENTRY_ID_INTERFACE);
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitInterfaceEntry(this);
        }
        for (FieldEntry field : this.fields.values()) {
            field.accept(visitor);
        }
        for (FieldEntry field : this.static_fields.values()) {
            field.accept(visitor);
        }
        for (MethodEntry method : this.methods.values()) {
            method.accept(visitor);
        }
        for (MethodEntry method : this.static_methods.values()) {
            method.accept(visitor);
        }
        for (Annotation anno : this.annotations.values()) {
            anno.accept(visitor);
        }
        if (visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitTypeEnd();
        }
    }

}

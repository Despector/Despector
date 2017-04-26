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
package org.spongepowered.despector.ast.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A type signature of a class with generic arguments.
 */
public class GenericClassTypeSignature extends TypeSignature {

    protected String type_name;
    private List<TypeArgument> args = new ArrayList<>();

    public GenericClassTypeSignature(String type) {
        this.type_name = checkNotNull(type, "type");
    }

    /**
     * Gets the type descriptor.
     */
    public String getType() {
        return this.type_name;
    }

    /**
     * Sets the type descriptor.
     */
    public void setType(String type) {
        this.type_name = checkNotNull(type, "type");
    }

    /**
     * Gets the type arguments.
     */
    public List<TypeArgument> getArguments() {
        return this.args;
    }

    @Override
    public boolean hasArguments() {
        return !this.args.isEmpty();
    }

    @Override
    public String getName() {
        return TypeHelper.descToType(this.type_name);
    }

    @Override
    public String getDescriptor() {
        return this.type_name;
    }

    @Override
    public boolean isArray() {
        return this.type_name.startsWith("[");
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(3);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_TYPECLASS);
        pack.writeString("type").writeString(this.type_name);
        pack.writeString("args").startArray(this.args.size());
        for (TypeArgument arg : this.args) {
            arg.writeTo(pack);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.type_name);
        if (!this.args.isEmpty()) {
            str.append("<");
            for (TypeArgument arg : this.args) {
                str.append(arg);
            }
            str.append(">");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GenericClassTypeSignature)) {
            if (o instanceof ClassTypeSignature) {
                ClassTypeSignature g = (ClassTypeSignature) o;
                return !this.hasArguments() && this.type_name.equals(g.getType());
            }
            return false;
        }
        GenericClassTypeSignature c = (GenericClassTypeSignature) o;
        if (c.args.size() != this.args.size()) {
            return false;
        }
        for (int i = 0; i < this.args.size(); i++) {
            if (!this.args.get(i).equals(c.args.get(i))) {
                return false;
            }
        }
        return c.type_name.equals(this.type_name);
    }
}

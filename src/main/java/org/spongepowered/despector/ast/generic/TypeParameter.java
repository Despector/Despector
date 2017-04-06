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

import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A generic type parameter.
 */
public class TypeParameter {

    private String identifier;
    @Nullable private TypeSignature class_bound;
    private List<TypeSignature> interface_bounds = new ArrayList<>();

    public TypeParameter(String ident, @Nullable TypeSignature cl) {
        this.identifier = checkNotNull(ident, "identifier");
        this.class_bound = cl;
    }

    /**
     * Gets the identifier of the parameter.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the identifier of the parameter.
     */
    public void setIdentifier(String ident) {
        this.identifier = checkNotNull(ident, "identifier");
    }

    /**
     * Gets the class bound of the type parameter, if present.
     */
    @Nullable
    public TypeSignature getClassBound() {
        return this.class_bound;
    }

    /**
     * Sets the class bound of the type parameter, may be null.
     */
    public void setClassBound(@Nullable TypeSignature sig) {
        this.class_bound = sig;
    }

    /**
     * Gets the interface bounds of the type parameter.
     */
    public List<TypeSignature> getInterfaceBounds() {
        return this.interface_bounds;
    }

    public void writeTo(MessagePacker pack) throws IOException {
        int len = 3;
        if (this.class_bound != null) {
            len++;
        }
        pack.startMap(len);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_PARAM);
        pack.writeString("identifier").writeString(this.identifier);
        if (this.class_bound != null) {
            pack.writeString("classbound");
            this.class_bound.writeTo(pack);
        }
        pack.writeString("interfacebounds").startArray(this.interface_bounds.size());
        for (TypeSignature sig : this.interface_bounds) {
            sig.writeTo(pack);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.identifier).append(":");
        if (this.class_bound != null) {
            str.append(this.class_bound);
        }
        if (this.interface_bounds.isEmpty()) {
            str.append(":");
        } else {
            for (TypeSignature sig : this.interface_bounds) {
                str.append(":");
                str.append(sig);
            }
        }
        return str.toString();
    }

}

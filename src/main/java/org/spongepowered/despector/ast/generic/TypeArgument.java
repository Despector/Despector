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

import javax.annotation.Nullable;

/**
 * A type argument is a generic type specified as a type parameter of a type.
 */
public class TypeArgument {

    private WildcardType wildcard;
    @Nullable
    private TypeSignature sig;

    public TypeArgument(WildcardType wildcard, @Nullable TypeSignature sig) {
        this.wildcard = checkNotNull(wildcard, "wildcard");
        this.sig = sig;
    }

    /**
     * Gets the wildcard type of this argument.
     */
    public WildcardType getWildcard() {
        return this.wildcard;
    }

    /**
     * Sets the wildcard type of this argument.
     */
    public void setWildcard(WildcardType wild) {
        this.wildcard = checkNotNull(wild, "wildcard");
    }

    /**
     * Gets the signature of the argument.
     */
    @Nullable
    public TypeSignature getSignature() {
        return this.sig;
    }

    /**
     * Sets the signature of the argument.
     */
    public void setSignature(@Nullable TypeSignature sig) {
        this.sig = sig;
    }

    /**
     * Writes this type argument to the given {@link MessagePacker}.
     */
    public void writeTo(MessagePacker pack) throws IOException {
        int len = 2;
        if (this.sig != null) {
            len++;
        }
        pack.startMap(len);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_ARG);
        pack.writeString("wildcard").writeInt(this.wildcard.ordinal());
        if (this.sig != null) {
            pack.writeString("signature");
            this.sig.writeTo(pack);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.wildcard.getRepresentation());
        str.append(this.sig);
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TypeArgument)) {
            return false;
        }
        TypeArgument c = (TypeArgument) o;
        return this.sig.equals(c.sig) && this.wildcard.equals(c.wildcard);
    }

}

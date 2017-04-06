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

/**
 * A type signature referencing a type variable.
 */
public class TypeVariableSignature extends TypeSignature {

    private String identifier;

    public TypeVariableSignature(String ident) {
        this.identifier = checkNotNull(ident, "identifier");
    }

    /**
     * Gets the identifier of the type variable.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the identifier of the type variable.
     */
    public void setIdentifier(String ident) {
        this.identifier = checkNotNull(ident, "identifier");
    }

    public String getIdentifierName() {
        return this.identifier.substring(1, this.identifier.length() - 1);
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public String getName() {
        return getIdentifierName();
    }

    @Override
    public String getDescriptor() {
        return this.identifier;
    }

    @Override
    public boolean isArray() {
        return this.identifier.startsWith("[");
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_TYPEVAR);
        pack.writeString(this.identifier);
    }

    @Override
    public String toString() {
        return this.identifier;
    }
}

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
package org.spongepowered.despector.ast.generic;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A type argument is a generic type specified as a type parameter of a type.
 */
public class TypeArgument {

    private WildcardType wildcard;
    private TypeSignature sig;

    public TypeArgument(WildcardType wildcard, TypeSignature sig) {
        this.wildcard = checkNotNull(wildcard, "wildcard");
        this.sig = checkNotNull(sig, "sig");
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
    public TypeSignature getSignature() {
        return this.sig;
    }

    /**
     * Sets the signature of the argument.
     */
    public void setSignature(TypeSignature sig) {
        this.sig = checkNotNull(sig, "sig");
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.wildcard.getRepresentation());
        str.append(this.sig);
        return str.toString();
    }

}

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

import java.util.ArrayList;
import java.util.List;

/**
 * A method signature containing generic type information on any type
 * parameters, parameters, exceptions, and the return type.
 */
public class MethodSignature {

    private final List<TypeParameter> type_parameters = new ArrayList<>();
    private final List<TypeSignature> parameters = new ArrayList<>();
    private final List<TypeSignature> exceptions = new ArrayList<>();
    private TypeSignature return_type;

    public MethodSignature() {
        this.return_type = VoidTypeSignature.VOID;
    }

    public MethodSignature(TypeSignature sig) {
        this.return_type = checkNotNull(sig, "sig");
    }

    /**
     * Gets the method type parameters.
     */
    public List<TypeParameter> getTypeParameters() {
        return this.type_parameters;
    }

    /**
     * Gets the signatures of the method's parameters.
     */
    public List<TypeSignature> getParameters() {
        return this.parameters;
    }

    /**
     * Gets the signature of the return type.
     */
    public TypeSignature getReturnType() {
        return this.return_type;
    }

    /**
     * Sets the signature of the return type.
     */
    public void setReturnType(TypeSignature sig) {
        this.return_type = checkNotNull(sig, "sig");
    }

    /**
     * Gets the signatures of any declared thrown exceptions.
     */
    public List<TypeSignature> getThrowsSignature() {
        return this.exceptions;
    }

}

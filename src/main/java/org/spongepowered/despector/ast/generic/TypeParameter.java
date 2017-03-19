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

import java.util.ArrayList;
import java.util.List;

public class TypeParameter {

    private String identifier;
    private TypeSignature class_bound;
    private List<TypeSignature> interface_bounds = new ArrayList<>();

    public TypeParameter(String ident, TypeSignature cl) {
        this.identifier = ident;
        this.class_bound = cl;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String ident) {
        this.identifier = ident;
    }

    public TypeSignature getClassBound() {
        return this.class_bound;
    }

    public void setClassBound(TypeSignature sig) {
        this.class_bound = sig;
    }

    public List<TypeSignature> getInterfaceBounds() {
        return this.interface_bounds;
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

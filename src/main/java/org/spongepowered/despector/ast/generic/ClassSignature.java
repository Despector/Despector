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

public class ClassSignature {

    private final List<TypeParameter> parameters = new ArrayList<>();
    private ClassTypeSignature superclass;
    private final List<ClassTypeSignature> interfaces = new ArrayList<>();

    public ClassSignature() {

    }

    public List<TypeParameter> getParameters() {
        return this.parameters;
    }

    public ClassTypeSignature getSuperclassSignature() {
        return this.superclass;
    }

    public void setSuperclassSignature(ClassTypeSignature sig) {
        this.superclass = sig;
    }

    public List<ClassTypeSignature> getInterfaceSignatures() {
        return this.interfaces;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (!this.parameters.isEmpty()) {
            str.append("<");
            for (TypeParameter param : this.parameters) {
                str.append(param);
            }
            str.append(">");
        }
        str.append(this.superclass);
        for (ClassTypeSignature inter : this.interfaces) {
            str.append(inter);
        }
        return str.toString();
    }
}

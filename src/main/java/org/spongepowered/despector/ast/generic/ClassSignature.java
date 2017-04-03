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

import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A class signature containing information of generic types on the class and
 * the direct supertype and superinterfaces.
 */
public class ClassSignature {

    private final List<TypeParameter> parameters = new ArrayList<>();
    @Nullable private ClassTypeSignature superclass;
    private final List<ClassTypeSignature> interfaces = new ArrayList<>();

    public ClassSignature() {
    }

    /**
     * Gets the type paramters of this class. The returned collection is
     * mutable.
     */
    public List<TypeParameter> getParameters() {
        return this.parameters;
    }

    /**
     * Gets the type signature of the direct superclass.
     */
    @Nullable
    public ClassTypeSignature getSuperclassSignature() {
        return this.superclass;
    }

    /**
     * Sets the type signature of the direct superclass.
     */
    public void setSuperclassSignature(@Nullable ClassTypeSignature sig) {
        this.superclass = sig;
    }

    /**
     * Gets the type signatures of the direct superinterfaces. The returned
     * collection is mutable.
     */
    public List<ClassTypeSignature> getInterfaceSignatures() {
        return this.interfaces;
    }

    public void writeTo(MessagePacker pack) throws IOException {
        int len = 3;
        if (this.superclass != null) {
            len++;
        }
        pack.startMap(len);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_CLASS);
        pack.writeString("parameters").startArray(this.parameters.size());
        for (TypeParameter param : this.parameters) {
            param.writeTo(pack);
        }
        if (this.superclass != null) {
            pack.writeString("superclass");
            this.superclass.writeTo(pack);
        }
        pack.writeString("interfaces").startArray(this.interfaces.size());
        for (ClassTypeSignature sig : this.interfaces) {
            sig.writeTo(pack);
        }
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

    @Override
    public int hashCode() {
        int h = 1;
        for (int i = 0; i < this.parameters.size(); i++) {
            h = h * 37 + this.parameters.get(i).hashCode();
        }
        h = h * 37 + (this.superclass == null ? 0 : this.superclass.hashCode());
        for (int i = 0; i < this.interfaces.size(); i++) {
            h = h * 37 + this.interfaces.get(i).hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ClassSignature)) {
            return false;
        }
        ClassSignature sig = (ClassSignature) o;
        if (this.superclass == null) {
            if (sig.superclass != null) {
                return false;
            }
        } else if (!this.superclass.equals(sig.superclass)) {
            return false;
        }
        if (this.parameters.size() != sig.parameters.size()) {
            return false;
        }
        for (int i = 0; i < this.parameters.size(); i++) {
            if (!this.parameters.get(i).equals(sig.parameters.get(i))) {
                return false;
            }
        }
        if (this.interfaces.size() != sig.interfaces.size()) {
            return false;
        }
        for (int i = 0; i < this.interfaces.size(); i++) {
            if (!this.interfaces.get(i).equals(sig.interfaces.get(i))) {
                return false;
            }
        }
        return true;
    }
}

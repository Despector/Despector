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

public class ClassTypeSignature extends TypeSignature {

    private String type_name;
    private List<TypeArgument> args = new ArrayList<>();

    public ClassTypeSignature(String type) {
        this.type_name = type;
    }

    public String getTypeName() {
        return this.type_name;
    }

    public void setTypeName(String type) {
        this.type_name = type;
    }

    public List<TypeArgument> getArguments() {
        return this.args;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("L");
        str.append(this.type_name);
        if (!this.args.isEmpty()) {
            str.append("<");
            for (TypeArgument arg : this.args) {
                str.append(arg);
            }
            str.append(">");
        }
        str.append(";");
        return str.toString();
    }

}

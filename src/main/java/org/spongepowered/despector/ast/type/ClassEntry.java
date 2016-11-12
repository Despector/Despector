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
package org.spongepowered.despector.ast.type;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * Represents a class type.
 */
public class ClassEntry extends TypeEntry {

    protected GenericType superclass = null;

    public ClassEntry(SourceSet src) {
        super(src);
    }

    /**
     * Gets the super class of this type. May be null prior to the lock, but
     * will never be null after.
     */
    public String getSuperclass() {
        return this.superclass.getType();
    }

    public String getSuperclassName() {
        return TypeHelper.descToType(this.superclass.getType());
    }

    /**
     * Sets the super class of this entry.
     */
    public void setSuperclass(String c) {
        setSuperclass(c, null);
    }

    /**
     * Sets the super class of this entry.
     */
    public void setSuperclass(String c, List<String> generics) {
        checkNotNull(c);
        this.superclass = new GenericType(c, generics);
    }

    // TODO override method getters to check superclass

}

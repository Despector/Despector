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
package org.spongepowered.despector.ast.members;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An ast entry for fields.
 */
public class FieldEntry extends AstEntry {

    protected AccessModifier access = AccessModifier.PACKAGE_PRIVATE;

    protected String owner;
    protected String type;
    protected String name;

    protected boolean is_final;
    protected boolean is_static;
    protected boolean is_synthetic;

    public FieldEntry(SourceSet source) {
        super(source);
    }

    public AccessModifier getAccessModifier() {
        return this.access;
    }

    public void setAccessModifier(AccessModifier mod) {
        this.access = checkNotNull(mod, "AccessModififer");
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinal() {
        return this.is_final;
    }

    public void setFinal(boolean state) {
        this.is_final = state;
    }

    /**
     * Gets if this field is static.
     */
    public boolean isStatic() {
        return this.is_static;
    }

    public void setStatic(boolean state) {
        this.is_static = state;
    }

    public boolean isSynthetic() {
        return this.is_synthetic;
    }

    public void setSynthetic(boolean state) {
        this.is_synthetic = state;
    }

    /**
     * Gets the type which owns this field.
     */
    public String getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return TypeHelper.descToType(this.owner);
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets the type of this field.
     */
    public String getType() {
        return this.type;
    }

    public String getTypeName() {
        return TypeHelper.descToType(this.type);
    }

    /**
     * Sets the type of this field.
     * 
     * <p>The field type can only be set once, and cannot be set after the field
     * entry has been locked.</p>
     */
    public void setType(String type) {
        this.type = checkNotNull(type);
    }

    @Override
    public String toString() {
        return (this.is_static ? "Static " : "") + "Field " + this.type + " " + this.name;
    }
}

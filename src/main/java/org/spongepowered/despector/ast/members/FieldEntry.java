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
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

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

    protected Instruction init;

    protected final Map<AnnotationType, Annotation> annotations = new LinkedHashMap<>();

    protected TypeSignature signature;

    public FieldEntry(SourceSet source) {
        super(source);
    }

    /**
     * Gets the access modifier of this field.
     */
    public AccessModifier getAccessModifier() {
        return this.access;
    }

    /**
     * Sets the access modifier of this field.
     */
    public void setAccessModifier(AccessModifier mod) {
        this.access = checkNotNull(mod, "AccessModififer");
    }

    /**
     * Gets the name of this field.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this field.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the generic signature of this field.
     */
    @Nullable
    public TypeSignature getSignature() {
        return this.signature;
    }

    /**
     * Sets the generic signature of this field.
     */
    public void setSignature(@Nullable TypeSignature sig) {
        this.signature = sig;
    }

    /**
     * Gets if this field is final.
     */
    public boolean isFinal() {
        return this.is_final;
    }

    /**
     * Sets if this field is final.
     */
    public void setFinal(boolean state) {
        this.is_final = state;
    }

    /**
     * Gets if this field is static.
     */
    public boolean isStatic() {
        return this.is_static;
    }

    /**
     * Sets if this field is static.
     */
    public void setStatic(boolean state) {
        this.is_static = state;
    }

    /**
     * Gets if this field is synthetic.
     */
    public boolean isSynthetic() {
        return this.is_synthetic;
    }

    /**
     * Sets if this field is synthetic.
     */
    public void setSynthetic(boolean state) {
        this.is_synthetic = state;
    }

    /**
     * Gets the initializer value of this field, if present.
     */
    @Nullable
    public Instruction getInitializer() {
        return this.init;
    }

    /**
     * Sets the initializer value of this field.
     */
    public void setInitializer(Instruction insn) {
        this.init = checkNotNull(insn, "initializer");
    }

    /**
     * Gets the type description of the owner of this field.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Gets the internal name of the owner of this field.
     */
    public String getOwnerName() {
        return TypeHelper.descToType(this.owner);
    }

    /**
     * Sets the type description of the owner of this field.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets the type description of this field.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the internal name of the type of this field.
     */
    public String getTypeName() {
        return TypeHelper.descToType(this.type);
    }

    /**
     * Sets the type of this field.
     */
    public void setType(String type) {
        this.type = checkNotNull(type);
    }

    /**
     * Gets the annotation present on this field of the given type, if present.
     */
    @Nullable
    public Annotation getAnnotation(AnnotationType type) {
        return this.annotations.get(type);
    }

    /**
     * Gets all annotations on this field.
     */
    public Collection<Annotation> getAnnotations() {
        return this.annotations.values();
    }

    /**
     * Adds the given annotation onto this field.
     */
    public void addAnnotation(Annotation anno) {
        this.annotations.put(anno.getType(), anno);
    }

    @Override
    public String toString() {
        return (this.is_static ? "Static " : "") + "Field " + this.type + " " + this.name;
    }
}

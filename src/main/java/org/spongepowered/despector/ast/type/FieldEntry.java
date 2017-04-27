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
package org.spongepowered.despector.ast.type;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
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
    protected TypeSignature type;
    protected String name;

    protected boolean is_final;
    protected boolean is_static;
    protected boolean is_synthetic;
    protected boolean is_volatile;
    protected boolean is_transient;
    protected boolean is_deprecated;

    @Nullable
    protected Instruction init;

    protected final Map<AnnotationType, Annotation> annotations = new LinkedHashMap<>();

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

    public boolean isVolatile() {
        return this.is_volatile;
    }

    public void setVolatile(boolean state) {
        this.is_volatile = state;
    }

    public boolean isTransient() {
        return this.is_transient;
    }

    public void setTransient(boolean state) {
        this.is_transient = state;
    }

    public boolean isDeprecated() {
        return this.is_deprecated;
    }

    public void setDeprecated(boolean state) {
        this.is_deprecated = state;
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
    public void setInitializer(@Nullable Instruction insn) {
        this.init = insn;
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
    public TypeSignature getType() {
        return this.type;
    }

    /**
     * Gets the internal name of the type of this field.
     */
    public String getTypeName() {
        return this.type.getName();
    }

    /**
     * Sets the type of this field.
     */
    public void setType(TypeSignature type) {
        this.type = checkNotNull(type, "type");
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

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(11);
        pack.writeString("id").writeInt(AstSerializer.ENTRY_ID_FIELD);
        pack.writeString("access").writeInt(this.access.ordinal());
        pack.writeString("name").writeString(this.name);
        pack.writeString("type");
        this.type.writeTo(pack);
        pack.writeString("final").writeBool(this.is_final);
        pack.writeString("static").writeBool(this.is_static);
        pack.writeString("synthetic").writeBool(this.is_synthetic);
        pack.writeString("volatile").writeBool(this.is_volatile);
        pack.writeString("deprecated").writeBool(this.is_deprecated);
        pack.writeString("transient").writeBool(this.is_transient);
        pack.writeString("annotations").startArray(this.annotations.size());
        for (Annotation anno : this.annotations.values()) {
            anno.writeTo(pack);
        }
        pack.endArray();
        pack.endMap();
    }

    public void accept(AstVisitor visitor) {
        if (visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitField(this);
        }
    }
}

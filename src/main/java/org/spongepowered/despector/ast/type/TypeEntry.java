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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Represents a type, may be a class, interface, or enum.
 */
public abstract class TypeEntry extends AstEntry {

    protected Language lang;

    protected AccessModifier access;

    protected boolean is_synthetic;
    protected boolean is_final;
    protected boolean inner_class;

    protected final String name;

    protected final List<String> interfaces = new ArrayList<>();

    protected final Map<String, FieldEntry> static_fields = new LinkedHashMap<>();
    protected final Multimap<String, MethodEntry> static_methods = LinkedHashMultimap.create();

    protected final Map<String, FieldEntry> fields = new LinkedHashMap<>();
    protected final Multimap<String, MethodEntry> methods = LinkedHashMultimap.create();

    protected final Map<AnnotationType, Annotation> annotations = new LinkedHashMap<>();
    protected final Map<String, InnerClassInfo> inner_classes = new LinkedHashMap<>();

    @Nullable protected ClassSignature signature;

    public TypeEntry(SourceSet source, Language lang, String name) {
        super(source);
        this.name = checkNotNull(name, "name");
        checkArgument(lang != Language.ANY, "Type language cannot be set to any");
        this.lang = lang;
        this.inner_class = name.contains("$");
    }

    public Language getLanguage() {
        return this.lang;
    }

    public AccessModifier getAccessModifier() {
        return this.access;
    }

    public void setAccessModifier(AccessModifier access) {
        this.access = checkNotNull(access, "AccessModifier");
    }

    public boolean isFinal() {
        return this.is_final;
    }

    public void setFinal(boolean state) {
        this.is_final = state;
    }

    /**
     * Gets if this type is synthetic.
     */
    public boolean isSynthetic() {
        return this.is_synthetic;
    }

    /**
     * Sets if this type is synthetic.
     */
    public void setSynthetic(boolean state) {
        this.is_synthetic = state;
    }

    /**
     * Gets if this type is an inner class of another type.
     */
    public boolean isInnerClass() {
        return this.inner_class;
    }

    /**
     * Gets the type internal name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the type descriptor.
     */
    public String getDescriptor() {
        return "L" + this.name + ";";
    }

    @Nullable
    public ClassSignature getSignature() {
        return this.signature;
    }

    public void setSignature(@Nullable ClassSignature signature) {
        this.signature = signature;
    }

    /**
     * Gets if this is an anonymous type.
     */
    public boolean isAnonType() {
        return TypeHelper.isAnonClass(this.name);
    }

    /**
     * Gets all interfaces that this type implements.
     */
    public List<String> getInterfaces() {
        return this.interfaces;
    }

    /**
     * Adds an interface for this type. Can only be used pre lock.
     */
    public void addInterface(String inter) {
        checkNotNull(inter);
        this.interfaces.add(inter);
    }

    protected MethodEntry findMethod(String name, Multimap<String, MethodEntry> map) {
        MethodEntry ret = null;
        for (MethodEntry m : map.values()) {
            if (m.getName().equals(name)) {
                if (ret != null) {
                    throw new IllegalStateException("Tried to get ambiguous method " + name);
                }
                ret = m;
            }
        }
        return ret;
    }

    protected MethodEntry findMethod(String name, String sig, Multimap<String, MethodEntry> map) {
        for (MethodEntry m : map.values()) {
            if (m.getName().equals(name) && m.getDescription().equals(sig)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Gets a static method with the given obfuscated name. If there is no
     * method with this name then an {@link IllegalStateException} is thrown. If
     * there are multiple methods defined with the same name then it is
     * undefined which one is returned.
     */
    public MethodEntry getStaticMethod(String name) {
        checkNotNull(name);
        MethodEntry ret = findMethod(name, this.static_methods);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown static method: " + name + " on class " + this);
        }
        return ret;
    }

    /**
     * Gets a static method with the given obfuscated name and signature. If
     * there is no method with this name then an {@link IllegalStateException}
     * is thrown.
     */
    public MethodEntry getStaticMethod(String name, String sig) {
        checkNotNull(name);
        MethodEntry ret = findMethod(name, sig, this.static_methods);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown single method: " + name + sig + " on class " + this);
        }
        return ret;
    }

    public MethodEntry getStaticMethodSafe(String name) {
        checkNotNull(name);
        return findMethod(name, this.static_methods);
    }

    /**
     * Gets the static method with the given obfuscated name and signature, or
     * null if not found.
     */
    public MethodEntry getStaticMethodSafe(String name, String sig) {
        checkNotNull(name);
        return findMethod(name, sig, this.static_methods);
    }

    /**
     * Gets an instance method with the given name. If no method with the name
     * is found then an {@link IllegalArgumentException} is thrown. If there are
     * multiple instance methods with the same name then it is undefined which
     * one is returned.
     */
    public MethodEntry getMethod(String name) {
        checkNotNull(name);
        MethodEntry ret = findMethod(name, this.methods);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown method: " + name + " on class " + this);
        }
        return ret;
    }

    /**
     * Gets the method with the given obfuscated name and signature from this
     * type, if not found then an {@link IllegalArgumentException} is thrown.
     */
    public MethodEntry getMethod(String name, String sig) {
        checkNotNull(name);
        checkNotNull(sig);
        MethodEntry ret = findMethod(name, sig, this.methods);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown method: " + name + sig + " on class " + this);
        }
        return ret;
    }

    public MethodEntry getMethodSafe(String name) {
        checkNotNull(name);
        return findMethod(name, this.methods);
    }

    /**
     * Gets the method with the given obfuscated name and signature from this
     * type, or null if not found.
     */
    public MethodEntry getMethodSafe(String name, String sig) {
        checkNotNull(name);
        checkNotNull(sig);
        return findMethod(name, sig, this.methods);
    }

    /**
     * Adds the given method to this type.
     */
    public void addMethod(MethodEntry m) {
        checkNotNull(m);
        if (m.isStatic()) {
            MethodEntry existing = getStaticMethodSafe(m.getName(), m.getDescription());
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate method " + existing);
            }
            this.static_methods.put(m.getName(), m);
        } else {
            MethodEntry existing = getMethodSafe(m.getName(), m.getDescription());
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate method " + existing);
            }
            this.methods.put(m.getName(), m);
        }
    }

    public int getMethodCount() {
        return this.methods.size();
    }

    public int getStaticMethodCount() {
        return this.static_methods.size();
    }

    public Collection<MethodEntry> getMethods() {
        return this.methods.values();
    }

    public Collection<MethodEntry> getStaticMethods() {
        return this.static_methods.values();
    }

    protected FieldEntry findField(String name, Map<String, FieldEntry> map) {
        FieldEntry e = map.get(name);
        return e;
    }

    /**
     * Gets the static field with the given obfuscated name. If the field is not
     * found then an {@link IllegalStateException} is thrown.
     */
    public FieldEntry getStaticField(String name) {
        checkNotNull(name);
        FieldEntry e = findField(name, this.static_fields);
        if (e == null) {
            throw new IllegalArgumentException("Unknown field: " + name + " on class " + this);
        }
        return e;
    }

    public FieldEntry getStaticFieldSafe(String name) {
        checkNotNull(name);
        return findField(name, this.static_fields);
    }

    /**
     * Gets the field with the given obfuscated name. If the field is not found
     * then an {@link IllegalStateException} is thrown.
     */
    public FieldEntry getField(String name) {
        checkNotNull(name);
        FieldEntry e = findField(name, this.fields);
        if (e == null) {
            throw new IllegalArgumentException("Unknown field: " + name + " on class " + this);
        }
        return e;
    }

    public FieldEntry getFieldSafe(String name) {
        checkNotNull(name);
        return findField(name, this.fields);
    }

    /**
     * Adds the given field entry to this type.
     */
    public void addField(FieldEntry f) {
        checkNotNull(f);
        if (f.isStatic()) {
            FieldEntry existing = this.static_fields.get(f.getName());
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate static field " + f.getName());
            }
            this.static_fields.put(f.getName(), f);
        } else {
            FieldEntry existing = this.fields.get(f.getName());
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate field " + f.getName());
            }
            this.fields.put(f.getName(), f);
        }
    }

    public int getFieldCount() {
        return this.fields.size();
    }

    public int getStaticFieldCount() {
        return this.static_fields.size();
    }

    public Collection<FieldEntry> getFields() {
        return this.fields.values();
    }

    public Collection<FieldEntry> getStaticFields() {
        return this.static_fields.values();
    }

    public Annotation getAnnotation(AnnotationType type) {
        return this.annotations.get(type);
    }

    public Collection<Annotation> getAnnotations() {
        return this.annotations.values();
    }

    public void addAnnotation(Annotation anno) {
        this.annotations.put(anno.getType(), anno);
    }

    public InnerClassInfo getInnerClassInfo(String name) {
        return this.inner_classes.get(name);
    }

    public Collection<InnerClassInfo> getInnerClasses() {
        return this.inner_classes.values();
    }

    public void addInnerClass(String name, String simple, String outer, int acc) {
        this.inner_classes.put(name, new InnerClassInfo(name, simple, outer, acc));
    }

    @Override
    public String toString() {
        return "Class " + this.name;
    }

    public void writeTo(MessagePacker pack, int extra, int id) throws IOException {
        int len = 15 + extra;
        if (this.signature != null) {
            len++;
        }
        pack.startMap(len);
        pack.writeString("id").writeInt(id);
        pack.writeString("language").writeInt(this.lang.ordinal());
        pack.writeString("access").writeInt(this.access.ordinal());
        pack.writeString("synthetic").writeBool(this.is_synthetic);
        pack.writeString("final").writeBool(this.is_final);
        pack.writeString("innerclass").writeBool(isInnerClass());
        pack.writeString("name").writeString(this.name);
        pack.writeString("interfaces").startArray(this.interfaces.size());
        for (String inter : this.interfaces) {
            pack.writeString(inter);
        }
        pack.writeString("staticfields").startArray(this.static_fields.size());
        for (FieldEntry fld : this.static_fields.values()) {
            fld.writeTo(pack);
        }
        pack.writeString("fields").startArray(this.fields.size());
        for (FieldEntry fld : this.fields.values()) {
            fld.writeTo(pack);
        }
        pack.writeString("staticmethods").startArray(this.static_methods.size());
        for (MethodEntry mth : this.static_methods.values()) {
            mth.writeTo(pack);
        }
        pack.writeString("methods").startArray(this.methods.size());
        for (MethodEntry mth : this.methods.values()) {
            mth.writeTo(pack);
        }
        if (this.signature != null) {
            pack.writeString("signature");
            this.signature.writeTo(pack);
        }
        pack.writeString("annotations").startArray(this.annotations.size());
        for (Annotation anno : this.annotations.values()) {
            anno.writeTo(pack);
        }
        pack.writeString("inner_classes").startArray(this.inner_classes.size());
        for (InnerClassInfo info : this.inner_classes.values()) {
            info.writeTo(pack);
        }
    }

    public static class InnerClassInfo {

        private String name;
        private String simple_name;
        private String outer_name;
        private boolean is_static;
        private AccessModifier access;
        private boolean is_final;
        private boolean is_abstract;
        private boolean is_synthetic;

        public InnerClassInfo(String name, String simple, String outer, int acc) {
            this.name = name;
            this.simple_name = simple;
            this.outer_name = outer;
            this.is_static = (acc & ACC_STATIC) != 0;
            this.is_final = (acc & ACC_FINAL) != 0;
            this.is_abstract = (acc & ACC_ABSTRACT) != 0;
            this.is_synthetic = (acc & ACC_SYNTHETIC) != 0;
            this.access = AccessModifier.fromModifiers(acc);
        }

        public void writeTo(MessagePacker pack) throws IOException {
            pack.startMap(8);
            pack.writeString("name").writeString(this.name);
            pack.writeString("simple_name").writeString(this.simple_name);
            pack.writeString("outer_name").writeString(this.outer_name);
            pack.writeString("static").writeBool(this.is_static);
            pack.writeString("final").writeBool(this.is_final);
            pack.writeString("abstract").writeBool(this.is_abstract);
            pack.writeString("synthetic").writeBool(this.is_synthetic);
            pack.writeString("access").writeInt(this.access.ordinal());
        }

        public String getName() {
            return this.name;
        }

        public String getSimpleName() {
            return this.simple_name;
        }

        public String getOuterName() {
            return this.outer_name;
        }

        public AccessModifier getAccessModifier() {
            return this.access;
        }

        public boolean isStatic() {
            return this.is_static;
        }

        public boolean isFinal() {
            return this.is_final;
        }

        public boolean isAbstract() {
            return this.is_abstract;
        }

        public boolean isSynthetic() {
            return this.is_synthetic;
        }
    }

}

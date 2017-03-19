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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a type, may be a class, interface, or enum.
 */
public abstract class TypeEntry extends AstEntry {

    protected AccessModifier access;

    protected boolean is_synthetic;
    protected boolean is_final;

    protected final String name;

    protected final List<GenericType> generic_interfaces = new ArrayList<>();
    protected final List<String> interfaces = new ArrayList<>();

    protected final Map<String, FieldEntry> static_fields = new LinkedHashMap<>();
    protected final Multimap<String, MethodEntry> static_methods = LinkedHashMultimap.create();

    protected final Map<String, FieldEntry> fields = new LinkedHashMap<>();
    protected final Multimap<String, MethodEntry> methods = LinkedHashMultimap.create();

    protected final List<GenericArgument> generic_args = new ArrayList<>();
    protected final Map<AnnotationType, Annotation> annotations = new LinkedHashMap<>();

    public TypeEntry(SourceSet source, String name) {
        super(source);
        this.name = checkNotNull(name, "name");
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

    public boolean isSynthetic() {
        return this.is_synthetic;
    }

    public void setSynthetic(boolean state) {
        this.is_synthetic = state;
    }

    /**
     * Gets the obfuscated name.
     */
    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return "L" + this.name + ";";
    }

    // TODO
//    public boolean isStatic() {
//        return !this.fields.containsKey("this$0");
//    }

    public List<GenericArgument> getGenericArgs() {
        return this.generic_args;
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

    public List<GenericType> getGenericInterfaces() {
        return this.generic_interfaces;
    }

    /**
     * Adds an interface for this type. Can only be used pre lock.
     */
    public void addInterface(String inter) {
        addInterface(inter, null);
    }

    public void addInterface(String inter, List<String> generics) {
        checkNotNull(inter);
        this.interfaces.add(inter);
        this.generic_interfaces.add(new GenericType(inter, generics));
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
//        if (ret == null && this.actual_class != null) {
//            Method found = null;
//            for (Method mth : this.actual_class.getMethods()) {
//                if (mth.getName().equals(name)) {
//                    if (found != null) {
//                        throw new IllegalStateException("Tried to get ambiguous method " + name);
//                    }
//                    found = mth;
//                }
//            }
//            String sig = "(";
//            for (Class<?> param : found.getParameterTypes()) {
//                sig += Type.getDescriptor(param);
//            }
//            sig += ")";
//            sig += Type.getDescriptor(found.getReturnType());
//            MethodEntry dummy =
//                    new MethodEntry(this, AccessModifier.fromModifiers(found.getModifiers()), name, sig, Modifier.isStatic(found.getModifiers()));
//            dummy.setReturnType(this.set.get(Type.getInternalName(found.getReturnType())));
//            if (Modifier.isStatic(found.getModifiers())) {
//                this.static_methods.put(name, dummy);
//            } else {
//                this.methods.put(name, dummy);
//            }
//            return dummy;
//        }
        return ret;
    }

    protected MethodEntry findMethod(String name, String sig, Multimap<String, MethodEntry> map) {
        for (MethodEntry m : map.values()) {
            if (m.getName().equals(name) && m.getSignature().equals(sig)) {
                return m;
            }
        }
//        if (this.actual_class != null) {
//            List<TypeEntry> params = Lists.newArrayList();
//            for (String param : TypeHelper.splitSig(sig)) {
//                params.add(this.set.get(TypeHelper.descToType(param)));
//            }
//            Class<?>[] args = new Class<?>[params.size()];
//            for (int i = 0; i < args.length; i++) {
//                args[i] = params.get(i).getActualClass();
//            }
//            if ("<init>".equals(name)) {
//                try {
//                    Constructor<?> ctor = this.actual_class.getConstructor(args);
//                    MethodEntry dummy = new MethodEntry(this, AccessModifier.fromModifiers(ctor.getModifiers()), name, sig, false);
//                    dummy.setReturnType(ClasspathSourceSet.void_t);
//                    this.methods.put(name, dummy);
//                    return dummy;
//                } catch (NoSuchMethodException | SecurityException e) {
//                    // TODO cache failures
//                }
//                MethodEntry dummy = new MethodEntry(this, AccessModifier.PACKAGE_PRIVATE, name, sig, false);
//                dummy.setReturnType(ClasspathSourceSet.void_t);
//                this.methods.put(name, dummy);
//                return dummy;
//            }
//            try {
//                Method method = this.actual_class.getDeclaredMethod(name, args);
//                MethodEntry dummy = new MethodEntry(this, AccessModifier.fromModifiers(method.getModifiers()), name, sig,
//                        Modifier.isStatic(method.getModifiers()));
//                dummy.setReturnType(this.set.get(Type.getInternalName(method.getReturnType())));
//                if (Modifier.isStatic(method.getModifiers())) {
//                    this.static_methods.put(name, dummy);
//                } else {
//                    this.methods.put(name, dummy);
//                }
//                return dummy;
//            } catch (NoSuchMethodException | SecurityException e) {
//                e.printStackTrace();
//            }
//        }
//        if (name.equals("clone") && sig.equals("()Ljava/lang/Object;")) {
//            MethodEntry dummy = new MethodEntry(this, AccessModifier.PUBLIC, name, sig, false);
//            dummy.setReturnType(ClasspathSourceSet.object_t);
//            this.methods.put(name, dummy);
//            return dummy;
//        } else if (getClass() == EnumEntry.class) {
//            if (name.equals("ordinal") && sig.equals("()I")) {
//                MethodEntry dummy = new MethodEntry(this, AccessModifier.PUBLIC, name, sig, false);
//                dummy.setReturnType(ClasspathSourceSet.int_t);
//                this.methods.put(name, dummy);
//                return dummy;
//            }
//        }
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
            MethodEntry existing = getStaticMethodSafe(m.getName(), m.getSignature());
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate method " + existing);
            }
            this.static_methods.put(m.getName(), m);
        } else {
            MethodEntry existing = getMethodSafe(m.getName(), m.getSignature());
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

//        if (e == null && this.actual_class != null) {
//            try {
//                Field field = this.actual_class.getDeclaredField(name);
//                boolean is_static = Modifier.isStatic(field.getModifiers());
//                FieldEntry dummy = new FieldEntry(this, AccessModifier.fromModifiers(field.getModifiers()), name, is_static);
//                dummy.setType(ClasspathSourceSet.classpath.get(Type.getInternalName(field.getType())));
//                if (is_static) {
//                    this.static_fields.put(name, dummy);
//                } else {
//                    this.fields.put(name, dummy);
//                }
//                return dummy;
//            } catch (NoSuchFieldException | SecurityException e1) {
//                e1.printStackTrace();
//            }
//        }

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

    @Override
    public String toString() {
        return "Class " + this.name;
    }

}

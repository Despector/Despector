/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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
package org.spongepowered.despector.ast;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.WildcardType;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.BaseDecompiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReferenceSourceSet extends SourceSet {

    private static final Language REF_LANG = new Language("ref", null);

    private final SourceSet tree;

    public ReferenceSourceSet(SourceSet tree) {
        this.tree = tree;
    }

    @Override
    public void add(TypeEntry e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeEntry get(String name) {
        TypeEntry src = this.tree.get(name);
        if (src != null) {
            return src;
        }
        src = this.classes.get(name);
        if (src != null) {
            return src;
        }
        try {
            Class<?> cls = Class.forName(name.replace('/', '.'));
            src = makeReferenceType(cls);
            super.add(src);
            return src;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TypeSignature getTypeOf(Type type) {
        if (type instanceof Class) {
            return ClassTypeSignature.of("L" + ((Class<?>) type).getName().replace('.', '/') + ";");
        }
        throw new IllegalStateException(type.toString());
    }

    private GenericClassTypeSignature getClassTypeOf(Type type) {
        if (type instanceof Class) {
            return new GenericClassTypeSignature("L" + ((Class<?>) type).getName().replace('.', '/') + ";");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            Type raw = p.getRawType();
            GenericClassTypeSignature sig = new GenericClassTypeSignature("L" + ((Class<?>) raw).getName().replace('.', '/') + ";");
            for (Type a : p.getActualTypeArguments()) {
                if (a instanceof Class) {
                    TypeSignature val = ClassTypeSignature.of("L" + ((Class<?>) a).getName().replace('.', '/') + ";");
                    sig.getArguments().add(new TypeArgument(WildcardType.NONE, val));
                }
                throw new IllegalStateException(a.toString());
            }
        }
        throw new IllegalStateException(type.toString());
    }

    private TypeEntry makeReferenceType(Class<?> cls) {
        TypeEntry entry = null;
        String name = cls.getName().replace('.', '/');
        if (cls.isAnnotation()) {
            entry = new AnnotationEntry(this, REF_LANG, name);
        } else if (cls.isInterface()) {
            entry = new InterfaceEntry(this, REF_LANG, name);
        } else if (cls.isEnum()) {
            entry = new EnumEntry(this, REF_LANG, name);
        } else {
            entry = new ClassEntry(this, REF_LANG, name);
            ((ClassEntry) entry).setSuperclass(cls.getSuperclass().getName().replace('.', '/'));
        }
        ClassSignature super_sig = new ClassSignature();
        super_sig.setSuperclassSignature(getClassTypeOf(cls.getGenericSuperclass()));
        for (Type inter : cls.getGenericInterfaces()) {
            super_sig.getInterfaceSignatures().add(getClassTypeOf(inter));
        }
        entry.setSignature(super_sig);

        // TODO type annotations

        for (Field field : cls.getDeclaredFields()) {
            FieldEntry fld = new FieldEntry(this);
            fld.setName(field.getName());
            fld.setType(getTypeOf(field.getGenericType()));
            int mod = field.getModifiers();
            fld.setAccessModifier(AccessModifier.fromModifiers(mod));
            fld.setDeprecated(field.isAnnotationPresent(Deprecated.class));
            fld.setFinal(Modifier.isFinal(mod));
            fld.setStatic(Modifier.isStatic(mod));
            fld.setSynthetic(field.isSynthetic());
            fld.setTransient(Modifier.isTransient(mod));
            fld.setVolatile(Modifier.isVolatile(mod));
            entry.addField(fld);
            // TODO field annotations
        }

        for (Method method : cls.getDeclaredMethods()) {
            MethodEntry mth = new MethodEntry(this);
            int mod = method.getModifiers();
            mth.setAbstract(Modifier.isAbstract(mod));
            mth.setAccessModifier(AccessModifier.fromModifiers(mod));
            mth.setBridge((mod & BaseDecompiler.ACC_BRIDGE) != 0);
            mth.setDeprecated(method.isAnnotationPresent(Deprecated.class));
            String desc = "(";
            for (Class<?> p : method.getParameterTypes()) {
                desc += org.objectweb.asm.Type.getType(p).getDescriptor();
            }
            desc += ")";
            desc += org.objectweb.asm.Type.getType(method.getReturnType()).getDescriptor();
            mth.setDescription(desc);
            mth.setFinal(Modifier.isFinal(mod));
            mth.setName(method.getName());
            mth.setOwner(name);
            mth.setNative(Modifier.isNative(mod));
            mth.setStatic(Modifier.isStatic(mod));
            mth.setStrictFp(Modifier.isStrict(mod));
            mth.setSynchronized(Modifier.isSynchronized(mod));
            mth.setSynthetic(method.isSynthetic());
            mth.setVarargs((mod & BaseDecompiler.ACC_VARARGS) != 0);
            entry.addMethod(mth);
            // TODO method signature
            // TODO method annotations
        }

        return entry;
    }

}

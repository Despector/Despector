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
package org.spongepowered.despector.ast;

import org.spongepowered.despector.ast.type.TypeEntry;

/**
 * A sourceset for types which are part of libraries or core java types.
 */
public final class ClasspathSourceSet /* implements SourceSet */ {

//    public static final SourceSet classpath = new ClasspathSourceSet();

    /**
     * The void type.
     */
    public static final TypeEntry void_t = createPrimative("void");
    /**
     * The object type java/lang/Object.
     */
    public static final TypeEntry object_t = createPrimative("java/lang/Object");

    /**
     * The primitive boolean type.
     */
    public static final TypeEntry boolean_t = createPrimative("boolean");
    /**
     * The primitive byte type.
     */
    public static final TypeEntry byte_t = createPrimative("byte");
    /**
     * The primitive char type.
     */
    public static final TypeEntry char_t = createPrimative("char");
    /**
     * The primitive short type.
     */
    public static final TypeEntry short_t = createPrimative("short");
    /**
     * The primitive int type.
     */
    public static final TypeEntry int_t = createPrimative("int");
    /**
     * The primitive long type.
     */
    public static final TypeEntry long_t = createPrimative("long");
    /**
     * The primitive float type.
     */
    public static final TypeEntry float_t = createPrimative("float");
    /**
     * The primitive double type.
     */
    public static final TypeEntry double_t = createPrimative("double");

    /**
     * The string type java/lang/String.
     */
    public static final TypeEntry String = createPrimative("java/lang/String");

    private static TypeEntry createPrimative(String name) {
        return null;
    }

//    private final Set<String> failed_cache = Sets.newHashSet();
//    private final Map<String, TypeEntry> cache = Maps.newHashMap();
//    private final Map<String, ArrayTypeEntry> array_cache = Maps.newHashMap();

//    private ClasspathSourceSet() {
//
//    }
//
//    private boolean checkFailed(String name) {
//        if (name.endsWith("[]")) {
//            return checkFailed(name.substring(0, name.length() - 2));
//        }
//        return this.failed_cache.contains(name);
//    }
//
//    private TypeEntry find(String name) {
//        if (name.endsWith("[]")) {
//            ArrayTypeEntry array = this.array_cache.get(name);
//            if (array == null) {
//                TypeEntry comp = find(name.substring(0, name.length() - 2));
//                array = new ArrayTypeEntry(this, comp);
//                this.array_cache.put(name, array);
//            }
//            return array;
//        }
//        TypeEntry entry = this.cache.get(name);
//        if (entry != null) {
//            return entry;
//        }
//        Class<?> type = TypeHelper.classForTypeName(name);
//        if (Enum.class.isAssignableFrom(type)) {
//            entry = new EnumEntry(this, type);
//        } else if (type.isInterface()) {
//            entry = new InterfaceEntry(this, type);
//        } else {
//            entry = new ClassEntry(this, type);
//        }
//        this.cache.put(name, entry);
//        return entry;
//    }
//
//    @Override
//    public TypeEntry get(String name) {
//        if (checkFailed(name)) {
//            throw new IllegalArgumentException("Unknown classpath type: " + name);
//        }
//        return find(name);
//    }
//
//    @Override
//    public TypeEntry getIfExists(String name) {
//        return get(name);
//    }
//
//    @Override
//    public EnumEntry getEnum(String name) {
//        TypeEntry entry = get(name);
//        if (!(entry instanceof EnumEntry)) {
//            throw new IllegalArgumentException("Type: " + name + " is not an enum type");
//        }
//        return (EnumEntry) entry;
//    }
//
//    @Override
//    public InterfaceEntry getInterface(String name) {
//        TypeEntry entry = get(name);
//        if (!(entry instanceof InterfaceEntry)) {
//            throw new IllegalArgumentException("Type: " + name + " is not an interface type");
//        }
//        return (InterfaceEntry) entry;
//    }
//
//    @Override
//    public MappingsSet getMappings() {
//        throw new UnsupportedOperationException("Classpath source set has no mappings");
//    }
//
//    @Override
//    public MappingsSet getValidationMappings() {
//        throw new UnsupportedOperationException("Classpath source set has no mappings");
//    }
//
//    @Override
//    public boolean hasValidationMappings() {
//        throw new UnsupportedOperationException("Classpath source set has no mappings");
//    }
//
//    @Override
//    public ConflictHandler getConflictHandler() {
//        // classpath entries shouldn't conflict by definition so we'll just
//        // throw an error for the bad case that they do
//        return ConflictHandler.ERROR;
//    }

}

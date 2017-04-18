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
package org.spongepowered.despector.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A source set for types which are part of the obfuscated source being mapped.
 */
public class SourceSet {

    private Loader loader;
    private final Set<String> load_failed_cache = new HashSet<>();

    private final Map<String, TypeEntry> classes = new HashMap<>();
    private final Map<String, EnumEntry> enums = new HashMap<>();
    private final Map<String, InterfaceEntry> interfaces = new HashMap<>();

    private final Map<String, AnnotationType> annotations = new HashMap<>();

    public SourceSet() {
    }

    public Loader getLoader() {
        return this.loader;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    /**
     * Inserts the given type into this source set.
     */
    public void add(TypeEntry e) {
        checkNotNull(e);
        if (e instanceof EnumEntry) {
            this.enums.put(e.getName(), (EnumEntry) e);
        } else if (e instanceof InterfaceEntry) {
            this.interfaces.put(e.getName(), (InterfaceEntry) e);
        }
        this.classes.put(e.getName(), e);
    }

    /**
     * Gets the type with the given internal name.
     */
    public TypeEntry get(String name) {
        checkNotNull(name);
        if (name.endsWith("[]")) {
            return get(name.substring(0, name.length() - 2));
        }
        TypeEntry entry = this.classes.get(name);
        if (entry == null && this.loader != null && !this.load_failed_cache.contains(name)) {
            InputStream data = this.loader.find(name);
            if (data == null) {
                this.load_failed_cache.add(name);
                return null;
            }
            try {
                entry = Decompilers.get(Language.ANY).decompile(data, this);
            } catch (IOException e) {
                e.printStackTrace();
                this.load_failed_cache.add(name);
                return null;
            }
            add(entry);
        }
        return entry;
    }

    public EnumEntry getEnum(String name) {
        EnumEntry entry = this.enums.get(name);
        return entry;
    }

    public InterfaceEntry getInterface(String name) {
        InterfaceEntry entry = this.interfaces.get(name);
        return entry;
    }

    /**
     * Gets all classes in the source set. This also includes all interfaces and
     * enums.
     */
    public Collection<TypeEntry> getAllClasses() {
        return this.classes.values();
    }

    /**
     * Gets all enum types in the source set.
     */
    public Collection<EnumEntry> getAllEnums() {
        return this.enums.values();
    }

    /**
     * Gets all interface types in the source set.
     */
    public Collection<InterfaceEntry> getAllInterfaces() {
        return this.interfaces.values();
    }

    public void addAnnotation(AnnotationType anno) {
        this.annotations.put(anno.getName(), anno);
    }

    /**
     * Gets the annotation type with the given internal name.
     */
    public AnnotationType getAnnotationType(String name) {
        AnnotationType anno = this.annotations.get(name);
        if (anno == null) {
            anno = new AnnotationType(name);
            this.annotations.put(name, anno);
        }
        return anno;
    }

    public Collection<AnnotationType> getAllAnnotations() {
        return this.annotations.values();
    }

    /**
     * Writes this source set to the given {@link MessagePacker}.
     */
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("version").writeInt(AstSerializer.VERSION);
        pack.writeString("classes");
        pack.startArray(this.classes.size());
        for (TypeEntry type : this.classes.values()) {
            type.writeTo(pack);
        }
    }

    /**
     * A loader which from which new types can be requested on demand.
     */
    public static interface Loader {

        InputStream find(String name);

    }

}

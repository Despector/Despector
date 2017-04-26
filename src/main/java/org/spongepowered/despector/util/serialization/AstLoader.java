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
package org.spongepowered.despector.util.serialization;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.BaseDecompiler;

import java.io.IOException;
import java.io.InputStream;

public class AstLoader {

    private static void expectKey(MessageUnpacker unpack, String key) throws IOException {
        String actual = unpack.readString();
        if (!actual.equals(key)) {
            throw new IllegalStateException("Expected key " + key + " but was " + actual);
        }
    }

    private static void startMap(MessageUnpacker unpack, int size) throws IOException {
        int actual = unpack.readMap();
        if (actual != size) {
            throw new IllegalStateException("Expected map size " + size + " but was " + actual);
        }
    }

    public static void loadSources(SourceSet set, InputStream stream) throws IOException {
        MessageUnpacker unpack = new MessageUnpacker(stream);
        startMap(unpack, 2);
        expectKey(unpack, "version");
        int version = unpack.readInt();
        if (version != AstSerializer.VERSION) {
            throw new IllegalStateException("Unsupported ast version " + version);
        }
        expectKey(unpack, "classes");
        int classes = unpack.readArray();
        for (int i = 0; i < classes; i++) {
            set.add(loadType(unpack, set));
        }
    }

    public static TypeEntry loadType(MessageUnpacker unpack, SourceSet set) throws IOException {
        TypeEntry entry = null;
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id == AstSerializer.ENTRY_ID_CLASS) {
            expectKey(unpack, "lang");
            Language lang = Language.values()[unpack.readInt()];
            expectKey(unpack, "name");
            String name = unpack.readString();
            entry = new ClassEntry(set, lang, name);
        }
        expectKey(unpack, "access");
        entry.setAccessModifier(AccessModifier.values()[unpack.readInt()]);
        expectKey(unpack, "synthetic");
        entry.setSynthetic(unpack.readBool());
        expectKey(unpack, "final");
        entry.setFinal(unpack.readBool());
        expectKey(unpack, "interfaces");
        int interfaces = unpack.readArray();
        for (int i = 0; i < interfaces; i++) {
            entry.getInterfaces().add(unpack.readString());
        }
        expectKey(unpack, "staticfields");
        int staticfields = unpack.readArray();
        for (int i = 0; i < staticfields; i++) {
            entry.addField(loadField(unpack, set));
        }
        expectKey(unpack, "fields");
        int fields = unpack.readArray();
        for (int i = 0; i < fields; i++) {
            entry.addField(loadField(unpack, set));
        }
        expectKey(unpack, "staticmethods");
        int staticmethods = unpack.readArray();
        for (int i = 0; i < staticmethods; i++) {
            entry.addMethod(loadMethod(unpack, set));
        }
        expectKey(unpack, "methods");
        int methods = unpack.readArray();
        for (int i = 0; i < methods; i++) {
            entry.addMethod(loadMethod(unpack, set));
        }
        String key = unpack.readString();
        if ("signature".equals(key)) {
            entry.setSignature(loadClassSignature(unpack));
        } else if (!"annotations".equals(key)) {
            throw new IllegalStateException("Expected key annotations but was " + key);
        }
        int annotations = unpack.readArray();
        for (int i = 0; i < annotations; i++) {
            entry.addAnnotation(loadAnnotation(unpack, set));
        }
        expectKey(unpack, "inner_classes");
        int innerclasses = unpack.readArray();
        for (int i = 0; i < innerclasses; i++) {
            startMap(unpack, 8);
            expectKey(unpack, "name");
            String name = unpack.readString();
            expectKey(unpack, "simple_name");
            String simple_name = unpack.readString();
            expectKey(unpack, "outer_name");
            String outer_name = unpack.readString();
            int acc = 0;
            expectKey(unpack, "static");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_STATIC;
            }
            expectKey(unpack, "final");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_FINAL;
            }
            expectKey(unpack, "abstract");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_ABSTRACT;
            }
            expectKey(unpack, "synthetic");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_SYNTHETIC;
            }
            expectKey(unpack, "access");
            AccessModifier mod = AccessModifier.values()[unpack.readInt()];
            switch (mod) {
            case PRIVATE:
                acc |= BaseDecompiler.ACC_PRIVATE;
                break;
            case PUBLIC:
                acc |= BaseDecompiler.ACC_PUBLIC;
                break;
            case PROTECTED:
                acc |= BaseDecompiler.ACC_PROTECTED;
                break;
            case PACKAGE_PRIVATE:
            default:
                break;
            }
            entry.addInnerClass(name, simple_name, outer_name, acc);
        }
        if (id == AstSerializer.ENTRY_ID_CLASS) {
            expectKey(unpack, "supername");
            ((ClassEntry) entry).setSuperclass(unpack.readString());
        }

        return entry;
    }

    public static FieldEntry loadField(MessageUnpacker unpack, SourceSet set) throws IOException {
        unpack.readMap();

        return null;
    }

    public static MethodEntry loadMethod(MessageUnpacker unpack, SourceSet set) throws IOException {
        unpack.readMap();

        return null;
    }

    public static Annotation loadAnnotation(MessageUnpacker unpack, SourceSet set) throws IOException {
        unpack.readMap();

        return null;
    }

    public static ClassSignature loadClassSignature(MessageUnpacker unpack) {

        return null;
    }

}

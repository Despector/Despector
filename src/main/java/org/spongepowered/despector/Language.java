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
package org.spongepowered.despector;

import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.Decompiler;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.Emitter;
import org.spongepowered.despector.emitter.Emitters;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a source language.
 */
public class Language {

    private static final Map<String, Language> LANGUAGES = new HashMap<>();

    public static final Language JAVA = new Language("java", ".java");
    public static final Language KOTLIN = new Language("kotlin", ".kt");
    public static final Language ANY = new Language("any", null);

    public static Language get(String id) {
        return LANGUAGES.get(id);
    }

    private final String id;
    private final String ext;

    private Decompiler decompiler;
    private Emitter<?> emitter;

    public Language(String id, String ext) {
        this.id = id;
        this.ext = ext;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Gets the type extension for this language.
     */
    public String getExtension(TypeEntry type) {
        if (this.ext != null) {
            return this.ext;
        }
        return type.getLanguage().getExtension(type);
    }

    public Decompiler getDecompiler() {
        if (this.decompiler == null) {
            if (this == JAVA) {
                this.decompiler = Decompilers.JAVA;
            } else if (this == KOTLIN) {
                this.decompiler = Decompilers.KOTLIN;
            } else if (this == ANY) {
                this.decompiler = Decompilers.WILD;
            }
        }
        return this.decompiler;
    }

    public Emitter<?> getEmitter() {
        if (this.emitter == null) {
            if (this == JAVA) {
                this.emitter = Emitters.JAVA;
            } else if (this == KOTLIN) {
                this.emitter = Emitters.KOTLIN;
            } else if (this == ANY) {
                this.emitter = Emitters.WILD;
            }
        }
        return this.emitter;
    }

}

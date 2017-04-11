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
package com.voxelgenesis.despector.core;

import com.voxelgenesis.despector.core.decompiler.BaseDecompiler;
import com.voxelgenesis.despector.core.emitter.Emitter;
import com.voxelgenesis.despector.core.loader.SourceLoader;

import java.util.HashMap;
import java.util.Map;

public class Language {

    private static final Map<String, Language> LANGUAGES = new HashMap<>();

    public static Language get(String name) {
        return LANGUAGES.get(name);
    }

    public static void register(Language lang) {
        LANGUAGES.put(lang.getName(), lang);
    }

    private final String name;
    private SourceLoader loader;
    private BaseDecompiler decompiler;
    private Emitter emitter;

    public Language(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public SourceLoader getLoader() {
        return this.loader;
    }

    public void setLoader(SourceLoader loader) {
        this.loader = loader;
    }

    public BaseDecompiler getDecompiler() {
        return this.decompiler;
    }

    public void setDecompiler(BaseDecompiler decompiler) {
        this.decompiler = decompiler;
    }

    public Emitter getEmitter() {
        return this.emitter;
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }
}

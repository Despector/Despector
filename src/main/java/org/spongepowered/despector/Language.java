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
package org.spongepowered.despector;

import org.spongepowered.despector.decompiler.Decompiler;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.EmitterSet;
import org.spongepowered.despector.emitter.Emitters;

public enum Language {

    JAVA(Decompilers.JAVA, ".java", Emitters.JAVA),
    KOTLIN(Decompilers.KOTLIN, ".kt", Emitters.KOTLIN);

    private final Decompiler decompiler;
    private final String ext;
    private final EmitterSet emitter;

    Language(Decompiler decomp, String ext, EmitterSet emitter) {
        this.decompiler = decomp;
        this.ext = ext;
        this.emitter = emitter;
    }

    public Decompiler getDecompiler() {
        return this.decompiler;
    }

    public String getFileExt() {
        return this.ext;
    }

    public EmitterSet getEmitter() {
        return this.emitter;
    }
}

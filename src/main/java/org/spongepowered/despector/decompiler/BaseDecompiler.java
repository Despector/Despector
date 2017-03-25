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
package org.spongepowered.despector.decompiler;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BaseDecompiler implements Decompiler {

    private final Language lang;
    private final List<DecompilerStep> steps = new ArrayList<>();

    public BaseDecompiler(Language lang) {
        this.lang = lang;
    }

    public List<DecompilerStep> getSteps() {
        return this.steps;
    }

    public void addStep(DecompilerStep step) {
        this.steps.add(step);
    }

    @Override
    public TypeEntry decompile(Path cls_path, SourceSet source) throws IOException {
        return decompile(cls_path.toFile(), source);
    }

    @Override
    public TypeEntry decompile(File cls_path, SourceSet source) throws IOException {
        return decompile(new FileInputStream(cls_path), source);
    }

    @Override
    public TypeEntry decompile(InputStream cls_path, SourceSet source) throws IOException {
        ClassReader reader = new ClassReader(cls_path);
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        return decompile(cn, source);
    }

    @Override
    public TypeEntry decompile(ClassNode cn, SourceSet source) {
        System.out.println("Decompiling class " + cn.name);
        int acc = cn.access;
        TypeEntry entry = null;
        if ((acc & ACC_ENUM) != 0) {
            entry = new EnumEntry(source, this.lang, cn.name);
        } else if ((acc & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(source, this.lang, cn.name);
        } else {
            entry = new ClassEntry(source, this.lang, cn.name);
            ((ClassEntry) entry).setSuperclass("L" + cn.superName + ";");
        }

        for (DecompilerStep step : this.steps) {
            step.process(cn, entry);
        }

        source.add(entry);
        return entry;
    }

    public static Annotation createAnnotation(SourceSet src, AnnotationNode an) {
        AnnotationType anno_type = src.getAnnotationType(an.desc);
        Annotation anno = new Annotation(anno_type);
        if (an.values != null) {
            for (int i = 0; i * 2 < an.values.size(); i += 2) {
                String key = (String) an.values.get(i * 2);
                Object value = an.values.get(i * 2 + 1);
                anno.setValue(key, value);
            }
        }
        return anno;
    }

}

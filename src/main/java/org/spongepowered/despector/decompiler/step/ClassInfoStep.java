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
package org.spongepowered.despector.decompiler.step;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.BaseDecompiler;
import org.spongepowered.despector.decompiler.DecompilerStep;
import org.spongepowered.despector.util.SignatureParser;

import java.util.List;

/**
 * A decompiler step which retrieves class metadata.
 */
public class ClassInfoStep implements DecompilerStep {

    @SuppressWarnings("unchecked")
    @Override
    public void process(ClassNode cn, TypeEntry entry) {
        entry.setAccessModifier(AccessModifier.fromModifiers(cn.access));
        entry.setFinal((cn.access & ACC_FINAL) != 0);
        entry.setSynthetic((cn.access & ACC_SYNTHETIC) != 0);

        if (cn.signature != null) {
            ClassSignature signature = SignatureParser.parse(cn.signature);
            entry.setSignature(signature);
        }

        if (cn.interfaces != null) {
            for (String inter : (List<String>) cn.interfaces) {
                entry.addInterface(inter);
            }
        }

        if (cn.innerClasses != null) {
            for (InnerClassNode in : (List<InnerClassNode>) cn.innerClasses) {
                entry.addInnerClass(in.name, in.innerName, in.outerName, in.access);
            }
        }

        if (cn.visibleAnnotations != null) {
            for (AnnotationNode an : (List<AnnotationNode>) cn.visibleAnnotations) {
                Annotation anno = BaseDecompiler.createAnnotation(entry.getSource(), an);
                anno.getType().setRuntimeVisible(true);
                entry.addAnnotation(anno);
            }
        }
        if (cn.invisibleAnnotations != null) {
            for (AnnotationNode an : (List<AnnotationNode>) cn.invisibleAnnotations) {
                Annotation anno = BaseDecompiler.createAnnotation(entry.getSource(), an);
                anno.getType().setRuntimeVisible(false);
                entry.addAnnotation(anno);
            }
        }
    }

}

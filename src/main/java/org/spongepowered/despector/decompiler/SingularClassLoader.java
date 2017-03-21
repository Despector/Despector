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

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.TypeHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class SingularClassLoader {

    private static final boolean SET_SINGLE_GRAPH_DEBUG = Boolean.getBoolean("despect.debug.single_graph");

    public static final SingularClassLoader instance = new SingularClassLoader();

    private SingularClassLoader() {
    }

    public TypeEntry load(Path file) throws IOException {
        ClassReader reader = new ClassReader(new FileInputStream(file.toFile()));
        ClassNode cn = new ClassNode();

        reader.accept(cn, 0);
        return load(cn, null);
    }

    public TypeEntry load(Path file, SourceSet src) throws IOException {
        ClassReader reader = new ClassReader(new FileInputStream(file.toFile()));
        ClassNode cn = new ClassNode();

        reader.accept(cn, 0);
        return load(cn, src);
    }

    private Annotation createAnnotation(SourceSet src, AnnotationNode an) {
        AnnotationType anno_type = src.getAnnotationType(an.desc);
        Annotation anno = new Annotation(anno_type);
        if (an.values != null) {
            for (int i = 0; i < an.values.size(); i += 2) {
                String key = (String) an.values.get(i * 2);
                Object value = an.values.get(i * 2 + 1);
                anno.setValue(key, value);
            }
        }
        return anno;
    }

    @SuppressWarnings("unchecked")
    public TypeEntry load(ClassNode cn, SourceSet src) {
//        System.out.println("Decompiling class " + cn.name);
        int acc = cn.access;
        TypeEntry entry = null;
        if ((acc & ACC_ENUM) != 0) {
            entry = new EnumEntry(src, cn.name);
        } else if ((acc & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(src, cn.name);
        } else {
            entry = new ClassEntry(src, cn.name);
            ((ClassEntry) entry).setSuperclass("L" + cn.superName + ";");
        }
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
                Annotation anno = createAnnotation(src, an);
                anno.getType().setRuntimeVisible(true);
                entry.addAnnotation(anno);
            }
        }
        if (cn.invisibleAnnotations != null) {
            for (AnnotationNode an : (List<AnnotationNode>) cn.invisibleAnnotations) {
                Annotation anno = createAnnotation(src, an);
                anno.getType().setRuntimeVisible(false);
                entry.addAnnotation(anno);
            }
        }

        // Find all fields
        for (FieldNode fn : (List<FieldNode>) cn.fields) {
            FieldEntry f = new FieldEntry(src);
            f.setAccessModifier(AccessModifier.fromModifiers(fn.access));
            f.setFinal((fn.access & ACC_FINAL) != 0);
            f.setName(fn.name);
            f.setOwner(cn.name);
            f.setStatic((fn.access & ACC_STATIC) != 0);
            f.setSynthetic((fn.access & ACC_SYNTHETIC) != 0);
            f.setType(fn.desc);

            if (fn.signature != null) {
                TypeSignature sig = SignatureParser.parseFieldTypeSignature(fn.signature);
                f.setSignature(sig);
            }

            if (fn.visibleAnnotations != null) {
                for (AnnotationNode an : (List<AnnotationNode>) fn.visibleAnnotations) {
                    Annotation anno = createAnnotation(src, an);
                    anno.getType().setRuntimeVisible(true);
                    f.addAnnotation(anno);
                }
            }
            if (fn.invisibleAnnotations != null) {
                for (AnnotationNode an : (List<AnnotationNode>) fn.invisibleAnnotations) {
                    Annotation anno = createAnnotation(src, an);
                    anno.getType().setRuntimeVisible(false);
                    f.addAnnotation(anno);
                }
            }

            entry.addField(f);
        }

        // Find all methods
        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            MethodEntry m = new MethodEntry(src);
            m.setAbstract((mn.access & ACC_ABSTRACT) != 0);
            m.setAccessModifier(AccessModifier.fromModifiers(mn.access));
            m.setFinal((mn.access & ACC_FINAL) != 0);
            m.setName(mn.name);
            m.setOwner(cn.name);
            m.setSignature(mn.desc);
            m.setStatic((mn.access & ACC_STATIC) != 0);
            m.setSynthetic((mn.access & ACC_SYNTHETIC) != 0);

            if (mn.signature != null) {
                m.setMethodSignature(SignatureParser.parseMethod(mn.signature));
            }

            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : (List<AnnotationNode>) mn.visibleAnnotations) {
                    Annotation anno = createAnnotation(src, an);
                    anno.getType().setRuntimeVisible(true);
                    m.addAnnotation(anno);
                }
            }
            if (mn.invisibleAnnotations != null) {
                for (AnnotationNode an : (List<AnnotationNode>) mn.invisibleAnnotations) {
                    Annotation anno = createAnnotation(src, an);
                    anno.getType().setRuntimeVisible(false);
                    m.addAnnotation(anno);
                }
            }

            if (SET_SINGLE_GRAPH_DEBUG) {
                OpcodeDecompiler.PRINT_BLOCKS = false;
                ConfigManager.getConfig().print_opcodes_on_error = false;
                if (cn.name.endsWith("BlockPattern") && mn.name.equals("translateOffset")) {
                    OpcodeDecompiler.PRINT_BLOCKS = true;
                    ConfigManager.getConfig().print_opcodes_on_error = true;
                }
            }

            try {
//                System.out.println("Decompiling method " + mn.name);
                StatementBlock insns = InstructionTreeBuilder.build(m, mn);
                m.setInstructions(insns);
            } catch (Exception ex) {
                System.err.println("Error decompiling method body for " + cn.name + " " + m.toString());
                ex.printStackTrace();
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    System.err.println("Offending method bytecode:");
                    Iterator<AbstractInsnNode> it = mn.instructions.iterator();
                    while (it.hasNext()) {
                        System.err.println(AstUtil.insnToString(it.next()));
                    }
                }
            }
            if (SET_SINGLE_GRAPH_DEBUG) {
                OpcodeDecompiler.PRINT_BLOCKS = false;
                ConfigManager.getConfig().print_opcodes_on_error = false;
            }
            entry.addMethod(m);
        }

        if (entry instanceof EnumEntry) {
            MethodEntry clinit = entry.getStaticMethodSafe("<clinit>");
            if (clinit != null && clinit.getInstructions() != null) {
                Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
                while (initializers.hasNext()) {
                    Statement next = initializers.next();
                    if (!(next instanceof StaticFieldAssignment)) {
                        break;
                    }
                    StaticFieldAssignment assign = (StaticFieldAssignment) next;
                    if (!TypeHelper.descToType(assign.getOwnerType()).equals(entry.getName()) || !(assign.getValue() instanceof New)) {
                        break;
                    }
                    ((EnumEntry) entry).addEnumConstant(assign.getFieldName());
                }
            }
        }
//        System.out.println("Done!");
        if (src != null) {
            src.add(entry);
        }
        if (ConfigManager.getConfig().emit_source_on_load) {
            PrintWriter out = new PrintWriter(System.out);
            EmitterContext emitter = new EmitterContext(Emitters.JAVA, out, EmitterFormat.defaults());
            emitter.enableBuffer();
            emitter.emit(entry);
            emitter.outputBuffer();
            out.flush();
        }
        return entry;
    }

}

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
package org.spongepowered.despector.decompiler;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Annotation.EnumConstant;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.despector.decompiler.error.SourceFormatException;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.loader.BytecodeTranslator;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.Entry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.MethodHandleEntry;
import org.spongepowered.despector.decompiler.method.MethodDecompiler;
import org.spongepowered.despector.decompiler.method.PartialMethod.TryCatchRegion;
import org.spongepowered.despector.parallel.Timing;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.TypeHelper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A language decompiler.
 */
public class BaseDecompiler implements Decompiler {

    private static final boolean DUMP_IR_ON_LOAD = Boolean.getBoolean("despector.debug.dump_ir");

    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SUPER = 0x0020;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_VOLATILE = 0x0040;
    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_TRANSIENT = 0x0080;
    public static final int ACC_VARARGS = 0x0080;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;

    private final BytecodeTranslator bytecode = new BytecodeTranslator();
    private final Language lang;

    public BaseDecompiler(Language lang) {
        this.lang = lang;
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
    public TypeEntry decompile(InputStream input, SourceSet set) throws IOException {
        DataInputStream data = (input instanceof DataInputStream) ? (DataInputStream) input : new DataInputStream(input);
        long decompile_start = System.nanoTime();
        int magic = data.readInt();
        if (magic != 0xCAFEBABE) {
            throw new SourceFormatException("Not a java class file");
        }

        short minor = data.readShort();
        short major = data.readShort();

        // TODO check versions and adapt loading to support a range of versions

        ClassConstantPool pool = new ClassConstantPool();
        pool.load(data);

        int access_flags = data.readUnsignedShort();

        String name = pool.getClass(data.readUnsignedShort()).name;
        if (!LibraryConfiguration.quiet) {
            System.out.println("Decompiling class " + name);
        }
        int super_index = data.readUnsignedShort();
        String supername = super_index != 0 ? "L" + pool.getClass(super_index).name + ";" : "Ljava/lang/Object;";

        int interfaces_count = data.readUnsignedShort();
        List<String> interfaces = new ArrayList<>(interfaces_count);
        for (int i = 0; i < interfaces_count; i++) {
            interfaces.add(pool.getClass(data.readUnsignedShort()).name);
        }

        Language actual_lang = Language.JAVA;
        TypeEntry entry = null;
        if ((access_flags & ACC_ANNOTATION) != 0) {
            entry = new AnnotationEntry(set, actual_lang, name);
        } else if ((access_flags & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(set, actual_lang, name);
        } else if ((access_flags & ACC_ENUM) != 0) {
            entry = new EnumEntry(set, actual_lang, name);
        } else {
            entry = new ClassEntry(set, actual_lang, name);
            ((ClassEntry) entry).setSuperclass(supername);
        }
        entry.setAccessModifier(AccessModifier.fromModifiers(access_flags));
        entry.setFinal((access_flags & ACC_FINAL) != 0);
        entry.setSynthetic((access_flags & ACC_SYNTHETIC) != 0);
        entry.setAbstract((access_flags & ACC_ABSTRACT) != 0);
        entry.getInterfaces().addAll(interfaces);

        int field_count = data.readUnsignedShort();
        for (int i = 0; i < field_count; i++) {
            int field_access = data.readUnsignedShort();
            String field_name = pool.getUtf8(data.readUnsignedShort());
            if ((field_access & ACC_ENUM) != 0) {
                ((EnumEntry) entry).addEnumConstant(field_name);
            }
            String field_desc = pool.getUtf8(data.readUnsignedShort());

            FieldEntry field = new FieldEntry(set);
            field.setAccessModifier(AccessModifier.fromModifiers(field_access));
            field.setFinal((field_access & ACC_FINAL) != 0);
            field.setName(field_name);
            field.setOwner(name);
            field.setStatic((field_access & ACC_STATIC) != 0);
            field.setSynthetic((field_access & ACC_SYNTHETIC) != 0);
            field.setVolatile((field_access & ACC_VOLATILE) != 0);
            field.setTransient((field_access & ACC_TRANSIENT) != 0);
            field.setName(field_name);
            field.setType(ClassTypeSignature.of(field_desc));
            entry.addField(field);

            int attribute_count = data.readUnsignedShort();
            for (int a = 0; a < attribute_count; a++) {
                String attribute_name = pool.getUtf8(data.readUnsignedShort());
                int length = data.readInt();
                if ("ConstantValue".equals(attribute_name)) {
                    /* int constant_value_index = */ data.readUnsignedShort();
                } else if ("Synthetic".equals(attribute_name)) {
                    field.setSynthetic(true);
                } else if ("Signature".equals(attribute_name)) {
                    field.setType(SignatureParser.parseFieldTypeSignature(pool.getUtf8(data.readUnsignedShort())));
                } else if ("Deprecated".equals(attribute_name)) {
                    field.setDeprecated(true);
                } else if ("RuntimeVisibleAnnotations".equals(attribute_name)) {
                    int annotation_count = data.readUnsignedShort();
                    for (int j = 0; j < annotation_count; j++) {
                        Annotation anno = readAnnotation(data, pool, set);
                        field.addAnnotation(anno);
                        anno.getType().setRuntimeVisible(true);
                    }
                } else if ("RuntimeInvisibleAnnotations".equals(attribute_name)) {
                    int annotation_count = data.readUnsignedShort();
                    for (int j = 0; j < annotation_count; j++) {
                        Annotation anno = readAnnotation(data, pool, set);
                        field.addAnnotation(anno);
                        anno.getType().setRuntimeVisible(false);
                    }
                } else {
                    System.err.println("Skipping unknown field attribute: " + attribute_name);
                    data.skipBytes(length);
                }
            }
        }
        List<UnfinishedMethod> unfinished_methods = new ArrayList<>();
        int method_count = data.readUnsignedShort();
        for (int i = 0; i < method_count; i++) {
            int method_access = data.readUnsignedShort();
            String method_name = pool.getUtf8(data.readUnsignedShort());
            String method_desc = pool.getUtf8(data.readUnsignedShort());

            List<TypeSignature> param_types = new ArrayList<>();
            for (String t : TypeHelper.splitSig(method_desc)) {
                param_types.add(ClassTypeSignature.of(t));
            }
            MethodEntry method = new MethodEntry(set);
            method.setName(method_name);
            method.setDescription(method_desc);
            method.setOwner(name);
            method.setAbstract((method_access & ACC_ABSTRACT) != 0);
            method.setAccessModifier(AccessModifier.fromModifiers(method_access));
            method.setFinal((method_access & ACC_FINAL) != 0);
            method.setStatic((method_access & ACC_STATIC) != 0);
            method.setSynthetic((method_access & ACC_SYNTHETIC) != 0);
            method.setBridge((method_access & ACC_BRIDGE) != 0);
            method.setSynchronized((method_access & ACC_SYNCHRONIZED) != 0);
            method.setNative((method_access & ACC_NATIVE) != 0);
            method.setVarargs((method_access & ACC_VARARGS) != 0);
            method.setStrictFp((method_access & ACC_STRICT) != 0);
            entry.addMethod(method);
            Locals locals = new Locals(method.isStatic());
            method.setLocals(locals);

            List<String> checked_exceptions = null;
            UnfinishedMethod unfinished = new UnfinishedMethod();
            unfinished_methods.add(unfinished);
            unfinished.mth = method;
            String method_sig = null;
            int attribute_count = data.readUnsignedShort();
            for (int a = 0; a < attribute_count; a++) {
                String attribute_name = pool.getUtf8(data.readUnsignedShort());
                int length = data.readInt();
                if ("Code".equals(attribute_name)) {
                    /* int max_stack = */ data.readUnsignedShort();
                    /* int max_locals = */ data.readUnsignedShort();
                    int code_length = data.readInt();
                    byte[] code = new byte[code_length];
                    int offs = 0;
                    while (code_length > 0) {
                        int len = data.read(code, offs, code_length);
                        code_length -= len;
                        offs += len;
                    }
                    List<TryCatchRegion> catch_regions = new ArrayList<>();
                    int exception_table_length = data.readUnsignedShort();
                    for (int j = 0; j < exception_table_length; j++) {
                        int start_pc = data.readUnsignedShort();
                        int end_pc = data.readUnsignedShort();
                        int catch_pc = data.readUnsignedShort();
                        int ex_index = data.readUnsignedShort();
                        String ex = ex_index != 0 ? pool.getClass(ex_index).name : "";
                        catch_regions.add(new TryCatchRegion(start_pc, end_pc, catch_pc, ex));
                    }
                    unfinished.code = code;
                    unfinished.catch_regions = catch_regions;
                    int code_attribute_count = data.readUnsignedShort();
                    for (int ca = 0; ca < code_attribute_count; ca++) {
                        String code_attribute_name = pool.getUtf8(data.readUnsignedShort());
                        int clength = data.readInt();
                        if ("LocalVariableTable".equals(code_attribute_name)) {
                            int lvt_length = data.readUnsignedShort();
                            for (int j = 0; j < lvt_length; j++) {
                                int start_pc = data.readUnsignedShort();
                                int local_length = data.readUnsignedShort();
                                String local_name = pool.getUtf8(data.readUnsignedShort());
                                String local_desc = pool.getUtf8(data.readUnsignedShort());
                                int index = data.readUnsignedShort();
                                Local loc = locals.getLocal(index);
                                loc.addLVT(start_pc, local_length, local_name, local_desc);
                            }
                        } else if ("LineNumberTable".equals(code_attribute_name)) {
                            data.skipBytes(clength);
                        } else if ("LocalVariableTypeTable".equals(code_attribute_name)) {
                            int lvt_length = data.readUnsignedShort();
                            for (int j = 0; j < lvt_length; j++) {
                                int start_pc = data.readUnsignedShort();
                                /* int local_length = */ data.readUnsignedShort();
                                /* String local_name = */ pool.getUtf8(data.readUnsignedShort());
                                String local_signature = pool.getUtf8(data.readUnsignedShort());
                                int index = data.readUnsignedShort();
                                Local loc = locals.getLocal(index);
                                loc.getLVT(start_pc).setSignature(local_signature);
                            }
                        } else if ("StackMapTable".equals(code_attribute_name)) {
                            data.skipBytes(clength);
                        } else {
                            System.err.println("Skipping unknown code attribute: " + code_attribute_name);
                            data.skipBytes(clength);
                        }
                    }
                } else if ("Exceptions".equals(attribute_name)) {
                    checked_exceptions = new ArrayList<>();
                    int exception_count = data.readUnsignedShort();
                    for (int j = 0; j < exception_count; j++) {
                        checked_exceptions.add(pool.getClass(data.readUnsignedShort()).name);
                    }
                } else if ("Synthetic".equals(attribute_name)) {
                    method.setSynthetic(true);
                } else if ("Signature".equals(attribute_name)) {
                    method_sig = pool.getUtf8(data.readUnsignedShort());
                } else if ("Deprecated".equals(attribute_name)) {
                    method.setDeprecated(true);
                } else if ("RuntimeVisibleAnnotations".equals(attribute_name)) {
                    int annotation_count = data.readUnsignedShort();
                    for (int j = 0; j < annotation_count; j++) {
                        Annotation anno = readAnnotation(data, pool, set);
                        method.addAnnotation(anno);
                        anno.getType().setRuntimeVisible(true);
                    }
                } else if ("RuntimeInvisibleAnnotations".equals(attribute_name)) {
                    int annotation_count = data.readUnsignedShort();
                    for (int j = 0; j < annotation_count; j++) {
                        Annotation anno = readAnnotation(data, pool, set);
                        method.addAnnotation(anno);
                        anno.getType().setRuntimeVisible(false);
                    }
                } else if ("RuntimeVisibleParameterAnnotations".equals(attribute_name)) {
                    if (unfinished.parameter_annotations == null) {
                        unfinished.parameter_annotations = new HashMap<>();
                    }
                    int num_params = data.readUnsignedByte();
                    int offs = method.isStatic() ? 0 : 1;
                    for (int k = offs; k < num_params + offs; k++) {
                        List<Annotation> annos = unfinished.parameter_annotations.get(k);
                        if (annos == null) {
                            annos = new ArrayList<>();
                            unfinished.parameter_annotations.put(k, annos);
                        }
                        int annotation_count = data.readUnsignedShort();
                        for (int j = 0; j < annotation_count; j++) {
                            Annotation anno = readAnnotation(data, pool, set);
                            annos.add(anno);
                            anno.getType().setRuntimeVisible(true);
                        }
                    }
                } else if ("RuntimeInvisibleParameterAnnotations".equals(attribute_name)) {
                    if (unfinished.parameter_annotations == null) {
                        unfinished.parameter_annotations = new HashMap<>();
                    }
                    int num_params = data.readUnsignedByte();
                    int offs = method.isStatic() ? 0 : 1;
                    for (int k = offs; k < num_params + offs; k++) {
                        List<Annotation> annos = unfinished.parameter_annotations.get(k);
                        if (annos == null) {
                            annos = new ArrayList<>();
                            unfinished.parameter_annotations.put(k, annos);
                        }
                        int annotation_count = data.readUnsignedShort();
                        for (int j = 0; j < annotation_count; j++) {
                            Annotation anno = readAnnotation(data, pool, set);
                            annos.add(anno);
                            anno.getType().setRuntimeVisible(false);
                        }
                    }
                } else if ("AnnotationDefault".equals(attribute_name)) {
                    Object val = readElementValue(data, pool, set);
                    method.setAnnotationValue(val);
                } else {
                    System.err.println("Skipping unknown method attribute: " + attribute_name);
                    data.skipBytes(length);
                }
            }
            if (method_sig != null) {
                method.setMethodSignature(SignatureParser.parseMethod(method_sig));
            } else {
                MethodSignature sig = SignatureParser.parseMethod(method_desc);
                method.setMethodSignature(sig);
                if (checked_exceptions != null && !checked_exceptions.isEmpty()) {
                    for (String ex : checked_exceptions) {
                        sig.getThrowsSignature().add(ClassTypeSignature.of(ex));
                    }
                }
            }
        }
        MethodDecompiler mth_decomp = Decompilers.JAVA_METHOD;
        if (this.lang == Language.KOTLIN) {
            mth_decomp = Decompilers.KOTLIN_METHOD;
        }
        List<BootstrapMethod> bootstrap_methods = new ArrayList<>();
        int class_attribute_count = data.readUnsignedShort();
        for (int i = 0; i < class_attribute_count; i++) {
            String attribute_name = pool.getUtf8(data.readUnsignedShort());
            int length = data.readInt();
            if ("InnerClasses".equals(attribute_name)) {
                int number_of_classes = data.readUnsignedShort();
                for (int j = 0; j < number_of_classes; j++) {
                    String inner_cls = pool.getClass(data.readUnsignedShort()).name;
                    int outer_index = data.readUnsignedShort();
                    String outer_cls = outer_index == 0 ? null : pool.getClass(outer_index).name;
                    int name_index = data.readUnsignedShort();
                    String inner_name = name_index == 0 ? null : pool.getUtf8(name_index);
                    int acc = data.readUnsignedShort();
                    entry.addInnerClass(inner_cls, inner_name, outer_cls, acc);
                }
            } else if ("EnclosingMethod".equals(attribute_name)) {
                data.skipBytes(length);
            } else if ("Synthetic".equals(attribute_name)) {
                entry.setSynthetic(true);
            } else if ("Signature".equals(attribute_name)) {
                entry.setSignature(SignatureParser.parse(pool.getUtf8(data.readUnsignedShort())));
            } else if ("SourceFile".equals(attribute_name)) {
                data.skipBytes(length);
            } else if ("SourceDebugExtension".equals(attribute_name)) {
                data.skipBytes(length);
            } else if ("Deprecated".equals(attribute_name)) {
                entry.setDeprecated(true);
            } else if ("RuntimeVisibleAnnotations".equals(attribute_name)) {
                int annotation_count = data.readUnsignedShort();
                for (int j = 0; j < annotation_count; j++) {
                    Annotation anno = readAnnotation(data, pool, set);
                    if (this.lang == Language.ANY && anno.getType().getName().startsWith("kotlin")) {
                        actual_lang = Language.KOTLIN;
                    }
                    entry.addAnnotation(anno);
                    anno.getType().setRuntimeVisible(true);
                }
            } else if ("RuntimeInvisibleAnnotations".equals(attribute_name)) {
                int annotation_count = data.readUnsignedShort();
                for (int j = 0; j < annotation_count; j++) {
                    Annotation anno = readAnnotation(data, pool, set);
                    entry.addAnnotation(anno);
                    anno.getType().setRuntimeVisible(false);
                }
            } else if ("BootstrapMethods".equals(attribute_name)) {
                int bsm_count = data.readUnsignedShort();
                for (int j = 0; j < bsm_count; j++) {
                    BootstrapMethod bsm = new BootstrapMethod();
                    bootstrap_methods.add(bsm);
                    bsm.handle = pool.getMethodHandle(data.readUnsignedShort());
                    int arg_count = data.readUnsignedShort();
                    bsm.arguments = new Entry[arg_count];
                    for (int k = 0; k < arg_count; k++) {
                        bsm.arguments[k] = pool.getEntry(data.readUnsignedShort());
                    }
                }
            } else {
                System.err.println("Skipping unknown class attribute: " + attribute_name);
                data.skipBytes(length);
            }
        }

        if (entry.getSignature() == null) {
            ClassSignature sig = new ClassSignature();
            sig.setSuperclassSignature(new GenericClassTypeSignature(supername));
            for (String intr : entry.getInterfaces()) {
                sig.getInterfaceSignatures().add(new GenericClassTypeSignature("L" + intr + ";"));
            }
            entry.setSignature(sig);
        }

        long classloading_time = System.nanoTime() - decompile_start;
        Timing.time_loading_classes += classloading_time;

        entry.setLanguage(actual_lang);
        long method_decompile_start = System.nanoTime();
        for (UnfinishedMethod unfinished : unfinished_methods) {
            if (unfinished.code == null) {
                continue;
            }
            MethodEntry mth = unfinished.mth;
            try {
                mth.setIR(this.bytecode.createIR(unfinished.code, mth.getLocals(), unfinished.catch_regions, pool, bootstrap_methods));

                if (unfinished.parameter_annotations != null) {
                    for (Map.Entry<Integer, List<Annotation>> e : unfinished.parameter_annotations.entrySet()) {
                        Local loc = mth.getLocals().getLocal(e.getKey());
                        loc.getInstance(0).getAnnotations().addAll(e.getValue());
                    }
                }

                if (DUMP_IR_ON_LOAD) {
                    System.out.println("Instructions of " + mth.getName() + " " + mth.getDescription());
                    System.out.println(mth.getIR());
                }
                StatementBlock block = mth_decomp.decompile(mth);
                mth.setInstructions(block);

                if (entry instanceof EnumEntry && mth.getName().equals("<clinit>")) {
                    EnumEntry e = (EnumEntry) entry;
                    Set<String> names = new HashSet<>(e.getEnumConstants());
                    e.getEnumConstants().clear();
                    for (Statement stmt : block) {
                        if (names.isEmpty() || !(stmt instanceof StaticFieldAssignment)) {
                            break;
                        }
                        StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
                        if (!names.remove(assign.getFieldName())) {
                            break;
                        }
                        New val = (New) assign.getValue();
                        StringConstant cst = (StringConstant) val.getParameters()[0];
                        e.addEnumConstant(cst.getConstant());
                    }
                    if (!names.isEmpty()) {
                        System.err.println("Warning: Failed to find names for all enum constants in " + entry.getName());
                    }
                }

            } catch (Exception ex) {
                if (!LibraryConfiguration.quiet) {
                    System.err.println("Error decompiling method body for " + name + " " + mth.toString());
                    ex.printStackTrace();
                }
                StatementBlock insns = new StatementBlock(StatementBlock.Type.METHOD);
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    List<String> text = new ArrayList<>();
                    text.add("Error decompiling block");
                    if (mth.getIR() != null) {
                        for (Insn next : mth.getIR()) {
                            text.add(next.toString());
                        }
                    } else {
                        mth.getLocals().bakeInstances(Collections.emptyList());
                    }
                    insns.append(new Comment(text));
                } else {
                    insns.append(new Comment("Error decompiling block"));
                }
                mth.setInstructions(insns);
            }
        }
        long method_decompile_time = System.nanoTime() - method_decompile_start;
        long decompile_time = System.nanoTime() - decompile_start;
        Timing.time_decompiling_methods += method_decompile_time;
        Timing.time_decompiling += decompile_time;
        set.add(entry);
        return entry;
    }

    private Annotation readAnnotation(DataInputStream data, ClassConstantPool pool, SourceSet set) throws IOException {
        String anno_type_name = pool.getUtf8(data.readUnsignedShort());
        AnnotationType anno_type = set.getAnnotationType(TypeHelper.descToType(anno_type_name));
        Annotation anno = new Annotation(anno_type);
        int value_paris = data.readUnsignedShort();
        for (int k = 0; k < value_paris; k++) {
            String element_name = pool.getUtf8(data.readUnsignedShort());
            anno.setValue(element_name, readElementValue(data, pool, set));
        }
        return anno;
    }

    private Object readElementValue(DataInputStream data, ClassConstantPool pool, SourceSet set) throws IOException {
        char element_type_tag = (char) data.readUnsignedByte();
        if (element_type_tag == 's') {
            String value = pool.getUtf8(data.readUnsignedShort());
            return value;
        } else if (element_type_tag == 'B') {
            int value = pool.getInt(data.readUnsignedShort());
            return Byte.valueOf((byte) value);
        } else if (element_type_tag == 'S') {
            int value = pool.getInt(data.readUnsignedShort());
            return Short.valueOf((short) value);
        } else if (element_type_tag == 'C') {
            int value = pool.getInt(data.readUnsignedShort());
            return Character.valueOf((char) value);
        } else if (element_type_tag == 'I') {
            int value = pool.getInt(data.readUnsignedShort());
            return Integer.valueOf(value);
        } else if (element_type_tag == 'F') {
            float value = pool.getFloat(data.readUnsignedShort());
            return Float.valueOf(value);
        } else if (element_type_tag == 'J') {
            long value = pool.getLong(data.readUnsignedShort());
            return Long.valueOf(value);
        } else if (element_type_tag == 'D') {
            double value = pool.getDouble(data.readUnsignedShort());
            return Double.valueOf(value);
        } else if (element_type_tag == 'Z') {
            int value = pool.getInt(data.readUnsignedShort());
            return Boolean.valueOf(value != 0);
        } else if (element_type_tag == 'c') {
            String value = pool.getUtf8(data.readUnsignedShort());
            return ClassTypeSignature.of(value);
        } else if (element_type_tag == '@') {
            Annotation value = readAnnotation(data, pool, set);
            return value;
        } else if (element_type_tag == 'e') {
            String enum_type = pool.getUtf8(data.readUnsignedShort());
            String enum_cst = pool.getUtf8(data.readUnsignedShort());
            EnumConstant value = new EnumConstant(enum_type, enum_cst);
            return value;
        } else if (element_type_tag == '[') {
            List<Object> value = new ArrayList<>();
            int num_values = data.readUnsignedShort();
            for (int i = 0; i < num_values; i++) {
                value.add(readElementValue(data, pool, set));
            }
            return value;
        }
        throw new IllegalStateException();
    }

    public static class BootstrapMethod {

        public MethodHandleEntry handle;
        public Entry[] arguments;
    }

    private static class UnfinishedMethod {

        public MethodEntry mth;
        public byte[] code;
        public List<TryCatchRegion> catch_regions;
        public Map<Integer, List<Annotation>> parameter_annotations;

        UnfinishedMethod() {
        }
    }

}

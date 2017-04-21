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

import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.despector.decompiler.error.SourceFormatException;
import org.spongepowered.despector.decompiler.loader.BytecodeTranslator;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool;
import org.spongepowered.despector.decompiler.method.MethodDecompiler;
import org.spongepowered.despector.util.TypeHelper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A language decompiler.
 */
public class BaseDecompiler implements Decompiler {

    private static final boolean DUMP_IR_ON_LOAD = Boolean.getBoolean("despector.debug.dump_ir");

    private static final int ACC_PUBLIC = 0x0001;
    private static final int ACC_PRIVATE = 0x0002;
    private static final int ACC_PROTECTED = 0x0004;
    private static final int ACC_STATIC = 0x0008;
    private static final int ACC_FINAL = 0x0010;
    private static final int ACC_SUPER = 0x0020;
    private static final int ACC_VOLATILE = 0x0040;
    private static final int ACC_TRANSIENT = 0x0080;
    private static final int ACC_INTERFACE = 0x0200;
    private static final int ACC_ABSTRACT = 0x0400;
    private static final int ACC_SYNTHETIC = 0x1000;
    private static final int ACC_ANNOTATION = 0x2000;
    private static final int ACC_ENUM = 0x4000;

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

        int magic = data.readInt();
        if (magic != 0xCAFEBABE) {
            throw new SourceFormatException("Not a java class file");
        }

        short minor = data.readShort();
        short major = data.readShort();

        // TODO support older versions
        if (major != 52 && minor != 50) {
            throw new SourceFormatException("Unsupported java class version " + major + "." + minor);
        }

        ClassConstantPool pool = new ClassConstantPool();
        pool.load(data);

        int access_flags = data.readUnsignedShort();

        String name = pool.getClass(data.readUnsignedShort()).name;
        if (!LibraryConfiguration.quiet) {
            System.out.println("Decompiling class " + name);
        }
        int super_index = data.readUnsignedShort();
        String supername = super_index != 0 ? pool.getClass(super_index).name : null;

        int interfaces_count = data.readUnsignedShort();
        List<String> interfaces = new ArrayList<>(interfaces_count);
        for (int i = 0; i < interfaces_count; i++) {
            interfaces.add(pool.getClass(data.readUnsignedShort()).name);
        }

        TypeEntry entry = null;
        if ((access_flags & ACC_ANNOTATION) != 0) {
            entry = new AnnotationEntry(set, lang, name);
        } else if ((access_flags & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(set, lang, name);
        } else if ((access_flags & ACC_ENUM) != 0) {
            entry = new EnumEntry(set, lang, name);
        } else {
            entry = new ClassEntry(set, lang, name);
        }
        entry.setAccessModifier(AccessModifier.fromModifiers(access_flags));

        int field_count = data.readUnsignedShort();
        List<FieldEntry> fields = new ArrayList<>();
        for (int i = 0; i < field_count; i++) {
            int field_access = data.readUnsignedShort();
            String field_name = pool.getUtf8(data.readUnsignedShort());
            String field_desc = pool.getUtf8(data.readUnsignedShort());

            FieldEntry field = new FieldEntry(set);
            field.setName(field_name);
            field.setType(ClassTypeSignature.of(field_desc));
            fields.add(field);

            int attribute_count = data.readUnsignedShort();
            for (int a = 0; a < attribute_count; a++) {
                String attribute_name = pool.getUtf8(data.readUnsignedShort());
                int length = data.readInt();
                System.err.println("Skipping unknown field attribute: " + attribute_name);
                data.skipBytes(length);
            }
        }

        int method_count = data.readUnsignedShort();
        List<MethodEntry> methods = new ArrayList<>();
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
            methods.add(method);
            method.setStatic((method_access & ACC_STATIC) != 0);
            Locals locals = new Locals();

            int attribute_count = data.readUnsignedShort();
            for (int a = 0; a < attribute_count; a++) {
                String attribute_name = pool.getUtf8(data.readUnsignedShort());
                int length = data.readInt();
                if ("Code".equals(attribute_name)) {
                    int max_stack = data.readUnsignedShort();
                    int max_locals = data.readUnsignedShort();
                    int code_length = data.readInt();
                    byte[] code = new byte[code_length];
                    data.read(code, 0, code_length);

                    int exception_table_length = data.readUnsignedShort();
                    for (int ex = 0; ex < exception_table_length; ex++) {
                        data.skipBytes(8);
                    }
                    // TODO exceptions

                    int code_attribute_count = data.readUnsignedShort();
                    for (int ca = 0; ca < code_attribute_count; ca++) {
                        String code_attribute_name = pool.getUtf8(data.readUnsignedShort());
                        int clength = data.readInt();
                        if ("LocalVariableTable".equals(code_attribute_name)) {
                            int lvt_length = data.readUnsignedShort();
                            int param_count = method.getParamTypes().size();
                            if (!method.isStatic()) {
                                param_count++;
                            }
                            for (int j = 0; j < lvt_length; j++) {
                                int start_pc = data.readUnsignedShort();
                                int local_length = data.readUnsignedShort();
                                String local_name = pool.getUtf8(data.readUnsignedShort());
                                String local_desc = pool.getUtf8(data.readUnsignedShort());
                                int index = data.readUnsignedShort();
                                Local loc = locals.getLocal(index);
                                loc.
                            }
                        } else {
                            System.err.println("Skipping unknown code attribute: " + code_attribute_name);
                            data.skipBytes(clength);
                        }

                        method.setIR(this.bytecode.createIR(code, pool));

                        if (DUMP_IR_ON_LOAD) {
                            System.out.println("Instructions of " + method_name + " " + method_desc);
                            System.out.println(method.getIR());
                        }
                    }

                } else {
                    System.err.println("Skipping unknown method attribute: " + attribute_name);
                    data.skipBytes(length);
                }
            }
        }

        for (MethodEntry mth : methods) {
            entry.addMethod(mth);
        }
        for (FieldEntry fld : fields) {
            entry.addField(fld);
        }
        MethodDecompiler mth_decomp = Decompilers.JAVA_METHOD;
        if (this.lang == Language.KOTLIN) {
            mth_decomp = Decompilers.KOTLIN_METHOD;
        }
        int class_attribute_count = data.readUnsignedShort();
        for (int i = 0; i < class_attribute_count; i++) {
            String attribute_name = pool.getUtf8(data.readUnsignedShort());
            int length = data.readInt();
            System.err.println("Skipping unknown class attribute: " + attribute_name);
            data.skipBytes(length);
            // TODO look for kotlin annotation and set method decomp if found and lang == ANY
        }
        
        for(MethodEntry mth: entry.getMethods()) {
            mth_decomp.decompile(mth, null, mth.getLocals());
        }
        set.add(entry);
        return entry;
    }

    /**
     * Creates a new annotation instance for the given annotation node.
     */
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

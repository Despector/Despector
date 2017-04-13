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
package com.voxelgenesis.despector.jvm.loader;

import com.voxelgenesis.despector.core.Language;
import com.voxelgenesis.despector.core.ast.SourceSet;
import com.voxelgenesis.despector.core.ast.method.Local;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.ast.type.FieldEntry;
import com.voxelgenesis.despector.core.ast.type.MethodEntry;
import com.voxelgenesis.despector.core.ast.type.TypeEntry;
import com.voxelgenesis.despector.core.loader.SourceFormatException;
import com.voxelgenesis.despector.core.loader.SourceLoader;
import com.voxelgenesis.despector.jvm.ast.type.AnnotationEntry;
import com.voxelgenesis.despector.jvm.ast.type.ClassEntry;
import com.voxelgenesis.despector.jvm.ast.type.EnumEntry;
import com.voxelgenesis.despector.jvm.ast.type.InterfaceEntry;
import com.voxelgenesis.despector.jvm.loader.bytecode.BytecodeTranslator;
import org.spongepowered.despector.util.TypeHelper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassSourceLoader implements SourceLoader {

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

    @Override
    public void load(InputStream input, Language lang, SourceSet set) throws IOException {
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
        int super_index = data.readUnsignedShort();
        String supername = super_index != 0 ? pool.getClass(super_index).name : null;

        int interfaces_count = data.readUnsignedShort();
        List<String> interfaces = new ArrayList<>(interfaces_count);
        for (int i = 0; i < interfaces_count; i++) {
            interfaces.add(pool.getClass(data.readUnsignedShort()).name);
        }

        int field_count = data.readUnsignedShort();
        List<FieldEntry> fields = new ArrayList<>();
        for (int i = 0; i < field_count; i++) {
            int field_access = data.readUnsignedShort();
            String field_name = pool.getUtf8(data.readUnsignedShort());
            String field_desc = pool.getUtf8(data.readUnsignedShort());

            FieldEntry field = new FieldEntry(field_name, JvmHelper.of(field_desc));
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
                param_types.add(JvmHelper.of(t));
            }

            MethodEntry method = new MethodEntry(method_name, param_types, JvmHelper.of(TypeHelper.getRet(method_desc)));
            methods.add(method);
            method.setStatic((method_access & ACC_STATIC) != 0);

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
                                Local local = method.getLocals().get(index);
                                local.setName(local_name);
                                local.setType(JvmHelper.of(local_desc));
                                if (j < param_count) {
                                    local.setParameter(true);
                                }
                                // TODO need to store data to pass to bytecode
                                // translator to seperate all overloading of
                                // local slots to unique indices
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

        TypeEntry entry = null;
        if ((access_flags & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(name);
        } else if ((access_flags & ACC_ENUM) != 0) {
            entry = new EnumEntry(name);
        } else if ((access_flags & ACC_ANNOTATION) != 0) {
            entry = new AnnotationEntry(name);
        } else {
            entry = new ClassEntry(name);
        }

        for (MethodEntry mth : methods) {
            entry.addMethod(mth);
        }
        for (FieldEntry fld : fields) {
            entry.addField(fld);
        }

        int class_attribute_count = data.readUnsignedShort();
        for (int i = 0; i < class_attribute_count; i++) {
            String attribute_name = pool.getUtf8(data.readUnsignedShort());
            int length = data.readInt();
            System.err.println("Skipping unknown class attribute: " + attribute_name);
            data.skipBytes(length);
        }
        lang.getDecompiler().decompile(entry, set);
        set.add(entry);
    }

}

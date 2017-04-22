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

import static org.objectweb.asm.Opcodes.ACC_BRIDGE;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.StatementBlock.Type;
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
import org.spongepowered.despector.decompiler.method.MethodDecompiler;
import org.spongepowered.despector.decompiler.method.PartialMethod.TryCatchRegion;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.TypeHelper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
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
        entry.getInterfaces().addAll(interfaces);

        int field_count = data.readUnsignedShort();
        for (int i = 0; i < field_count; i++) {
            int field_access = data.readUnsignedShort();
            String field_name = pool.getUtf8(data.readUnsignedShort());
            String field_desc = pool.getUtf8(data.readUnsignedShort());

            FieldEntry field = new FieldEntry(set);
            field.setAccessModifier(AccessModifier.fromModifiers(field_access));
            field.setFinal((field_access & ACC_FINAL) != 0);
            field.setName(field_name);
            field.setOwner(name);
            field.setStatic((field_access & ACC_STATIC) != 0);
            field.setSynthetic((field_access & ACC_SYNTHETIC) != 0);
            field.setName(field_name);
            field.setType(ClassTypeSignature.of(field_desc));
            entry.addField(field);

            int attribute_count = data.readUnsignedShort();
            for (int a = 0; a < attribute_count; a++) {
                String attribute_name = pool.getUtf8(data.readUnsignedShort());
                int length = data.readInt();
                System.err.println("Skipping unknown field attribute: " + attribute_name);
                data.skipBytes(length);
            }
        }

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
            entry.addMethod(method);
            Locals locals = new Locals();
            method.setLocals(locals);

            String method_sig = null;
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
                    List<TryCatchRegion> catch_regions = new ArrayList<>();
                    int exception_table_length = data.readUnsignedShort();
                    for (int j = 0; j < exception_table_length; j++) {
                        int start_pc = data.readUnsignedShort();
                        int end_pc = data.readUnsignedShort();
                        int catch_pc = data.readUnsignedShort();
                        String ex = pool.getClass(data.readUnsignedShort()).name;
                        catch_regions.add(new TryCatchRegion(start_pc, end_pc, catch_pc, ex));
                    }
                    // TODO exceptions

                    int code_attribute_count = data.readUnsignedShort();
                    for (int ca = 0; ca < code_attribute_count; ca++) {
                        String code_attribute_name = pool.getUtf8(data.readUnsignedShort());
                        int clength = data.readInt();
                        if ("LocalVariableTable".equals(code_attribute_name)) {
                            int lvt_length = data.readUnsignedShort();
                            int param_count = TypeHelper.paramCount(method_desc);
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
                                loc.addLVT(start_pc, local_length, local_name, local_desc);
                                if (index < param_count) {
                                    loc.setAsParameter();
                                }
                            }
                        } else {
                            System.err.println("Skipping unknown code attribute: " + code_attribute_name);
                            data.skipBytes(clength);
                        }
                    }

                    method.setIR(this.bytecode.createIR(code, locals, catch_regions, pool));

                    if (DUMP_IR_ON_LOAD) {
                        System.out.println("Instructions of " + method_name + " " + method_desc);
                        System.out.println(method.getIR());
                    }
                } else if ("Signature".equals(attribute_name)) {
                    method_sig = pool.getUtf8(data.readUnsignedShort());
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
                // TODO
//                if (mn.exceptions != null && !mn.exceptions.isEmpty()) {
//                    for (String ex : (List<String>) mn.exceptions) {
//                        sig.getThrowsSignature().add(ClassTypeSignature.of(ex));
//                    }
//                }
            }
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
            // TODO look for kotlin annotation and set method decomp if found
            // and lang == ANY
        }

        entry.setLanguage(actual_lang);

        for (MethodEntry mth : entry.getMethods()) {
            try {
                StatementBlock block = mth_decomp.decompile(mth);
                mth.setInstructions(block);
            } catch (Exception ex) {
                System.err.println("Error decompiling method body for " + name + " " + mth.toString());
                ex.printStackTrace();
                StatementBlock insns = new StatementBlock(Type.METHOD);
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    List<String> text = new ArrayList<>();
                    text.add("Error decompiling block");
                    for (Insn next : mth.getIR()) {
                        text.add(next.toString());
                    }
                    insns.append(new Comment(text));
                } else {
                    insns.append(new Comment("Error decompiling block"));
                }
                mth.setInstructions(insns);
            }
        }
        for (MethodEntry mth : entry.getStaticMethods()) {
            try {
                StatementBlock block = mth_decomp.decompile(mth);
                mth.setInstructions(block);
            } catch (Exception ex) {
                System.err.println("Error decompiling method body for " + name + " " + mth.toString());
                ex.printStackTrace();
                StatementBlock insns = new StatementBlock(Type.METHOD);
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    List<String> text = new ArrayList<>();
                    text.add("Error decompiling block");
                    for (Insn next : mth.getIR()) {
                        text.add(next.toString());
                    }
                    insns.append(new Comment(text));
                } else {
                    insns.append(new Comment("Error decompiling block"));
                }
                mth.setInstructions(insns);
            }
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

        set.add(entry);
        return entry;
    }

    /**
     * Creates a new annotation instance for the given annotation node.
     */
//    public static Annotation createAnnotation(SourceSet src, AnnotationNode an) {
//        AnnotationType anno_type = src.getAnnotationType(an.desc);
//        Annotation anno = new Annotation(anno_type);
//        if (an.values != null) {
//            for (int i = 0; i * 2 < an.values.size(); i += 2) {
//                String key = (String) an.values.get(i * 2);
//                Object value = an.values.get(i * 2 + 1);
//                anno.setValue(key, value);
//            }
//        }
//        return anno;
//    }

}

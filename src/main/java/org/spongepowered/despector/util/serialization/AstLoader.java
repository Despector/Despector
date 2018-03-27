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
package org.spongepowered.despector.util.serialization;

import com.google.common.base.Throwables;
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Annotation.EnumConstant;
import org.spongepowered.despector.ast.AnnotationType;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.generic.WildcardType;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.AndCondition;
import org.spongepowered.despector.ast.insn.condition.BooleanCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition.CompareOperator;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.condition.InverseCondition;
import org.spongepowered.despector.ast.insn.condition.OrCondition;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.cst.TypeConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.ast.insn.misc.MultiNewArray;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.op.OperatorType;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.Break.Breakable;
import org.spongepowered.despector.ast.stmt.branch.DoWhile;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.Switch;
import org.spongepowered.despector.ast.stmt.branch.TryCatch;
import org.spongepowered.despector.ast.stmt.branch.While;
import org.spongepowered.despector.ast.stmt.invoke.Lambda;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.BaseDecompiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AstLoader {

    private static void expectKey(MessageUnpacker unpack, String key) throws IOException {
        String actual = unpack.readString();
        if (!actual.equals(key)) {
            throw new IllegalStateException("Expected key " + key + " but was " + actual);
        }
    }

    private static void startMap(MessageUnpacker unpack, int size) throws IOException {
        int actual = unpack.readMap();
        if (actual != size) {
            throw new IllegalStateException("Expected map size " + size + " but was " + actual);
        }
    }

    public static void loadSources(SourceSet set, InputStream stream) throws IOException {
        MessageUnpacker unpack = new MessageUnpacker(stream);
        startMap(unpack, 2);
        expectKey(unpack, "version");
        int version = unpack.readInt();
        if (version != AstSerializer.VERSION) {
            throw new IllegalStateException("Unsupported ast version " + version);
        }
        expectKey(unpack, "classes");
        int classes = unpack.readArray();
        for (int i = 0; i < classes; i++) {
            set.add(loadType(unpack, set));
        }
    }

    public static TypeEntry loadType(MessageUnpacker unpack, SourceSet set) throws IOException {
        TypeEntry entry = null;
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        expectKey(unpack, "language");
        Language lang = Language.values()[unpack.readInt()];
        expectKey(unpack, "name");
        String name = unpack.readString();
        if (id == AstSerializer.ENTRY_ID_CLASS) {
            entry = new ClassEntry(set, lang, name);
        } else if (id == AstSerializer.ENTRY_ID_ENUM) {
            entry = new EnumEntry(set, lang, name);
        } else if (id == AstSerializer.ENTRY_ID_INTERFACE) {
            entry = new InterfaceEntry(set, lang, name);
        } else if (id == AstSerializer.ENTRY_ID_ANNOTATIONTYPE) {
            entry = new AnnotationEntry(set, lang, name);
        }
        expectKey(unpack, "access");
        entry.setAccessModifier(AccessModifier.values()[unpack.readInt()]);
        expectKey(unpack, "synthetic");
        entry.setSynthetic(unpack.readBool());
        expectKey(unpack, "final");
        entry.setFinal(unpack.readBool());
        expectKey(unpack, "interfaces");
        int interfaces = unpack.readArray();
        for (int i = 0; i < interfaces; i++) {
            entry.getInterfaces().add(unpack.readString());
        }
        expectKey(unpack, "staticfields");
        int staticfields = unpack.readArray();
        for (int i = 0; i < staticfields; i++) {
            entry.addField(loadField(unpack, set));
        }
        expectKey(unpack, "fields");
        int fields = unpack.readArray();
        for (int i = 0; i < fields; i++) {
            entry.addField(loadField(unpack, set));
        }
        expectKey(unpack, "staticmethods");
        int staticmethods = unpack.readArray();
        for (int i = 0; i < staticmethods; i++) {
            entry.addMethod(loadMethod(unpack, set));
        }
        expectKey(unpack, "methods");
        int methods = unpack.readArray();
        for (int i = 0; i < methods; i++) {
            entry.addMethod(loadMethod(unpack, set));
        }
        String key = unpack.readString();
        if ("signature".equals(key)) {
            ClassSignature sig = loadClassSignature(unpack);
            if (sig != null) {
                entry.setSignature(sig);
            }
            expectKey(unpack, "annotations");
        } else if (!"annotations".equals(key)) {
            throw new IllegalStateException("Expected key annotations but was " + key);
        }
        int annotations = unpack.readArray();
        for (int i = 0; i < annotations; i++) {
            entry.addAnnotation(loadAnnotation(unpack, set));
        }
        expectKey(unpack, "inner_classes");
        int innerclasses = unpack.readArray();
        for (int i = 0; i < innerclasses; i++) {
            startMap(unpack, 8);
            expectKey(unpack, "name");
            String innername = unpack.readString();
            expectKey(unpack, "simple_name");
            String simple_name = null;
            if (unpack.peekType() == MessageType.NIL) {
                unpack.readNil();
            } else {
                simple_name = unpack.readString();
            }
            expectKey(unpack, "outer_name");
            String outer_name = null;
            if (unpack.peekType() == MessageType.NIL) {
                unpack.readNil();
            } else {
                outer_name = unpack.readString();
            }
            int acc = 0;
            expectKey(unpack, "static");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_STATIC;
            }
            expectKey(unpack, "final");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_FINAL;
            }
            expectKey(unpack, "abstract");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_ABSTRACT;
            }
            expectKey(unpack, "synthetic");
            if (unpack.readBool()) {
                acc |= BaseDecompiler.ACC_SYNTHETIC;
            }
            expectKey(unpack, "access");
            AccessModifier mod = AccessModifier.values()[unpack.readInt()];
            switch (mod) {
            case PRIVATE:
                acc |= BaseDecompiler.ACC_PRIVATE;
                break;
            case PUBLIC:
                acc |= BaseDecompiler.ACC_PUBLIC;
                break;
            case PROTECTED:
                acc |= BaseDecompiler.ACC_PROTECTED;
                break;
            case PACKAGE_PRIVATE:
            default:
                break;
            }
            entry.addInnerClass(innername, simple_name, outer_name, acc);
        }
        if (id == AstSerializer.ENTRY_ID_CLASS) {
            expectKey(unpack, "supername");
            ((ClassEntry) entry).setSuperclass(unpack.readString());
        } else if (id == AstSerializer.ENTRY_ID_ENUM) {
            expectKey(unpack, "enumconstants");
            int csts = unpack.readArray();
            EnumEntry e = (EnumEntry) entry;
            for (int i = 0; i < csts; i++) {
                e.addEnumConstant(unpack.readString());
            }
        }

        if (entry.getSignature() == null) {
            ClassSignature sig = new ClassSignature();
            if (entry instanceof ClassEntry) {
                sig.setSuperclassSignature(new GenericClassTypeSignature(((ClassEntry) entry).getSuperclass()));
            } else {
                sig.setSuperclassSignature(new GenericClassTypeSignature("Ljava/lang/Object;"));
            }
            for (String intr : entry.getInterfaces()) {
                sig.getInterfaceSignatures().add(new GenericClassTypeSignature("L" + intr + ";"));
            }
            entry.setSignature(sig);
        }

        return entry;
    }

    public static FieldEntry loadField(MessageUnpacker unpack, SourceSet set) throws IOException {
        startMap(unpack, 12);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.ENTRY_ID_FIELD) {
            throw new IllegalStateException("Expected field");
        }
        FieldEntry entry = new FieldEntry(set);
        expectKey(unpack, "access");
        entry.setAccessModifier(AccessModifier.values()[unpack.readInt()]);
        expectKey(unpack, "name");
        entry.setName(unpack.readString());
        expectKey(unpack, "owner");
        entry.setOwner(unpack.readString());
        expectKey(unpack, "type");
        entry.setType(loadTypeSignature(unpack));
        expectKey(unpack, "final");
        entry.setFinal(unpack.readBool());
        expectKey(unpack, "static");
        entry.setStatic(unpack.readBool());
        expectKey(unpack, "synthetic");
        entry.setSynthetic(unpack.readBool());
        expectKey(unpack, "volatile");
        entry.setVolatile(unpack.readBool());
        expectKey(unpack, "deprecated");
        entry.setDeprecated(unpack.readBool());
        expectKey(unpack, "transient");
        entry.setTransient(unpack.readBool());
        expectKey(unpack, "annotations");
        int annotations = unpack.readArray();
        for (int i = 0; i < annotations; i++) {
            entry.addAnnotation(loadAnnotation(unpack, set));
        }
        return entry;
    }

    public static MethodEntry loadMethod(MessageUnpacker unpack, SourceSet set) throws IOException {
        startMap(unpack, 18);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.ENTRY_ID_METHOD) {
            throw new IllegalStateException("Expected method");
        }
        MethodEntry entry = new MethodEntry(set);
        expectKey(unpack, "access");
        entry.setAccessModifier(AccessModifier.values()[unpack.readInt()]);
        expectKey(unpack, "owner");
        entry.setOwner(unpack.readString());
        expectKey(unpack, "name");
        entry.setName(unpack.readString());
        expectKey(unpack, "description");
        entry.setDescription(unpack.readString());
        expectKey(unpack, "abstract");
        entry.setAbstract(unpack.readBool());
        expectKey(unpack, "final");
        entry.setFinal(unpack.readBool());
        expectKey(unpack, "static");
        entry.setStatic(unpack.readBool());
        expectKey(unpack, "synthetic");
        entry.setSynthetic(unpack.readBool());
        expectKey(unpack, "bridge");
        entry.setBridge(unpack.readBool());
        expectKey(unpack, "varargs");
        entry.setVarargs(unpack.readBool());
        expectKey(unpack, "strictfp");
        entry.setStrictFp(unpack.readBool());
        expectKey(unpack, "synchronized");
        entry.setSynchronized(unpack.readBool());
        expectKey(unpack, "native");
        entry.setNative(unpack.readBool());
        expectKey(unpack, "methodsignature");
        entry.setMethodSignature(loadMethodSignature(unpack));
        expectKey(unpack, "locals");
        Locals locals = loadLocals(unpack, entry, set);
        method_locals = locals;
        entry.setLocals(locals);
        expectKey(unpack, "instructions");
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
        } else {
            StatementBlock block = loadBlock(unpack, StatementBlock.Type.METHOD);
            entry.setInstructions(block);
        }
        expectKey(unpack, "annotations");
        int annotations = unpack.readArray();
        for (int i = 0; i < annotations; i++) {
            entry.addAnnotation(loadAnnotation(unpack, set));
        }
        return entry;
    }

    public static Locals loadLocals(MessageUnpacker unpack, MethodEntry method, SourceSet set) throws IOException {
        int size = unpack.readArray();
        Locals locals = new Locals(method);

        for (int i = 0; i < size; i++) {
            startMap(unpack, 2);
            expectKey(unpack, "index");
            int index = unpack.readInt();
            Local loc = locals.getLocal(index);
            expectKey(unpack, "instances");
            int sz = unpack.readArray();
            for (int j = 0; j < sz; j++) {
                startMap(unpack, 6);
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "start");
                int start = unpack.readInt();
                expectKey(unpack, "end");
                int end = unpack.readInt();
                expectKey(unpack, "final");
                boolean efinal = unpack.readBool();
                LocalInstance insn = new LocalInstance(loc, name, type, start, end);
                insn.setEffectivelyFinal(efinal);
                expectKey(unpack, "annotations");
                int annotations = unpack.readArray();
                for (int k = 0; k < annotations; k++) {
                    insn.getAnnotations().add(loadAnnotation(unpack, set));
                }
                loc.addInstance(insn);
            }
        }

        return locals;
    }

    private static StatementBlock loadBlock(MessageUnpacker unpack, StatementBlock.Type type) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        int statements = unpack.readArray();
        StatementBlock block = new StatementBlock(type);
        for (int i = 0; i < statements; i++) {
            block.append(loadStatement(unpack));
        }
        return block;
    }

    private static Statement loadStatement(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        Function<MessageUnpacker, Statement> loader = statement_loaders.get(id);
        return loader.apply(unpack);
    }

    private static Instruction loadInstruction(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        Function<MessageUnpacker, Instruction> loader = instruction_loaders.get(id);
        return loader.apply(unpack);
    }

    private static Condition loadCondition(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        Function<MessageUnpacker, Condition> loader = condition_loaders.get(id);
        return loader.apply(unpack);
    }

    public static Annotation loadAnnotation(MessageUnpacker unpack, SourceSet set) throws IOException {
        startMap(unpack, 4);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.ENTRY_ID_ANNOTATION) {
            throw new IllegalStateException("Expected annotation");
        }
        expectKey(unpack, "typename");
        String typename = unpack.readString();
        AnnotationType type = set.getAnnotationType(typename);
        Annotation anno = new Annotation(type);
        expectKey(unpack, "runtime");
        type.setRuntimeVisible(unpack.readBool());
        expectKey(unpack, "values");
        int sz = unpack.readArray();
        for (int i = 0; i < sz; i++) {
            startMap(unpack, 4);
            expectKey(unpack, "name");
            String key = unpack.readString();
            expectKey(unpack, "type");
            String cl = unpack.readString();
            Class<?> cls = null;
            try {
                cls = Class.forName(cl);
                type.setType(key, cls);
            } catch (ClassNotFoundException e) {
                Throwables.propagate(e);
            }
            expectKey(unpack, "default");
            Object def = loadAnnotationObject(unpack, set);
            type.setDefault(key, def);
            expectKey(unpack, "value");
            Object val = loadAnnotationObject(unpack, set);
            anno.setValue(key, val);
        }
        return anno;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object loadAnnotationObject(MessageUnpacker unpack, SourceSet set) throws IOException {
        startMap(unpack, 2);
        expectKey(unpack, "typename");
        String cl = unpack.readString();
        Class<?> type = null;
        try {
            type = Class.forName(cl);
        } catch (ClassNotFoundException e) {
            Throwables.propagate(e);
        }
        expectKey(unpack, "value");
        if (type == Integer.class) {
            return unpack.readInt();
        } else if (type == Byte.class) {
            return unpack.readByte();
        } else if (type == Short.class) {
            return unpack.readShort();
        } else if (type == Long.class) {
            return unpack.readLong();
        } else if (type == Float.class) {
            return unpack.readFloat();
        } else if (type == Double.class) {
            return unpack.readDouble();
        } else if (type == String.class) {
            return unpack.readString();
        } else if (type == ArrayList.class) {
            int sz = unpack.readArray();
            List lst = new ArrayList();
            for (int i = 0; i < sz; i++) {
                lst.add(loadAnnotationObject(unpack, set));
            }
        } else if (type == ClassTypeSignature.class) {
            return loadTypeSignature(unpack);
        } else if (type == Annotation.class) {
            return loadAnnotation(unpack, set);
        } else if (type == EnumConstant.class) {
            unpack.readMap();
            expectKey(unpack, "enumtype");
            String enumtype = unpack.readString();
            expectKey(unpack, "constant");
            String cst = unpack.readString();
            return new EnumConstant(enumtype, cst);
        }
        throw new IllegalStateException("Unsupported annotation value type " + type.getName());
    }

    public static ClassSignature loadClassSignature(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        startMap(unpack, 4);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.SIGNATURE_ID_CLASS) {
            throw new IllegalStateException("Expected class signature");
        }
        ClassSignature sig = new ClassSignature();
        expectKey(unpack, "parameters");
        int sz = unpack.readArray();
        for (int i = 0; i < sz; i++) {
            sig.getParameters().add(loadTypeParameter(unpack));
        }
        expectKey(unpack, "superclass");
        sig.setSuperclassSignature((GenericClassTypeSignature) loadTypeSignature(unpack));
        expectKey(unpack, "interfaces");
        int interfaces = unpack.readArray();
        for (int i = 0; i < interfaces; i++) {
            sig.getInterfaceSignatures().add((GenericClassTypeSignature) loadTypeSignature(unpack));
        }
        return sig;
    }

    private static TypeParameter loadTypeParameter(MessageUnpacker unpack) throws IOException {
        startMap(unpack, 4);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.SIGNATURE_ID_PARAM) {
            throw new IllegalStateException("Expected type parameter");
        }
        expectKey(unpack, "identifier");
        String ident = unpack.readString();
        expectKey(unpack, "classbound");
        TypeSignature cl = loadTypeSignature(unpack);
        TypeParameter param = new TypeParameter(ident, cl);
        expectKey(unpack, "interfacebounds");
        int sz = unpack.readArray();
        for (int i = 0; i < sz; i++) {
            param.getInterfaceBounds().add(loadTypeSignature(unpack));
        }
        return param;
    }

    public static MethodSignature loadMethodSignature(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        startMap(unpack, 5);
        expectKey(unpack, "id");
        int id = unpack.readInt();
        if (id != AstSerializer.SIGNATURE_ID_METHOD) {
            throw new IllegalStateException("Expected method signature");
        }
        MethodSignature sig = new MethodSignature();
        expectKey(unpack, "type_parameters");
        {
            int sz = unpack.readArray();
            for (int i = 0; i < sz; i++) {
                sig.getTypeParameters().add(loadTypeParameter(unpack));
            }
        }
        expectKey(unpack, "parameters");
        {
            int sz = unpack.readArray();
            for (int i = 0; i < sz; i++) {
                sig.getParameters().add(loadTypeSignature(unpack));
            }
        }
        expectKey(unpack, "exceptions");
        {
            int sz = unpack.readArray();
            for (int i = 0; i < sz; i++) {
                sig.getThrowsSignature().add(loadTypeSignature(unpack));
            }
        }
        expectKey(unpack, "returntype");
        sig.setReturnType(loadTypeSignature(unpack));
        return sig;
    }

    public static TypeSignature loadTypeSignature(MessageUnpacker unpack) throws IOException {
        if (unpack.peekType() == MessageType.NIL) {
            unpack.readNil();
            return null;
        }
        unpack.readMap();
        expectKey(unpack, "id");
        int id = unpack.readInt();
        Function<MessageUnpacker, TypeSignature> loader = signature_loaders.get(id);
        return loader.apply(unpack);
    }

    private static LocalInstance loadLocal(MessageUnpacker unpack) throws IOException {
        startMap(unpack, 3);
        expectKey(unpack, "local");
        int index = unpack.readInt();
        expectKey(unpack, "start");
        int start = unpack.readInt();
        expectKey(unpack, "type");
        String type = null;
        if (unpack.peekType() != MessageType.NIL) {
            type = unpack.readString();
        } else {
            unpack.readNil();
        }
        Local loc = method_locals.getLocal(index);
        return loc.find(start, type);
    }

    private static final Map<Integer, Function<MessageUnpacker, Statement>> statement_loaders;
    private static final Map<Integer, Function<MessageUnpacker, Instruction>> instruction_loaders;
    private static final Map<Integer, Function<MessageUnpacker, Condition>> condition_loaders;
    private static final Map<Integer, Function<MessageUnpacker, TypeSignature>> signature_loaders;

    private static final Map<Integer, Breakable> breakables = new HashMap<>();
    private static Locals method_locals;

    static {
        statement_loaders = new HashMap<>();
        instruction_loaders = new HashMap<>();
        condition_loaders = new HashMap<>();
        signature_loaders = new HashMap<>();
        instruction_loaders.put(AstSerializer.STATEMENT_ID_ARRAY_ACCESS, (unpack) -> {
            try {
                expectKey(unpack, "array");
                Instruction array = loadInstruction(unpack);
                expectKey(unpack, "index");
                Instruction index = loadInstruction(unpack);
                ArrayAccess stmt = new ArrayAccess(array, index);
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_ARRAY_ASSIGN, (unpack) -> {
            try {
                expectKey(unpack, "array");
                Instruction array = loadInstruction(unpack);
                expectKey(unpack, "index");
                Instruction index = loadInstruction(unpack);
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                ArrayAssignment stmt = new ArrayAssignment(array, index, val);
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_BREAK, (unpack) -> {
            try {
                expectKey(unpack, "type");
                Break.Type type = Break.Type.values()[unpack.readInt()];
                expectKey(unpack, "nested");
                boolean nested = unpack.readBool();
                expectKey(unpack, "break_id");
                int key = unpack.readInt();
                Breakable brk = breakables.get(key);
                return new Break(brk, type, nested);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_CAST, (unpack) -> {
            try {
                expectKey(unpack, "value");
                Instruction val = loadInstruction(unpack);
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                return new Cast(type, val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_COMMENT, (unpack) -> {
            try {
                expectKey(unpack, "comment");
                int size = unpack.readArray();
                List<String> text = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    text.add(unpack.readString());
                }
                return new Comment(text);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_DO_WHILE, (unpack) -> {
            try {
                expectKey(unpack, "condition");
                Condition cond = loadCondition(unpack);
                DoWhile loop = new DoWhile(cond, new StatementBlock(StatementBlock.Type.WHILE));
                expectKey(unpack, "breakpoints");
                int brk_size = unpack.readArray();
                for (int i = 0; i < brk_size; i++) {
                    breakables.put(unpack.readInt(), loop);
                }
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.WHILE);
                loop.setBody(body);
                for (Iterator<Map.Entry<Integer, Breakable>> it = breakables.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, Breakable> n = it.next();
                    if (n.getValue() == loop) {
                        it.remove();
                    }
                }
                return loop;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_DOUBLE_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new DoubleConstant(unpack.readDouble());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_DYNAMIC_INVOKE, (unpack) -> {
            try {
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "method");
                String method = unpack.readString();
                expectKey(unpack, "desc");
                String desc = unpack.readString();
                return new Lambda(owner, method, desc, type, name);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_FLOAT_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new FloatConstant(unpack.readFloat());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_FOR, (unpack) -> {
            try {
                expectKey(unpack, "init");
                Statement init = loadStatement(unpack);
                expectKey(unpack, "condition");
                Condition condition = loadCondition(unpack);
                expectKey(unpack, "incr");
                Statement incr = loadStatement(unpack);
                For loop = new For(init, condition, incr, new StatementBlock(StatementBlock.Type.WHILE));
                expectKey(unpack, "breakpoints");
                int brk_size = unpack.readArray();
                for (int i = 0; i < brk_size; i++) {
                    breakables.put(unpack.readInt(), loop);
                }
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.WHILE);
                loop.setBody(body);
                for (Iterator<Map.Entry<Integer, Breakable>> it = breakables.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, Breakable> n = it.next();
                    if (n.getValue() == loop) {
                        it.remove();
                    }
                }
                return loop;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_FOREACH, (unpack) -> {
            try {
                expectKey(unpack, "local");
                LocalInstance loc = loadLocal(unpack);
                expectKey(unpack, "collection");
                Instruction col = loadInstruction(unpack);
                ForEach loop = new ForEach(col, loc, new StatementBlock(StatementBlock.Type.WHILE));
                expectKey(unpack, "breakpoints");
                int brk_size = unpack.readArray();
                for (int i = 0; i < brk_size; i++) {
                    breakables.put(unpack.readInt(), loop);
                }
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.WHILE);
                loop.setBody(body);
                for (Iterator<Map.Entry<Integer, Breakable>> it = breakables.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, Breakable> n = it.next();
                    if (n.getValue() == loop) {
                        it.remove();
                    }
                }
                return loop;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_IF, (unpack) -> {
            try {
                expectKey(unpack, "condition");
                Condition cond = loadCondition(unpack);
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.IF);
                If iif = new If(cond, body);
                expectKey(unpack, "elif");
                int sz = unpack.readArray();
                for (int i = 0; i < sz; i++) {
                    startMap(unpack, 2);
                    expectKey(unpack, "condition");
                    Condition elif_cond = loadCondition(unpack);
                    expectKey(unpack, "body");
                    StatementBlock elif_body = loadBlock(unpack, StatementBlock.Type.IF);
                    iif.new Elif(elif_cond, elif_body);
                }
                expectKey(unpack, "else");
                StatementBlock else_body = loadBlock(unpack, StatementBlock.Type.IF);
                if (else_body != null) {
                    iif.new Else(else_body);
                }
                return iif;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_INCREMENT, (unpack) -> {
            try {
                expectKey(unpack, "local");
                LocalInstance loc = loadLocal(unpack);
                expectKey(unpack, "increment");
                int incr = unpack.readInt();
                return new Increment(loc, incr);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_FIELD_ACCESS, (unpack) -> {
            try {
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "desc");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "owner_val");
                Instruction oval = loadInstruction(unpack);
                return new InstanceFieldAccess(name, type, owner, oval);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_FIELD_ASSIGN, (unpack) -> {
            try {
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "owner_val");
                Instruction oval = loadInstruction(unpack);
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                return new InstanceFieldAssignment(name, type, owner, oval, val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_INVOKE, (unpack) -> {
            try {
                expectKey(unpack, "type");
                InstanceMethodInvoke.Type t = InstanceMethodInvoke.Type.values()[unpack.readInt()];
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "desc");
                String desc = unpack.readString();
                expectKey(unpack, "params");
                int sz = unpack.readArray();
                Instruction[] args = new Instruction[sz];
                for (int i = 0; i < sz; i++) {
                    args[i] = loadInstruction(unpack);
                }
                expectKey(unpack, "callee");
                Instruction callee = loadInstruction(unpack);
                return new InstanceMethodInvoke(t, name, desc, owner, args, callee);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_OF, (unpack) -> {
            try {
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                expectKey(unpack, "type");
                String type = unpack.readString();
                return new InstanceOf(val, ClassTypeSignature.of(type));
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INT_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new IntConstant(unpack.readInt());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_INVOKE, (unpack) -> {
            try {
                expectKey(unpack, "inner");
                Instruction inner = loadInstruction(unpack);
                return new InvokeStatement(inner);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_LOCAL_ACCESS, (unpack) -> {
            try {
                expectKey(unpack, "local");
                LocalInstance loc = loadLocal(unpack);
                return new LocalAccess(loc);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_LOCAL_ASSIGN, (unpack) -> {
            try {
                expectKey(unpack, "local");
                LocalInstance loc = loadLocal(unpack);
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                return new LocalAssignment(loc, val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_LONG_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new LongConstant(unpack.readLong());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_MULTI_NEW_ARRAY, (unpack) -> {
            try {
                expectKey(unpack, "type");
                String type = unpack.readString();
                expectKey(unpack, "sizes");
                int sz = unpack.readArray();
                Instruction[] sizes = new Instruction[sz];
                for (int i = 0; i < sz; i++) {
                    sizes[i] = loadInstruction(unpack);
                }
                return new MultiNewArray(ClassTypeSignature.of(type), sizes);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEGATIVE_OPERATOR, (unpack) -> {
            try {
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                return new NegativeOperator(val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEW, (unpack) -> {
            try {
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "ctor");
                String ctor = unpack.readString();
                expectKey(unpack, "params");
                int sz = unpack.readArray();
                Instruction[] params = new Instruction[sz];
                for (int i = 0; i < sz; i++) {
                    params[i] = loadInstruction(unpack);
                }
                return new New(type, ctor, params);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEW_ARRAY, (unpack) -> {
            try {
                expectKey(unpack, "type");
                String type = unpack.readString();
                expectKey(unpack, "size");
                Instruction size = loadInstruction(unpack);
                Instruction[] values = null;
                expectKey(unpack, "values");
                if (unpack.peekType() == MessageType.NIL) {
                    unpack.readNil();
                } else {
                    int sz = unpack.readArray();
                    values = new Instruction[sz];
                    for (int i = 0; i < sz; i++) {
                        values[i] = loadInstruction(unpack);
                    }
                }
                return new NewArray(ClassTypeSignature.of(type), size, values);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NULL_CONSTANT, (unpack) -> {
            return NullConstant.NULL;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NUMBER_COMPARE, (unpack) -> {
            try {
                expectKey(unpack, "left");
                Instruction left = loadInstruction(unpack);
                expectKey(unpack, "right");
                Instruction right = loadInstruction(unpack);
                return new NumberCompare(left, right);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_OPERATOR, (unpack) -> {
            try {
                expectKey(unpack, "left");
                Instruction left = loadInstruction(unpack);
                expectKey(unpack, "right");
                Instruction right = loadInstruction(unpack);
                expectKey(unpack, "operator");
                OperatorType op = OperatorType.values()[unpack.readInt()];
                return new Operator(op, left, right);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_RETURN, (unpack) -> {
            try {
                expectKey(unpack, "value");
                Instruction val = null;
                if (unpack.peekType() == MessageType.NIL) {
                    unpack.readNil();
                } else {
                    val = loadInstruction(unpack);
                }
                return new Return(val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STATIC_FIELD_ACCESS, (unpack) -> {
            try {
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "desc");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                return new StaticFieldAccess(name, type, owner);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_STATIC_FIELD_ASSIGN, (unpack) -> {
            try {
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "type");
                TypeSignature type = loadTypeSignature(unpack);
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                return new StaticFieldAssignment(name, type, owner, val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STATIC_INVOKE, (unpack) -> {
            try {
                expectKey(unpack, "name");
                String name = unpack.readString();
                expectKey(unpack, "owner");
                String owner = unpack.readString();
                expectKey(unpack, "desc");
                String desc = unpack.readString();
                expectKey(unpack, "params");
                int sz = unpack.readArray();
                Instruction[] args = new Instruction[sz];
                for (int i = 0; i < sz; i++) {
                    args[i] = loadInstruction(unpack);
                }
                return new StaticMethodInvoke(name, desc, owner, args);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STRING_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new StringConstant(unpack.readString());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_SWITCH, (unpack) -> {
            try {
                expectKey(unpack, "var");
                Instruction var = loadInstruction(unpack);
                expectKey(unpack, "cases");
                int sz = unpack.readArray();
                Switch sw = new Switch(var);
                for (int i = 0; i < sz; i++) {
                    unpack.readMap();
                    expectKey(unpack, "body");
                    StatementBlock body = loadBlock(unpack, StatementBlock.Type.SWITCH);
                    expectKey(unpack, "breaks");
                    boolean breaks = unpack.readBool();
                    expectKey(unpack, "default");
                    boolean is_def = unpack.readBool();
                    expectKey(unpack, "indices");
                    List<Integer> index = new ArrayList<>();
                    int s = unpack.readArray();
                    for (int k = 0; k < s; k++) {
                        index.add(unpack.readInt());
                    }
                    sw.new Case(body, breaks, is_def, index);
                }
                return sw;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_TERNARY, (unpack) -> {
            try {
                expectKey(unpack, "condition");
                Condition cond = loadCondition(unpack);
                expectKey(unpack, "true");
                Instruction tr = loadInstruction(unpack);
                expectKey(unpack, "false");
                Instruction fl = loadInstruction(unpack);
                return new Ternary(cond, tr, fl);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_THROW, (unpack) -> {
            try {
                expectKey(unpack, "ex");
                Instruction tr = loadInstruction(unpack);
                return new Throw(tr);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_TRY_CATCH, (unpack) -> {
            try {
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.TRY);
                expectKey(unpack, "catch");
                int catches = unpack.readArray();
                TryCatch tr = new TryCatch(body);
                for (int i = 0; i < catches; i++) {
                    unpack.readMap();
                    expectKey(unpack, "exceptions");
                    int ex = unpack.readArray();
                    List<String> exceptions = new ArrayList<>();
                    for (int k = 0; k < ex; k++) {
                        exceptions.add(unpack.readString());
                    }
                    expectKey(unpack, "block");
                    StatementBlock catch_body = loadBlock(unpack, StatementBlock.Type.CATCH);
                    String k = unpack.readString();
                    if ("local".equals(k)) {
                        LocalInstance loc = loadLocal(unpack);
                        tr.new CatchBlock(loc, exceptions, catch_body);
                    } else if ("dummy_name".equals(k)) {
                        String dummy = unpack.readString();
                        tr.new CatchBlock(dummy, exceptions, catch_body);
                    } else {
                        throw new IllegalStateException("Expected key local or dummy_name but was " + k);
                    }
                }
                return tr;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_TYPE_CONSTANT, (unpack) -> {
            try {
                expectKey(unpack, "cst");
                return new TypeConstant(ClassTypeSignature.of(unpack.readString()));
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_WHILE, (unpack) -> {
            try {
                expectKey(unpack, "condition");
                Condition condition = loadCondition(unpack);
                While loop = new While(condition, new StatementBlock(StatementBlock.Type.WHILE));
                expectKey(unpack, "breakpoints");
                int brk_size = unpack.readArray();
                for (int i = 0; i < brk_size; i++) {
                    breakables.put(unpack.readInt(), loop);
                }
                expectKey(unpack, "body");
                StatementBlock body = loadBlock(unpack, StatementBlock.Type.WHILE);
                loop.setBody(body);
                for (Iterator<Map.Entry<Integer, Breakable>> it = breakables.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, Breakable> n = it.next();
                    if (n.getValue() == loop) {
                        it.remove();
                    }
                }
                return loop;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        condition_loaders.put(AstSerializer.CONDITION_ID_AND, (unpack) -> {
            try {
                expectKey(unpack, "args");
                int sz = unpack.readArray();
                Condition[] args = new Condition[sz];
                for (int i = 0; i < sz; i++) {
                    args[i] = loadCondition(unpack);
                }
                return new AndCondition(args);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        condition_loaders.put(AstSerializer.CONDITION_ID_BOOL, (unpack) -> {
            try {
                expectKey(unpack, "val");
                Instruction val = loadInstruction(unpack);
                expectKey(unpack, "inverse");
                boolean inv = unpack.readBool();
                return new BooleanCondition(val, inv);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        condition_loaders.put(AstSerializer.CONDITION_ID_COMPARE, (unpack) -> {
            try {
                expectKey(unpack, "left");
                Instruction left = loadInstruction(unpack);
                expectKey(unpack, "right");
                Instruction right = loadInstruction(unpack);
                expectKey(unpack, "op");
                CompareOperator op = CompareOperator.values()[unpack.readInt()];
                return new CompareCondition(left, right, op);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        condition_loaders.put(AstSerializer.CONDITION_ID_INVERSE, (unpack) -> {
            try {
                expectKey(unpack, "val");
                Condition val = loadCondition(unpack);
                return new InverseCondition(val);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        condition_loaders.put(AstSerializer.CONDITION_ID_OR, (unpack) -> {
            try {
                expectKey(unpack, "args");
                int sz = unpack.readArray();
                Condition[] args = new Condition[sz];
                for (int i = 0; i < sz; i++) {
                    args[i] = loadCondition(unpack);
                }
                return new OrCondition(args);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        signature_loaders.put(AstSerializer.SIGNATURE_ID_TYPECLASS, (unpack) -> {
            try {
                expectKey(unpack, "type");
                return ClassTypeSignature.of(unpack.readString());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        signature_loaders.put(AstSerializer.SIGNATURE_ID_TYPEVOID, (unpack) -> {
            return VoidTypeSignature.VOID;
        });
        signature_loaders.put(AstSerializer.SIGNATURE_ID_TYPEVAR, (unpack) -> {
            try {
                expectKey(unpack, "identifier");
                return new TypeVariableSignature(unpack.readString());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        signature_loaders.put(AstSerializer.SIGNATURE_ID_TYPEGENERIC, (unpack) -> {
            try {
                expectKey(unpack, "type");
                GenericClassTypeSignature sig = new GenericClassTypeSignature(unpack.readString());
                expectKey(unpack, "args");
                int sz = unpack.readArray();
                for (int i = 0; i < sz; i++) {
                    startMap(unpack, 3);
                    expectKey(unpack, "id");
                    int id = unpack.readInt();
                    if (id != AstSerializer.SIGNATURE_ID_ARG) {
                        throw new IllegalStateException("Expected type argument");
                    }
                    expectKey(unpack, "wildcard");
                    WildcardType wild = WildcardType.values()[unpack.readInt()];
                    expectKey(unpack, "signature");
                    TypeSignature arg = loadTypeSignature(unpack);
                    sig.getArguments().add(new TypeArgument(wild, arg));
                }
                return sig;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
    }

}

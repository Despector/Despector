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
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.Break.Breakable;
import org.spongepowered.despector.ast.stmt.branch.DoWhile;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.invoke.DynamicInvoke;
import org.spongepowered.despector.ast.stmt.misc.Comment;
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
            entry.setSignature(loadClassSignature(unpack));
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
            String simple_name = unpack.readString();
            expectKey(unpack, "outer_name");
            String outer_name = unpack.readString();
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

        return entry;
    }

    public static FieldEntry loadField(MessageUnpacker unpack, SourceSet set) throws IOException {
        unpack.readMap();
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
        unpack.readMap();
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
        expectKey(unpack, "desc");
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
        unpack.readMap();
        throw new IllegalStateException();
    }

    public static ClassSignature loadClassSignature(MessageUnpacker unpack) {

        throw new IllegalStateException();
    }

    public static MethodSignature loadMethodSignature(MessageUnpacker unpack) {

        throw new IllegalStateException();
    }

    public static TypeSignature loadTypeSignature(MessageUnpacker unpack) {

        throw new IllegalStateException();
    }

    private static final Map<Integer, Function<MessageUnpacker, Statement>> statement_loaders;
    private static final Map<Integer, Function<MessageUnpacker, Instruction>> instruction_loaders;
    private static final Map<Integer, Function<MessageUnpacker, Condition>> condition_loaders;

    private static final Map<Integer, Breakable> breakables = new HashMap<>();

    static {
        statement_loaders = new HashMap<>();
        instruction_loaders = new HashMap<>();
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
                return new DynamicInvoke(owner, method, desc, type, name);
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
                ForEach loop = new ForEach(init, condition, incr, new StatementBlock(StatementBlock.Type.WHILE));
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
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_INCREMENT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_FIELD_ACCESS, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_FIELD_ASSIGN, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_INVOKE, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INSTANCE_OF, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INT_CONSTANT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_INVOKE, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_LOCAL_ACCESS, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_LOCAL_ASSIGN, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_LONG_CONSTANT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_MULTI_NEW_ARRAY, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEGATIVE_OPERATOR, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEW, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NEW_ARRAY, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_NULL_CONSTANT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_NUMBER_COMPARE, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_OPERATOR, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_RETURN, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STATIC_FIELD_ACCESS, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_STATIC_FIELD_ASSIGN, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STATIC_INVOKE, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_STRING_CONSTANT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_SWITCH, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_TERNARY, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_THROW, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_TRY_CATCH, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        instruction_loaders.put(AstSerializer.STATEMENT_ID_TYPE_CONSTANT, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
        statement_loaders.put(AstSerializer.STATEMENT_ID_WHILE, (unpack) -> {
            try {
                return stmt;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            return null;
        });
    }

}

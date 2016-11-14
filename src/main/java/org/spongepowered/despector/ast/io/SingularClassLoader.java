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
package org.spongepowered.despector.ast.io;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.io.insn.InstructionTreeBuilder;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.NewRefArg;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssign;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.TypeHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class SingularClassLoader {

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

    @SuppressWarnings("unchecked")
    public TypeEntry load(ClassNode cn, SourceSet src) {
        System.out.println("Decompiling class " + cn.name);
        int acc = cn.access;
        TypeEntry entry = null;
        if ((acc & ACC_ENUM) != 0) {
            entry = new EnumEntry(src);
        } else if ((acc & ACC_INTERFACE) != 0) {
            entry = new InterfaceEntry(src);
        } else {
            entry = new ClassEntry(src);
            ((ClassEntry) entry).setSuperclass("L" + cn.superName + ";");
        }
        entry.setName(cn.name);
        entry.setAccessModifier(AccessModifier.fromModifiers(cn.access));
        entry.setFinal((cn.access & ACC_FINAL) != 0);
        entry.setSynthetic((cn.access & ACC_SYNTHETIC) != 0);
        entry.getGenericArgs().addAll(TypeHelper.getGenericArgs(cn.signature));

        // Find all fields
        for (FieldNode fn : (List<FieldNode>) cn.fields) {
            FieldEntry f = new FieldEntry(src);
            f.setAccessModifier(AccessModifier.fromModifiers(fn.access));
            f.setFinal((fn.access & ACC_FINAL) != 0);
            f.setName(fn.name);
            f.setOwner(cn.name);
            f.setStatic((fn.access & ACC_STATIC) != 0);
            f.setType(fn.desc);
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
            try {
                StatementBlock insns = InstructionTreeBuilder.build(mn);
                m.setInstructions(insns);
            } catch (Exception ex) {
                System.err.println("Error decompiling method body for " + cn.name + " " + m.toString());
                ex.printStackTrace();
                System.err.println("Offending method bytecode:");
                Iterator<AbstractInsnNode> it = mn.instructions.iterator();
                while (it.hasNext()) {
                    System.err.println(AstUtil.insnToString(it.next()));
                }
            }
            entry.addMethod(m);
        }

        if (entry instanceof EnumEntry) {
            MethodEntry clinit = entry.getStaticMethod("<clinit>");
            if (clinit != null) {
                Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
                while (initializers.hasNext()) {
                    Statement next = initializers.next();
                    if (!(next instanceof StaticFieldAssign)) {
                        break;
                    }
                    StaticFieldAssign assign = (StaticFieldAssign) next;
                    if (!TypeHelper.descToType(assign.getOwner()).equals(entry.getName()) || !(assign.getValue() instanceof NewRefArg)) {
                        break;
                    }
                    ((EnumEntry) entry).addEnumConstant(assign.getFieldName());
                }
            }
        }
        if (src != null) {
            src.add(entry);
        }
        return entry;

    }

}

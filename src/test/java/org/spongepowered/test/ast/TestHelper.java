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
package org.spongepowered.test.ast;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.decompiler.InstructionTreeBuilder;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.AstUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestHelper {
    
    private static final Map<Class<?>, ClassNode> cached_types = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static StatementBlock get(Class<?> cls, String method_name) throws IOException {
        ClassNode type = cached_types.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            type = new ClassNode();
            cr.accept(type, 0);
            cached_types.put(cls, type);
        }
        MethodNode mn = null;
        for (MethodNode m : ((List<MethodNode>) type.methods)) {
            if (m.name.equals(method_name)) {
                mn = m;
                break;
            }
        }
        if (mn == null) {
            return null;
        }
        StatementBlock insns = null;
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = InstructionTreeBuilder.build(mn);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + method_name);
            ex.printStackTrace();
            System.err.println("Offending method bytecode:");
            Iterator<AbstractInsnNode> it = mn.instructions.iterator();
            while (it.hasNext()) {
                System.err.println(AstUtil.insnToString(it.next()));
            }
            return null;
        }
        return insns;
    }

    @SuppressWarnings("unchecked")
    public static String getAsString(Class<?> cls, String method_name) throws IOException {
        ClassNode type = cached_types.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            type = new ClassNode();
            cr.accept(type, 0);
            cached_types.put(cls, type);
        }
        MethodNode mn = null;
        for (MethodNode m : ((List<MethodNode>) type.methods)) {
            if (m.name.equals(method_name)) {
                mn = m;
                break;
            }
        }
        if (mn == null) {
            return "";
        }
        StatementBlock insns = null;
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = InstructionTreeBuilder.build(mn);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + method_name);
            ex.printStackTrace();
            System.err.println("Offending method bytecode:");
            Iterator<AbstractInsnNode> it = mn.instructions.iterator();
            while (it.hasNext()) {
                System.err.println(AstUtil.insnToString(it.next()));
            }
            return "";
        }
        StringWriter writer = new StringWriter();
        EmitterContext emitter = new EmitterContext(Emitters.JAVA, writer, EmitterFormat.defaults());
        emitter.emitBody(insns);
        return writer.toString();
    }

}

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
package org.spongepowered.test.util;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.SignatureParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class TestHelper {

    private static final Map<Class<?>, ClassNode> CACHED_TYPES = Maps.newHashMap();
    private static final SourceSet DUMMY_SOURCE_SET = new SourceSet();

    @SuppressWarnings("unchecked")
    public static StatementBlock get(Class<?> cls, String method_name) throws IOException {
        ClassNode type = CACHED_TYPES.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            type = new ClassNode();
            cr.accept(type, 0);
            CACHED_TYPES.put(cls, type);
        }
        MethodEntry dummy = new MethodEntry(DUMMY_SOURCE_SET);
        dummy.setOwner("Ljava/lang/Object;");
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
        Locals locals = Decompilers.JAVA_METHOD.createLocals(dummy, mn);
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = Decompilers.JAVA_METHOD.decompile(dummy, mn, locals);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + method_name);
            ex.printStackTrace();
            return null;
        }
        return insns;
    }

    public static String getAsString(Class<?> cls, String method_name) throws IOException {
        ClassNode type = CACHED_TYPES.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            type = new ClassNode();
            cr.accept(type, 0);
            CACHED_TYPES.put(cls, type);
        }
        return getAsString(type, method_name);
    }

    public static String getAsString(byte[] cls, String method_name) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(cls));
        ClassNode type = new ClassNode();
        cr.accept(type, 0);
        return getAsString(type, method_name);
    }

    @SuppressWarnings("unchecked")
    private static MethodNode getMethod(ClassNode type, String method_name) {
        MethodNode mn = null;
        for (MethodNode m : ((List<MethodNode>) type.methods)) {
            if (m.name.equals(method_name)) {
                mn = m;
                break;
            }
        }
        if (mn == null) {
            throw new IllegalArgumentException("Method not found: " + method_name);
        }
        return mn;
    }

    public static String getAsString(ClassNode type, String method_name) {
        MethodNode mn = getMethod(type, method_name);
        return getAsString(type, mn);
    }

    public static String getAsString(ClassNode type, MethodNode mn) {
        MethodEntry dummy = new MethodEntry(DUMMY_SOURCE_SET);
        dummy.setAbstract((mn.access & ACC_ABSTRACT) != 0);
        dummy.setAccessModifier(AccessModifier.fromModifiers(mn.access));
        dummy.setFinal((mn.access & ACC_FINAL) != 0);
        dummy.setName(mn.name);
        dummy.setOwner(type.name);
        dummy.setSignature(mn.desc);
        dummy.setStatic((mn.access & ACC_STATIC) != 0);
        dummy.setSynthetic((mn.access & ACC_SYNTHETIC) != 0);

        if (mn.signature != null) {
            dummy.setMethodSignature(SignatureParser.parseMethod(mn.signature));
        }
        StatementBlock insns = null;
        Locals locals = Decompilers.JAVA_METHOD.createLocals(dummy, mn);
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = Decompilers.JAVA_METHOD.decompile(dummy, mn, locals);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + mn.name);
            ex.printStackTrace();
            throw ex;
        }
        StringWriter writer = new StringWriter();
        EmitterContext emitter = new EmitterContext(writer, EmitterFormat.defaults());
        emitter.setEmitterSet(Emitters.JAVA_SET);
        emitter.setMethod(dummy);
        emitter.emitBody(insns);
        return writer.toString();
    }

    public static void check(Class<?> cls, String method_name, String expected) throws IOException {
        ClassNode type = CACHED_TYPES.get(cls);
        if (type == null) {
            String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path, cls.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(new FileInputStream(file));
            type = new ClassNode();
            cr.accept(type, 0);
            CACHED_TYPES.put(cls, type);
        }
        check(type, method_name, expected);
    }

    @SuppressWarnings("unchecked")
    public static void check(ClassNode type, String method_name, String expected) {
        MethodNode mn = getMethod(type, method_name);
        String actual = getAsString(type, method_name);
        if (!actual.equals(expected)) {
            System.err.println("Test " + method_name + " failed!");
            System.err.println("Expected:");
            System.err.println(expected);
            System.err.println("Found:");
            System.err.println(actual);
            System.err.println("Bytecode:");
            for (Iterator<AbstractInsnNode> it = mn.instructions.iterator(); it.hasNext();) {
                AbstractInsnNode next = it.next();
                System.err.println(AstUtil.insnToString(next));
            }
            Assert.assertEquals(expected, actual);
        }
    }

}

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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.config.Constants;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.AstUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = Decompilers.JAVA_METHOD.decompile(dummy, mn);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + method_name);
            ex.printStackTrace();
            if (!Constants.TRACE_ALL) {
                System.out.println("Starting error trace");
                System.out.flush();
                System.err.flush();
                Constants.TRACE_ACTIVE = true;
                try {
                    Decompilers.JAVA_METHOD.decompile(dummy, mn);
                } catch (Exception e) {
                }
                Constants.TRACE_ACTIVE = false;
            }
            System.err.println("Offending method bytecode:");
            Iterator<AbstractInsnNode> it = mn.instructions.iterator();
            while (it.hasNext()) {
                System.err.println(AstUtil.insnToString(it.next()));
            }
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
    public static String getAsString(ClassNode type, String method_name) {
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
        MethodEntry dummy = new MethodEntry(DUMMY_SOURCE_SET);
        dummy.setOwner("Ljava/lang/Object;");
        StatementBlock insns = null;
        try {
            System.out.println("Decompiling method " + mn.name);
            insns = Decompilers.JAVA_METHOD.decompile(dummy, mn);
        } catch (Exception ex) {
            System.err.println("Error decompiling method body for " + type.name + " " + method_name);
            ex.printStackTrace();
            if (!Constants.TRACE_ALL) {
                System.out.println("Starting error trace");
                System.out.flush();
                System.err.flush();
                Constants.TRACE_ACTIVE = true;
                try {
                    Decompilers.JAVA_METHOD.decompile(dummy, mn);
                } catch (Exception e) {
                }
                Constants.TRACE_ACTIVE = false;
            }
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

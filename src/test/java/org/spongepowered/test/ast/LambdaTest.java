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
package org.spongepowered.test.ast;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LambdaTest {

    @BeforeClass
    public static void setup() {
        LibraryConfiguration.quiet = false;
        LibraryConfiguration.parallel = false;
    }

    public void test_lambda() {
        Runnable r = () -> System.out.println("Hello World");
        r.run();
    }

    @Test
    public void testLambda() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        if (TestHelper.IS_ECLIPSE) {
            mv.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("()V"), new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$0", "()V"),
                            Type.getType("()V")});
        } else {
            // javac adds an extra decoration to the lambda method name with the
            // name of the calling method
            mv.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("()V"),
                            new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$test_lambda$0", "()V"),
                            Type.getType("()V")});
        }
        mv.visitVarInsn(ASTORE, 1);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true);
        mv.visitInsn(RETURN);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", "Lorg/spongepowered/test/ast/LambdaTest;", null, start, end, 0);
        mv.visitLocalVariable("r", "Ljava/lang/Runnable;", null, l1, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "Runnable r = () -> System.out.println(\"Hello World\");\n"
                + "r.run();";
        Assert.assertEquals(good, insn);
    }

    public void test_consumer() {
        Consumer<Object> r = (obj) -> System.out.println("Hello World");
        r.accept(null);
    }

    @Test
    public void testConsumer() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        if (TestHelper.IS_ECLIPSE) {
            mv.visitInvokeDynamicInsn("accept", "()Ljava/util/function/Consumer;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("(Ljava/lang/Object;)V"),
                            new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$1", "(Ljava/lang/Object;)V"),
                            Type.getType("(Ljava/lang/Object;)V")});
        } else {
            // javac adds an extra decoration to the lambda method name with the
            // name of the calling method
            mv.visitInvokeDynamicInsn("accept", "()Ljava/util/function/Consumer;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("(Ljava/lang/Object;)V"),
                            new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$test_consumer$1", "(Ljava/lang/Object;)V"),
                            Type.getType("(Ljava/lang/Object;)V")});
        }
        mv.visitVarInsn(ASTORE, 1);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);
        mv.visitInsn(RETURN);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", "Lorg/spongepowered/test/ast/LambdaTest;", null, start, end, 0);
        mv.visitLocalVariable("r", "Ljava/util/function/Consumer;", "Ljava/util/function/Consumer<Ljava/lang/Object;>;", l1, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "java.util.function.Consumer<Object> r = (obj) -> System.out.println(\"Hello World\");\n"
                + "r.accept(null);";
        Assert.assertEquals(good, insn);
    }

    public void test_producer() throws Exception {
        Callable<Object> r = () -> null;
        r.call();
    }

    @Test
    public void testProducer() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        if (TestHelper.IS_ECLIPSE) {
            mv.visitInvokeDynamicInsn("call", "()Ljava/util/concurrent/Callable;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("()Ljava/lang/Object;"),
                            new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$2", "()Ljava/lang/Object;"),
                            Type.getType("()Ljava/lang/Object;")});
        } else {
            // javac adds an extra decoration to the lambda method name with the
            // name of the calling method
            mv.visitInvokeDynamicInsn("call", "()Ljava/util/concurrent/Callable;",
                    new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                    new Object[] {Type.getType("()Ljava/lang/Object;"),
                            new Handle(H_INVOKESTATIC, "org/spongepowered/test/ast/LambdaTest", "lambda$test_producer$2", "()Ljava/lang/Object;"),
                            Type.getType("()Ljava/lang/Object;")});
        }
        mv.visitVarInsn(ASTORE, 1);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Callable", "call", "()Ljava/lang/Object;", true);
        mv.visitInsn(POP);
        mv.visitInsn(RETURN);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", "Lorg/spongepowered/test/ast/LambdaTest;", null, start, end, 0);
        mv.visitLocalVariable("r", "Ljava/util/concurrent/Callable;", "Ljava/util/concurrent/Callable<Ljava/lang/Object;>;", l1, end, 1);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "java.util.concurrent.Callable<Object> r = () -> null;\n"
                + "r.call();";
        Assert.assertEquals(good, insn);
    }

    public void test_method_ref(Runnable r) {
        Runnable a = r::run;
        a.run();
    }

    @Test
    public void testMethodRef() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(Ljava/lang/Runnable;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInvokeDynamicInsn("run", "(Ljava/lang/Runnable;)Ljava/lang/Runnable;",
                new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                new Object[] {Type.getType("()V"), new Handle(H_INVOKEINTERFACE, "java/lang/Runnable", "run", "()V"), Type.getType("()V")});
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true);
        mv.visitInsn(RETURN);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", "Lorg/spongepowered/test/ast/LambdaTest;", null, start, end, 0);
        mv.visitLocalVariable("r", "Ljava/lang/Runnable;", null, start, end, 1);
        mv.visitLocalVariable("a", "Ljava/lang/Runnable;", null, l1, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "Runnable a = r::run;\n"
                + "a.run();";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testMethodRefJavac() {
        // apparently javac chooses it add a call to r.getClass() before the
        // method ref
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(Ljava/lang/Runnable;)V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitInsn(POP);
        mv.visitInvokeDynamicInsn("run", "(Ljava/lang/Runnable;)Ljava/lang/Runnable;",
                new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
                new Object[] {Type.getType("()V"), new Handle(H_INVOKEINTERFACE, "java/lang/Runnable", "run", "()V"), Type.getType("()V")});
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true);
        mv.visitInsn(RETURN);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", "Lorg/spongepowered/test/ast/LambdaTest;", null, start, end, 0);
        mv.visitLocalVariable("r", "Ljava/lang/Runnable;", null, start, end, 1);
        mv.visitLocalVariable("a", "Ljava/lang/Runnable;", null, l1, end, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "r.getClass();\n"
                + "Runnable a = r::run;\n"
                + "a.run();";
        Assert.assertEquals(good, insn);
    }

}

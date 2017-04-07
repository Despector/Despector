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
package org.spongepowered.test.kotlin;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.test.util.KotlinTestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class BasicTests {

    @Test
    public void testSimple() {
        TestMethodBuilder builder = new TestMethodBuilder("sum", "(II)I");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IADD);
        mv.visitLabel(end);
        mv.visitInsn(IRETURN);
        mv.visitLocalVariable("a", "I", null, start, end, 0);
        mv.visitLocalVariable("b", "I", null, start, end, 1);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "sum");
        String good = "fun sum(a: Int, b: Int) = a + b";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testNoVoidReturnDeclared() {
        TestMethodBuilder builder = new TestMethodBuilder("main", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitLdcInsn("Hello");
        mv.visitVarInsn(ASTORE, 1);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", true);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "main");
        String good = "fun main() {\n"
                + "    println(\"Hello\")\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testLocals() {
        TestMethodBuilder builder = new TestMethodBuilder("main", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitLdcInsn("Hello");
        mv.visitVarInsn(ASTORE, 0);
        mv.visitInsn(ICONST_2);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitIincInsn(1, 5);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", true);
        mv.visitLabel(end);
        mv.visitInsn(RETURN);
        mv.visitLocalVariable("s", "Ljava/lang/String;", null, start, end, 0);
        mv.visitLocalVariable("a", "I", null, start, end, 1);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "main");
        String good = "fun main() {\n"
                + "    val s: String = \"Hello\"\n"
                + "    var a: Int = 2\n"
                + "    a += 5\n"
                + "    println(s)\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

}

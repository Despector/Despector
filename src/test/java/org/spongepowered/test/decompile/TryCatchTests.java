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
package org.spongepowered.test.decompile;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.test.util.TestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class TryCatchTests {

    private static final Type THIS_TYPE = Type.getType(TryCatchTests.class);

    @Test
    public void testTryCatch() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NullPointerException");
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NullPointerException", "printStackTrace", "()V", false);
        mv.visitLabel(l4);
        mv.visitInsn(RETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("e", "Ljava/lang/NullPointerException;", null, l2, l5, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    org.spongepowered.test.decompile.TryCatchTests.body();\n"
                + "} catch (NullPointerException e) {\n"
                + "    e.printStackTrace();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTryMultiCatch() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NullPointerException");
        mv.visitTryCatchBlock(l0, l1, l3, "java/lang/OutOfMemoryError");
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NullPointerException", "printStackTrace", "()V", false);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l3);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/OutOfMemoryError", "printStackTrace", "()V", false);
        mv.visitLabel(l4);
        mv.visitInsn(RETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("e", "Ljava/lang/NullPointerException;", null, l2, l3, 2);
        mv.visitLocalVariable("e", "Ljava/lang/OutOfMemoryError;", null, l3, l4, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    org.spongepowered.test.decompile.TryCatchTests.body();\n"
                + "} catch (NullPointerException e) {\n"
                + "    e.printStackTrace();\n"
                + "} catch (OutOfMemoryError e) {\n"
                + "    e.printStackTrace();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTryPipeCatch() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NullPointerException");
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/OutOfMemoryError");
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NullPointerException", "printStackTrace", "()V", false);
        mv.visitLabel(l4);
        mv.visitInsn(RETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("e", "Ljava/lang/NullPointerException;", null, l2, l4, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    org.spongepowered.test.decompile.TryCatchTests.body();\n"
                + "} catch (NullPointerException | OutOfMemoryError e) {\n"
                + "    e.printStackTrace();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTryCatchNested() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
        mv.visitTryCatchBlock(l0, l3, l4, "java/lang/NullPointerException");
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        Label l5 = new Label();
        mv.visitJumpInsn(GOTO, l5);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 3);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
        mv.visitLabel(l3);
        mv.visitJumpInsn(GOTO, l5);
        mv.visitLabel(l4);
        mv.visitVarInsn(ASTORE, 3);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NullPointerException", "printStackTrace", "()V", false);
        mv.visitLabel(l5);
        mv.visitInsn(RETURN);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l6, l3, 3);
        mv.visitLocalVariable("e", "Ljava/lang/NullPointerException;", null, l7, l5, 3);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    try {\n"
                + "        org.spongepowered.test.decompile.TryCatchTests.body();\n"
                + "    } catch (Exception e) {\n"
                + "        e.printStackTrace();\n"
                + "    }\n"
                + "} catch (NullPointerException e) {\n"
                + "    e.printStackTrace();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTryCatchWithControlFlow() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "()V");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NullPointerException");
        mv.visitLabel(l0);
        mv.visitMethodInsn(INVOKESTATIC, THIS_TYPE.getInternalName(), "body", "()V", false);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitJumpInsn(IFNULL, l4);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NullPointerException", "printStackTrace", "()V", false);
        mv.visitLabel(l4);
        mv.visitInsn(RETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("e", "Ljava/lang/NullPointerException;", null, l2, l5, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    org.spongepowered.test.decompile.TryCatchTests.body();\n"
                + "} catch (NullPointerException e) {\n"
                + "    if (e != null) {\n"
                + "        e.printStackTrace();\n"
                + "    }\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testTryCatchReturnedTernary() {
        TestMethodBuilder builder = new TestMethodBuilder("test_mth", "(Z)I");
        MethodVisitor mv = builder.getGenerator();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
        mv.visitLabel(l0);
        mv.visitVarInsn(ILOAD, 1);
        Label l3 = new Label();
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l3);
        mv.visitInsn(ICONST_3);
        mv.visitLabel(l1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("a", "Z", null, l0, l5, 1);
        mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l4, l5, 2);

        String insn = TestHelper.getAsString(builder.finish(), "test_mth");
        String good = "try {\n"
                + "    return a ? 6 : 3;\n"
                + "} catch (Exception e) {\n"
                + "    return 0;\n"
                + "}";
        Assert.assertEquals(good, insn);
    }
}

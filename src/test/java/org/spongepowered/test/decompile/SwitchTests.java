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

import org.spongepowered.test.util.TestHelper;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class SwitchTests {

    private static enum TestEnum {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
    }

    private static final Type TEST_ENUM_TYPE = Type.getType(TestEnum.class);
    private static final Type THIS_TYPE = Type.getType(SwitchTests.class);

    public static void body() {

    }

    @Test
    public void testTableSwitchEclipse() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "SwitchTests_Class", null, "java/lang/Object", null);

        FieldVisitor fv =
                cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I",
                        null, null);
        fv.visitEnd();

        {
            Method m = Method.getMethod("void <init> ()");
            GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
            mg.loadThis();
            mg.invokeConstructor(Type.getType(Object.class), m);
            mg.returnValue();
            mg.endMethod();
        }

        {
            Method m = Method.getMethod("void test_mth (" + TEST_ENUM_TYPE.getClassName() + ")");
            GeneratorAdapter mv = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
            Label start = mv.newLabel();
            mv.visitLabel(start);
            Label l1 = mv.newLabel();
            Label l2 = mv.newLabel();
            Label def = mv.newLabel();
            Label end = mv.newLabel();
            mv.invokeStatic(THIS_TYPE, Method.getMethod("int[] $SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum ()"));
            mv.loadArg(0);
            mv.invokeVirtual(TEST_ENUM_TYPE, Method.getMethod("int ordinal ()"));
            mv.arrayLoad(Type.INT_TYPE);
            mv.visitTableSwitchInsn(1, 2, def, l1, l2);
            mv.visitLabel(l1);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.goTo(end);
            mv.visitLabel(l2);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.goTo(end);
            mv.visitLabel(def);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitLocalVariable("ie", TEST_ENUM_TYPE.getDescriptor(), null, start, end, 0);
            mv.endMethod();
        }
        generateSwitchSyntheticEclipse(cw);

        cw.visitEnd();
        String insn = TestHelper.getAsString(cw.toByteArray(), "test_mth");
        String good = "switch (ie) {\n"
                + "case ONE:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    break;\n"
                + "case TWO:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    break;\n"
                + "default:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testLookupSwitchEclipse() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "SwitchTests_Class", null, "java/lang/Object", null);

        FieldVisitor fv =
                cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I",
                        null, null);
        fv.visitEnd();

        {
            Method m = Method.getMethod("void <init> ()");
            GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
            mg.loadThis();
            mg.invokeConstructor(Type.getType(Object.class), m);
            mg.returnValue();
            mg.endMethod();
        }

        {
            Method m = Method.getMethod("void test_mth (" + TEST_ENUM_TYPE.getClassName() + ")");
            GeneratorAdapter mv = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
            Label start = mv.newLabel();
            mv.visitLabel(start);
            Label l1 = mv.newLabel();
            Label l2 = mv.newLabel();
            Label def = mv.newLabel();
            Label end = mv.newLabel();
            mv.invokeStatic(THIS_TYPE, Method.getMethod("int[] $SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum ()"));
            mv.loadArg(0);
            mv.invokeVirtual(TEST_ENUM_TYPE, Method.getMethod("int ordinal ()"));
            mv.arrayLoad(Type.INT_TYPE);
            mv.visitLookupSwitchInsn(def, new int[] {1, 8}, new Label[] {l1, l2});
            mv.visitLabel(l1);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.goTo(end);
            mv.visitLabel(l2);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.goTo(end);
            mv.visitLabel(def);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitLocalVariable("ie", TEST_ENUM_TYPE.getDescriptor(), null, start, end, 0);
            mv.endMethod();
        }
        generateSwitchSyntheticEclipse(cw);

        cw.visitEnd();
        String insn = TestHelper.getAsString(cw.toByteArray(), "test_mth");
        String good = "switch (ie) {\n"
                + "case ONE:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    break;\n"
                + "case EIGHT:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    break;\n"
                + "default:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "}";
        Assert.assertEquals(good, insn);
    }

    @Test
    public void testReturnSwitchEclipse() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "SwitchTests_Class", null, "java/lang/Object", null);

        FieldVisitor fv =
                cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I",
                        null, null);
        fv.visitEnd();

        {
            Method m = Method.getMethod("void <init> ()");
            GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
            mg.loadThis();
            mg.invokeConstructor(Type.getType(Object.class), m);
            mg.returnValue();
            mg.endMethod();
        }

        {
            Method m = Method.getMethod("void test_mth (" + TEST_ENUM_TYPE.getClassName() + ")");
            GeneratorAdapter mv = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
            Label start = mv.newLabel();
            mv.visitLabel(start);
            Label l1 = mv.newLabel();
            Label l2 = mv.newLabel();
            Label def = mv.newLabel();
            Label end = mv.newLabel();
            mv.invokeStatic(THIS_TYPE, Method.getMethod("int[] $SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum ()"));
            mv.loadArg(0);
            mv.invokeVirtual(TEST_ENUM_TYPE, Method.getMethod("int ordinal ()"));
            mv.arrayLoad(Type.INT_TYPE);
            mv.visitLookupSwitchInsn(def, new int[] {1, 8}, new Label[] {l1, l2});
            mv.visitLabel(l1);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.visitInsn(RETURN);
            mv.visitLabel(l2);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.visitInsn(RETURN);
            mv.visitLabel(def);
            mv.invokeStatic(THIS_TYPE, Method.getMethod("void body ()"));
            mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitLocalVariable("ie", TEST_ENUM_TYPE.getDescriptor(), null, start, end, 0);
            mv.endMethod();
        }
        generateSwitchSyntheticEclipse(cw);

        cw.visitEnd();
        String insn = TestHelper.getAsString(cw.toByteArray(), "test_mth");
        String good = "switch (ie) {\n"
                + "case ONE:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    return;\n"
                + "case EIGHT:\n"
                + "    org.spongepowered.test.decompile.SwitchTests.body();\n"
                + "    return;\n"
                + "}\n\n"
                + "org.spongepowered.test.decompile.SwitchTests.body();";
        Assert.assertEquals(good, insn);
    }

    private static void generateSwitchSyntheticEclipse(ClassWriter cw) {

        MethodVisitor mv =
                cw.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "()[I", null,
                        null);
        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NoSuchFieldError");
        Label l3 = new Label();
        Label l4 = new Label();
        Label l5 = new Label();
        mv.visitTryCatchBlock(l3, l4, l5, "java/lang/NoSuchFieldError");
        Label l6 = new Label();
        Label l7 = new Label();
        Label l8 = new Label();
        mv.visitTryCatchBlock(l6, l7, l8, "java/lang/NoSuchFieldError");
        Label l9 = new Label();
        Label l10 = new Label();
        Label l11 = new Label();
        mv.visitTryCatchBlock(l9, l10, l11, "java/lang/NoSuchFieldError");
        Label l12 = new Label();
        Label l13 = new Label();
        Label l14 = new Label();
        mv.visitTryCatchBlock(l12, l13, l14, "java/lang/NoSuchFieldError");
        Label l15 = new Label();
        Label l16 = new Label();
        Label l17 = new Label();
        mv.visitTryCatchBlock(l15, l16, l17, "java/lang/NoSuchFieldError");
        Label l18 = new Label();
        Label l19 = new Label();
        Label l20 = new Label();
        mv.visitTryCatchBlock(l18, l19, l20, "java/lang/NoSuchFieldError");
        Label l21 = new Label();
        Label l22 = new Label();
        Label l23 = new Label();
        mv.visitTryCatchBlock(l21, l22, l23, "java/lang/NoSuchFieldError");
        Label l24 = new Label();
        Label l25 = new Label();
        Label l26 = new Label();
        mv.visitTryCatchBlock(l24, l25, l26, "java/lang/NoSuchFieldError");
        Label l27 = new Label();
        Label l28 = new Label();
        Label l29 = new Label();
        mv.visitTryCatchBlock(l27, l28, l29, "java/lang/NoSuchFieldError");
        Label l30 = new Label();
        mv.visitLabel(l30);
        mv.visitLineNumber(28, l30);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests",
                "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I");
        Label l31 = new Label();
        mv.visitJumpInsn(IFNULL, l31);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests",
                "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I");
        mv.visitInsn(ARETURN);
        mv.visitLabel(l31);
        mv.visitMethodInsn(INVOKESTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "values",
                "()[Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;", false);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitVarInsn(ASTORE, 0);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "EIGHT",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitIntInsn(BIPUSH, 8);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitInsn(POP);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "FIVE",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitInsn(ICONST_5);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l4);
        mv.visitJumpInsn(GOTO, l6);
        mv.visitLabel(l5);
        mv.visitInsn(POP);
        mv.visitLabel(l6);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "FOUR",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitInsn(ICONST_4);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l7);
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l8);
        mv.visitInsn(POP);
        mv.visitLabel(l9);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "NINE",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitIntInsn(BIPUSH, 9);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l10);
        mv.visitJumpInsn(GOTO, l12);
        mv.visitLabel(l11);
        mv.visitInsn(POP);
        mv.visitLabel(l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ONE",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l13);
        mv.visitJumpInsn(GOTO, l15);
        mv.visitLabel(l14);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/NoSuchFieldError"});
        mv.visitInsn(POP);
        mv.visitLabel(l15);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "SEVEN",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitIntInsn(BIPUSH, 7);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l16);
        mv.visitJumpInsn(GOTO, l18);
        mv.visitLabel(l17);
        mv.visitInsn(POP);
        mv.visitLabel(l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "SIX",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitIntInsn(BIPUSH, 6);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l19);
        mv.visitJumpInsn(GOTO, l21);
        mv.visitLabel(l20);
        mv.visitInsn(POP);
        mv.visitLabel(l21);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "TEN",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l22);
        mv.visitJumpInsn(GOTO, l24);
        mv.visitLabel(l23);
        mv.visitInsn(POP);
        mv.visitLabel(l24);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "THREE",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitInsn(ICONST_3);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l25);
        mv.visitJumpInsn(GOTO, l27);
        mv.visitLabel(l26);
        mv.visitInsn(POP);
        mv.visitLabel(l27);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "TWO",
                "Lorg/spongepowered/test/decompile/SwitchTests$TestEnum;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/test/decompile/SwitchTests$TestEnum", "ordinal", "()I", false);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(IASTORE);
        mv.visitLabel(l28);
        Label l32 = new Label();
        mv.visitJumpInsn(GOTO, l32);
        mv.visitLabel(l29);
        mv.visitInsn(POP);
        mv.visitLabel(l32);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(PUTSTATIC, "org/spongepowered/test/decompile/SwitchTests",
                "$SWITCH_TABLE$org$spongepowered$test$decompile$SwitchTests$TestEnum", "[I");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

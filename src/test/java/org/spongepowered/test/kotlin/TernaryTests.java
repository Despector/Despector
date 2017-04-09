/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.test.kotlin;

import static org.objectweb.asm.Opcodes.*;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.test.util.KotlinTestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class TernaryTests {

    @Test
    public void testSimpleTernary() {
        TestMethodBuilder builder = new TestMethodBuilder("maxOf", "(II)I");
        MethodVisitor mv = builder.getGenerator();
        Label start = new Label();
        Label l1 = new Label();
        mv.visitLabel(start);
        Label end = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IF_ICMPLE, l1);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitLabel(end);
        mv.visitInsn(IRETURN);
        mv.visitLocalVariable("a", "I", null, start, end, 0);
        mv.visitLocalVariable("b", "I", null, start, end, 1);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "maxOf");
        String good = "fun maxOf(a: Int, b: Int) = if (a > b) a else b";
        Assert.assertEquals(good, insn);
    }
}

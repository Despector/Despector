/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.test.kotlin;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.test.util.KotlinTestHelper;
import org.spongepowered.test.util.TestMethodBuilder;

public class IfTests {

    @Test
    public void testSimpleIf() {
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
        mv.visitInsn(IRETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitLabel(end);
        mv.visitInsn(IRETURN);
        mv.visitLocalVariable("a", "I", null, start, end, 0);
        mv.visitLocalVariable("b", "I", null, start, end, 1);

        String insn = KotlinTestHelper.getMethodAsString(builder.finish(), "maxOf");
        String good = "fun maxOf(a: Int, b: Int): Int {\n"
                + "    if (a > b) {\n"
                + "        return a\n"
                + "    }\n\n"
                + "    return b\n"
                + "}";
        Assert.assertEquals(good, insn);
    }
}

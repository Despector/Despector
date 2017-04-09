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

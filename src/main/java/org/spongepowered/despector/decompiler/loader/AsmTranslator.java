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
package org.spongepowered.despector.decompiler.loader;

import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.spongepowered.despector.decompiler.ir.FieldInsn;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.IntInsn;
import org.spongepowered.despector.decompiler.ir.OpInsn;
import org.spongepowered.despector.decompiler.ir.TypeInsn;
import org.spongepowered.despector.decompiler.ir.VarIntInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;

import java.util.Map;

public class AsmTranslator {

    private static final int[] op_translation = new int[256];

    static {
        op_translation[Opcodes.NOP] = Insn.NOOP;
        op_translation[Opcodes.ACONST_NULL] = Insn.PUSH;
        op_translation[Opcodes.ICONST_M1] = Insn.ICONST;
        op_translation[Opcodes.ICONST_0] = Insn.ICONST;
        op_translation[Opcodes.ICONST_1] = Insn.ICONST;
        op_translation[Opcodes.ICONST_2] = Insn.ICONST;
        op_translation[Opcodes.ICONST_3] = Insn.ICONST;
        op_translation[Opcodes.ICONST_4] = Insn.ICONST;
        op_translation[Opcodes.ICONST_5] = Insn.ICONST;
        op_translation[Opcodes.LCONST_0] = Insn.LCONST;
        op_translation[Opcodes.LCONST_1] = Insn.LCONST;
        op_translation[Opcodes.FCONST_0] = Insn.FCONST;
        op_translation[Opcodes.FCONST_1] = Insn.FCONST;
        op_translation[Opcodes.FCONST_2] = Insn.FCONST;
        op_translation[Opcodes.DCONST_0] = Insn.DCONST;
        op_translation[Opcodes.DCONST_1] = Insn.DCONST;
        op_translation[Opcodes.BIPUSH] = Insn.ICONST;
        op_translation[Opcodes.SIPUSH] = Insn.ICONST;
        op_translation[Opcodes.LDC] = Insn.PUSH;
        op_translation[Opcodes.ILOAD] = Insn.LOCAL_LOAD;
        op_translation[Opcodes.LLOAD] = Insn.LOCAL_LOAD;
        op_translation[Opcodes.FLOAD] = Insn.LOCAL_LOAD;
        op_translation[Opcodes.DLOAD] = Insn.LOCAL_LOAD;
        op_translation[Opcodes.ALOAD] = Insn.LOCAL_LOAD;
        op_translation[Opcodes.IALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.LALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.FALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.DALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.AALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.BALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.CALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.SALOAD] = Insn.ARRAY_LOAD;
        op_translation[Opcodes.ISTORE] = Insn.LOCAL_STORE;
        op_translation[Opcodes.LSTORE] = Insn.LOCAL_STORE;
        op_translation[Opcodes.FSTORE] = Insn.LOCAL_STORE;
        op_translation[Opcodes.DSTORE] = Insn.LOCAL_STORE;
        op_translation[Opcodes.ASTORE] = Insn.LOCAL_STORE;
        op_translation[Opcodes.IASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.LASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.FASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.DASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.AASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.BASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.CASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.SASTORE] = Insn.ARRAY_STORE;
        op_translation[Opcodes.POP] = Insn.POP;
        op_translation[Opcodes.DUP] = Insn.DUP;
        op_translation[Opcodes.DUP_X1] = Insn.DUP_X1;
        op_translation[Opcodes.DUP_X2] = Insn.DUP_X2;
        op_translation[Opcodes.DUP2] = Insn.DUP2;
        op_translation[Opcodes.DUP2_X1] = Insn.DUP2_X1;
        op_translation[Opcodes.DUP2_X2] = Insn.DUP2_X2;
        op_translation[Opcodes.SWAP] = Insn.SWAP;
        op_translation[Opcodes.IADD] = Insn.ADD;
        op_translation[Opcodes.LADD] = Insn.ADD;
        op_translation[Opcodes.FADD] = Insn.ADD;
        op_translation[Opcodes.DADD] = Insn.ADD;
        op_translation[Opcodes.ISUB] = Insn.SUB;
        op_translation[Opcodes.LSUB] = Insn.SUB;
        op_translation[Opcodes.FSUB] = Insn.SUB;
        op_translation[Opcodes.DSUB] = Insn.SUB;
        op_translation[Opcodes.IMUL] = Insn.MUL;
        op_translation[Opcodes.LMUL] = Insn.MUL;
        op_translation[Opcodes.FMUL] = Insn.MUL;
        op_translation[Opcodes.DMUL] = Insn.MUL;
        op_translation[Opcodes.IDIV] = Insn.DIV;
        op_translation[Opcodes.LDIV] = Insn.DIV;
        op_translation[Opcodes.FDIV] = Insn.DIV;
        op_translation[Opcodes.DDIV] = Insn.DIV;
        op_translation[Opcodes.IREM] = Insn.REM;
        op_translation[Opcodes.LREM] = Insn.REM;
        op_translation[Opcodes.FREM] = Insn.REM;
        op_translation[Opcodes.DREM] = Insn.REM;
        op_translation[Opcodes.INEG] = Insn.NEG;
        op_translation[Opcodes.LNEG] = Insn.NEG;
        op_translation[Opcodes.FNEG] = Insn.NEG;
        op_translation[Opcodes.DNEG] = Insn.NEG;
        op_translation[Opcodes.ISHL] = Insn.SHL;
        op_translation[Opcodes.LSHL] = Insn.SHL;
        op_translation[Opcodes.ISHR] = Insn.SHR;
        op_translation[Opcodes.LSHR] = Insn.SHR;
        op_translation[Opcodes.IUSHR] = Insn.USHR;
        op_translation[Opcodes.LUSHR] = Insn.USHR;
        op_translation[Opcodes.IAND] = Insn.AND;
        op_translation[Opcodes.LAND] = Insn.AND;
        op_translation[Opcodes.IOR] = Insn.OR;
        op_translation[Opcodes.LOR] = Insn.OR;
        op_translation[Opcodes.IXOR] = Insn.XOR;
        op_translation[Opcodes.LXOR] = Insn.XOR;
        op_translation[Opcodes.IINC] = Insn.IINC;
        op_translation[Opcodes.IFEQ] = Insn.IFEQ;
        op_translation[Opcodes.IFNE] = Insn.IFNE;
        op_translation[Opcodes.IFLT] = Insn.IF_CMPLT;
        op_translation[Opcodes.IFGT] = Insn.IF_CMPGT;
        op_translation[Opcodes.IFLE] = Insn.IF_CMPLE;
        op_translation[Opcodes.IFGE] = Insn.IF_CMPGE;
        op_translation[Opcodes.IF_ICMPEQ] = Insn.IF_CMPEQ;
        op_translation[Opcodes.IF_ICMPNE] = Insn.IF_CMPNE;
        op_translation[Opcodes.IF_ICMPLT] = Insn.IF_CMPLT;
        op_translation[Opcodes.IF_ICMPLE] = Insn.IF_CMPLE;
        op_translation[Opcodes.IF_ICMPGT] = Insn.IF_CMPGT;
        op_translation[Opcodes.IF_ICMPGE] = Insn.IF_CMPGE;
        op_translation[Opcodes.IF_ACMPEQ] = Insn.IF_CMPEQ;
        op_translation[Opcodes.IF_ACMPNE] = Insn.IF_CMPNE;
        op_translation[Opcodes.GOTO] = Insn.GOTO;
        op_translation[Opcodes.IRETURN] = Insn.ARETURN;
        op_translation[Opcodes.LRETURN] = Insn.ARETURN;
        op_translation[Opcodes.FRETURN] = Insn.ARETURN;
        op_translation[Opcodes.DRETURN] = Insn.ARETURN;
        op_translation[Opcodes.ARETURN] = Insn.ARETURN;
        op_translation[Opcodes.RETURN] = Insn.RETURN;
        op_translation[Opcodes.GETSTATIC] = Insn.GETSTATIC;
        op_translation[Opcodes.PUTSTATIC] = Insn.PUTSTATIC;
        op_translation[Opcodes.GETFIELD] = Insn.GETFIELD;
        op_translation[Opcodes.PUTFIELD] = Insn.PUTFIELD;
        op_translation[Opcodes.INVOKEVIRTUAL] = Insn.INVOKE;
        op_translation[Opcodes.INVOKESPECIAL] = Insn.INVOKE;
        op_translation[Opcodes.INVOKESTATIC] = Insn.INVOKESTATIC;
        op_translation[Opcodes.INVOKEINTERFACE] = Insn.INVOKE;
        op_translation[Opcodes.NEW] = Insn.NEW;
        op_translation[Opcodes.NEW] = Insn.NEWARRAY;
        op_translation[Opcodes.ANEWARRAY] = Insn.NEWARRAY;
        op_translation[Opcodes.ATHROW] = Insn.THROW;
        op_translation[Opcodes.CHECKCAST] = Insn.CAST;
    }

    private static int translate(int op) {
        return op_translation[op];
    }

    public static InsnBlock convert(PartialMethod partial, InsnList asm_ops) {
        Map<Label, Integer> label_indices = Maps.newHashMap();
        InsnBlock block = new InsnBlock();
        for (int i = 0; i < asm_ops.size(); i++) {
            AbstractInsnNode next = asm_ops.get(i);
            if (next instanceof LabelNode) {
                label_indices.put(((LabelNode) next).getLabel(), i);
            } else if (next instanceof FrameNode || next instanceof LineNumberNode) {
            } else if (next instanceof FieldInsnNode) {
                FieldInsnNode node = (FieldInsnNode) next;
                block.append(new FieldInsn(translate(node.getOpcode()), node.owner, node.name, node.desc));
            } else if (next instanceof IincInsnNode) {
                IincInsnNode iinc = (IincInsnNode) next;
                block.append(new VarIntInsn(Insn.IINC, iinc.var, iinc.incr));
            } else if (next instanceof InsnNode) {
                InsnNode insn = (InsnNode) next;
                if (insn.getOpcode() == Opcodes.L2I || insn.getOpcode() == Opcodes.F2I || insn.getOpcode() == Opcodes.D2I) {
                    block.append(new TypeInsn(Insn.CAST, "I"));
                } else if (insn.getOpcode() == Opcodes.I2L || insn.getOpcode() == Opcodes.F2L || insn.getOpcode() == Opcodes.D2L) {
                    block.append(new TypeInsn(Insn.CAST, "L"));
                } else if (insn.getOpcode() == Opcodes.I2F || insn.getOpcode() == Opcodes.L2F || insn.getOpcode() == Opcodes.D2F) {
                    block.append(new TypeInsn(Insn.CAST, "F"));
                } else if (insn.getOpcode() == Opcodes.I2D || insn.getOpcode() == Opcodes.L2D || insn.getOpcode() == Opcodes.F2D) {
                    block.append(new TypeInsn(Insn.CAST, "D"));
                } else if (insn.getOpcode() == Opcodes.I2B) {
                    block.append(new TypeInsn(Insn.CAST, "B"));
                } else if (insn.getOpcode() == Opcodes.I2C) {
                    block.append(new TypeInsn(Insn.CAST, "C"));
                } else if (insn.getOpcode() == Opcodes.I2S) {
                    block.append(new TypeInsn(Insn.CAST, "S"));
                } else {
                    block.append(new OpInsn(translate(insn.getOpcode())));
                }
            } else if (next instanceof IntInsnNode) {
                IntInsnNode insn = (IntInsnNode) next;
                if (insn.getOpcode() == Opcodes.BIPUSH || insn.getOpcode() == Opcodes.SIPUSH) {
                    block.append(new IntInsn(Insn.ICONST, insn.operand));
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
        }

        partial.getLocals().bakeInstances(label_indices);
        return block;
    }

}

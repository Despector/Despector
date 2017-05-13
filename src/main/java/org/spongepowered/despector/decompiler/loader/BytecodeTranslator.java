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

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.decompiler.BaseDecompiler.BootstrapMethod;
import org.spongepowered.despector.decompiler.error.SourceFormatException;
import org.spongepowered.despector.decompiler.ir.DoubleInsn;
import org.spongepowered.despector.decompiler.ir.FieldInsn;
import org.spongepowered.despector.decompiler.ir.FloatInsn;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.IntInsn;
import org.spongepowered.despector.decompiler.ir.InvokeDynamicInsn;
import org.spongepowered.despector.decompiler.ir.InvokeInsn;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.ir.LdcInsn;
import org.spongepowered.despector.decompiler.ir.LongInsn;
import org.spongepowered.despector.decompiler.ir.OpInsn;
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
import org.spongepowered.despector.decompiler.ir.TypeInsn;
import org.spongepowered.despector.decompiler.ir.TypeIntInsn;
import org.spongepowered.despector.decompiler.ir.VarIntInsn;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.ClassEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.DoubleEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.Entry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.FieldRefEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.FloatEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.IntEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.InvokeDynamicEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.LongEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.MethodHandleEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.MethodRefEntry;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool.StringEntry;
import org.spongepowered.despector.decompiler.method.PartialMethod.TryCatchRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeTranslator {

    public BytecodeTranslator() {

    }

    public InsnBlock createIR(byte[] code, Locals locals, List<TryCatchRegion> catch_regions, ClassConstantPool pool,
            List<BootstrapMethod> bootstrap_methods) {
        InsnBlock block = new InsnBlock();
        List<Integer> insn_starts = new ArrayList<>();

        for (int i = 0; i < code.length;) {
            int opcode_index = i;
            insn_starts.add(opcode_index);
            int next = code[i++] & 0xFF;
            switch (next) {
            case 0: // NOP
                block.append(new OpInsn(Insn.NOOP));
                break;
            case 1: // ACONST_NULL
                block.append(new LdcInsn(Insn.PUSH, null));
                break;
            case 2: // ICONST_M1
                block.append(new IntInsn(Insn.ICONST, -1));
                break;
            case 3: // ICONST_0
                block.append(new IntInsn(Insn.ICONST, 0));
                break;
            case 4: // ICONST_1
                block.append(new IntInsn(Insn.ICONST, 1));
                break;
            case 5: // ICONST_2
                block.append(new IntInsn(Insn.ICONST, 2));
                break;
            case 6: // ICONST_3
                block.append(new IntInsn(Insn.ICONST, 3));
                break;
            case 7: // ICONST_4
                block.append(new IntInsn(Insn.ICONST, 4));
                break;
            case 8: // ICONST_5
                block.append(new IntInsn(Insn.ICONST, 5));
                break;
            case 9: // LCONST_0
                block.append(new LongInsn(Insn.LCONST, 0));
                break;
            case 10: // LCONST_1
                block.append(new LongInsn(Insn.LCONST, 1));
                break;
            case 11: // FCONST_0
                block.append(new FloatInsn(Insn.FCONST, 0));
                break;
            case 12: // FCONST_1
                block.append(new FloatInsn(Insn.FCONST, 1));
                break;
            case 13: // FCONST_2
                block.append(new FloatInsn(Insn.FCONST, 2));
                break;
            case 14: // DCONST_0
                block.append(new DoubleInsn(Insn.DCONST, 0));
                break;
            case 15: // DCONST_1
                block.append(new DoubleInsn(Insn.DCONST, 1));
                break;
            case 16: {// BIPUSH
                int val = code[i++];
                block.append(new IntInsn(Insn.ICONST, val));
                break;
            }
            case 17: {// SIPUSH
                short val = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new IntInsn(Insn.ICONST, val));
                break;
            }
            case 18: {// LDC
                int index = code[i++] & 0xFF;
                Entry entry = pool.getEntry(index);
                if (entry instanceof IntEntry) {
                    block.append(new IntInsn(Insn.ICONST, ((IntEntry) entry).value));
                } else if (entry instanceof FloatEntry) {
                    block.append(new FloatInsn(Insn.FCONST, ((FloatEntry) entry).value));
                } else if (entry instanceof StringEntry) {
                    block.append(new LdcInsn(Insn.PUSH, ((StringEntry) entry).value));
                } else if (entry instanceof ClassEntry) {
                    String type = ((ClassEntry) entry).name;
                    if (!type.startsWith("[")) {
                        type = "L" + type + ";";
                    }
                    block.append(new LdcInsn(Insn.PUSH, ClassTypeSignature.of(type)));
                } else {
                    throw new IllegalStateException("Unsupported constant pool entry type in LDC node " + entry.getClass().getSimpleName());
                }
                break;
            }
            case 19: {// LDC_W
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                Entry entry = pool.getEntry(index);
                if (entry instanceof IntEntry) {
                    block.append(new IntInsn(Insn.ICONST, ((IntEntry) entry).value));
                } else if (entry instanceof FloatEntry) {
                    block.append(new FloatInsn(Insn.FCONST, ((FloatEntry) entry).value));
                } else if (entry instanceof StringEntry) {
                    block.append(new LdcInsn(Insn.PUSH, ((StringEntry) entry).value));
                } else if (entry instanceof ClassEntry) {
                    block.append(new LdcInsn(Insn.PUSH, ClassTypeSignature.of("L" + ((ClassEntry) entry).name + ";")));
                } else {
                    throw new IllegalStateException("Unsupported constant pool entry type in LDC node " + entry.getClass().getSimpleName());
                }
                break;
            }
            case 20: {// LDC2_W
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                Entry entry = pool.getEntry(index);
                if (entry instanceof LongEntry) {
                    block.append(new LongInsn(Insn.LCONST, ((LongEntry) entry).value));
                } else if (entry instanceof DoubleEntry) {
                    block.append(new DoubleInsn(Insn.DCONST, ((DoubleEntry) entry).value));
                } else {
                    throw new IllegalStateException("Unsupported constant pool entry type in LDC node " + entry.getClass().getSimpleName());
                }
                break;
            }
            case 21: // ILOAD
            case 22: // LLOAD
            case 23: // FLOAD
            case 24: // DLOAD
            case 25: { // ALOAD
                int val = code[i++] & 0xFF;
                block.append(new IntInsn(Insn.LOCAL_LOAD, val));
                break;
            }
            case 26: // ILOAD_0
            case 30: // LLOAD_0
            case 34: // FLOAD_0
            case 38: // DLOAD_0
            case 42: // ALOAD_0
                block.append(new IntInsn(Insn.LOCAL_LOAD, 0));
                break;
            case 27: // ILOAD_1
            case 31: // LLOAD_1
            case 35: // FLOAD_1
            case 39: // DLOAD_1
            case 43: // ALOAD_1
                block.append(new IntInsn(Insn.LOCAL_LOAD, 1));
                break;
            case 28: // ILOAD_2
            case 32: // LLOAD_2
            case 36: // FLOAD_2
            case 40: // DLOAD_2
            case 44: // ALOAD_2
                block.append(new IntInsn(Insn.LOCAL_LOAD, 2));
                break;
            case 29: // ILOAD_3
            case 33: // LLOAD_3
            case 37: // FLOAD_3
            case 41: // DLOAD_3
            case 45: // ALOAD_3
                block.append(new IntInsn(Insn.LOCAL_LOAD, 3));
                break;
            case 46: // IALOAD
            case 47: // LALOAD
            case 48: // FALOAD
            case 49: // DALOAD
            case 50: // AALOAD
            case 51: // BALOAD
            case 52: // CALOAD
            case 53: // SALOAD
                block.append(new OpInsn(Insn.ARRAY_LOAD));
                break;
            case 54: { // ISTORE
                int local = code[i++] & 0xFF;
                block.append(new IntInsn(Insn.LOCAL_STORE, local));
                break;
            }
            case 55: // LSTORE
            case 56: // FSTORE
            case 57: // DSTORE
            case 58: { // ASTORE
                int val = code[i++] & 0xFF;
                block.append(new IntInsn(Insn.LOCAL_STORE, val));
                break;
            }
            case 59: // ISTORE_0
            case 63: // LSTORE_0
            case 67: // FSTORE_0
            case 71: // DSTORE_0
            case 75: // ASTORE_0
                block.append(new IntInsn(Insn.LOCAL_STORE, 0));
                break;
            case 60: // ISTORE_1
            case 64: // LSTORE_1
            case 68: // FSTORE_1
            case 72: // DSTORE_1
            case 76: // ASTORE_1
                block.append(new IntInsn(Insn.LOCAL_STORE, 1));
                break;
            case 61: // ISTORE_2
            case 65: // LSTORE_2
            case 69: // FSTORE_2
            case 73: // DSTORE_2
            case 77: // ASTORE_2
                block.append(new IntInsn(Insn.LOCAL_STORE, 2));
                break;
            case 62: // ISTORE_3
            case 66: // LSTORE_3
            case 70: // FSTORE_3
            case 74: // DSTORE_3
            case 78: // ASTORE_3
                block.append(new IntInsn(Insn.LOCAL_STORE, 3));
                break;
            case 79: // IASTORE
            case 80: // LASTORE
            case 81: // FASTORE
            case 82: // DASTORE
            case 83: // AASTORE
            case 84: // BASTORE
            case 85: // CASTORE
            case 86: // SASTORE
                block.append(new OpInsn(Insn.ARRAY_STORE));
                break;
            case 87: // POP
                block.append(new OpInsn(Insn.POP));
                break;
            case 88: // POP2
                block.append(new OpInsn(Insn.POP));
                insn_starts.add(opcode_index);
                block.append(new OpInsn(Insn.POP));
                break;
            case 89: // DUP
                block.append(new OpInsn(Insn.DUP));
                break;
            case 90: // DUP_X1
                block.append(new OpInsn(Insn.DUP_X1));
                break;
            case 91: // DUP_X2
                block.append(new OpInsn(Insn.DUP_X2));
                break;
            case 92: // DUP2
                block.append(new OpInsn(Insn.DUP2));
                break;
            case 93: // DUP2_X1
                block.append(new OpInsn(Insn.DUP2_X1));
                break;
            case 94: // DUP2_X2
                block.append(new OpInsn(Insn.DUP2_X2));
                break;
            case 95: // SWAP
                block.append(new OpInsn(Insn.SWAP));
                break;
            case 96: // IADD
            case 97: // LADD
            case 98: // FADD
            case 99: // DADD
                block.append(new OpInsn(Insn.ADD));
                break;
            case 100: // ISUB
            case 101: // LSUB
            case 102: // FSUB
            case 103: // DSUB
                block.append(new OpInsn(Insn.SUB));
                break;
            case 104: // IMUL
            case 105: // LMUL
            case 106: // FMUL
            case 107: // DMUL
                block.append(new OpInsn(Insn.MUL));
                break;
            case 108: // IDIV
            case 109: // LDIV
            case 110: // FDIV
            case 111: // DDIV
                block.append(new OpInsn(Insn.DIV));
                break;
            case 112: // IREM
            case 113: // LREM
            case 114: // FREM
            case 115: // DREM
                block.append(new OpInsn(Insn.REM));
                break;
            case 116: // INEG
            case 117: // LNEG
            case 118: // FNEG
            case 119: // DNEG
                block.append(new OpInsn(Insn.NEG));
                break;
            case 120: // ISHL
            case 121: // LSHL
                block.append(new OpInsn(Insn.SHL));
                break;
            case 122: // ISHR
            case 123: // LSHR
                block.append(new OpInsn(Insn.SHR));
                break;
            case 124: // IUSHR
            case 125: // LUSHR
                block.append(new OpInsn(Insn.USHR));
                break;
            case 126: // IAND
            case 127: // LAND
                block.append(new OpInsn(Insn.AND));
                break;
            case 128: // IOR
            case 129: // LOR
                block.append(new OpInsn(Insn.OR));
                break;
            case 130: // IXOR
            case 131: // LXOR
                block.append(new OpInsn(Insn.XOR));
                break;
            case 132: {// IINC
                int local = code[i++] & 0xFF;
                int incr = code[i++];
                block.append(new VarIntInsn(Insn.IINC, local, incr));
                break;
            }
            case 136: // L2I
            case 139: // F2I
            case 142: // D2I
                block.append(new TypeInsn(Insn.CAST, "I"));
                break;
            case 133: // I2L
            case 140: // F2L
            case 143: // D2L
                block.append(new TypeInsn(Insn.CAST, "J"));
                break;
            case 134: // I2F
            case 137: // L2F
            case 144: // D2F
                block.append(new TypeInsn(Insn.CAST, "F"));
                break;
            case 135: // I2D
            case 138: // L2D
            case 141: // F2D
                block.append(new TypeInsn(Insn.CAST, "D"));
                break;
            case 145: // I2B
                block.append(new TypeInsn(Insn.CAST, "B"));
                break;
            case 146: // I2C
                block.append(new TypeInsn(Insn.CAST, "C"));
                break;
            case 147: // I2S
                block.append(new TypeInsn(Insn.CAST, "S"));
                break;
            case 148: // LCMP
            case 149: // FCMPL
            case 150: // FCMPG
            case 151: // DCMPL
            case 152: // DCMPG
                block.append(new OpInsn(Insn.CMP));
                break;
            case 153: {// IFEQ
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IFEQ, opcode_index + index));
                break;
            }
            case 154: {// IFNE
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IFNE, opcode_index + index));
                break;
            }
            case 155: {// IFLT
                block.append(new IntInsn(Insn.ICONST, 0));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPLT, opcode_index + index));
                break;
            }
            case 156: {// IFGE
                block.append(new IntInsn(Insn.ICONST, 0));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPGE, opcode_index + index));
                break;
            }
            case 157: {// IFGT
                block.append(new IntInsn(Insn.ICONST, 0));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPGT, opcode_index + index));
                break;
            }
            case 158: {// IFLE
                block.append(new IntInsn(Insn.ICONST, 0));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPLE, opcode_index + index));
                break;
            }
            case 159: {// IF_ICMPEQ
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPEQ, opcode_index + index));
                break;
            }
            case 160: {// IF_ICMPNE
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPNE, opcode_index + index));
                break;
            }
            case 161: {// IF_ICMPLT
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPLT, opcode_index + index));
                break;
            }
            case 162: {// IF_ICMPGE
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPGE, opcode_index + index));
                break;
            }
            case 163: {// IF_ICMPGT
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPGT, opcode_index + index));
                break;
            }
            case 164: {// IF_ICMPLE
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPLE, opcode_index + index));
                break;
            }
            case 165: {// IF_ACMPEQ
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPEQ, opcode_index + index));
                break;
            }
            case 166: {// IF_ACMPNE
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPNE, opcode_index + index));
                break;
            }
            case 167: {// GOTO
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.GOTO, opcode_index + index));
                break;
            }
            case 168: // JSR
            case 169: // RET
                throw new SourceFormatException("Unsupported java opcode: " + next);
            case 170: {// TABLESWITCH
                while (i % 4 != 0) {
                    i++;
                }
                int def = opcode_index + readInt(code, i);
                i += 4;
                int low = readInt(code, i);
                i += 4;
                int high = readInt(code, i);
                i += 4;
                Map<Integer, Integer> targets = new HashMap<>();
                for (int j = 0; j < high - low + 1; j++) {
                    targets.put(low + j, opcode_index + readInt(code, i));
                    i += 4;
                }
                block.append(new SwitchInsn(Insn.SWITCH, targets, def));
                break;
            }
            case 171: {// LOOKUPSWITCH
                while (i % 4 != 0) {
                    i++;
                }
                int def = opcode_index + readInt(code, i);
                i += 4;
                int npairs = readInt(code, i);
                i += 4;
                Map<Integer, Integer> targets = new HashMap<>();
                for (int j = 0; j < npairs; j++) {
                    int key = readInt(code, i);
                    i += 4;
                    targets.put(key, opcode_index + readInt(code, i));
                    i += 4;
                }
                block.append(new SwitchInsn(Insn.SWITCH, targets, def));
                break;
            }
            case 172: // IRETURN
            case 173: // LRETURN
            case 174: // FRETURN
            case 175: // DRETURN
            case 176: // ARETURN
                block.append(new OpInsn(Insn.ARETURN));
                break;
            case 177: // RETURN
                block.append(new OpInsn(Insn.RETURN));
                break;
            case 178: { // GETSTATIC
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                FieldRefEntry ref = pool.getFieldRef(index);
                block.append(new FieldInsn(Insn.GETSTATIC, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 179: { // PUTSTATIC
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                FieldRefEntry ref = pool.getFieldRef(index);
                block.append(new FieldInsn(Insn.PUTSTATIC, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 180: { // GETFIELD
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                FieldRefEntry ref = pool.getFieldRef(index);
                block.append(new FieldInsn(Insn.GETFIELD, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 181: { // PUTFIELD
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                FieldRefEntry ref = pool.getFieldRef(index);
                block.append(new FieldInsn(Insn.PUTFIELD, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 182: // INVOKEVIRTUAL
            case 183: { // INVOKESPECIAL
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                MethodRefEntry ref = pool.getMethodRef(index);
                InstanceMethodInvoke.Type t = next == 182 ? InstanceMethodInvoke.Type.VIRTUAL : InstanceMethodInvoke.Type.SPECIAL;
                block.append(new InvokeInsn(Insn.INVOKE, t, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 184: { // INVOKESTATIC
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                MethodRefEntry ref = pool.getMethodRef(index);
                block.append(new InvokeInsn(Insn.INVOKESTATIC, null, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 185: {// INVOKEINTERFACE
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                // skip count and constant 0 (historical)
                i += 2;
                MethodRefEntry ref = pool.getInterfaceMethodRef(index);
                block.append(new InvokeInsn(Insn.INVOKE, InstanceMethodInvoke.Type.INTERFACE, ref.cls, ref.name, ref.type_name));
                break;
            }
            case 186: {// INVOKEDYNAMIC
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                // skip constant 0 (historical)
                i += 2;
                InvokeDynamicEntry handle = pool.getInvokeDynamic(index);
                BootstrapMethod bsm = bootstrap_methods.get(handle.bootstrap_index);
                MethodRefEntry bsmArg = pool.getMethodRef(((MethodHandleEntry) bsm.arguments[1]).reference_index);
                block.append(new InvokeDynamicInsn(Insn.INVOKEDYNAMIC, bsmArg.cls, bsmArg.name, bsmArg.type_name, handle.name, handle.type_name,
                        bsmArg.type == ClassConstantPool.EntryType.INTERFACE_METHOD_REF));
                break;
            }
            case 187: {// NEW
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                ClassEntry ref = pool.getClass(index);
                block.append(new TypeInsn(Insn.NEW, ref.name));
                break;
            }
            case 188: {// NEWARRAY
                String type = null;
                byte atype = code[i++];
                switch (atype) {
                case 4: // T_BOOLEAN
                    type = "Z";
                    break;
                case 5: // T_CHAR
                    type = "C";
                    break;
                case 6: // T_FLOAT
                    type = "F";
                    break;
                case 7: // T_DOUBLE
                    type = "D";
                    break;
                case 8: // T_BYTE
                    type = "B";
                    break;
                case 9: // T_SHORT
                    type = "S";
                    break;
                case 10: // T_INT
                    type = "I";
                    break;
                case 11: // T_LONG
                    type = "J";
                    break;
                default:
                    throw new SourceFormatException("Unsupported NEWARRAY type value: " + atype);
                }
                block.append(new TypeInsn(Insn.NEWARRAY, type));
                break;
            }
            case 189: {// ANEWARRAY
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                ClassEntry ref = pool.getClass(index);
                block.append(new TypeInsn(Insn.NEWARRAY, ref.name));
                break;
            }
            case 190: // ARRAYLENGTH
                block.append(new FieldInsn(Insn.GETFIELD, "", "length", "I"));
                break;
            case 191: // ATHROW
                block.append(new OpInsn(Insn.THROW));
                break;
            case 192: {// CHECKCAST
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                ClassEntry ref = pool.getClass(index);
                String desc = ref.name;
                if (!desc.startsWith("[")) {
                    desc = "L" + desc + ";";
                }
                block.append(new TypeInsn(Insn.CAST, desc));
                break;
            }
            case 193: {// INSTANCEOF
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                ClassEntry ref = pool.getClass(index);
                block.append(new TypeInsn(Insn.INSTANCEOF, ref.name));
                break;
            }
            case 194: // MONITORENTER
            case 195: // MONITOREXIT
            case 196: // WIDE
                throw new SourceFormatException("Unsupported java opcode: " + next);
            case 197: {// MULTINEWARRAY
                int index = ((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF);
                ClassEntry ref = pool.getClass(index);
                int dims = code[i++] & 0xFF;
                block.append(new TypeIntInsn(Insn.MULTINEWARRAY, ref.name, dims));
                break;
            }
            case 198: {// IFNULL
                block.append(new LdcInsn(Insn.PUSH, null));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPEQ, opcode_index + index));
                break;
            }
            case 199: {// IFNONNULL
                block.append(new LdcInsn(Insn.PUSH, null));
                insn_starts.add(opcode_index);
                short index = (short) (((code[i++] & 0xFF) << 8) | (code[i++] & 0xFF));
                block.append(new JumpInsn(Insn.IF_CMPNE, opcode_index + index));
                break;
            }
            case 200: // GOTO_W
            case 201: // JSR_W
                throw new SourceFormatException("Unsupported java opcode: " + next);
            default:
                throw new SourceFormatException("Unknown java opcode: " + next);
            }
        }

        for (Insn insn : block) {
            if (insn instanceof JumpInsn) {
                JumpInsn jump = (JumpInsn) insn;
                jump.setTarget(insn_starts.indexOf(jump.getTarget()));
            } else if (insn instanceof SwitchInsn) {
                SwitchInsn sw = (SwitchInsn) insn;
                sw.setDefault(insn_starts.indexOf(sw.getDefault()));
                Map<Integer, Integer> new_targets = new HashMap<>();
                for (Map.Entry<Integer, Integer> e : sw.getTargets().entrySet()) {
                    new_targets.put(e.getKey(), insn_starts.indexOf(e.getValue()));
                }
                sw.getTargets().clear();
                sw.getTargets().putAll(new_targets);
            }
        }

        for (TryCatchRegion region : catch_regions) {
            int start_pc = insn_starts.indexOf(region.getStart());
            int end_pc = insn_starts.indexOf(region.getEnd());
            int catch_pc = insn_starts.indexOf(region.getCatch());
            block.getCatchRegions().add(new TryCatchRegion(start_pc, end_pc, catch_pc, region.getException()));
        }

        locals.bakeInstances(insn_starts);

        return block;
    }

    private int readInt(byte[] code, int i) {
        int byte1 = code[i++] & 0xFF;
        int byte2 = code[i++] & 0xFF;
        int byte3 = code[i++] & 0xFF;
        int byte4 = code[i++] & 0xFF;
        return (byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4;
    }

}

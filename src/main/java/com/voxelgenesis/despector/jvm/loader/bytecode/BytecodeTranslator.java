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
package com.voxelgenesis.despector.jvm.loader.bytecode;

import com.voxelgenesis.despector.core.ir.Insn;
import com.voxelgenesis.despector.core.ir.InsnBlock;
import com.voxelgenesis.despector.core.ir.IntInsn;
import com.voxelgenesis.despector.core.ir.InvokeInsn;
import com.voxelgenesis.despector.core.ir.LdcInsn;
import com.voxelgenesis.despector.core.ir.OpInsn;
import com.voxelgenesis.despector.core.loader.SourceFormatException;
import com.voxelgenesis.despector.jvm.loader.ClassConstantPool;
import com.voxelgenesis.despector.jvm.loader.ClassConstantPool.MethodRef;

public class BytecodeTranslator {

    public BytecodeTranslator() {

    }

    public InsnBlock createIR(byte[] code, ClassConstantPool pool) {
        InsnBlock block = new InsnBlock();

        for (int i = 0; i < code.length;) {
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
                block.append(new IntInsn(Insn.LCONST, 0));
                break;
            case 10: // LCONST_1
                block.append(new IntInsn(Insn.LCONST, 1));
                break;
            case 11: // FCONST_0
                block.append(new IntInsn(Insn.FCONST, 0));
                break;
            case 12: // FCONST_1
                block.append(new IntInsn(Insn.FCONST, 1));
                break;
            case 13: // FCONST_2
                block.append(new IntInsn(Insn.FCONST, 2));
                break;
            case 14: // DCONST_0
                block.append(new IntInsn(Insn.DCONST, 0));
                break;
            case 15: // DCONST_1
                block.append(new IntInsn(Insn.DCONST, 1));
                break;
            case 16: {// BIPUSH
                int val = code[i++];
                block.append(new IntInsn(Insn.ICONST, val));
                break;
            }
            case 17: {// SIPUSH
                int val = code[i++];
                block.append(new IntInsn(Insn.ICONST, val));
                break;
            }
            case 18: // LDC
            case 19: // LDC_W
            case 20: // LDC2_W
            case 21: // ILOAD
            case 22: // LLOAD
            case 23: // FLOAD
            case 24: // DLOAD
            case 25: // ALOAD
            case 26: // ILOAD_0
            case 27: // ILOAD_1
            case 28: // ILOAD_2
            case 29: // ILOAD_3
            case 30: // LLOAD_0
            case 31: // LLOAD_1
            case 32: // LLOAD_2
            case 33: // LLOAD_3
            case 34: // FLOAD_0
            case 35: // FLOAD_1
            case 36: // FLOAD_2
            case 37: // FLOAD_3
            case 38: // DLOAD_0
            case 39: // DLOAD_1
            case 40: // DLOAD_2
            case 41: // DLOAD_3
                break;
            case 42: // ALOAD_0
                block.append(new IntInsn(Insn.LOCAL_LOAD, 0));
                break;
            case 43: // ALOAD_1
            case 44: // ALOAD_2
            case 45: // ALOAD_3
            case 46: // IALOAD
            case 47: // LALOAD
            case 48: // FALOAD
            case 49: // DALOAD
            case 50: // AALOAD
            case 51: // BALOAD
            case 52: // CALOAD
            case 53: // SALOAD
                break;
            case 54: { // ISTORE
                int local = code[i++];
                block.append(new IntInsn(Insn.LOCAL_STORE, local));
                break;
            }
            case 55: // LSTORE
            case 56: // FSTORE
            case 57: // DSTORE
            case 58: // ASTORE
            case 59: // ISTORE_0
            case 60: // ISTORE_1
            case 61: // ISTORE_2
            case 62: // ISTORE_3
            case 63: // LSTORE_0
            case 64: // LSTORE_1
            case 65: // LSTORE_2
            case 66: // LSTORE_3
            case 67: // FSTORE_0
            case 68: // FSTORE_1
            case 69: // FSTORE_2
            case 70: // FSTORE_3
            case 71: // DSTORE_0
            case 72: // DSTORE_1
            case 73: // DSTORE_2
            case 74: // DSTORE_3
            case 75: // ASTORE_0
            case 76: // ASTORE_1
            case 77: // ASTORE_2
            case 78: // ASTORE_3
            case 79: // IASTORE
            case 80: // LASTORE
            case 81: // FASTORE
            case 82: // DASTORE
            case 83: // AASTORE
            case 84: // BASTORE
            case 85: // CASTORE
            case 86: // SASTORE
            case 87: // POP
            case 88: // POP2
            case 89: // DUP
            case 90: // DUP_X1
            case 91: // DUP_X2
            case 92: // DUP2
            case 93: // DUP2_X1
            case 94: // DUP2_X2
            case 95: // SWAP
            case 96: // IADD
            case 97: // LADD
            case 98: // FADD
            case 99: // DADD
            case 100: // ISUB
            case 101: // LSUB
            case 102: // FSUB
            case 103: // DSUB
            case 104: // IMUL
            case 105: // LMUL
            case 106: // FMUL
            case 107: // DMUL
            case 108: // IDIV
            case 109: // LDIV
            case 110: // FDIV
            case 111: // DDIV
            case 112: // IREM
            case 113: // LREM
            case 114: // FREM
            case 115: // DREM
            case 116: // INEG
            case 117: // LNEG
            case 118: // FNEG
            case 119: // DNEG
            case 120: // ISHL
            case 121: // LSHL
            case 122: // ISHR
            case 123: // LSHR
            case 124: // IUSHR
            case 125: // LUSHR
            case 126: // IAND
            case 127: // LAND
            case 128: // IOR
            case 129: // LOR
            case 130: // IXOR
            case 131: // LXOR
            case 132: // IINC
            case 133: // I2L
            case 134: // I2F
            case 135: // I2D
            case 136: // L2I
            case 137: // L2F
            case 138: // L2D
            case 139: // F2I
            case 140: // F2L
            case 141: // F2D
            case 142: // D2I
            case 143: // D2L
            case 144: // D2F
            case 145: // I2B
            case 146: // I2C
            case 147: // I2S
            case 148: // LCMP
            case 149: // FCMPL
            case 150: // FCMPG
            case 151: // DCMPL
            case 152: // DCMPG
            case 153: // IFEQ
            case 154: // IFNE
            case 155: // IFLT
            case 156: // IFGE
            case 157: // IFGT
            case 158: // IFLE
            case 159: // IF_ICMPEQ
            case 160: // IF_ICMPNE
            case 161: // IF_ICMPLT
            case 162: // IF_ICMPGE
            case 163: // IF_ICMPGT
            case 164: // IF_ICMPLE
            case 165: // IF_ACMPEQ
            case 166: // IF_ACMPNE
            case 167: // GOTO
            case 168: // JSR
            case 169: // RET
            case 170: // TABLESWITCH
            case 171: // LOOKUPSWITCH
            case 172: // IRETURN
            case 173: // LRETURN
            case 174: // FRETURN
            case 175: // DRETURN
            case 176: // ARETURN
                break;
            case 177: // RETURN
                block.append(new OpInsn(Insn.RETURN));
                break;
            case 178: // GETSTATIC
            case 179: // PUTSTATIC
            case 180: // GETFIELD
            case 181: // PUTFIELD
            case 182: // INVOKEVIRTUAL
                break;
            case 183: { // INVOKESPECIAL
                int index = (code[i++] << 8) | code[i++];
                MethodRef ref = pool.getMethodRef(index);
                block.append(new InvokeInsn(Insn.INVOKE, ref.cls, ref.name, ref.type));
                break;
            }
            case 184: // INVOKESTATIC
            case 185: // INVOKEINTERFACE
            case 186: // INVOKEDYNAMIC
            case 187: // NEW
            case 188: // NEWARRAY
            case 189: // ANEWARRAY
            case 190: // ARRAYLENGTH
            case 191: // ATHROW
            case 192: // CHECKCAST
            case 193: // INSTANCEOF
            case 194: // MONITORENTER
            case 195: // MONITOREXIT
            case 196: // WIDE
            case 197: // MULTINEWARRAY
            case 198: // IFNULL
            case 199: // IFNONNULL
            case 200: // GOTO_W
            case 201: // JSR_W
                break;
            default:
                throw new SourceFormatException("Unknown java opcode: " + next);
            }
        }

        return block;
    }

}

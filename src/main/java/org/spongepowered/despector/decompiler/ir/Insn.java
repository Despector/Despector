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
package org.spongepowered.despector.decompiler.ir;

public abstract class Insn {

    public static final int NOOP = 0;
    public static final int PUSH = 1;
    public static final int ICONST = 2;
    public static final int LCONST = 3;
    public static final int FCONST = 4;
    public static final int DCONST = 5;

    public static final int LOCAL_LOAD = 10;
    public static final int LOCAL_STORE = 11;
    public static final int ARRAY_LOAD = 12;
    public static final int ARRAY_STORE = 13;
    public static final int GETFIELD = 14;
    public static final int PUTFIELD = 15;
    public static final int GETSTATIC = 16;
    public static final int PUTSTATIC = 17;

    public static final int INVOKE = 20;
    public static final int INVOKESTATIC = 21;
    public static final int NEW = 22;
    public static final int NEWARRAY = 23;
    public static final int THROW = 24;
    public static final int RETURN = 25;
    public static final int ARETURN = 26;
    public static final int CAST = 27;

    public static final int POP = 30;
    public static final int DUP = 31;
    public static final int DUP_X1 = 32;
    public static final int DUP_X2 = 33;
    public static final int DUP2 = 34;
    public static final int DUP2_X1 = 35;
    public static final int DUP2_X2 = 36;
    public static final int SWAP = 37;

    public static final int ADD = 40;
    public static final int SUB = 41;
    public static final int MUL = 42;
    public static final int DIV = 43;
    public static final int REM = 44;
    public static final int NEG = 45;
    public static final int SHL = 46;
    public static final int SHR = 47;
    public static final int USHR = 48;
    public static final int AND = 49;
    public static final int OR = 50;
    public static final int XOR = 51;

    public static final int IINC = 60;
    public static final int CMP = 61;

    public static final int IFEQ = 70;
    public static final int IFNE = 71;
    public static final int IF_CMPLT = 72;
    public static final int IF_CMPGT = 73;
    public static final int IF_CMPGE = 74;
    public static final int IF_CMPLE = 75;
    public static final int IF_CMPEQ = 76;
    public static final int IF_CMPNE = 77;
    public static final int GOTO = 78;
    public static final int SWITCH = 79;

    protected int opcode;

    public Insn(int op) {
        this.opcode = op;
    }

    public int getOpcode() {
        return this.opcode;
    }

}

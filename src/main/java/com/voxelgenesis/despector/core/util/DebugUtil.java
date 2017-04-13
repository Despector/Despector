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
package com.voxelgenesis.despector.core.util;

import com.voxelgenesis.despector.core.ir.Insn;

public final class DebugUtil {

    private static final String[] opcodes = new String[256];

    static {
        opcodes[Insn.NOOP] = "NOOP";
        opcodes[Insn.PUSH] = "PUSH";
        opcodes[Insn.ICONST] = "ICONST";
        opcodes[Insn.LCONST] = "LCONST";
        opcodes[Insn.FCONST] = "FCONST";
        opcodes[Insn.DCONST] = "DCONST";

        opcodes[Insn.LOCAL_LOAD] = "LOCAL_LOAD";
        opcodes[Insn.LOCAL_STORE] = "LOCAL_STORE";
        opcodes[Insn.ARRAY_LOAD] = "ARRAY_LOAD";
        opcodes[Insn.ARRAY_STORE] = "ARRAY_STORE";
        opcodes[Insn.GETFIELD] = "GETFIELD";
        opcodes[Insn.PUTFIELD] = "PUTFIELD";
        opcodes[Insn.GETSTATIC] = "GETSTATIC";
        opcodes[Insn.PUTSTATIC] = "PUTSTATIC";

        opcodes[Insn.INVOKE] = "INVOKE";
        opcodes[Insn.INVOKESTATIC] = "INVOKESTATIC";
        opcodes[Insn.NEW] = "NEW";
        opcodes[Insn.NEWARRAY] = "NEWARRAY";
        opcodes[Insn.THROW] = "THROW";
        opcodes[Insn.RETURN] = "RETURN";
        opcodes[Insn.ARETURN] = "ARETURN";

        opcodes[Insn.POP] = "POP";
        opcodes[Insn.DUP] = "DUP";
        opcodes[Insn.DUP_X1] = "DUP_X1";
        opcodes[Insn.DUP_X2] = "DUP_X2";
        opcodes[Insn.DUP2] = "DUP2";
        opcodes[Insn.DUP2_X1] = "DUP2_X1";
        opcodes[Insn.DUP2_X2] = "DUP2_X2";
        opcodes[Insn.SWAP] = "SWAP";

        opcodes[Insn.ADD] = "ADD";
        opcodes[Insn.SUB] = "SUB";
        opcodes[Insn.MUL] = "MUL";
        opcodes[Insn.DIV] = "DIV";
        opcodes[Insn.REM] = "REM";
        opcodes[Insn.NEG] = "NEG";
        opcodes[Insn.SHL] = "SHL";
        opcodes[Insn.SHR] = "SHR";
        opcodes[Insn.USHR] = "USHR";
        opcodes[Insn.AND] = "AND";
        opcodes[Insn.OR] = "OR";
        opcodes[Insn.XOR] = "XOR";

        opcodes[Insn.IINC] = "IINC";
        opcodes[Insn.CMP] = "CMP";

        opcodes[Insn.IFEQ] = "IFEQ";
        opcodes[Insn.IFNE] = "IFNE";
        opcodes[Insn.IF_CMPLT] = "IF_CMPLT";
        opcodes[Insn.IF_CMPLE] = "IF_CMPLE";
        opcodes[Insn.IF_CMPGE] = "IF_CMPGE";
        opcodes[Insn.IF_CMPGT] = "IF_CMPGT";
        opcodes[Insn.IF_CMPEQ] = "IF_CMPEQ";
        opcodes[Insn.IF_CMPNE] = "IF_CMPNE";
        opcodes[Insn.GOTO] = "GOTO";
        opcodes[Insn.SWITCH] = "SWITCH";
    }

    public static String opcodeToString(int opcode) {
        return opcodes[opcode];
    }

    private DebugUtil() {
    }

}

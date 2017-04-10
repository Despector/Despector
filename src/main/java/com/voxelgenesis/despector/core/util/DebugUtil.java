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
import com.voxelgenesis.despector.core.ir.IntInsn;
import com.voxelgenesis.despector.core.ir.InvokeInsn;

public final class DebugUtil {

    private static final String[] opcodes = new String[256];

    static {
        opcodes[Insn.INVOKE] = "INVOKE";
        opcodes[Insn.INVOKESTATIC] = "INVOKESTATIC";
        opcodes[Insn.LOCAL_LOAD] = "LOCAL_LOAD";
        opcodes[Insn.LOCAL_STORE] = "LOCAL_STORE";
        opcodes[Insn.NOOP] = "NOOP";
        opcodes[Insn.PUSH] = "PUSH";
        opcodes[Insn.RETURN] = "RETURN";
    }

    public static String opcodeToString(int opcode) {
        return opcodes[opcode];
    }

    public static String irToString(Insn insn) {
        StringBuilder str = new StringBuilder();
        str.append(opcodeToString(insn.getOpcode()));
        if (insn instanceof IntInsn) {
            str.append(" ").append(((IntInsn) insn).getOperand());
        } else if (insn instanceof InvokeInsn) {
            InvokeInsn invoke = (InvokeInsn) insn;
            str.append(" ").append(invoke.getOwner());
            str.append(" ").append(invoke.getName());
            str.append(" ").append(invoke.getDescription());
        }
        return str.toString();
    }

    private DebugUtil() {
    }

}

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
            case 16: {// BIPUSH
                int val = code[i++];
                block.append(new IntInsn(Insn.PUSH, val));
                break;
            }
            case 42: // ALOAD_0
                block.append(new IntInsn(Insn.LOCAL_LOAD, 0));
                break;
            case 54: { // ISTORE
                int local = code[i++];
                block.append(new IntInsn(Insn.LOCAL_STORE, local));
                break;
            }
            case 177: // RETURN
                block.append(new OpInsn(Insn.RETURN));
                break;
            case 183: { // INVOKESPECIAL
                int index = (code[i++] << 8) | code[i++];
                MethodRef ref = pool.getMethodRef(index);
                block.append(new InvokeInsn(Insn.INVOKE, ref.cls, ref.name, ref.type));
                break;
            }
            default:
                throw new SourceFormatException("Unknown java opcode: " + next);
            }
        }

        return block;
    }

}

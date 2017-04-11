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
package com.voxelgenesis.despector.core.decompiler;

import com.voxelgenesis.despector.core.ast.method.Local;
import com.voxelgenesis.despector.core.ast.method.Locals;
import com.voxelgenesis.despector.core.ast.method.StatementBlock;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.method.insn.cst.IntConstant;
import com.voxelgenesis.despector.core.ast.method.insn.cst.NullConstant;
import com.voxelgenesis.despector.core.ast.method.insn.var.LocalAccess;
import com.voxelgenesis.despector.core.ast.method.invoke.InstanceMethodInvoke;
import com.voxelgenesis.despector.core.ast.method.invoke.InvokeStatement;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.LocalAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.misc.Return;
import com.voxelgenesis.despector.core.ir.Insn;
import com.voxelgenesis.despector.core.ir.IntInsn;
import com.voxelgenesis.despector.core.ir.InvokeInsn;
import com.voxelgenesis.despector.core.ir.LdcInsn;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Deque;

public class InsnAppender {

    public static void append(Insn insn, StatementBlock block, Locals locals, Deque<Instruction> stack) {
        switch (insn.getOpcode()) {
        case Insn.NOOP:
            break;
        case Insn.PUSH: {
            LdcInsn ldc = (LdcInsn) insn;
            if (ldc.getConstant() == null) {
                stack.push(NullConstant.NULL);
            } else {
                throw new IllegalStateException("Unsupported constant value " + ldc.getConstant().getClass().getName());
            }
            break;
        }
        case Insn.ICONST: {
            IntInsn i = (IntInsn) insn;
            stack.push(new IntConstant(i.getValue()));
            break;
        }
        case Insn.LCONST:
        case Insn.FCONST:
        case Insn.DCONST:
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        case Insn.LOCAL_LOAD: {
            IntInsn i = (IntInsn) insn;
            Local loc = locals.get(i.getValue());
            stack.push(new LocalAccess(loc));
            break;
        }
        case Insn.LOCAL_STORE: {
            IntInsn i = (IntInsn) insn;
            Local local = locals.get(i.getValue());
            block.append(new LocalAssignment(local, stack.pop()));
            break;
        }
        case Insn.ARRAY_LOAD:
        case Insn.ARRAY_STORE:
        case Insn.GETFIELD:
        case Insn.PUTFIELD:
        case Insn.GETSTATIC:
        case Insn.PUTSTATIC:
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        case Insn.INVOKE: {
            InvokeInsn invoke = (InvokeInsn) insn;
            int param_count = TypeHelper.paramCount(invoke.getDescription());
            Instruction[] args = new Instruction[param_count];
            for (int i = 0; i < args.length; i++) {
                args[i] = stack.pop();
            }
            Instruction callee = stack.pop();
            InstanceMethodInvoke mth = new InstanceMethodInvoke(invoke.getName(), invoke.getDescription(), invoke.getOwner(), args, callee);
            if ("V".equals(TypeHelper.getRet(invoke.getDescription()))) {
                block.append(new InvokeStatement(mth));
            } else {
                stack.push(mth);
            }
            break;
        }
        case Insn.INVOKESTATIC:
        case Insn.NEW:
        case Insn.NEWARRAY:
        case Insn.THROW:
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        case Insn.RETURN: {
            block.append(new Return());
            break;
        }
        case Insn.ARETURN:
        case Insn.POP:
        case Insn.DUP:
        case Insn.DUP_X1:
        case Insn.DUP_X2:
        case Insn.DUP2:
        case Insn.DUP2_X1:
        case Insn.DUP2_X2:
        case Insn.SWAP:
        case Insn.ADD:
        case Insn.SUB:
        case Insn.MUL:
        case Insn.DIV:
        case Insn.REM:
        case Insn.NEG:
        case Insn.SHL:
        case Insn.SHR:
        case Insn.USHR:
        case Insn.AND:
        case Insn.OR:
        case Insn.XOR:
        case Insn.IINC:
        case Insn.CMP:
        case Insn.IFEQ:
        case Insn.IFNE:
        case Insn.IF_CMPLT:
        case Insn.IF_CMPGT:
        case Insn.IF_CMPGE:
        case Insn.IF_CMPLE:
        case Insn.GOTO:
        case Insn.SWITCH:
        default:
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        }
    }

}

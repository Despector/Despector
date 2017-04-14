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
import com.voxelgenesis.despector.core.ast.method.insn.cst.StringConstant;
import com.voxelgenesis.despector.core.ast.method.insn.operator.NegativeOperator;
import com.voxelgenesis.despector.core.ast.method.insn.operator.Operator;
import com.voxelgenesis.despector.core.ast.method.insn.operator.OperatorType;
import com.voxelgenesis.despector.core.ast.method.insn.var.FieldAccess;
import com.voxelgenesis.despector.core.ast.method.insn.var.InstanceFieldAccess;
import com.voxelgenesis.despector.core.ast.method.insn.var.LocalAccess;
import com.voxelgenesis.despector.core.ast.method.insn.var.StaticFieldAccess;
import com.voxelgenesis.despector.core.ast.method.invoke.InstanceMethodInvoke;
import com.voxelgenesis.despector.core.ast.method.invoke.InvokeStatement;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.FieldAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.InstanceFieldAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.LocalAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.StaticFieldAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.misc.Return;
import com.voxelgenesis.despector.core.ir.FieldInsn;
import com.voxelgenesis.despector.core.ir.Insn;
import com.voxelgenesis.despector.core.ir.IntInsn;
import com.voxelgenesis.despector.core.ir.InvokeInsn;
import com.voxelgenesis.despector.core.ir.LdcInsn;
import com.voxelgenesis.despector.jvm.loader.JvmHelper;
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
            } else if (ldc.getConstant() instanceof String) {
                stack.push(new StringConstant((String) ldc.getConstant()));
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
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        case Insn.GETSTATIC: {
            FieldInsn field = (FieldInsn) insn;
            String owner = field.getOwner();
            if (!owner.startsWith("[")) {
                owner = "L" + owner + ";";
            }
            FieldAccess arg = new StaticFieldAccess(field.getName(), JvmHelper.of(field.getDescription()), owner);
            stack.push(arg);
            break;
        }
        case Insn.PUTSTATIC: {
            FieldInsn field = (FieldInsn) insn;
            Instruction val = stack.pop();
            String owner = field.getOwner();
            if (!owner.startsWith("[")) {
                owner = "L" + owner + ";";
            }
            FieldAssignment assign = new StaticFieldAssignment(field.getName(), field.getDescription(), owner, val);
            block.append(assign);
            break;
        }
        case Insn.GETFIELD: {
            FieldInsn field = (FieldInsn) insn;
            String owner = field.getOwner();
            if (!owner.startsWith("[")) {
                owner = "L" + owner + ";";
            }
            FieldAccess arg = new InstanceFieldAccess(field.getName(), JvmHelper.of(field.getDescription()), owner, stack.pop());
            stack.push(arg);
            break;
        }
        case Insn.PUTFIELD: {
            FieldInsn field = (FieldInsn) insn;
            Instruction val = stack.pop();
            Instruction owner = stack.pop();
            String owner_t = field.getOwner();
            if (!owner_t.startsWith("[")) {
                owner_t = "L" + owner_t + ";";
            }
            FieldAssignment assign = new InstanceFieldAssignment(field.getName(), field.getDescription(), owner_t, owner, val);
            block.append(assign);
            break;
        }
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
            throw new IllegalStateException("Unsupported opcode in appender: " + insn);
        case Insn.ADD: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.ADD, left, right));
            break;
        }
        case Insn.SUB: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.SUBTRACT, left, right));
            break;
        }
        case Insn.MUL: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.MULTIPLY, left, right));
            break;
        }
        case Insn.DIV: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.DIVIDE, left, right));
            break;
        }
        case Insn.REM: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.REMAINDER, left, right));
            break;
        }
        case Insn.NEG: {
            stack.push(new NegativeOperator(stack.pop()));
            break;
        }
        case Insn.SHL: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.SHIFT_LEFT, left, right));
            break;
        }
        case Insn.SHR: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.SHIFT_RIGHT, left, right));
            break;
        }
        case Insn.USHR: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.UNSIGNED_SHIFT_RIGHT, left, right));
            break;
        }
        case Insn.AND: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.AND, left, right));
            break;
        }
        case Insn.OR: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.OR, left, right));
            break;
        }
        case Insn.XOR: {
            Instruction right = stack.pop();
            Instruction left = stack.pop();
            stack.push(new Operator(OperatorType.XOR, left, right));
            break;
        }
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

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
package org.spongepowered.despector.decompiler.method;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.cst.TypeConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.op.OperatorType;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.FieldAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.assign.FieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.DynamicInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;
import org.spongepowered.despector.decompiler.ir.DoubleInsn;
import org.spongepowered.despector.decompiler.ir.FieldInsn;
import org.spongepowered.despector.decompiler.ir.FloatInsn;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.IntInsn;
import org.spongepowered.despector.decompiler.ir.InvokeDynamicInsn;
import org.spongepowered.despector.decompiler.ir.InvokeInsn;
import org.spongepowered.despector.decompiler.ir.LdcInsn;
import org.spongepowered.despector.decompiler.ir.LongInsn;
import org.spongepowered.despector.decompiler.ir.TypeInsn;
import org.spongepowered.despector.decompiler.ir.VarIntInsn;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Deque;

/**
 * A utility for forming non-control flow opcodes into statements.
 */
public final class StatementBuilder {

    /**
     * Appends the given opcode block to the statement block.
     */
    public static void appendBlock(OpcodeBlock op, StatementBlock block, Locals locals, Deque<Instruction> stack) {

        // Decompiles a set of opcodes into statements.

        for (int index = 0; index < op.getOpcodes().size(); index++) {
            int label_index = op.getStart() + index;
            Insn next = op.getOpcodes().get(index);
            switch (next.getOpcode()) {
            case Insn.NOOP:
                break;
            case Insn.ICONST:
                stack.push(new IntConstant(((IntInsn) next).getValue()));
                break;
            case Insn.LCONST:
                stack.push(new LongConstant(((LongInsn) next).getValue()));
                break;
            case Insn.FCONST:
                stack.push(new FloatConstant(((FloatInsn) next).getValue()));
                break;
            case Insn.DCONST:
                stack.push(new DoubleConstant(((DoubleInsn) next).getValue()));
                break;
            case Insn.PUSH: {
                LdcInsn ldc = (LdcInsn) next;
                if (ldc.getConstant() == null) {
                    stack.push(NullConstant.NULL);
                } else if (ldc.getConstant() instanceof String) {
                    stack.push(new StringConstant((String) ldc.getConstant()));
                } else if (ldc.getConstant() instanceof ClassTypeSignature) {
                    stack.push(new TypeConstant((ClassTypeSignature) ldc.getConstant()));
                } else {
                    throw new IllegalStateException("Unsupported ldc constant: " + ldc.getConstant().getClass().getName());
                }
                break;
            }
            case Insn.LOCAL_LOAD: {
                IntInsn var = (IntInsn) next;
                Local local = locals.getLocal(var.getValue());
                stack.push(new LocalAccess(local.getInstance(label_index)));
                break;
            }
            case Insn.LOCAL_STORE: {
                IntInsn var = (IntInsn) next;
                Instruction val = stack.pop();
                Local local = locals.getLocal(var.getValue());
                LocalInstance instance = local.getInstance(label_index);
                if (!local.isParameter() && local.getParameterInstance() != null) {
                    instance.setType(val.inferType());
                }
                block.append(new LocalAssignment(instance, val));
                break;
            }
            case Insn.ARRAY_LOAD: {
                Instruction index_arg = stack.pop();
                Instruction var = stack.pop();
                stack.push(new ArrayAccess(var, index_arg));
                break;
            }
            case Insn.ARRAY_STORE: {
                Instruction val = stack.pop();
                Instruction index_arg = stack.pop();
                Instruction var = stack.pop();
                if (var instanceof LocalAccess) {
                    LocalInstance local = ((LocalAccess) var).getLocal();
                    if (local.getType() == null) {
                        local.setType(TypeSignature.arrayOf(val.inferType()));
                    }
                }
                if (var instanceof NewArray) {
                    NewArray array = (NewArray) var;
                    if (array.getInitializer() == null) {
                        array.setInitialValues(new Instruction[((IntConstant) array.getSize()).getConstant()]);
                    }
                    array.getInitializer()[((IntConstant) index_arg).getConstant()] = val;
                    break;
                }
                block.append(new ArrayAssignment(var, index_arg, val));
                break;
            }
            case Insn.POP: {
                Instruction arg = stack.pop();
                if (arg instanceof InstanceMethodInvoke || arg instanceof StaticMethodInvoke) {
                    block.append(new InvokeStatement(arg));
                }
                break;
            }
            case Insn.DUP: {
                stack.push(stack.peek());
                break;
            }
            case Insn.DUP_X1: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                stack.push(val);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case Insn.DUP_X2: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                Instruction val3 = stack.pop();
                stack.push(val);
                stack.push(val3);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case Insn.DUP2: {
                Instruction val = stack.pop();
                Instruction val2 = stack.peek();
                stack.push(val);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case Insn.DUP2_X1: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                Instruction val3 = stack.pop();
                stack.push(val2);
                stack.push(val);
                stack.push(val3);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case Insn.DUP2_X2: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                Instruction val3 = stack.pop();
                Instruction val4 = stack.pop();
                stack.push(val2);
                stack.push(val);
                stack.push(val4);
                stack.push(val3);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case Insn.SWAP: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                stack.push(val);
                stack.push(val2);
                break;
            }
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
                Instruction right = stack.pop();
                stack.push(new NegativeOperator(right));
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
            case Insn.IINC: {
                VarIntInsn inc = (VarIntInsn) next;
                Local local = locals.getLocal(inc.getLocal());
                Increment insn = new Increment(local.getInstance(label_index), inc.getValue());
                block.append(insn);
                break;
            }
            case Insn.CMP: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new NumberCompare(left, right));
                break;
            }
            case Insn.ARETURN:
                block.append(new Return(stack.pop()));
                break;
            case Insn.RETURN:
                block.append(new Return());
                break;
            case Insn.GETSTATIC: {
                FieldInsn field = (FieldInsn) next;
                String owner = field.getOwner();
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldAccess arg = new StaticFieldAccess(field.getName(), ClassTypeSignature.of(field.getDescription()), owner);
                stack.push(arg);
                break;
            }
            case Insn.PUTSTATIC: {
                FieldInsn field = (FieldInsn) next;
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
                FieldInsn field = (FieldInsn) next;
                String owner = field.getOwner();
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldAccess arg = new InstanceFieldAccess(field.getName(), ClassTypeSignature.of(field.getDescription()), owner, stack.pop());
                stack.push(arg);
                break;
            }
            case Insn.PUTFIELD: {
                FieldInsn field = (FieldInsn) next;
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
                InvokeInsn method = (InvokeInsn) next;
                if (method.getName().equals("<init>")) {
                    Instruction[] args = new Instruction[TypeHelper.paramCount(method.getDescription())];
                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = stack.pop();
                    }
                    if (stack.peek() instanceof New) {
                        New new_arg = (New) stack.pop();
                        if (stack.peek() instanceof New) {
                            New new_arg2 = (New) stack.pop();
                            if (new_arg2 == new_arg) {
                                new_arg.setCtorDescription(method.getDescription());
                                new_arg.setParameters(args);
                                stack.push(new_arg);
                                break;
                            }
                            stack.push(new_arg2);
                        }
                        New insn = new New(new_arg.getType(), method.getDescription(), args);
                        block.append(new InvokeStatement(insn));
                        break;
                    } else if (stack.peek() instanceof LocalAccess) {
                        LocalAccess callee = (LocalAccess) stack.pop();
                        String owner = method.getOwner();
                        if (!owner.startsWith("[")) {
                            owner = "L" + owner + ";";
                        }
                        InstanceMethodInvoke arg = new InstanceMethodInvoke(method.getName(), method.getDescription(), owner, args, callee);
                        block.append(new InvokeStatement(arg));
                        break;
                    }
                    throw new IllegalStateException("Callee of call to <init> was " + stack.pop());
                }
                String ret = TypeHelper.getRet(method.getDescription());
                Instruction[] args = new Instruction[TypeHelper.paramCount(method.getDescription())];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = stack.pop();
                }
                Instruction callee = stack.pop();
                String owner = method.getOwner();
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                InstanceMethodInvoke arg = new InstanceMethodInvoke(method.getName(), method.getDescription(), owner, args, callee);
                if (ret.equals("V")) {
                    block.append(new InvokeStatement(arg));
                } else {
                    stack.push(arg);
                }
                break;
            }
            case Insn.INVOKESTATIC: {
                InvokeInsn method = (InvokeInsn) next;
                String ret = TypeHelper.getRet(method.getDescription());
                Instruction[] args = new Instruction[TypeHelper.paramCount(method.getDescription())];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = stack.pop();
                }
                String owner = method.getOwner();
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                StaticMethodInvoke arg = new StaticMethodInvoke(method.getName(), method.getDescription(), owner, args);
                if (ret.equals("V")) {
                    block.append(new InvokeStatement(arg));
                } else {
                    stack.push(arg);
                }
                break;
            }
            case Insn.INVOKEDYNAMIC: {
                InvokeDynamicInsn invoke = (InvokeDynamicInsn) next;
                TypeSignature type = ClassTypeSignature.of(invoke.getType());
                DynamicInvoke handle = new DynamicInvoke(invoke.getLambdaOwner(), invoke.getLambdaName(), invoke.getLambdaDescription(),
                        type, invoke.getName());
                stack.push(handle);
                break;
            }
            case Insn.NEW: {
                TypeSignature type = ClassTypeSignature.of("L" + ((TypeInsn) next).getType() + ";");
                stack.push(new New(type, null, null));
                break;
            }
            case Insn.NEWARRAY: {
                Instruction size = stack.pop();
                TypeInsn array = (TypeInsn) next;
                stack.push(new NewArray(array.getType(), size, null));
                break;
            }
            case Insn.THROW:
                block.append(new Throw(stack.pop()));
                break;
            case Insn.CAST: {
                TypeInsn cast = (TypeInsn) next;
                String desc = cast.getType();
                if (!desc.startsWith("[")) {
                    desc = "L" + desc + ";";
                }
                stack.push(new Cast(ClassTypeSignature.of(desc), stack.pop()));
                break;
            }
            case Insn.INSTANCEOF: {
                TypeInsn insn = (TypeInsn) next;
                Instruction val = stack.pop();
                String type = insn.getType();
                if (!type.startsWith("[")) {
                    type = "L" + type + ";";
                }
                stack.push(new InstanceOf(val, type));
                break;
            }
            case Insn.IFEQ:
            case Insn.IFNE:
            case Insn.IF_CMPEQ:
            case Insn.IF_CMPNE:
            case Insn.IF_CMPLT:
            case Insn.IF_CMPGE:
            case Insn.IF_CMPGT:
            case Insn.IF_CMPLE:
            case Insn.GOTO:
                // All jumps are handled by the implicit structure of the
                // graph
                break;
            default:
                System.err.println("Unsupported opcode: " + next.getOpcode());
                throw new IllegalStateException();
            }
        }
    }

    private StatementBuilder() {
    }

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOf;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArray;
import org.spongepowered.despector.ast.members.insn.arg.NumberCompare;
import org.spongepowered.despector.ast.members.insn.arg.cst.DoubleConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.FloatConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.LongConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.NullConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.TypeConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.FieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.operator.NegativeOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.arg.operator.OperatorType;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.function.DynamicInvokeHandle;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.InvokeStatement;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.members.insn.misc.Throw;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Deque;

public class StatementBuilder {

    public static void appendBlock(OpcodeBlock op, StatementBlock block, Locals locals, Deque<Instruction> stack) {

        // Decompiles a set of opcodes into statements.

        for (int index = 0; index < op.getOpcodes().size(); index++) {
            int label_index = op.getBreakpoint() - (op.getOpcodes().size() - index);
            AbstractInsnNode next = op.getOpcodes().get(index);
            if (next instanceof LabelNode) {
                continue;
            } else if (next instanceof FrameNode) {
                continue;
            } else if (next instanceof LineNumberNode) {
                continue;
            }

            switch (next.getOpcode()) {
            case NOP:
                break;
            case ACONST_NULL:
                stack.push(NullConstant.NULL);
                break;
            case ICONST_M1:
                stack.push(new IntConstant(-1));
                break;
            case ICONST_0:
                stack.push(new IntConstant(0));
                break;
            case ICONST_1:
                stack.push(new IntConstant(1));
                break;
            case ICONST_2:
                stack.push(new IntConstant(2));
                break;
            case ICONST_3:
                stack.push(new IntConstant(3));
                break;
            case ICONST_4:
                stack.push(new IntConstant(4));
                break;
            case ICONST_5:
                stack.push(new IntConstant(5));
                break;
            case LCONST_0:
                stack.push(new LongConstant(0));
                break;
            case LCONST_1:
                stack.push(new LongConstant(1));
                break;
            case FCONST_0:
                stack.push(new FloatConstant(0));
                break;
            case FCONST_1:
                stack.push(new FloatConstant(1));
                break;
            case FCONST_2:
                stack.push(new FloatConstant(2));
                break;
            case DCONST_0:
                stack.push(new DoubleConstant(0));
                break;
            case DCONST_1:
                stack.push(new DoubleConstant(1));
                break;
            case BIPUSH:
            case SIPUSH: {
                IntInsnNode insn = (IntInsnNode) next;
                stack.push(new IntConstant(insn.operand));
                break;
            }
            case LDC: {
                LdcInsnNode ldc = (LdcInsnNode) next;
                if (ldc.cst instanceof String) {
                    stack.push(new StringConstant((String) ldc.cst));
                } else if (ldc.cst instanceof Integer) {
                    stack.push(new IntConstant((Integer) ldc.cst));
                } else if (ldc.cst instanceof Float) {
                    stack.push(new FloatConstant((Float) ldc.cst));
                } else if (ldc.cst instanceof Long) {
                    // LDC_W appears to be merged with this opcode by asm so
                    // long
                    // and double constants will also be here
                    stack.push(new LongConstant((Long) ldc.cst));
                } else if (ldc.cst instanceof Double) {
                    stack.push(new DoubleConstant((Double) ldc.cst));
                } else if (ldc.cst instanceof Type) {
                    stack.push(new TypeConstant((Type) ldc.cst));
                } else {
                    throw new IllegalStateException("Unsupported ldc constant: " + ldc.cst);
                }
                break;
            }
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD: {
                VarInsnNode var = (VarInsnNode) next;
                Local local = locals.getLocal(var.var);
                stack.push(new LocalAccess(local.getInstance(label_index)));
                break;
            }
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD: {
                Instruction index_arg = stack.pop();
                Instruction var = stack.pop();
                stack.push(new ArrayAccess(var, index_arg));
                break;
            }
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE: {
                VarInsnNode var = (VarInsnNode) next;
                Instruction val = stack.pop();
                Local local = locals.getLocal(var.var);
                LocalInstance instance = local.getInstance(label_index);
                if (!local.isParameter() && local.getParameterInstance() != null) {
                    instance.setType(val.inferType());
                }
                block.append(new LocalAssignment(instance, val));
                break;
            }
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE: {
                Instruction val = stack.pop();
                Instruction index_arg = stack.pop();
                Instruction var = stack.pop();
                if (var instanceof LocalAccess) {
                    LocalInstance local = ((LocalAccess) var).getLocal();
                    if (local.getType() == null) {
                        local.setType("[" + val.inferType());
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
            case POP: {
                Instruction arg = stack.pop();
                if (arg instanceof InstanceMethodInvoke || arg instanceof StaticMethodInvoke) {
                    block.append(new InvokeStatement(arg));
                }
                break;
            }
            case POP2: {
                for (int i = 0; i < 2; i++) {
                    Instruction arg = stack.pop();
                    if (arg instanceof InstanceMethodInvoke || arg instanceof StaticMethodInvoke) {
                        block.append(new InvokeStatement(arg));
                    }
                }
                break;
            }
            case DUP: {
                stack.push(stack.peek());
                break;
            }
            case DUP_X1: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                stack.push(val);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case DUP_X2: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                Instruction val3 = stack.pop();
                stack.push(val);
                stack.push(val3);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case DUP2: {
                Instruction val = stack.pop();
                Instruction val2 = stack.peek();
                stack.push(val);
                stack.push(val2);
                stack.push(val);
                break;
            }
            case DUP2_X1: {
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
            case DUP2_X2: {
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
            case SWAP: {
                Instruction val = stack.pop();
                Instruction val2 = stack.pop();
                stack.push(val);
                stack.push(val2);
                break;
            }
            case IADD:
            case LADD:
            case FADD:
            case DADD: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.ADD, left, right));
                break;
            }
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.SUBTRACT, left, right));
                break;
            }
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.MULTIPLY, left, right));
                break;
            }
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.DIVIDE, left, right));
                break;
            }
            case IREM:
            case LREM:
            case FREM:
            case DREM: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.REMAINDER, left, right));
                break;
            }
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG: {
                Instruction right = stack.pop();
                stack.push(new NegativeOperator(right));
                break;
            }
            case ISHL:
            case LSHL: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.SHIFT_LEFT, left, right));
                break;
            }
            case ISHR:
            case LSHR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.SHIFT_RIGHT, left, right));
                break;
            }
            case IUSHR:
            case LUSHR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.UNSIGNED_SHIFT_RIGHT, left, right));
                break;
            }
            case IAND:
            case LAND: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.AND, left, right));
                break;
            }
            case IOR:
            case LOR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.OR, left, right));
                break;
            }
            case IXOR:
            case LXOR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new Operator(OperatorType.XOR, left, right));
                break;
            }
            case IINC: {
                IincInsnNode inc = (IincInsnNode) next;
                Local local = locals.getLocal(inc.var);
                Increment insn = new Increment(local.getInstance(label_index), inc.incr);
                block.append(insn);
                break;
            }
            case L2I:
            case F2I:
            case D2I:
                stack.push(new Cast("I", stack.pop()));
                break;
            case I2L:
            case F2L:
            case D2L:
                stack.push(new Cast("J", stack.pop()));
                break;
            case I2F:
            case L2F:
            case D2F:
                stack.push(new Cast("F", stack.pop()));
                break;
            case I2D:
            case F2D:
            case L2D:
                stack.push(new Cast("D", stack.pop()));
                break;
            case I2B:
                stack.push(new Cast("B", stack.pop()));
                break;
            case I2C:
                stack.push(new Cast("C", stack.pop()));
                break;
            case I2S:
                stack.push(new Cast("S", stack.pop()));
                break;
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new NumberCompare(left, right));
                break;
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case GOTO:
            case JSR:
            case RET:
            case IFNULL:
            case IFNONNULL:
                // All jumps are handled by the implicit structure of the
                // graph
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                block.append(new Return(stack.pop()));
                break;
            case RETURN:
                block.append(new Return());
                break;
            case GETSTATIC: {
                FieldInsnNode field = (FieldInsnNode) next;
                String owner = field.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldAccess arg = new StaticFieldAccess(field.name, field.desc, owner);
                stack.push(arg);
                break;
            }
            case PUTSTATIC: {
                FieldInsnNode field = (FieldInsnNode) next;
                Instruction val = stack.pop();
                String owner = field.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldAssignment assign = new StaticFieldAssignment(field.name, field.desc, owner, val);
                block.append(assign);
                break;
            }
            case GETFIELD: {
                FieldInsnNode field = (FieldInsnNode) next;
                String owner = field.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldAccess arg = new InstanceFieldAccess(field.name, field.desc, owner, stack.pop());
                stack.push(arg);
                break;
            }
            case PUTFIELD: {
                FieldInsnNode field = (FieldInsnNode) next;
                Instruction val = stack.pop();
                Instruction owner = stack.pop();
                String owner_t = field.owner;
                if (!owner_t.startsWith("[")) {
                    owner_t = "L" + owner_t + ";";
                }
                FieldAssignment assign = new InstanceFieldAssignment(field.name, field.desc, owner_t, owner, val);
                block.append(assign);
                break;
            }
            case INVOKESPECIAL: {
                MethodInsnNode ctor = (MethodInsnNode) next;
                if (ctor.name.equals("<init>")) {
                    Instruction[] args = new Instruction[TypeHelper.paramCount(ctor.desc)];
                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = stack.pop();
                    }
                    if (stack.peek() instanceof New) {
                        New new_arg = (New) stack.pop();
                        if (stack.peek() instanceof New) {
                            New new_arg2 = (New) stack.pop();
                            if (new_arg2 == new_arg) {
                                new_arg.setCtorDescription(ctor.desc);
                                new_arg.setParameters(args);
                                stack.push(new_arg);
                                break;
                            }
                            stack.push(new_arg2);
                        }
                        New insn = new New(new_arg.getType(), ctor.desc, args);
                        block.append(new InvokeStatement(insn));
                        break;
                    } else if (stack.peek() instanceof LocalAccess) {
                        LocalAccess callee = (LocalAccess) stack.pop();
                        String owner = ctor.owner;
                        if (!owner.startsWith("[")) {
                            owner = "L" + owner + ";";
                        }
                        InstanceMethodInvoke arg = new InstanceMethodInvoke(ctor.name, ctor.desc, owner, args, callee);
                        block.append(new InvokeStatement(arg));
                        break;
                    }
                    throw new IllegalStateException("Callee of call to <init> was " + stack.pop());
                }
            }
            case INVOKEVIRTUAL:
            case INVOKEINTERFACE: {
                MethodInsnNode method = (MethodInsnNode) next;
                String ret = TypeHelper.getRet(method.desc);
                Instruction[] args = new Instruction[TypeHelper.paramCount(method.desc)];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = stack.pop();
                }
                Instruction callee = stack.pop();
                String owner = method.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                InstanceMethodInvoke arg = new InstanceMethodInvoke(method.name, method.desc, owner, args, callee);
                if (ret.equals("V")) {
                    block.append(new InvokeStatement(arg));
                } else {
                    stack.push(arg);
                }
                break;
            }
            case INVOKESTATIC: {
                MethodInsnNode method = (MethodInsnNode) next;
                String ret = TypeHelper.getRet(method.desc);
                Instruction[] args = new Instruction[TypeHelper.paramCount(method.desc)];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = stack.pop();
                }
                String owner = method.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                StaticMethodInvoke arg = new StaticMethodInvoke(method.name, method.desc, owner, args);
                if (ret.equals("V")) {
                    block.append(new InvokeStatement(arg));
                } else {
                    stack.push(arg);
                }
                break;
            }
            case INVOKEDYNAMIC: {
                InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) next;
                Handle lambda = (Handle) invoke.bsmArgs[1];
                String type = invoke.desc.substring(2);
                String method = invoke.name;
                DynamicInvokeHandle handle = new DynamicInvokeHandle(lambda.getOwner(), lambda.getName(), lambda.getDesc(), type, method);
                stack.push(handle);
                break;
            }
            case NEW: {
                String type = ((TypeInsnNode) next).desc;
                stack.push(new New("L" + type + ";", null, null));
                break;
            }
            case NEWARRAY:
            case ANEWARRAY: {
                Instruction size = stack.pop();
                String array_type = null;
                if (next instanceof IntInsnNode) {
                    IntInsnNode array = (IntInsnNode) next;
                    array_type = AstUtil.opcodeToType(array.operand);
                } else if (next instanceof TypeInsnNode) {
                    TypeInsnNode array = (TypeInsnNode) next;
                    array_type = array.desc;
                }
                stack.push(new NewArray(array_type, size, null));
                break;
            }
            case ARRAYLENGTH:
                stack.push(new InstanceFieldAccess("length", "I", "hidden-array-field", stack.pop()));
                break;
            case ATHROW:
                block.append(new Throw(stack.pop()));
                break;
            case CHECKCAST: {
                TypeInsnNode cast = (TypeInsnNode) next;
                String desc = cast.desc;
                if (!desc.startsWith("[")) {
                    desc = "L" + desc + ";";
                }
                stack.push(new Cast(desc, stack.pop()));
                break;
            }
            case INSTANCEOF: {
                TypeInsnNode insn = (TypeInsnNode) next;
                Instruction val = stack.pop();
                String type = insn.desc;
                if (!type.startsWith("[")) {
                    type = "L" + insn.desc + ";";
                }
                stack.push(new InstanceOf(val, type));
                break;
            }
            case MONITORENTER:
            case MONITOREXIT:
                // TODO synchronized
                throw new IllegalStateException();
                // break;
            case MULTIANEWARRAY:
                // TODO
                throw new IllegalStateException();
            default:
                System.err.println("Unsupported opcode: " + next.getOpcode());
                throw new IllegalStateException();
            }
        }
    }

}

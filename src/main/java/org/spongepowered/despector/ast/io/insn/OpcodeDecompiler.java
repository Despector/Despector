/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.despector.ast.io.insn;

import static org.objectweb.asm.Opcodes.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.despector.ast.io.insn.Locals.DummyLocalInstance;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.CastArg;
import org.spongepowered.despector.ast.members.insn.arg.CompareArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceFunctionArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOfArg;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArrayArg;
import org.spongepowered.despector.ast.members.insn.arg.NewRefArg;
import org.spongepowered.despector.ast.members.insn.arg.StaticFunctionArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.DoubleConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.FloatConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.LongConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.NullConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.TypeConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayLoadArg;
import org.spongepowered.despector.ast.members.insn.arg.field.FieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalArg;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.AddArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.DivideArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.MultiplyArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.NegArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.RemainderArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftLeftArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.UnsignedShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.AndArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.OrArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.XorArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssign;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssign;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssign;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssign;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssign;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodCall;
import org.spongepowered.despector.ast.members.insn.function.NewInstance;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodCall;
import org.spongepowered.despector.ast.members.insn.misc.IncrementStatement;
import org.spongepowered.despector.ast.members.insn.misc.ReturnValue;
import org.spongepowered.despector.ast.members.insn.misc.ReturnVoid;
import org.spongepowered.despector.ast.members.insn.misc.ThrowException;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpcodeDecompiler {

    @SuppressWarnings("unchecked")
    public static StatementBlock decompile(InsnList instructions, Locals locals, List<TryCatchBlockNode> tryCatchBlocks, DecompilerOptions options) {
        List<AbstractInsnNode> ops = Lists.newArrayList();
        Iterator<AbstractInsnNode> it = instructions.iterator();
        while (it.hasNext()) {
            ops.add(it.next());
        }
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD, locals);

        Map<Label, Integer> label_indices = Maps.newHashMap();
        for (int index = 0; index < ops.size(); index++) {
            AbstractInsnNode next = ops.get(index);
            if (next instanceof LabelNode) {
                label_indices.put(((LabelNode) next).getLabel(), index);
            }
        }
        locals.bakeInstances(label_indices);

        Deque<Instruction> stack = Queues.newArrayDeque();
        Map<Label, Integer> label_block_indices = Maps.newHashMap();
        Deque<StatementBlock> block_stack = Queues.newArrayDeque();
        Deque<LabelNode> block_ends = Queues.newArrayDeque();

        for (int index = 0; index < ops.size(); index++) {
            AbstractInsnNode next = ops.get(index);
            if (next instanceof LabelNode) {
                label_block_indices.put(((LabelNode) next).getLabel(), block.getStatements().size());
                if (stack.peek() instanceof JumpIntermediate) {
                    JumpIntermediate jmp = (JumpIntermediate) stack.pop();
                    List<JumpIntermediate> conditions = Lists.newArrayList();
                    conditions.add(jmp);
                    while (stack.peek() instanceof JumpIntermediate) {
                        JumpIntermediate next_jmp = (JumpIntermediate) stack.pop();
                        if (jmp.jump.label != jmp.jump.label) {
                            stack.push(next_jmp);
                            break;
                        }
                        conditions.add(next_jmp);
                    }
                    block_ends.push(jmp.jump.label);
                } else if (block_ends.peek() == next) {
                    block = block_stack.pop();
                }
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
                stack.push(new NullConstantArg());
                break;
            case ICONST_M1:
                stack.push(new IntConstantArg(-1));
                break;
            case ICONST_0:
                stack.push(new IntConstantArg(0));
                break;
            case ICONST_1:
                stack.push(new IntConstantArg(1));
                break;
            case ICONST_2:
                stack.push(new IntConstantArg(2));
                break;
            case ICONST_3:
                stack.push(new IntConstantArg(3));
                break;
            case ICONST_4:
                stack.push(new IntConstantArg(4));
                break;
            case ICONST_5:
                stack.push(new IntConstantArg(5));
                break;
            case LCONST_0:
                stack.push(new LongConstantArg(0));
                break;
            case LCONST_1:
                stack.push(new LongConstantArg(1));
                break;
            case FCONST_0:
                stack.push(new FloatConstantArg(0));
                break;
            case FCONST_1:
                stack.push(new FloatConstantArg(1));
                break;
            case FCONST_2:
                stack.push(new FloatConstantArg(2));
                break;
            case DCONST_0:
                stack.push(new DoubleConstantArg(0));
                break;
            case DCONST_1:
                stack.push(new DoubleConstantArg(1));
                break;
            case BIPUSH:
            case SIPUSH: {
                IntInsnNode insn = (IntInsnNode) next;
                stack.push(new IntConstantArg(insn.operand));
                break;
            }
            case LDC: {
                LdcInsnNode ldc = (LdcInsnNode) next;
                if (ldc.cst instanceof String) {
                    stack.push(new StringConstantArg((String) ldc.cst));
                } else if (ldc.cst instanceof Integer) {
                    stack.push(new IntConstantArg((Integer) ldc.cst));
                } else if (ldc.cst instanceof Float) {
                    stack.push(new FloatConstantArg((Float) ldc.cst));
                } else if (ldc.cst instanceof Long) {
                    // LDC_W appears to be merged with this opcode by asm so long
                    // and double constants will also be here
                    stack.push(new LongConstantArg((Long) ldc.cst));
                } else if (ldc.cst instanceof Double) {
                    stack.push(new DoubleConstantArg((Double) ldc.cst));
                } else if (ldc.cst instanceof Type) {
                    stack.push(new TypeConstantArg((Type) ldc.cst));
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
                stack.push(new LocalArg(local.getInstance(index)));
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
                stack.push(new ArrayLoadArg(var, index_arg));
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
                block.append(new LocalAssign(local.getInstance(index), val));
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
                if (var instanceof NewArrayArg) {
                    NewArrayArg array = (NewArrayArg) var;
                    if (array.getInitializer() == null) {
                        array.setInitialValues(new Instruction[((IntConstantArg) array.getSize()).getConstant()]);
                    }
                    array.getInitializer()[((IntConstantArg) index_arg).getConstant()] = val;
                    break;
                }
                block.append(new ArrayAssign(var, index_arg, val));
                break;
            }
            case POP: {
                Instruction arg = stack.pop();
                if (arg instanceof InstanceFunctionArg) {
                    block.append(new InstanceMethodCall((InstanceFunctionArg) arg));
                } else if (arg instanceof StaticFunctionArg) {
                    block.append(new StaticMethodCall((StaticFunctionArg) arg));
                }
                break;
            }
            case POP2: {
                for (int i = 0; i < 2; i++) {
                    Instruction arg = stack.pop();
                    if (arg instanceof InstanceFunctionArg) {
                        block.append(new InstanceMethodCall((InstanceFunctionArg) arg));
                    } else if (arg instanceof StaticFunctionArg) {
                        block.append(new StaticMethodCall((StaticFunctionArg) arg));
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
                stack.push(new AddArg(left, right));
                break;
            }
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new SubtractArg(left, right));
                break;
            }
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new MultiplyArg(left, right));
                break;
            }
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new DivideArg(left, right));
                break;
            }
            case IREM:
            case LREM:
            case FREM:
            case DREM: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new RemainderArg(left, right));
                break;
            }
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG: {
                Instruction right = stack.pop();
                stack.push(new NegArg(right));
                break;
            }
            case ISHL:
            case LSHL: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new ShiftLeftArg(left, right));
                break;
            }
            case ISHR:
            case LSHR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new ShiftRightArg(left, right));
                break;
            }
            case IUSHR:
            case LUSHR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new UnsignedShiftRightArg(left, right));
                break;
            }
            case IAND:
            case LAND: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new AndArg(left, right));
                break;
            }
            case IOR:
            case LOR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new OrArg(left, right));
                break;
            }
            case IXOR:
            case LXOR: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new XorArg(left, right));
                break;
            }
            case IINC: {
                IincInsnNode inc = (IincInsnNode) next;
                Local local = locals.getLocal(inc.var);
                IncrementStatement insn = new IncrementStatement(new DummyLocalInstance(local), inc.incr);
                block.append(insn);
                break;
            }
            case L2I:
            case F2I:
            case D2I:
                stack.push(new CastArg("I", stack.pop()));
                break;
            case I2L:
            case F2L:
            case D2L:
                stack.push(new CastArg("J", stack.pop()));
                break;
            case I2F:
            case L2F:
            case D2F:
                stack.push(new CastArg("F", stack.pop()));
                break;
            case I2D:
            case F2D:
            case L2D:
                stack.push(new CastArg("D", stack.pop()));
                break;
            case I2B:
                stack.push(new CastArg("B", stack.pop()));
                break;
            case I2C:
                stack.push(new CastArg("C", stack.pop()));
                break;
            case I2S:
                stack.push(new CastArg("S", stack.pop()));
                break;
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG: {
                Instruction right = stack.pop();
                Instruction left = stack.pop();
                stack.push(new CompareArg(left, right));
                break;
            }
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE: {
                Instruction val = stack.pop();
                stack.push(new JumpIntermediate((JumpInsnNode) next, val));
                break;
            }
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
                throw new IllegalStateException();
                // TODO
                // break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                block.append(new ReturnValue(stack.pop()));
                break;
            case RETURN:
                block.append(new ReturnVoid());
                break;
            case GETSTATIC: {
                FieldInsnNode field = (FieldInsnNode) next;
                String owner = field.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldArg arg = new StaticFieldArg(field.name, field.desc, owner);
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
                FieldAssign assign = new StaticFieldAssign(field.name, field.desc, owner, val);
                block.append(assign);
                break;
            }
            case GETFIELD: {
                FieldInsnNode field = (FieldInsnNode) next;
                String owner = field.owner;
                if (!owner.startsWith("[")) {
                    owner = "L" + owner + ";";
                }
                FieldArg arg = new InstanceFieldArg(field.name, field.desc, owner, stack.pop());
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
                FieldAssign assign = new InstanceFieldAssign(field.name, field.desc, owner_t, owner, val);
                block.append(assign);
                break;
            }
            case INVOKESPECIAL: {
                MethodInsnNode ctor = (MethodInsnNode) next;
                if (ctor.name.equals("<init>")) {
                    Instruction[] args = new Instruction[TypeHelper.paramCount(ctor.desc)];
                    for (int i = 0; i < args.length; i++) {
                        args[i] = stack.pop();
                    }
                    NewRefArg new_arg = (NewRefArg) stack.pop();
                    if (stack.peek() instanceof NewRefArg) {
                        NewRefArg new_arg2 = (NewRefArg) stack.pop();
                        if (new_arg2 == new_arg) {
                            new_arg.setCtorDescription(ctor.desc);
                            new_arg.setParameters(args);
                            stack.push(new_arg);
                            break;
                        }
                        stack.push(new_arg2);
                    }
                    NewInstance insn = new NewInstance(new_arg.getType(), ctor.desc, args);
                    block.append(insn);
                    break;
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
                if (ret.equals("V")) {
                    block.append(new InstanceMethodCall(method.name, method.desc, owner, args, callee));
                } else {
                    InstanceFunctionArg arg = new InstanceFunctionArg(method.name, method.desc, owner, args, callee);
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
                if (ret.equals("V")) {
                    block.append(new StaticMethodCall(method.name, method.desc, owner, args));
                } else {
                    StaticFunctionArg arg = new StaticFunctionArg(method.name, method.desc, owner, args);
                    stack.push(arg);
                }
                break;
            }
            case INVOKEDYNAMIC:
                // TODO
                break;
            case NEW: {
                String type = ((TypeInsnNode) next).desc;
                stack.push(new NewRefArg("L" + type + ";", null, null));
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
                stack.push(new NewArrayArg(array_type, size, null));
                break;
            }
            case ARRAYLENGTH:
                stack.push(new InstanceFieldArg("length", "I", "hidden-array-field", stack.pop()));
                break;
            case ATHROW:
                block.append(new ThrowException(stack.pop()));
                break;
            case CHECKCAST: {
                TypeInsnNode cast = (TypeInsnNode) next;
                String desc = cast.desc;
                if (!desc.startsWith("[")) {
                    desc = "L" + desc + ";";
                }
                stack.push(new CastArg(desc, stack.pop()));
                break;
            }
            case INSTANCEOF: {
                TypeInsnNode insn = (TypeInsnNode) next;
                Instruction val = stack.pop();
                String type = insn.desc;
                if (!type.startsWith("[")) {
                    type = "L" + insn.desc + ";";
                }
                stack.push(new InstanceOfArg(val, type));
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

        return block;
    }

}

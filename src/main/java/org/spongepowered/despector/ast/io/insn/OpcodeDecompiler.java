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
import org.spongepowered.despector.ast.members.insn.branch.IfBlock;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition.CompareOp;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodCall;
import org.spongepowered.despector.ast.members.insn.function.NewInstance;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodCall;
import org.spongepowered.despector.ast.members.insn.misc.IncrementStatement;
import org.spongepowered.despector.ast.members.insn.misc.ReturnValue;
import org.spongepowered.despector.ast.members.insn.misc.ReturnVoid;
import org.spongepowered.despector.ast.members.insn.misc.ThrowException;
import org.spongepowered.despector.util.AstUtil;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpcodeDecompiler {

    @SuppressWarnings("unchecked")
    public static StatementBlock decompile(InsnList instructions, Locals locals, List<TryCatchBlockNode> tryCatchBlocks, DecompilerOptions options) {
        List<AbstractInsnNode> ops = Lists.newArrayList(instructions.iterator());
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD, locals);

        Map<Label, Integer> label_indices = Maps.newHashMap();
        for (int index = 0; index < ops.size(); index++) {
            AbstractInsnNode next = ops.get(index);
            if (next instanceof LabelNode) {
                label_indices.put(((LabelNode) next).getLabel(), index);
            }
        }
        locals.bakeInstances(label_indices);

        List<OpcodeBlock> graph = makeGraph(ops, label_indices);
        cleanupGraph(graph);

        for (OpcodeBlock op : graph) {
            op.print();
        }

        List<BlockSection> flat_graph = flattenGraph(graph);

        for (BlockSection op : flat_graph) {
            appendBlock(op, block);
        }

        return block;
    }

    private static List<OpcodeBlock> makeGraph(List<AbstractInsnNode> instructions, Map<Label, Integer> label_indices) {
        Set<Integer> break_points = new HashSet<>();

        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode next = instructions.get(i);
            if (next instanceof JumpInsnNode) {
                break_points.add(i);
                break_points.add(label_indices.get(((JumpInsnNode) next).label.getLabel()));
                continue;
            }
            int op = next.getOpcode();
            if (op <= RETURN && op >= IRETURN) {
                break_points.add(i);
            }
        }

        List<Integer> sorted_break_points = new ArrayList<>(break_points);
        sorted_break_points.sort(Comparator.naturalOrder());
        Map<Integer, OpcodeBlock> blocks = new HashMap<>();
        List<OpcodeBlock> block_list = new ArrayList<>();

        int last_brk = 0;
        for (int brk : sorted_break_points) {
            OpcodeBlock block = new OpcodeBlock();
            block_list.add(block);
            block.break_point = brk;
            for (int i = last_brk; i < brk; i++) {
                block.opcodes.add(instructions.get(i));
            }
            AbstractInsnNode last = instructions.get(brk);
            block.last = last;
            blocks.put(brk, block);
            last_brk = brk + 1;
        }

        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            OpcodeBlock block = e.getValue();
            if (block.last instanceof LabelNode) {
                OpcodeBlock next = blocks.get(sorted_break_points.get(sorted_break_points.indexOf(e.getKey()) + 1));
                block.last = null;
                block.target = next;
            } else if (block.last instanceof JumpInsnNode) {
                Label label = ((JumpInsnNode) block.last).label.getLabel();
                block.target = blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label_indices.get(label)) + 1));
                if (block.last.getOpcode() != GOTO) {
                    OpcodeBlock next = blocks.get(sorted_break_points.get(sorted_break_points.indexOf(e.getKey()) + 1));
                    block.else_target = next;
                }
            }
        }

        return block_list;
    }

    private static void cleanupGraph(List<OpcodeBlock> blocks) {
        for (Iterator<OpcodeBlock> it = blocks.iterator(); it.hasNext();) {
            OpcodeBlock block = it.next();
            if (block.opcodes.isEmpty() && block.last == null) {
                // some conditions can create an empty block as a breakpoint
                // gets inserted on either side of a label immediately following
                // a jump

                for (OpcodeBlock other : blocks) {
                    if (other == block) {
                        continue;
                    }
                    if (other.target == block) {
                        other.target = block.target;
                    }
                    if (other.else_target == block) {
                        other.else_target = block.target;
                    }
                }
                it.remove();
            }
        }
    }

    private static List<BlockSection> flattenGraph(List<OpcodeBlock> blocks) {
        List<BlockSection> final_blocks = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            OpcodeBlock next = blocks.get(i);

            if (next.isConditional()) {
                IfBlockSection ifblock = new IfBlockSection(next);
                OpcodeBlock current = next;
                Deque<OpcodeBlock> condition_blocks = new ArrayDeque<>();

                OpcodeBlock target = current.target;
                condition_blocks.push(current);
                current = current.else_target;
                int possible = -1;
                OpcodeBlock old_target = null;
                while (current.isConditional()) {
                    if (current == old_target) {
                        possible = -1;
                        break;
                    } else if (target != current.target && target != current.else_target && possible != -1) {
                        break;
                    } else if (target == current.else_target) {
                        if(target.isConditional()) {
                            target = target.target;
                        } else {
                            condition_blocks.push(current);
                            break;
                        }
                    } else {
                        possible = condition_blocks.size();
                        old_target = target;
                        target = current.target;
                    }
                    condition_blocks.push(current);
                    current = current.else_target;
                }
                if (possible != -1) {
                    while (condition_blocks.size() > possible) {
                        condition_blocks.pop();
                    }
                    target = old_target;
                }
                OpcodeBlock else_target = condition_blocks.peek().else_target;
                if (else_target == target) {
                    else_target = condition_blocks.peek().target;
                }
                ifblock.condition = new ConditionBlock(condition_blocks.pop());
                if (target.break_point < else_target.break_point) {
                    OpcodeBlock t = else_target;
                    else_target = target;
                    target = t;
                    ifblock.condition.inverted = true;
                }
                // target is always break
                // else_target is always body
                for (OpcodeBlock cond_block : condition_blocks) {
                    if (cond_block.target == target) {
                        ifblock.condition = new AndConditionPart(new ConditionBlock(cond_block), ifblock.condition);
                    } else {
                        ifblock.condition.inverted = !ifblock.condition.inverted;
                        ConditionBlock new_block = new ConditionBlock(cond_block);
                        new_block.inverted = true;
                        ifblock.condition = new OrConditionPart(new_block, ifblock.condition);
                    }
                }
                // TODO this isn't right at all, need to actually figure out
                // which of target and else_target is the body and which is the
                // break
                if (else_target.break_point > next.break_point) {
                    i = blocks.indexOf(else_target);
                    // if-statement
                    List<OpcodeBlock> body = new ArrayList<>();
                    for (;; i++) {
                        OpcodeBlock n = blocks.get(i);
                        if (target == n) {
                            i--;
                            break;
                        }
                        body.add(n);
                    }
                    ifblock.body.addAll(flattenGraph(body));

                    final_blocks.add(ifblock);
                }
            } else {
                final_blocks.add(new InlineBlockSection(next));
            }
        }

        return final_blocks;
    }

    private static Condition buildCondition(ConditionPart part, StatementBlock block, Deque<Instruction> stack) {
        Condition condition = null;
        if (part instanceof OrConditionPart) {
            Condition left = buildCondition(((OrConditionPart) part).left, block, stack);
            Condition right = buildCondition(((OrConditionPart) part).right, block, stack);
            if (part.inverted) {
                condition = new AndCondition(new InverseCondition(left), new InverseCondition(right));
            } else {
                condition = new OrCondition(left, right);
            }
        } else if (part instanceof AndConditionPart) {
            Condition left = buildCondition(((AndConditionPart) part).left, block, stack);
            Condition right = buildCondition(((AndConditionPart) part).right, block, stack);
            if (part.inverted) {
                condition = new OrCondition(new InverseCondition(left), new InverseCondition(right));
            } else {
                condition = new AndCondition(left, right);
            }
        } else if (part instanceof ConditionBlock) {
            OpcodeBlock op = ((ConditionBlock) part).block;
            appendBlock(op, block, block.getLocals(), stack);
            int jump_op = op.last.getOpcode();
            if (jump_op >= IF_ICMPEQ && jump_op <= IF_ACMPNE) {
                Instruction right = stack.pop();
                condition = new CompareCondition(stack.pop(), right, CompareCondition.fromOpcode(jump_op));
            } else if (jump_op == IFNULL || jump_op == IFNONNULL) {
                condition = new CompareCondition(stack.pop(), new NullConstantArg(), jump_op == IFNULL ? CompareOp.NOT_EQUAL : CompareOp.EQUAL);
            } else {
                Instruction val = stack.pop();
                if (val.inferType().equals("Z")) {
                    boolean inverted = CompareCondition.fromOpcode(jump_op) == CompareOp.NOT_EQUAL;
                    if (part.inverted) {
                        inverted = !inverted;
                    }
                    condition = new BooleanCondition(val, inverted);
                } else {
                    condition = new CompareCondition(val, new IntConstantArg(0), CompareCondition.fromOpcode(jump_op));
                }
            }
            if (!stack.isEmpty()) {
                throw new IllegalStateException("Condition building did not empty stack");
            }
        }
        return condition;
    }

    private static void appendBlock(BlockSection block_section, StatementBlock block) {
        Deque<Instruction> stack = Queues.newArrayDeque();

        if (block_section instanceof IfBlockSection) {
            IfBlockSection ifblock = (IfBlockSection) block_section;
            Condition condition = buildCondition(ifblock.condition, block, stack);
            StatementBlock body = new StatementBlock(StatementBlock.Type.IF, block.getLocals());
            for (BlockSection body_section : ifblock.body) {
                appendBlock(body_section, body);
            }
            IfBlock iff = new IfBlock(condition, body);
            block.append(iff);
        } else if (block_section instanceof InlineBlockSection) {
            OpcodeBlock op = ((InlineBlockSection) block_section).block;
            appendBlock(op, block, block.getLocals(), stack);
        }
    }

    private static void appendBlock(OpcodeBlock op, StatementBlock block, Locals locals, Deque<Instruction> stack) {
        for (int index = 0; index < op.opcodes.size() + 1; index++) {
            int label_index = op.break_point - (op.opcodes.size() - index);
            AbstractInsnNode next;
            if (index < op.opcodes.size()) {
                next = op.opcodes.get(index);
            } else if (op.isReturn()) {
                next = op.last;
            } else {
                break;
            }
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
                    // LDC_W appears to be merged with this opcode by asm so
                    // long
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
                stack.push(new LocalArg(local.getInstance(label_index)));
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
                block.append(new LocalAssign(local.getInstance(label_index), val));
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
                IncrementStatement insn = new IncrementStatement(local.getInstance(label_index), inc.incr);
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
    }

    private static class OpcodeBlock {

        public int break_point;
        public final List<AbstractInsnNode> opcodes = new ArrayList<>();
        public AbstractInsnNode last;
        public OpcodeBlock target;
        public OpcodeBlock else_target;

        public OpcodeBlock() {

        }

        public boolean isJump() {
            return this.last != null && this.last instanceof JumpInsnNode;
        }

        public boolean isReturn() {
            return this.last != null && this.last.getOpcode() >= IRETURN && this.last.getOpcode() <= RETURN;
        }

        public boolean isConditional() {
            return this.else_target != null;
        }

        public void print() {
            System.out.println("Block " + this.break_point + ":");
            for (AbstractInsnNode op : this.opcodes) {
                System.out.println(AstUtil.insnToString(op));
            }
            System.out.println("Last: " + (this.last != null ? AstUtil.insnToString(this.last) : "null"));
            System.out.println("Target: " + (this.target != null ? this.target.break_point : -1));
            System.out.println("Else Target: " + (this.else_target != null ? this.else_target.break_point : -1));
        }

    }

    private static abstract class BlockSection {

        public BlockSection() {
        }

    }

    private static class InlineBlockSection extends BlockSection {

        public OpcodeBlock block;

        public InlineBlockSection(OpcodeBlock block) {
            this.block = block;
        }
    }

    private static abstract class ConditionPart {

        public boolean inverted = false;
    }

    private static class ConditionBlock extends ConditionPart {

        public OpcodeBlock block;

        public ConditionBlock(OpcodeBlock op) {
            this.block = op;
        }
    }

    private static class OrConditionPart extends ConditionPart {

        public ConditionPart left;
        public ConditionPart right;

        public OrConditionPart(ConditionPart l, ConditionPart r) {
            this.left = l;
            this.right = r;
        }
    }

    private static class AndConditionPart extends ConditionPart {

        public ConditionPart left;
        public ConditionPart right;

        public AndConditionPart(ConditionPart l, ConditionPart r) {
            this.left = l;
            this.right = r;
        }
    }

    private static class IfBlockSection extends BlockSection {

        public ConditionPart condition;
        public List<BlockSection> body = new ArrayList<>();

        public IfBlockSection(ConditionPart cond) {
            this.condition = cond;
        }

        public IfBlockSection(OpcodeBlock cond) {
            this.condition = new ConditionBlock(cond);
        }
    }

}

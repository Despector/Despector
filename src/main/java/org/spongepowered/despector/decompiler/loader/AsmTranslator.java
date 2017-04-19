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
package org.spongepowered.despector.decompiler.loader;

import static org.objectweb.asm.Opcodes.*;

import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.despector.decompiler.ir.DoubleInsn;
import org.spongepowered.despector.decompiler.ir.FieldInsn;
import org.spongepowered.despector.decompiler.ir.FloatInsn;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.IntInsn;
import org.spongepowered.despector.decompiler.ir.InvokeDynamicInsn;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.ir.LdcInsn;
import org.spongepowered.despector.decompiler.ir.LongInsn;
import org.spongepowered.despector.decompiler.ir.MethodInsn;
import org.spongepowered.despector.decompiler.ir.OpInsn;
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
import org.spongepowered.despector.decompiler.ir.TypeInsn;
import org.spongepowered.despector.decompiler.ir.VarIntInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.PartialMethod.TryCatchRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsmTranslator {

    private static final int[] op_translation = new int[256];

    static {
        op_translation[NOP] = Insn.NOOP;
        op_translation[ICONST_M1] = Insn.ICONST;
        op_translation[ICONST_0] = Insn.ICONST;
        op_translation[ICONST_1] = Insn.ICONST;
        op_translation[ICONST_2] = Insn.ICONST;
        op_translation[ICONST_3] = Insn.ICONST;
        op_translation[ICONST_4] = Insn.ICONST;
        op_translation[ICONST_5] = Insn.ICONST;
        op_translation[LCONST_0] = Insn.LCONST;
        op_translation[LCONST_1] = Insn.LCONST;
        op_translation[FCONST_0] = Insn.FCONST;
        op_translation[FCONST_1] = Insn.FCONST;
        op_translation[FCONST_2] = Insn.FCONST;
        op_translation[DCONST_0] = Insn.DCONST;
        op_translation[DCONST_1] = Insn.DCONST;
        op_translation[BIPUSH] = Insn.ICONST;
        op_translation[SIPUSH] = Insn.ICONST;
        op_translation[LDC] = Insn.PUSH;
        op_translation[ILOAD] = Insn.LOCAL_LOAD;
        op_translation[LLOAD] = Insn.LOCAL_LOAD;
        op_translation[FLOAD] = Insn.LOCAL_LOAD;
        op_translation[DLOAD] = Insn.LOCAL_LOAD;
        op_translation[ALOAD] = Insn.LOCAL_LOAD;
        op_translation[IALOAD] = Insn.ARRAY_LOAD;
        op_translation[LALOAD] = Insn.ARRAY_LOAD;
        op_translation[FALOAD] = Insn.ARRAY_LOAD;
        op_translation[DALOAD] = Insn.ARRAY_LOAD;
        op_translation[AALOAD] = Insn.ARRAY_LOAD;
        op_translation[BALOAD] = Insn.ARRAY_LOAD;
        op_translation[CALOAD] = Insn.ARRAY_LOAD;
        op_translation[SALOAD] = Insn.ARRAY_LOAD;
        op_translation[ISTORE] = Insn.LOCAL_STORE;
        op_translation[LSTORE] = Insn.LOCAL_STORE;
        op_translation[FSTORE] = Insn.LOCAL_STORE;
        op_translation[DSTORE] = Insn.LOCAL_STORE;
        op_translation[ASTORE] = Insn.LOCAL_STORE;
        op_translation[IASTORE] = Insn.ARRAY_STORE;
        op_translation[LASTORE] = Insn.ARRAY_STORE;
        op_translation[FASTORE] = Insn.ARRAY_STORE;
        op_translation[DASTORE] = Insn.ARRAY_STORE;
        op_translation[AASTORE] = Insn.ARRAY_STORE;
        op_translation[BASTORE] = Insn.ARRAY_STORE;
        op_translation[CASTORE] = Insn.ARRAY_STORE;
        op_translation[SASTORE] = Insn.ARRAY_STORE;
        op_translation[POP] = Insn.POP;
        op_translation[DUP] = Insn.DUP;
        op_translation[DUP_X1] = Insn.DUP_X1;
        op_translation[DUP_X2] = Insn.DUP_X2;
        op_translation[DUP2] = Insn.DUP2;
        op_translation[DUP2_X1] = Insn.DUP2_X1;
        op_translation[DUP2_X2] = Insn.DUP2_X2;
        op_translation[SWAP] = Insn.SWAP;
        op_translation[IADD] = Insn.ADD;
        op_translation[LADD] = Insn.ADD;
        op_translation[FADD] = Insn.ADD;
        op_translation[DADD] = Insn.ADD;
        op_translation[ISUB] = Insn.SUB;
        op_translation[LSUB] = Insn.SUB;
        op_translation[FSUB] = Insn.SUB;
        op_translation[DSUB] = Insn.SUB;
        op_translation[IMUL] = Insn.MUL;
        op_translation[LMUL] = Insn.MUL;
        op_translation[FMUL] = Insn.MUL;
        op_translation[DMUL] = Insn.MUL;
        op_translation[IDIV] = Insn.DIV;
        op_translation[LDIV] = Insn.DIV;
        op_translation[FDIV] = Insn.DIV;
        op_translation[DDIV] = Insn.DIV;
        op_translation[IREM] = Insn.REM;
        op_translation[LREM] = Insn.REM;
        op_translation[FREM] = Insn.REM;
        op_translation[DREM] = Insn.REM;
        op_translation[INEG] = Insn.NEG;
        op_translation[LNEG] = Insn.NEG;
        op_translation[FNEG] = Insn.NEG;
        op_translation[DNEG] = Insn.NEG;
        op_translation[ISHL] = Insn.SHL;
        op_translation[LSHL] = Insn.SHL;
        op_translation[ISHR] = Insn.SHR;
        op_translation[LSHR] = Insn.SHR;
        op_translation[IUSHR] = Insn.USHR;
        op_translation[LUSHR] = Insn.USHR;
        op_translation[IAND] = Insn.AND;
        op_translation[LAND] = Insn.AND;
        op_translation[IOR] = Insn.OR;
        op_translation[LOR] = Insn.OR;
        op_translation[IXOR] = Insn.XOR;
        op_translation[LXOR] = Insn.XOR;
        op_translation[IINC] = Insn.IINC;
        op_translation[IFEQ] = Insn.IFEQ;
        op_translation[IFNE] = Insn.IFNE;
        op_translation[IFLT] = Insn.IF_CMPLT;
        op_translation[IFGT] = Insn.IF_CMPGT;
        op_translation[IFLE] = Insn.IF_CMPLE;
        op_translation[IFGE] = Insn.IF_CMPGE;
        op_translation[IF_ICMPEQ] = Insn.IF_CMPEQ;
        op_translation[IF_ICMPNE] = Insn.IF_CMPNE;
        op_translation[IF_ICMPLT] = Insn.IF_CMPLT;
        op_translation[IF_ICMPLE] = Insn.IF_CMPLE;
        op_translation[IF_ICMPGT] = Insn.IF_CMPGT;
        op_translation[IF_ICMPGE] = Insn.IF_CMPGE;
        op_translation[IF_ACMPEQ] = Insn.IF_CMPEQ;
        op_translation[IF_ACMPNE] = Insn.IF_CMPNE;
        op_translation[IFNULL] = Insn.IF_CMPEQ;
        op_translation[IFNONNULL] = Insn.IF_CMPNE;
        op_translation[GOTO] = Insn.GOTO;
        op_translation[IRETURN] = Insn.ARETURN;
        op_translation[LRETURN] = Insn.ARETURN;
        op_translation[FRETURN] = Insn.ARETURN;
        op_translation[DRETURN] = Insn.ARETURN;
        op_translation[ARETURN] = Insn.ARETURN;
        op_translation[RETURN] = Insn.RETURN;
        op_translation[GETSTATIC] = Insn.GETSTATIC;
        op_translation[PUTSTATIC] = Insn.PUTSTATIC;
        op_translation[GETFIELD] = Insn.GETFIELD;
        op_translation[PUTFIELD] = Insn.PUTFIELD;
        op_translation[INVOKEVIRTUAL] = Insn.INVOKE;
        op_translation[INVOKESPECIAL] = Insn.INVOKE;
        op_translation[INVOKESTATIC] = Insn.INVOKESTATIC;
        op_translation[INVOKEINTERFACE] = Insn.INVOKE;
        op_translation[NEW] = Insn.NEW;
        op_translation[NEWARRAY] = Insn.NEWARRAY;
        op_translation[ANEWARRAY] = Insn.NEWARRAY;
        op_translation[ATHROW] = Insn.THROW;
        op_translation[CHECKCAST] = Insn.CAST;
        op_translation[INSTANCEOF] = Insn.INSTANCEOF;
    }

    private static int translate(int op) {
        return op_translation[op];
    }

    @SuppressWarnings("unchecked")
    public static InsnBlock convert(PartialMethod partial, MethodNode asm) {
        InsnList asm_ops = asm.instructions;
        // TODO:
        // Not the most efficient conversion in the world, but this is just a
        // temporary shim until I finish the class loader which will replace asm
        // entirely.
        Map<Label, Integer> label_indices = Maps.newHashMap();
        InsnBlock block = new InsnBlock();
        List<Label> seen = new ArrayList<>();
        int index = 0;
        outer: for (int i = 0; i < asm_ops.size(); i++) {
            AbstractInsnNode next = asm_ops.get(i);
            if (next instanceof LabelNode) {
                label_indices.put(((LabelNode) next).getLabel(), index);
            } else if (next instanceof FrameNode || next instanceof LineNumberNode) {
            } else if (next instanceof FieldInsnNode) {
                FieldInsnNode node = (FieldInsnNode) next;
                block.append(new FieldInsn(translate(node.getOpcode()), node.owner, node.name, node.desc));
                index++;
            } else if (next instanceof IincInsnNode) {
                IincInsnNode iinc = (IincInsnNode) next;
                block.append(new VarIntInsn(Insn.IINC, iinc.var, iinc.incr));
                index++;
            } else if (next instanceof InsnNode) {
                InsnNode insn = (InsnNode) next;
                switch (insn.getOpcode()) {
                case L2I:
                case F2I:
                case D2I:
                    block.append(new TypeInsn(Insn.CAST, "I"));
                    break;
                case I2L:
                case F2L:
                case D2L:
                    block.append(new TypeInsn(Insn.CAST, "L"));
                    break;
                case I2F:
                case L2F:
                case D2F:
                    block.append(new TypeInsn(Insn.CAST, "F"));
                    break;
                case I2D:
                case L2D:
                case F2D:
                    block.append(new TypeInsn(Insn.CAST, "D"));
                    break;
                case I2B:
                    block.append(new TypeInsn(Insn.CAST, "B"));
                    break;
                case I2C:
                    block.append(new TypeInsn(Insn.CAST, "C"));
                    break;
                case I2S:
                    block.append(new TypeInsn(Insn.CAST, "S"));
                    break;
                case POP2:
                    block.append(new OpInsn(Insn.POP));
                    block.append(new OpInsn(Insn.POP));
                    index++;
                    break;
                case ARRAYLENGTH:
                    block.append(new FieldInsn(Insn.GETFIELD, "", "length", "I"));
                    break;
                case ICONST_M1:
                    block.append(new IntInsn(Insn.ICONST, -1));
                    break;
                case ICONST_0:
                    block.append(new IntInsn(Insn.ICONST, 0));
                    break;
                case ICONST_1:
                    block.append(new IntInsn(Insn.ICONST, 1));
                    break;
                case ICONST_2:
                    block.append(new IntInsn(Insn.ICONST, 2));
                    break;
                case ICONST_3:
                    block.append(new IntInsn(Insn.ICONST, 3));
                    break;
                case ICONST_4:
                    block.append(new IntInsn(Insn.ICONST, 4));
                    break;
                case ICONST_5:
                    block.append(new IntInsn(Insn.ICONST, 5));
                    break;
                case LCONST_0:
                    block.append(new LongInsn(Insn.LCONST, 0));
                    break;
                case LCONST_1:
                    block.append(new LongInsn(Insn.LCONST, 1));
                    break;
                case FCONST_0:
                    block.append(new FloatInsn(Insn.FCONST, 0));
                    break;
                case FCONST_1:
                    block.append(new FloatInsn(Insn.FCONST, 1));
                    break;
                case FCONST_2:
                    block.append(new FloatInsn(Insn.FCONST, 2));
                    break;
                case DCONST_0:
                    block.append(new DoubleInsn(Insn.DCONST, 0));
                    break;
                case DCONST_1:
                    block.append(new DoubleInsn(Insn.DCONST, 1));
                    break;
                case LCMP:
                case FCMPG:
                case FCMPL:
                case DCMPG:
                case DCMPL:
                    if (i < asm_ops.size() - 1 && asm_ops.get(i + 1) instanceof JumpInsnNode) {
                        JumpInsnNode next_jump = (JumpInsnNode) asm_ops.get(i + 1);
                        if (next_jump.getOpcode() == IFEQ) {
                            next_jump.setOpcode(IF_ICMPEQ);
                        } else if (next_jump.getOpcode() == IFNE) {
                            next_jump.setOpcode(IF_ICMPNE);
                        } else if (next_jump.getOpcode() == IFLT) {
                            next_jump.setOpcode(IF_ICMPLT);
                        } else if (next_jump.getOpcode() == IFGT) {
                            next_jump.setOpcode(IF_ICMPGT);
                        } else if (next_jump.getOpcode() == IFGE) {
                            next_jump.setOpcode(IF_ICMPGE);
                        } else if (next_jump.getOpcode() == IFLE) {
                            next_jump.setOpcode(IF_ICMPLE);
                        } else {
                            throw new IllegalStateException();
                        }
                        continue outer;
                    }
                case ACONST_NULL:
                    block.append(new LdcInsn(Insn.PUSH, null));
                    break;
                default:
                    block.append(new OpInsn(translate(insn.getOpcode())));
                    break;
                }
                index++;
            } else if (next instanceof IntInsnNode) {
                IntInsnNode insn = (IntInsnNode) next;
                if (insn.getOpcode() == BIPUSH || insn.getOpcode() == SIPUSH) {
                    block.append(new IntInsn(Insn.ICONST, insn.operand));
                } else if (insn.getOpcode() == NEWARRAY) {
                    switch (insn.operand) {
                    case T_BOOLEAN:
                        block.append(new TypeInsn(Insn.NEWARRAY, "Z"));
                        break;
                    case T_CHAR:
                        block.append(new TypeInsn(Insn.NEWARRAY, "C"));
                        break;
                    case T_FLOAT:
                        block.append(new TypeInsn(Insn.NEWARRAY, "F"));
                        break;
                    case T_DOUBLE:
                        block.append(new TypeInsn(Insn.NEWARRAY, "D"));
                        break;
                    case T_BYTE:
                        block.append(new TypeInsn(Insn.NEWARRAY, "B"));
                        break;
                    case T_SHORT:
                        block.append(new TypeInsn(Insn.NEWARRAY, "S"));
                        break;
                    case T_INT:
                        block.append(new TypeInsn(Insn.NEWARRAY, "I"));
                        break;
                    case T_LONG:
                        block.append(new TypeInsn(Insn.NEWARRAY, "J"));
                        break;
                    default:
                        throw new IllegalStateException("Unsupported type in NEWARRAY: " + insn.getOpcode());
                    }
                } else {
                    throw new IllegalStateException("Unsupported int insn: " + insn.getOpcode());
                }
                index++;
            } else if (next instanceof VarInsnNode) {
                VarInsnNode insn = (VarInsnNode) next;
                if (insn.getOpcode() >= ILOAD && insn.getOpcode() <= ALOAD) {
                    block.append(new IntInsn(Insn.LOCAL_LOAD, insn.var));
                } else {
                    block.append(new IntInsn(Insn.LOCAL_STORE, insn.var));
                }
                index++;
            } else if (next instanceof MethodInsnNode) {
                MethodInsnNode node = (MethodInsnNode) next;
                block.append(new MethodInsn(translate(node.getOpcode()), node.owner, node.name, node.desc));
                index++;
            } else if (next instanceof TypeInsnNode) {
                block.append(new TypeInsn(translate(next.getOpcode()), ((TypeInsnNode) next).desc));
                index++;
            } else if (next instanceof JumpInsnNode) {
                JumpInsnNode jump = (JumpInsnNode) next;
                if (jump.getOpcode() >= IFLT && jump.getOpcode() <= IFLE) {
                    block.append(new IntInsn(Insn.ICONST, 0));
                    index++;
                } else if (jump.getOpcode() == IFNULL || jump.getOpcode() == IFNONNULL) {
                    block.append(new LdcInsn(Insn.PUSH, null));
                    index++;
                }
                Label target = jump.label.getLabel();
                int t = seen.indexOf(target);
                if (t == -1) {
                    seen.add(target);
                    t = seen.size() - 1;
                }
                block.append(new JumpInsn(translate(jump.getOpcode()), t));
                index++;
            } else if (next instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode sw = (TableSwitchInsnNode) next;
                Map<Integer, Integer> targets = new HashMap<>();
                for (int j = 0; j < sw.labels.size(); j++) {
                    Label target = ((LabelNode) sw.labels.get(j)).getLabel();
                    int t = seen.indexOf(target);
                    if (t == -1) {
                        seen.add(target);
                        t = seen.size() - 1;
                    }
                    targets.put(sw.min + j, t);
                }
                Label target = sw.dflt.getLabel();
                int t = seen.indexOf(target);
                if (t == -1) {
                    seen.add(target);
                    t = seen.size() - 1;
                }
                block.append(new SwitchInsn(Insn.SWITCH, targets, t));
                index++;
            } else if (next instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode sw = (LookupSwitchInsnNode) next;
                Map<Integer, Integer> targets = new HashMap<>();
                for (int j = 0; j < sw.labels.size(); j++) {
                    Label target = ((LabelNode) sw.labels.get(j)).getLabel();
                    int t = seen.indexOf(target);
                    if (t == -1) {
                        seen.add(target);
                        t = seen.size() - 1;
                    }
                    targets.put((Integer) sw.keys.get(j), t);
                }
                Label target = sw.dflt.getLabel();
                int t = seen.indexOf(target);
                if (t == -1) {
                    seen.add(target);
                    t = seen.size() - 1;
                }
                block.append(new SwitchInsn(Insn.SWITCH, targets, t));
                index++;
            } else if (next instanceof LdcInsnNode) {
                LdcInsnNode ldc = (LdcInsnNode) next;
                if (ldc.cst instanceof String) {
                    block.append(new LdcInsn(Insn.PUSH, ldc.cst));
                } else if (ldc.cst instanceof Type) {
                    block.append(new LdcInsn(Insn.PUSH, ldc.cst));
                } else if (ldc.cst instanceof Integer) {
                    block.append(new IntInsn(Insn.ICONST, (Integer) ldc.cst));
                } else if (ldc.cst instanceof Long) {
                    block.append(new LongInsn(Insn.LCONST, (Long) ldc.cst));
                } else if (ldc.cst instanceof Float) {
                    block.append(new FloatInsn(Insn.FCONST, (Float) ldc.cst));
                } else if (ldc.cst instanceof Double) {
                    block.append(new DoubleInsn(Insn.DCONST, (Double) ldc.cst));
                } else {
                    throw new IllegalStateException("Unsupported asm ldc constant type " + ldc.cst.getClass().getName());
                }
                index++;
            } else if (next instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) next;
                Handle handle = (Handle) invoke.bsmArgs[1];
                block.append(
                        new InvokeDynamicInsn(Insn.INVOKEDYNAMIC, handle.getOwner(), handle.getName(), handle.getDesc(), invoke.name, invoke.desc));
                index++;
            } else {
                throw new IllegalStateException(next.getClass().getName());
            }
        }

        for (Insn insn : block) {
            if (insn instanceof JumpInsn) {
                JumpInsn jump = (JumpInsn) insn;
                int target = label_indices.get(seen.get(jump.getTarget()));
                jump.setTarget(target);
            } else if (insn instanceof SwitchInsn) {
                SwitchInsn s = (SwitchInsn) insn;
                Map<Integer, Integer> final_targets = new HashMap<>();
                for (Map.Entry<Integer, Integer> e : s.getTargets().entrySet()) {
                    int target = label_indices.get(seen.get(e.getValue()));
                    final_targets.put(e.getKey(), target);
                }
                s.getTargets().clear();
                s.getTargets().putAll(final_targets);
                int target = label_indices.get(seen.get(s.getDefault()));
                s.setDefault(target);
            }
        }

        for (TryCatchBlockNode node : (List<TryCatchBlockNode>) asm.tryCatchBlocks) {
            int start_pc = label_indices.get(node.start.getLabel());
            int end_pc = label_indices.get(node.end.getLabel());
            int catch_pc = label_indices.get(node.handler.getLabel());
            partial.getCatchRegions().add(new TryCatchRegion(start_pc, end_pc, catch_pc, node.type));
            // TODO annotations
        }

        partial.getLocals().bakeInstances(label_indices);
        return block;
    }

}

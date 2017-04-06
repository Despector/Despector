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
package org.spongepowered.despector.decompiler.kotlin.method.special;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.decompiler.method.special.LocalsProcessor;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinLocalsProcessor implements LocalsProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public void process(MethodNode asm, Locals locals) {
        List<AbstractInsnNode> ops = Lists.newArrayList(asm.instructions.iterator());
        InsnList insn = asm.instructions;
        VarInsnNode last = null;

        for (int i = 0; i < ops.size(); i++) {
            AbstractInsnNode next = ops.get(i);
            if (next.getOpcode() >= ILOAD && next.getOpcode() <= ALOAD) {
                last = (VarInsnNode) next;
            } else if (next.getOpcode() >= ISTORE && next.getOpcode() <= ASTORE && last != null) {
                VarInsnNode store = (VarInsnNode) next;
                int life = nextMod(ops, i + 1, store.var);
                if (life == -1) {
                    life = ops.size();
                }
                int last_read = lastRead(ops, i + 1, life, store.var);

                if (last_read == -1) {
                    // the local is never read before it is next written to
                    insn.remove(last);
                    insn.remove(next);
                    ops.remove(last);
                    ops.remove(next);
                    i -= 2;
                    last = null;
                    continue;
                }
                int invalid = nextMod(ops, i + 1, last.var);
                if (invalid == -1) {
                    invalid = ops.size();
                }

                if (invalid > last_read) {
                    invalid = Math.min(invalid, life);

                    for (int j = i + 1; j < invalid; j++) {
                        AbstractInsnNode n = ops.get(j);
                        if (n.getOpcode() >= ILOAD && n.getOpcode() <= ALOAD) {
                            VarInsnNode var = (VarInsnNode) n;
                            if (var.var == store.var) {
                                var.var = last.var;
                            }
                        }
                    }

                    insn.remove(last);
                    insn.remove(next);
                    ops.remove(last);
                    ops.remove(next);
                    i -= 2;
                }
                last = null;
            } else {
                last = null;
            }
        }

        Map<Label, List<Integer>> label_targets = new HashMap<>();
        for (int i = 0; i < ops.size(); i++) {
            AbstractInsnNode next = ops.get(i);
            if (next instanceof JumpInsnNode) {
                JumpInsnNode jump = (JumpInsnNode) next;
                List<Integer> target = label_targets.get(jump.label.getLabel());
                if (target == null) {
                    target = new ArrayList<>();
                    label_targets.put(jump.label.getLabel(), target);
                }
                target.add(i);
            }
        }

        outer: for (int i = 0; i < ops.size(); i++) {
            AbstractInsnNode next = ops.get(i);
            if (next.getOpcode() >= ISTORE && next.getOpcode() <= ASTORE) {
                VarInsnNode store = (VarInsnNode) next;
                int life = nextMod(ops, i + 1, store.var);
                if (life == -1) {
                    life = ops.size();
                }
                int reads = countReads(ops, i + 1, life, store.var, label_targets);
                if (reads == 1 && locals.getLocal(store.var).getLVT().isEmpty()) {
                    List<AbstractInsnNode> val = new ArrayList<>();
                    int required = -1;
                    for (int o = i - 1; o >= 0; o--) {
                        AbstractInsnNode prev = ops.get(o);
                        if (prev instanceof JumpInsnNode || prev instanceof LabelNode || prev instanceof FrameNode
                                || prev instanceof LineNumberNode) {
                            continue outer;
                        }
                        val.add(0, prev);
                        required += AstUtil.getStackDelta(prev);
                        if (required == 0) {
                            break;
                        }
                    }
                    if (val.size() > 1 && reads != 1) {
                        continue;
                    }
                    for (AbstractInsnNode v : val) {
                        insn.remove(v);
                        ops.remove(v);
                        i--;
                    }
                    insn.remove(next);
                    for (int o = i + 1; o < life; o++) {
                        AbstractInsnNode n = ops.get(o);
                        if (n.getOpcode() >= ILOAD && n.getOpcode() <= ALOAD) {
                            VarInsnNode var = (VarInsnNode) n;
                            if (var.var == store.var) {
                                for (AbstractInsnNode v : val) {
                                    insn.insertBefore(var, v.clone(null));
                                    ops.add(o++, v);
                                }
                                insn.remove(var);
                                ops.remove(var);
                                o--;
                                life--;
                            }
                        }
                    }
                }
            }
        }

    }

    public static int countReads(List<AbstractInsnNode> ops, int start, int end, int local, Map<Label, List<Integer>> label_targets) {
        int count = 0;
        for (int i = end - 1; i >= start; i--) {
            AbstractInsnNode next = ops.get(i);
            if (next.getOpcode() >= ILOAD && next.getOpcode() <= ALOAD) {
                VarInsnNode node = (VarInsnNode) next;
                if (node.var == local) {
                    count++;
                }
            } else if (next instanceof JumpInsnNode) {
                int target = ops.indexOf(((JumpInsnNode) next).label);
                if (target < start || target > end) {
                    return -1;
                }
            } else if (next instanceof LabelNode) {
                List<Integer> targets = label_targets.get(((LabelNode) next).getLabel());
                if (targets != null) {
                    for (int t : targets) {
                        if (t < start || t > end) {
                            return -1;
                        }
                    }
                }
            }
        }

        return count;
    }

    public static int lastRead(List<AbstractInsnNode> ops, int start, int end, int local) {

        for (int i = end - 1; i >= start; i--) {
            AbstractInsnNode next = ops.get(i);
            if (next.getOpcode() >= ILOAD && next.getOpcode() <= ALOAD) {
                VarInsnNode node = (VarInsnNode) next;
                if (node.var == local) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static int nextMod(List<AbstractInsnNode> ops, int start, int local) {

        for (int i = start; i < ops.size(); i++) {
            AbstractInsnNode next = ops.get(i);
            if (next.getOpcode() >= ISTORE && next.getOpcode() <= ASTORE) {
                VarInsnNode node = (VarInsnNode) next;
                if (node.var == local) {
                    return i;
                }
            }
        }

        return -1;
    }

}

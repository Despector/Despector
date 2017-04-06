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
package org.spongepowered.despector.decompiler.method.graph.process;

import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.CommentBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection.SwitchCaseBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.SwitchOpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A graph processor to process switch statements.
 */
public class SwitchBlockProcessor implements GraphProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public int process(PartialMethod partial, List<OpcodeBlock> blocks, OpcodeBlock region_start, List<BlockSection> final_blocks) {
        if (region_start instanceof SwitchOpcodeBlock) {
            SwitchOpcodeBlock sblock = (SwitchOpcodeBlock) region_start;
            List<LabelNode> labels = null;
            List<Integer> keys = null;
            LabelNode dflt = null;
            if (sblock.getLast() instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode ts = (TableSwitchInsnNode) sblock.getLast();
                labels = ts.labels;
                dflt = ts.dflt;
                keys = new ArrayList<>();
                for (int k = ts.min; k <= ts.max; k++) {
                    keys.add(k);
                }
            } else if (sblock.getLast() instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode ts = (LookupSwitchInsnNode) sblock.getLast();
                labels = ts.labels;
                dflt = ts.dflt;
                keys = ts.keys;
            }
            SwitchBlockSection sswitch = new SwitchBlockSection(region_start);
            final_blocks.add(sswitch);
            Map<Label, SwitchCaseBlockSection> cases = new HashMap<>();
            int index = 0;
            OpcodeBlock end = null;
            Label end_label = null;
            OpcodeBlock fartherst = null;
            int farthest_break = 0;
            boolean all_return = true;
            for (LabelNode l : labels) {
                SwitchCaseBlockSection cs = cases.get(l.getLabel());
                if (cs != null) {
                    cs.getTargets().add(keys.get(index++));
                    continue;
                }
                cs = sswitch.new SwitchCaseBlockSection();
                sswitch.addCase(cs);
                cases.put(l.getLabel(), cs);
                cs.getTargets().add(keys.get(index++));
                List<OpcodeBlock> case_region = new ArrayList<>();
                OpcodeBlock block = sblock.getAdditionalTargets().get(l.getLabel());
                case_region.add(block);
                int start = blocks.indexOf(block) + 1;
                if (start < blocks.size()) {
                    block = blocks.get(start);
                    while (!sblock.getAdditionalTargets().containsValue(block) && block != end) {
                        case_region.add(block);
                        start++;
                        if (start >= blocks.size()) {
                            break;
                        }
                        block = blocks.get(start);
                    }
                }

                OpcodeBlock last = case_region.get(case_region.size() - 1);
                if (last.getBreakpoint() > farthest_break) {
                    fartherst = last;
                    farthest_break = last.getBreakpoint();
                }
                if (last instanceof BodyOpcodeBlock) {
                    int op = AstUtil.getLastOpcode(last.getOpcodes()).getOpcode();
                    if (op < IRETURN || op > RETURN) {
                        all_return = false;
                    }
                } else {
                    all_return = false;
                }
                if (last instanceof GotoOpcodeBlock) {
                    end = last.getTarget();
                    end_label = ((JumpInsnNode) last.getLast()).label.getLabel();
                    case_region.remove(last);
                    cs.setBreaks(true);
                }
                try {
                    partial.getDecompiler().flattenGraph(partial, case_region, case_region.size(), cs.getBody());
                } catch (Throwable e) {
                    if (ConfigManager.getConfig().print_opcodes_on_error) {
                        List<String> comment = new ArrayList<>();
                        for (OpcodeBlock op : case_region) {
                            comment.add(op.getDebugHeader());
                            for (AbstractInsnNode insn : op.getOpcodes()) {
                                comment.add(AstUtil.insnToString(insn));
                            }
                        }
                        cs.getBody().add(new CommentBlockSection(comment));
                    } else {
                        throw e;
                    }
                }
            }
            SwitchCaseBlockSection cs = cases.get(dflt.getLabel());
            if (cs != null) {
                cs.setDefault(true);
            } else if (!all_return && end_label != dflt.getLabel()) {
                cs = sswitch.new SwitchCaseBlockSection();
                cases.put(dflt.getLabel(), cs);
                sswitch.addCase(cs);
                List<OpcodeBlock> case_region = new ArrayList<>();
                OpcodeBlock block = sblock.getAdditionalTargets().get(dflt.getLabel());
                case_region.add(block);
                int start = blocks.indexOf(block) + 1;
                if (start < blocks.size()) {
                    block = blocks.get(start);
                    while (!sblock.getAdditionalTargets().containsValue(block) && block != end) {
                        case_region.add(block);
                        start++;
                        if (start >= blocks.size()) {
                            break;
                        }
                        block = blocks.get(start);
                    }
                }
                OpcodeBlock last = case_region.get(case_region.size() - 1);
                if (last.getBreakpoint() > farthest_break) {
                    fartherst = last;
                    farthest_break = last.getBreakpoint();
                }
                cs.setDefault(true);
                try {
                    partial.getDecompiler().flattenGraph(partial, case_region, case_region.size(), cs.getBody());
                } catch (Exception e) {
                    if (ConfigManager.getConfig().print_opcodes_on_error) {
                        List<String> comment = new ArrayList<>();
                        for (OpcodeBlock op : case_region) {
                            comment.add(op.getDebugHeader());
                            for (AbstractInsnNode insn : op.getOpcodes()) {
                                comment.add(AstUtil.insnToString(insn));
                            }
                        }
                        cs.getBody().add(new CommentBlockSection(comment));
                    } else {
                        throw e;
                    }
                }
            }
            if (end == null) {
                return blocks.indexOf(fartherst);
            }
            return blocks.indexOf(end) - 1;
        }
        return -1;
    }

}

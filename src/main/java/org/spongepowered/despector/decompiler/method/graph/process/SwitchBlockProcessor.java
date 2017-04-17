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

import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.ir.SwitchInsn;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A graph processor to process switch statements.
 */
public class SwitchBlockProcessor implements GraphProcessor {

    @Override
    public int process(PartialMethod partial, List<OpcodeBlock> blocks, OpcodeBlock region_start, List<BlockSection> final_blocks) {
        if (region_start instanceof SwitchOpcodeBlock) {
            SwitchOpcodeBlock sblock = (SwitchOpcodeBlock) region_start;
            SwitchInsn ts = (SwitchInsn) sblock.getLast();
            SwitchBlockSection sswitch = new SwitchBlockSection(region_start);
            final_blocks.add(sswitch);
            Map<Integer, SwitchCaseBlockSection> cases = new HashMap<>();
            OpcodeBlock end = null;
            int end_label = -1;
            OpcodeBlock fartherst = null;
            int farthest_break = 0;
            boolean all_return = true;
            for (Map.Entry<Integer, Integer> l : ts.getTargets().entrySet()) {
                SwitchCaseBlockSection cs = cases.get(l.getValue());
                if (cs != null) {
                    cs.getTargets().add(l.getKey());
                    continue;
                }
                cs = sswitch.new SwitchCaseBlockSection();
                sswitch.addCase(cs);
                cases.put(l.getValue(), cs);
                cs.getTargets().add(l.getKey());
                List<OpcodeBlock> case_region = new ArrayList<>();
                OpcodeBlock block = sblock.getAdditionalTargets().get(l.getKey());
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
                    int op = last.getLast().getOpcode();
                    if (op < IRETURN || op > RETURN) {
                        all_return = false;
                    }
                } else {
                    all_return = false;
                }
                if (last instanceof GotoOpcodeBlock) {
                    end = last.getTarget();
                    end_label = ((JumpInsn) last.getLast()).getTarget();
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
                            for (Insn insn : op.getOpcodes()) {
                                comment.add(insn.toString());
                            }
                        }
                        cs.getBody().add(new CommentBlockSection(comment));
                    } else {
                        throw e;
                    }
                }
            }
            SwitchCaseBlockSection cs = cases.get(ts.getDefault());
            if (cs != null) {
                cs.setDefault(true);
            } else if (!all_return && end_label != ts.getDefault()) {
                cs = sswitch.new SwitchCaseBlockSection();
                cases.put(ts.getDefault(), cs);
                sswitch.addCase(cs);
                List<OpcodeBlock> case_region = new ArrayList<>();
                OpcodeBlock block = sblock.getAdditionalTargets().get(-1);
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
                            for (Insn insn : op.getOpcodes()) {
                                comment.add(insn.toString());
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

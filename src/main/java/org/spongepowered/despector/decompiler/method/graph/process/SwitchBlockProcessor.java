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
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
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
            // create block section for this switch
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
                    // if multiple targets for the same block we'll already have
                    // a case made for this target
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
                        // while we don't run into another case ass blocks to
                        // this case
                        // and we don't run into the end (which we'll find later
                        // based on
                        // the targets of the break statements).
                        case_region.add(block);
                        start++;
                        if (start >= blocks.size()) {
                            break;
                        }
                        block = blocks.get(start);
                    }
                }

                OpcodeBlock last = case_region.get(case_region.size() - 1);
                if (last.getStart() > farthest_break) {
                    // update the farthest block found
                    fartherst = last;
                    farthest_break = last.getStart();
                }
                if (last instanceof BodyOpcodeBlock) {
                    int op = last.getLast().getOpcode();
                    if (op != Insn.RETURN && op != Insn.ARETURN) {
                        // not the case that all cases return
                        all_return = false;
                    }
                } else {
                    all_return = false;
                }
                if (last instanceof GotoOpcodeBlock) {
                    // break statements become gotos, so if we find a goto
                    // at the end of the case we use its target to end the last
                    // case
                    end = last.getTarget();
                    GotoOpcodeBlock goto_block = (GotoOpcodeBlock) last;
                    end_label = ((JumpInsn) last.getLast()).getTarget();
                    case_region.remove(last);
                    cs.setBreaks(true);

                    for (OpcodeBlock o : case_region) {
                        if (o instanceof ConditionalOpcodeBlock) {
                            ConditionalOpcodeBlock c = (ConditionalOpcodeBlock) o;
                            if (c.getTarget() == goto_block.getTarget()) {
                                c.setTarget(goto_block);
                            }
                        }
                    }
                }
                try {
                    // recursively flatten the case area
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
                // set the case pointed to as default as the default block
                cs.setDefault(true);
            } else if (!all_return && end_label != ts.getDefault()) {
                // no block was pointed to as default, and they didn't all
                // return
                // (if they did all return then we have no way of telling where
                // the default case ends, and it doesn't matter that we emit it
                // anyway so we just ignore it and let it sit after the switch)
                // otherwise we build a new case for everything between the end
                // of our last case and the end block as the default block.
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
                if (last.getStart() > farthest_break) {
                    fartherst = last;
                    farthest_break = last.getStart();
                }
                cs.setDefault(true);
                try {
                    partial.getDecompiler().flattenGraph(partial, case_region, case_region.size(), cs.getBody());
                } catch (Exception e) {
                    // TODO: should make a util function for this, it appears in
                    // a lot of places
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
            if (!blocks.contains(end)) {
                return blocks.size();
            }
            // end points to the block after the last block which is part of
            // this switch so we subtract 1
            return blocks.indexOf(end) - 1;
        }
        return -1;
    }

}

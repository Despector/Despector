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
package org.spongepowered.despector.decompiler.method.graph.process;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection.SwitchCaseBlockSection;
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
                block = blocks.get(start);
                while (!sblock.getAdditionalTargets().containsValue(block) && block != end) {
                    case_region.add(block);
                    start++;
                    block = blocks.get(start);
                }

                OpcodeBlock last = case_region.get(case_region.size() - 1);
                if (last instanceof GotoOpcodeBlock) {
                    end = last.getTarget();
                    case_region.remove(last);
                    cs.setBreaks(true);
                }

                cs.getBody().addAll(partial.getDecompiler().flattenGraph(partial, case_region, case_region.size()));
            }
            SwitchCaseBlockSection cs = cases.get(dflt.getLabel());
            if (cs != null) {
                cs.setDefault(true);
                cs.getTargets().add(index);
            } else {
                cs = sswitch.new SwitchCaseBlockSection();
                cases.put(dflt.getLabel(), cs);
                sswitch.addCase(cs);
                List<OpcodeBlock> case_region = new ArrayList<>();
                OpcodeBlock block = sblock.getAdditionalTargets().get(dflt.getLabel());
                case_region.add(block);
                int start = blocks.indexOf(block) + 1;
                block = blocks.get(start);
                while (!sblock.getAdditionalTargets().containsValue(block) && block != end) {
                    case_region.add(block);
                    start++;
                    block = blocks.get(start);
                }
                cs.setDefault(true);
                cs.getBody().addAll(partial.getDecompiler().flattenGraph(partial, case_region, case_region.size()));
            }
            return blocks.indexOf(end) - 1;
        }
        return -1;
    }

}

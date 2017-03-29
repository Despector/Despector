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
package org.spongepowered.despector.decompiler.method.graph.region;

import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.BreakBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ProcessedOpcodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A region processor which checks for sub regions and recursively processes
 * them.
 */
public class ChildRegionProcessor implements RegionProcessor {

    @Override
    public BlockSection process(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start) {

        // The first step is to find any points within the region that are sub
        // regions (eg. both dominate and post-domintate the rest of the graph
        // excluding the end point of the current region). These sub-regions are
        // then processes ahead of time into their own control flow statements
        // which are then nested inside of this region.
        int subregion_search_end = 1;
        boolean is_first_condition = true;
        OpcodeBlock sstart = region.get(0);
        if (sstart instanceof GotoOpcodeBlock) {
            subregion_search_end = region.size() - region.indexOf(sstart.getTarget());
            is_first_condition = false;
        }
        for (int i = body_start; i < region.size() - subregion_search_end; i++) {
            OpcodeBlock next = region.get(i);
            if (!(next instanceof ConditionalOpcodeBlock) && !(next instanceof GotoOpcodeBlock)) {
                is_first_condition = false;
                continue;
            }

            // The end block is already not included in `region` so we can
            // simply try and get the region end of any block in this region and
            // if there is an end defined then we know that it forms a sub
            // region.
            int end = -1;
            if (next.getTarget() == ret) {
                region.add(ret);
                end = RegionProcessor.getRegionEnd(region, i);
                region.remove(ret);
                if (end == region.size()) {
                    OpcodeBlock last = region.get(region.size() - 1);
                    boolean is_break = false;
                    if (last instanceof GotoOpcodeBlock) {
                        GotoOpcodeBlock last_goto = (GotoOpcodeBlock) last;
                        if (last_goto.getTarget() == sstart) {
                            // while loop and this is a break;
                            is_break = true;
                        } else if(last_goto.getTarget() == next) {
                            is_first_condition = false;
                        }
                    } else if (last instanceof ConditionalOpcodeBlock) {
                        ConditionalOpcodeBlock cond = (ConditionalOpcodeBlock) last;
                        if (cond.getTarget() == sstart) {
                            is_break = true;
                        }
                    }
                    if (is_break) {
                        BreakBlockSection sec = null;
                        if (region.get(i - 1) instanceof ProcessedOpcodeBlock) {
                            BlockSection compiled = ((ProcessedOpcodeBlock) region.get(i - 1)).getPrecompiledSection();
                            if (compiled instanceof BreakBlockSection) {
                                sec = (BreakBlockSection) compiled;
                            }
                        }
                        if (sec == null) {
                            sec = new BreakBlockSection(new BreakMarkerOpcodeBlock(next.getBreakpoint(), BreakMarkerOpcodeBlock.MarkerType.BREAK),
                                    BreakMarkerOpcodeBlock.MarkerType.BREAK);
                            sec.getInlinedConditions().add((ConditionalOpcodeBlock) next);
                            OpcodeBlock replace = new ProcessedOpcodeBlock(next.getBreakpoint(), sec);
                            region.set(i, replace);
                            GraphOperation.remap(region, next, replace);
                        } else {
                            sec.getInlinedConditions().add((ConditionalOpcodeBlock) next);
                            region.remove(i);
                            i--;
                        }
                        continue;
                    }
                }
            } else if(next.getTarget() == sstart) {
                // this is a continue statement
                boolean part_of_end = true;
                for(int o = i + 1; o < region.size() - subregion_search_end; o++) {
                    OpcodeBlock n = region.get(o);
                    if(n instanceof ConditionalOpcodeBlock) {
                        if(n.getTarget() == sstart || n.getTarget() == ret) {
                            continue;
                        }
                    }
                    part_of_end = false;
                    break;
                }
                if(part_of_end) {
                    return null;
                }
                BreakBlockSection sec = null;
                if (region.get(i - 1) instanceof ProcessedOpcodeBlock) {
                    BlockSection compiled = ((ProcessedOpcodeBlock) region.get(i - 1)).getPrecompiledSection();
                    if (compiled instanceof BreakBlockSection) {
                        sec = (BreakBlockSection) compiled;
                    }
                }
                if (sec == null) {
                    sec = new BreakBlockSection(new BreakMarkerOpcodeBlock(next.getBreakpoint(), BreakMarkerOpcodeBlock.MarkerType.CONTINUE),
                            BreakMarkerOpcodeBlock.MarkerType.CONTINUE);
                    sec.getInlinedConditions().add((ConditionalOpcodeBlock) next);
                    OpcodeBlock replace = new ProcessedOpcodeBlock(next.getBreakpoint(), sec);
                    region.set(i, replace);
                    GraphOperation.remap(region, next, replace);
                } else {
                    sec.getInlinedConditions().add((ConditionalOpcodeBlock) next);
                    region.remove(i);
                    i--;
                }
                OpcodeBlock last = region.get(region.size() - 1);
                if(!(last instanceof GotoOpcodeBlock) && sstart instanceof ConditionalOpcodeBlock) {
                    GotoOpcodeBlock fakeLoop = new GotoOpcodeBlock(last.getBreakpoint());
                    fakeLoop.setTarget(sstart);
                    region.add(fakeLoop);
                }
                continue;
            } else {
                end = RegionProcessor.getRegionEnd(region, i);
            }

            if (end != -1 && (!is_first_condition || end < region.size())) {

                List<OpcodeBlock> subregion = new ArrayList<>();
                for (int o = i; o < end; o++) {
                    subregion.add(region.get(o));
                }

                OpcodeBlock sub_ret = end >= region.size() ? ret : region.get(end);
                BlockSection s = partial.getDecompiler().processRegion(partial, subregion, sub_ret, 1);

                // the first block is set to the condensed subregion block and
                // the rest if the blocks in the subregion are removed.
                ProcessedOpcodeBlock replacement = new ProcessedOpcodeBlock(region.get(i).getBreakpoint(), s);
                replacement.setTarget(sub_ret);
                GraphOperation.remap(region, region.get(i), replacement);
                region.set(i, replacement);
                for (int o = end - 1; o > i; o--) {
                    region.remove(o);
                }
            }
        }
        return null;
    }

}

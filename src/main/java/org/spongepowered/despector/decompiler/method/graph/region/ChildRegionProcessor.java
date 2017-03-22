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
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.OpcodeBlock;

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
        if (sstart.isGoto()) {
            subregion_search_end = region.size() - region.indexOf(sstart.getTarget());
            is_first_condition = false;
        }
        for (int i = body_start; i < region.size() - subregion_search_end; i++) {
            OpcodeBlock next = region.get(i);
            if (!next.isJump()) {
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
            } else {
                end = RegionProcessor.getRegionEnd(region, i);
            }

            if (is_first_condition) {
                for (int o = i; o < end; o++) {
                    if (region.get(o).isGoto()) {
                        is_first_condition = false;
                        break;
                    }
                }
            }

            if (end != -1 && (!is_first_condition || end < region.size() - 1)) {

                List<OpcodeBlock> subregion = new ArrayList<>();
                for (int o = i; o < end; o++) {
                    subregion.add(region.get(o));
                }
                OpcodeBlock sub_ret = end >= region.size() ? ret : region.get(end);
                BlockSection s = partial.getDecompiler().processRegion(partial, subregion, sub_ret, 1);

                // the first block is set to the condensed subregion block and
                // the rest if the blocks in the subregion are removed.
                OpcodeBlock replacement = region.get(i);
                replacement.setPrecompiled(s);
                replacement.setTarget(sub_ret);
                replacement.setElseTarget(null);
                replacement.getOpcodes().clear();

                for (int o = end - 1; o > i; o--) {
                    region.remove(o);
                }
            }
        }
        return null;
    }

}

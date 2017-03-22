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
package org.spongepowered.despector.decompiler.method.graph;

import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.data.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.OpcodeBlock;

import java.util.List;

/**
 * Processes a region of the graph into a single {@link BlockSection}.
 */
public interface RegionProcessor {

    /**
     * Processes the given region.
     */
    BlockSection process(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start);

    static int getRegionEnd(List<OpcodeBlock> blocks, int start) {
        OpcodeBlock region_start = blocks.get(start);
        // if the target is behind the start then we break as this is likely the
        // condition of a do-while

        // the check is less or equal because when a header is split from a
        // condition its break point is set to the same as the condition, in a
        // simple do-while the condition targets the body which was originally
        // split from the condition and therefore has the same break_point so we
        // do this check to catch that. There are no conditions where two
        // conditions may have the same break point that could break this (ie.
        // the case where a condition was targeting a condition after it that
        // has the same break_point is impossible).
        if (region_start.getTarget().getBreakpoint() <= region_start.getBreakpoint()) {
            return -1;
        }
        int end_a = blocks.indexOf(region_start.getTarget());
        if (end_a == -1 && region_start.hasTarget()) {
            end_a = blocks.size();
        }
        int end_b = blocks.indexOf(region_start.getElseTarget());
        if (end_b == -1 && region_start.hasElseTarget()) {
            end_b = blocks.size();
        }

        // Use the target of the start node as a starting point for our search
        int end = Math.max(end_a, end_b);
        boolean isGoto = region_start.isGoto();
        return getRegionEnd(blocks, start, end, isGoto);
    }

    static int getRegionEnd(List<OpcodeBlock> blocks, int start, int end, boolean isGoto) {

        // TODO: break and continue targeting labels on outer loops will break
        // this completely

        // This is a rather brute force search for the next node after the start
        // node which post-dominates the preceding nodes.
        int end_extension = 0;
        int end_a, end_b;

        check: while (true) {
            for (int o = 0; o < start; o++) {
                OpcodeBlock next = blocks.get(o);
                end_a = blocks.indexOf(next.getTarget());
                if (end_a == -1 && next.hasTarget()) {
                    end_a = blocks.size();
                }
                end_b = blocks.indexOf(next.getTarget());
                if (end_b == -1 && next.hasElseTarget()) {
                    end_b = blocks.size();
                }
                if ((end_a > start && end_a < end) || (end_b > start && end_b < end)) {
                    // If any block before the start points into the region then
                    // our start node wasn't actually the start of a subregion.
                    return -1;
                }
            }
            for (int o = start + 1; o < end; o++) {
                OpcodeBlock next = blocks.get(o);
                end_a = blocks.indexOf(next.getTarget());
                if (end_a == -1 && next.hasTarget()) {
                    end_a = blocks.size();
                }
                end_b = blocks.indexOf(next.getTarget());
                if (end_b == -1 && next.hasElseTarget()) {
                    end_b = blocks.size();
                }

                int new_end = Math.max(end_a, end_b);

                if (new_end > end) {
                    if (next.isGoto()) {
                        OpcodeBlock target = next.getTarget();
                        OpcodeBlock alt = next;
                        int alt_end = o;
                        for (OpcodeBlock block : target.getTargettedBy()) {
                            if (block.isGoto()) {
                                int block_index = blocks.indexOf(block);
                                if (block_index > start && block_index < end && block_index > alt_end) {
                                    alt_end = block_index;
                                    alt = block;
                                }
                            }
                        }
                        if (alt != next) {
                            end = Math.max(end, alt_end);
                            next.setTarget(alt);
                            continue;
                        }
                    }
                    // We've found a block inside the current region that points
                    // to a block past the current end of the region. Resize the
                    // region to include it and restart the search.
                    end = new_end;
                    continue check;
                }
            }
            if (isGoto) {
                OpcodeBlock next = blocks.get(end);
                int pos_ext = end_extension;
                while (next.isConditional()) {
                    end_a = blocks.indexOf(next.getTarget());
                    end_b = blocks.indexOf(next.getElseTarget());
                    if ((end_a > start && end_a < end) || (end_b > start && end_b < end)) {
                        end_extension = ++pos_ext;
                        next = blocks.get(end + end_extension);
                        continue;
                    }
                    pos_ext++;
                    next = blocks.get(end + pos_ext);
                }
            }
            for (int o = end + end_extension; o < blocks.size(); o++) {
                OpcodeBlock next = blocks.get(o);
                end_a = blocks.indexOf(next.getTarget());
                if (end_a == -1 && next.hasTarget()) {
                    end_a = blocks.size();
                }
                end_b = blocks.indexOf(next.getTarget());
                if (end_b == -1 && next.hasElseTarget()) {
                    end_b = blocks.size();
                }
                if ((end_a > start && end_a < end) || (end_b > start && end_b < end)) {
                    return -1;
                }
            }
            break;
        }
        if (end >= blocks.size()) {
            return -1;
        }
        return end + end_extension;
    }

}

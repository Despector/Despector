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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.CommentBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.InlineBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A graph processor that checks the region for the next sub region and
 * processes it.
 */
public class SubRegionBlockProcessor implements GraphProcessor {

    @Override
    public int process(PartialMethod partial, List<OpcodeBlock> blocks, OpcodeBlock region_start, List<BlockSection> final_blocks) {

        // here we're splitting a graph into regions by points that both
        // dominate and post-dominate the rest of the graph (eg. points that all
        // control points pass through).

        // we can then process these regions and transform them into a single
        // block representing the control flow statement of the region. Each
        // region will always contain exactly one control flow statement (not
        // counting nesting).

        // We iterate the blocks in order to ensure that we always fine the
        // earliest block of any control flow statement which makes sure that we
        // don't miss any parts of the control flow statement.
        int i = blocks.indexOf(region_start);
        int end = -1;
        boolean targeted_in_future = false;
        if (!(region_start instanceof ConditionalOpcodeBlock) && !(region_start instanceof GotoOpcodeBlock)) {
            for (OpcodeBlock t : region_start.getTargettedBy()) {
                int index = blocks.indexOf(t);
                if (index > i) {
                    targeted_in_future = true;
                    if (index > end) {
                        end = index;
                    }
                }
            }
            // if the block is targeted by a block farther in the code then
            // this block is the first block in a do_while loop
            if (!targeted_in_future) {
                // If the next block isn't conditional then we simply append
                // it
                // to the output.
                final_blocks.add(new InlineBlockSection(region_start));
                return i;
            }
        }
        if (end == -1) {
            end = RegionProcessor.getRegionEnd(blocks, i);
        } else if (end != blocks.size() - 1) {
            end++;
        }

        if (end == -1) {
            // Any conditional block should always form the start of a
            // region at this level.
            throw new IllegalStateException("Conditional jump not part of control flow statement??");
        }

        OpcodeBlock last = blocks.get(end);
        if (blocks.get(end - 1) instanceof TryCatchMarkerOpcodeBlock) {
            end--;
        }

        List<OpcodeBlock> region = new ArrayList<>();
        for (int o = i; o < end; o++) {
            region.add(blocks.get(o));
        }
        // process the region down to a single block
        try {
            final_blocks.add(partial.getDecompiler().processRegion(partial, region, last, targeted_in_future ? 0 : 1));
        } catch (Exception e) {
            if (ConfigManager.getConfig().print_opcodes_on_error) {
                List<String> comment = new ArrayList<>();
                for (OpcodeBlock op : region) {
                    comment.add(op.getDebugHeader());
                    for (AbstractInsnNode insn : op.getOpcodes()) {
                        comment.add(AstUtil.insnToString(insn));
                    }
                }
                OpcodeBlock op = blocks.get(end);
                if (targeted_in_future && op instanceof GotoOpcodeBlock) {
                    comment.add(op.getDebugHeader());
                    for (AbstractInsnNode insn : op.getOpcodes()) {
                        comment.add(AstUtil.insnToString(insn));
                    }
                }
                final_blocks.add(new CommentBlockSection(comment));
            } else {
                throw e;
            }
        }

        if (targeted_in_future && blocks.get(end) instanceof GotoOpcodeBlock) {
            return end;
        }
        return end - 1;
    }

}

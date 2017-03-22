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
package org.spongepowered.despector.decompiler.method.graph.operate;

import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.data.OpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * a graph operation that splits the opcodes that are before a jump but not part
 * of the actual jump statement away from the jump.
 */
public class JumpSeparateOperation implements GraphOperation {

    @Override
    public void process(PartialMethod partial) {
        // Split the opcodes that form the condition away from the preceding
        // statements.
        List<OpcodeBlock> blocks = partial.getGraph();
        List<OpcodeBlock> fblocks = new ArrayList<>();
        for (OpcodeBlock block : blocks) {
            if (block.isGoto()) {
                if (AstUtil.isEmptyOfLogic(block.getOpcodes())) {
                    fblocks.add(block);
                    continue;
                }
                OpcodeBlock header = new OpcodeBlock(block.getBreakpoint());
                header.getOpcodes().addAll(block.getOpcodes());
                block.getOpcodes().clear();
                // Have to ensure that we remap any blocks that were
                // targeting this block to target the header.
                GraphOperation.remap(blocks, block, header);
                GraphOperation.remap(fblocks, block, header);
                header.setTarget(block);
                fblocks.add(header);
                fblocks.add(block);
            } else if (block.isConditional() || block.isSwitch()) {
                int cond_start = AstUtil.findStartLastStatement(block.getOpcodes(), block.getLast());
                if (cond_start > 0) {
                    OpcodeBlock header = new OpcodeBlock(block.getBreakpoint());
                    for (int i = 0; i < cond_start; i++) {
                        header.getOpcodes().add(block.getOpcodes().get(i));
                    }
                    if (AstUtil.isEmptyOfLogic(header.getOpcodes())) {
                        // If there are no useful opcodes left in the header
                        // then we do not perform the split.
                        fblocks.add(block);
                        continue;
                    }
                    for (int i = cond_start - 1; i >= 0; i--) {
                        block.getOpcodes().remove(i);
                    }
                    // Have to ensure that we remap any blocks that were
                    // targeting this block to target the header.
                    GraphOperation.remap(blocks, block, header);
                    GraphOperation.remap(fblocks, block, header);
                    header.setTarget(block);
                    fblocks.add(header);
                    fblocks.add(block);
                } else {
                    fblocks.add(block);
                }
            } else {
                fblocks.add(block);
            }
        }

        blocks.clear();
        blocks.addAll(fblocks);
    }

}

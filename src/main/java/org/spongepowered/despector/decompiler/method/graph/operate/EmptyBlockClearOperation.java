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
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BodyOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.Iterator;
import java.util.List;

/**
 * A graph operation that removes blocks that are empty of any functional
 * opcodes.
 */
public class EmptyBlockClearOperation implements GraphOperation {

    @Override
    public void process(PartialMethod partial) {
        List<OpcodeBlock> blocks = partial.getGraph();
        for (Iterator<OpcodeBlock> it = blocks.iterator(); it.hasNext();) {
            OpcodeBlock block = it.next();
            if (!(block instanceof BodyOpcodeBlock)) {
                continue;
            }
            if (AstUtil.isEmptyOfLogic(block.getOpcodes())) {
                // some conditions can create an empty block as a breakpoint
                // gets inserted on either side of a label immediately following
                // a jump

                for (OpcodeBlock other : blocks) {
                    if (other == block) {
                        continue;
                    }
                    if (other.getTarget() == block) {
                        other.setTarget(block.getTarget());
                    }
                    if (other instanceof ConditionalOpcodeBlock) {
                        ConditionalOpcodeBlock cond = (ConditionalOpcodeBlock) other;
                        if (cond.getElseTarget() == block) {
                            cond.setElseTarget(block.getTarget());
                        }
                    }
                }
                it.remove();
            }
        }

    }

}

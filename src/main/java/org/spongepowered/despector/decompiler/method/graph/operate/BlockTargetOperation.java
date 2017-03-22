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

import java.util.List;

/**
 * A graph operation that populates the {@link OpcodeBlock#getTargettedBy()}
 * collection.
 */
public class BlockTargetOperation implements GraphOperation {

    @Override
    public void process(PartialMethod partial) {
        List<OpcodeBlock> blocks = partial.getGraph();
        for (int i = 0; i < blocks.size(); i++) {
            OpcodeBlock block = blocks.get(i);
            if (block.isConditional()) {
                block.getElseTarget().targettedBy(block);
            }
            if (block.isJump()) {
                block.getTarget().targettedBy(block);
            } else if (block.hasTarget() && blocks.indexOf(block.getTarget()) != i + 1) {
                block.getTarget().targettedBy(block);
            }
        }
    }

}

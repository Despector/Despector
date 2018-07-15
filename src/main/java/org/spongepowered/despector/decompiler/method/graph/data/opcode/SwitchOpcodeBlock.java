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
package org.spongepowered.despector.decompiler.method.graph.data.opcode;

import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;

import java.util.HashMap;
import java.util.Map;

/**
 * An opcode block ending with a switch instruction.
 */
public class SwitchOpcodeBlock extends OpcodeBlock {

    private final Map<Integer, OpcodeBlock> additional_targets = new HashMap<>();

    public SwitchOpcodeBlock(int start, int end) {
        super(start, end);
    }

    /**
     * Gets a map of any additional target blocks. Used by switches to represent
     * the blocks targetted by the various cases.
     */
    public Map<Integer, OpcodeBlock> getAdditionalTargets() {
        return this.additional_targets;
    }

    @Override
    public BlockSection toBlockSection() {
        throw new IllegalStateException("Unexpected switch block");
    }

    @Override
    public String getDebugHeader() {
        String s = "Switch: " + this.start_pc + "-" + this.end_pc + " (target: " + (this.target != null ? this.target.getStart() : -1) + ")\n";
        for (Map.Entry<Integer, OpcodeBlock> a : this.additional_targets.entrySet()) {
            s += "    " + a.getKey() + " -> " + a.getValue().getStart() + "\n";
        }
        return s;
    }

}

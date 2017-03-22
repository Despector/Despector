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

import org.objectweb.asm.Label;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.data.OpcodeBlock;

import java.util.List;
import java.util.Map;

/**
 * An operation on the opcode graph that modifies the graph in place.
 */
public interface GraphOperation {

    /**
     * Processes the opcode graph of the given partial method.
     */
    void process(PartialMethod partial);

    static void remap(List<OpcodeBlock> blocks, OpcodeBlock from, OpcodeBlock to) {
        for (OpcodeBlock other : blocks) {
            if (other.getTarget() == from) {
                other.setTarget(to);
            }
            if (other.getElseTarget() == from) {
                other.setElseTarget(to);
            }
            for (Map.Entry<Label, OpcodeBlock> e : other.getAdditionalTargets().entrySet()) {
                if (e.getValue() == from) {
                    other.getAdditionalTargets().put(e.getKey(), to);
                }
            }
        }
    }

}

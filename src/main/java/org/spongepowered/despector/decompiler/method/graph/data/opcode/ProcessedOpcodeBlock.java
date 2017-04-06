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

public class ProcessedOpcodeBlock extends OpcodeBlock {

    private BlockSection internal = null;

    public ProcessedOpcodeBlock(int br, BlockSection internal) {
        super(br);
        this.internal = internal;
    }

    /**
     * Gets the precompiled {@link BlockSection} of this block if it exists.
     */
    public BlockSection getPrecompiledSection() {
        return this.internal;
    }

    /**
     * Sets the precompiled {@link BlockSection} of this block..
     */
    public void setPrecompiled(BlockSection section) {
        this.internal = section;
    }

    @Override
    public BlockSection toBlockSection() {
        return this.internal;
    }

    @Override
    public String getDebugHeader() {
        return "Processed: " + this.break_point + " (target: " + (this.target != null ? this.target.getBreakpoint() : -1) + ", internal: "
                + this.internal.getClass().getSimpleName() + ")";
    }

}

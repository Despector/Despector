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
package org.spongepowered.despector.decompiler.method.graph.data.opcode;

import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.BreakBlockSection;

public class BreakMarkerOpcodeBlock extends OpcodeBlock {

    private MarkerType type;
    private OpcodeBlock marked;

    public BreakMarkerOpcodeBlock(int br, MarkerType type) {
        super(br);
        this.type = type;
    }

    public MarkerType getType() {
        return this.type;
    }

    public void setType(MarkerType type) {
        this.type = type;
    }

    public OpcodeBlock getMarked() {
        return this.marked;
    }

    public void setMarked(OpcodeBlock marked) {
        this.marked = marked;
    }

    @Override
    public OpcodeBlock getTarget() {
        return null;
    }

    @Override
    public boolean hasTarget() {
        return false;
    }

    public OpcodeBlock getOldTarget() {
        return this.target;
    }

    @Override
    public BlockSection toBlockSection() {
        return new BreakBlockSection(this, this.type);
    }

    @Override
    public String getDebugHeader() {
        return "Break: " + this.break_point + " (target: " + (this.target != null ? this.target.getBreakpoint() : -1) + ", marker: "
                + this.type.name() + ", marked: " + (this.marked != null ? this.marked.getBreakpoint() : -1) + ")";
    }

    public static enum MarkerType {
        BREAK,
        CONTINUE;
    }

}

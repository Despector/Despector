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

import org.objectweb.asm.tree.TryCatchBlockNode;
import org.spongepowered.despector.decompiler.method.graph.data.TryCatchMarkerType;

/**
 * An opcode block that acts as a marker to mark the start and end of a try
 * block and the start of a catch block as there are no opcodes to deliminate
 * these.
 */
public class TryCatchMarkerOpcodeBlock extends OpcodeBlock {

    private final TryCatchMarkerType marker_type;
    private final TryCatchBlockNode tc_node;

    private TryCatchMarkerOpcodeBlock start;
    private TryCatchMarkerOpcodeBlock end;

    public TryCatchMarkerOpcodeBlock(TryCatchMarkerType marker, TryCatchBlockNode tc) {
        super(-1);
        this.marker_type = marker;
        this.tc_node = tc;
        if (this.marker_type == TryCatchMarkerType.START) {
            this.start = this;
        } else if (this.marker_type == TryCatchMarkerType.END) {
            this.end = this;
        }
    }

    /**
     * Gets the type of this marker.
     */
    public TryCatchMarkerType getType() {
        return this.marker_type;
    }

    /**
     * Gets the {@link TryCatchBlockNode} that this marker is from.
     */
    public TryCatchBlockNode getAsmNode() {
        return this.tc_node;
    }

    /**
     * Gets the start marker corresponding to this marker.
     */
    public TryCatchMarkerOpcodeBlock getStartMarker() {
        return this.start;
    }

    /**
     * Sets the start marker corresponding to this marker.
     */
    public void setStartMarker(TryCatchMarkerOpcodeBlock start) {
        this.start = start;
    }

    /**
     * Gets the end marker corresponding to this marker.
     */
    public TryCatchMarkerOpcodeBlock getEndMarker() {
        return this.end;
    }

    /**
     * Sets the end marker corresponding to this marker.
     */
    public void setEndMarker(TryCatchMarkerOpcodeBlock end) {
        this.end = end;
    }

    @Override
    public void print() {
        System.out.println("TryCatch marker: " + this.marker_type.name());
        System.out.println("    part of " + this.tc_node);
    }
}

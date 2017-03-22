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
package org.spongepowered.despector.decompiler.method.graph.data;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a section of opcodes in the original method that forms part of a
 * graph representing the control flow through the method. The opcode blocks are
 * created by sectioning the original opcodes at any point that may cause a jump
 * and any point targeted by a jump.
 */
public class OpcodeBlock {

    // TODO: split in to different types for jumps, switches, precompiled, etc.

    private final int break_point;
    private final List<AbstractInsnNode> opcodes = new ArrayList<>();
    private AbstractInsnNode last;
    private OpcodeBlock target;
    private OpcodeBlock else_target;
    private final Map<Label, OpcodeBlock> additional_targets = new HashMap<>();

    private Set<OpcodeBlock> targetted_by = new HashSet<>();
    private BlockSection internal = null;
    private boolean exclude_from_ternary_check = false;

    public OpcodeBlock(int br) {
        this.break_point = br;
    }

    /**
     * Gets the index of the opcode which this block was formed starting at. Can
     * be used as a general method of ordering blocks but beware as it is not
     * recalculated if block are split or joined.
     */
    public int getBreakpoint() {
        return this.break_point;
    }

    /**
     * Gets the opcodes that are part of this block.
     */
    public List<AbstractInsnNode> getOpcodes() {
        return this.opcodes;
    }

    /**
     * Gets the last opcode in this block if it is a special opcode (eg. a jump,
     * switch, etc.). This opcode is not included in the results of
     * {@link #getOpcodes()}.
     */
    public AbstractInsnNode getLast() {
        return this.last;
    }

    /**
     * Sets the last opcode of this block.
     * 
     * @see #getLast()
     */
    public void setLast(AbstractInsnNode insn) {
        this.last = insn;
    }

    /**
     * Gets if this block ends with a goto.
     */
    public boolean isGoto() {
        return this.last != null && this.last.getOpcode() == GOTO;
    }

    /**
     * Gets if this block ends with a conditional jump.
     */
    public boolean isJump() {
        return this.last != null && this.last instanceof JumpInsnNode;
    }

    /**
     * Gets if this block ends with a return.
     */
    public boolean isReturn() {
        return this.last != null && this.last.getOpcode() >= IRETURN && this.last.getOpcode() <= RETURN;
    }

    /**
     * Gets if this block ends with a throw.
     */
    public boolean isThrow() {
        return this.last != null && this.last.getOpcode() == ATHROW;
    }

    /**
     * Gets if this block ends with a switch.
     */
    public boolean isSwitch() {
        return this.last != null && (this.last.getOpcode() == TABLESWITCH || this.last.getOpcode() == LOOKUPSWITCH);
    }

    /**
     * Gets if this block has an alternate execution path.
     */
    public boolean isConditional() {
        return this.else_target != null;
    }

    /**
     * Gets the block targeted by this block. If this block ends with a
     * condition jump the target will be the block that control will be passed
     * to if the condition is true.
     */
    public OpcodeBlock getTarget() {
        return this.target;
    }

    /**
     * Gets if this block has a target block.
     */
    public boolean hasTarget() {
        return this.target != null;
    }

    /**
     * Sets the target block of this block.
     */
    public void setTarget(OpcodeBlock block) {
        this.target = block;
    }

    /**
     * Gets the alternate target of this block. Only present for conditional
     * jumps where it represents the block to which control is passed if the
     * condition is false.
     */
    public OpcodeBlock getElseTarget() {
        return this.else_target;
    }

    /**
     * Has an alternate target.
     */
    public boolean hasElseTarget() {
        return this.else_target != null;
    }

    /**
     * Sets the alternate target.
     */
    public void setElseTarget(OpcodeBlock block) {
        this.else_target = block;
    }

    /**
     * Gets a map of any additional target blocks. Used by switches to represent
     * the blocks targetted by the various cases.
     */
    public Map<Label, OpcodeBlock> getAdditionalTargets() {
        return this.additional_targets;
    }

    /**
     * Gets a set of blocks which target this block.
     */
    public Set<OpcodeBlock> getTargettedBy() {
        return this.targetted_by;
    }

    /**
     * Adds a block as having this block as a target.
     */
    public void targettedBy(OpcodeBlock block) {
        this.targetted_by.add(block);
    }

    /**
     * Gets the precompiled {@link BlockSection} of this block if it exists.
     */
    public BlockSection getPrecompiledSection() {
        return this.internal;
    }

    /**
     * Gets if this block has been precompiled into a {@link BlockSection}.
     */
    public boolean hasPrecompiledSection() {
        return this.internal != null;
    }

    /**
     * Sets the precompiled {@link BlockSection} of this block..
     */
    public void setPrecompiled(BlockSection section) {
        this.internal = section;
    }

    /**
     * Gets if this block is omitted from the check for ternaries.
     * 
     * <p>As an example: typically statements do not overlap blocks so a check
     * is done if a block's initial opcodes expect values to be already on the
     * stack which indicates that the block is preceeded by a ternary which is
     * then precompiled in a pre-pass.</p>
     * 
     * <p>However in the event of a try-catch statement ending in a return the
     * marker for the end of the try block will split the return from the values
     * it is returning.</p>
     */
    public boolean isOmittedFromTernaryCheck() {
        return this.exclude_from_ternary_check;
    }

    /**
     * Sets this block as omitted from the ternary check.
     */
    public void omitFromTernaryCheck(boolean state) {
        this.exclude_from_ternary_check = state;
    }

    /**
     * Prints this block's information for debugging.
     */
    public void print() {
        System.out.println("Block " + this.break_point + ":");
        for (AbstractInsnNode op : this.opcodes) {
            System.out.println(AstUtil.insnToString(op));
        }
        System.out.println("Last: " + (this.last != null ? AstUtil.insnToString(this.last) : "null"));
        System.out.println("Target: " + (this.target != null ? this.target.break_point : -1));
        System.out.println("Else Target: " + (this.else_target != null ? this.else_target.break_point : -1));
    }

}

/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.despector.ast.io.insn;

import org.objectweb.asm.tree.JumpInsnNode;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

public class JumpIntermediate implements Instruction {

    public Instruction  value;
    public JumpInsnNode jump;

    public JumpIntermediate(JumpInsnNode jump, Instruction value) {
        this.jump = jump;
        this.value = value;
    }

    @Override
    public String inferType() {
        return null;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
    }

}

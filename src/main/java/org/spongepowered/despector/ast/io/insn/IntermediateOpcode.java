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
package org.spongepowered.despector.ast.io.insn;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.AstUtil;

import java.util.List;

public interface IntermediateOpcode {

    public static interface IntermediateJump extends IntermediateOpcode {

        JumpInsnNode getNode();

    }

    public static class IntermediateGoto implements IntermediateJump {

        private final JumpInsnNode jump;

        public IntermediateGoto(JumpInsnNode jump) {
            this.jump = jump;
        }

        @Override
        public JumpInsnNode getNode() {
            return this.jump;
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.jump);
        }

    }

    public static abstract class AbstractSwitch {

        public abstract Instruction getSwitchVar();

        public abstract List<LabelNode> getLabels();

        public abstract LabelNode getDefault();

        public abstract int indexFor(LabelNode l);

    }

    public static class IntermediateTableSwitch extends AbstractSwitch implements IntermediateOpcode {

        private final Instruction variable;
        private final TableSwitchInsnNode jump;

        public IntermediateTableSwitch(TableSwitchInsnNode jump, Instruction var) {
            this.jump = jump;
            this.variable = var;
        }

        public TableSwitchInsnNode getNode() {
            return this.jump;
        }

        @Override
        public Instruction getSwitchVar() {
            return this.variable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<LabelNode> getLabels() {
            return Lists.newArrayList(this.jump.labels);
        }

        @Override
        public LabelNode getDefault() {
            return this.jump.dflt;
        }

        @Override
        public int indexFor(LabelNode l) {
            return this.jump.min + this.jump.labels.indexOf(l);
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.jump) + " switch on " + this.variable.toString();
        }

    }

    public static class IntermediateLookupSwitch extends AbstractSwitch implements IntermediateOpcode {

        private final Instruction variable;
        private final LookupSwitchInsnNode jump;

        public IntermediateLookupSwitch(LookupSwitchInsnNode jump, Instruction var) {
            this.jump = jump;
            this.variable = var;
        }

        public LookupSwitchInsnNode getNode() {
            return this.jump;
        }

        @Override
        public Instruction getSwitchVar() {
            return this.variable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<LabelNode> getLabels() {
            return Lists.newArrayList(this.jump.labels);
        }

        @Override
        public LabelNode getDefault() {
            return this.jump.dflt;
        }

        @Override
        public int indexFor(LabelNode l) {
            return (int) this.jump.keys.get(this.jump.labels.indexOf(l));
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.jump) + " switch on " + this.variable.toString();
        }

    }

    public static class IntermediateConditionalJump implements IntermediateJump {

        private final Instruction condition;
        private final JumpInsnNode jump;

        public IntermediateConditionalJump(JumpInsnNode jump, Instruction condition) {
            this.condition = condition;
            this.jump = jump;
        }

        @Override
        public JumpInsnNode getNode() {
            return this.jump;
        }

        public Instruction getCondition() {
            return this.condition;
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.jump) + " if " + this.condition.toString();
        }

    }

    public static class IntermediateCompareJump implements IntermediateJump {

        private final Instruction left;
        private final Instruction right;
        private final JumpInsnNode jump;

        public IntermediateCompareJump(JumpInsnNode jump, Instruction left, Instruction right) {
            this.left = left;
            this.right = right;
            this.jump = jump;
        }

        @Override
        public JumpInsnNode getNode() {
            return this.jump;
        }

        public Instruction getLeft() {
            return this.left;
        }

        public Instruction getRight() {
            return this.right;
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.jump) + " if " + this.left.toString() + " cmp " + this.right.toString();
        }

    }

    public static class IntermediateStatement implements IntermediateOpcode {

        private final Statement statement;

        public IntermediateStatement(Statement statement) {
            this.statement = statement;
        }

        public Statement getStatement() {
            return this.statement;
        }

        @Override
        public String toString() {
            return "IntermediateStatement: " + this.statement.toString();
        }

    }

    public static class IntermediateLabel implements IntermediateOpcode {

        private final LabelNode label;

        public IntermediateLabel(LabelNode label) {
            this.label = label;
        }

        public LabelNode getLabel() {
            return this.label;
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.label);
        }

    }

    public static class IntermediateFrame implements IntermediateOpcode {

        private final FrameNode label;

        public IntermediateFrame(FrameNode label) {
            this.label = label;
        }

        public FrameNode getFrame() {
            return this.label;
        }

        @Override
        public String toString() {
            return AstUtil.insnToString(this.label);
        }

    }

    public static class IntermediateStackValue implements IntermediateOpcode, Statement {

        private final Instruction val;

        public IntermediateStackValue(Instruction val) {
            this.val = val;
        }

        public Instruction getStackVal() {
            return this.val;
        }

        @Override
        public void accept(InstructionVisitor visitor) {
            this.val.accept(visitor);
        }

        @Override
        public String toString() {
            return "IntermediateStackValue: " + this.val.toString();
        }
    }

    public static class DummyInstruction implements Instruction {

        public DummyInstruction() {
        }

        @Override
        public String inferType() {
            return null;
        }

        @Override
        public void accept(InstructionVisitor visitor) {
        }

        @Override
        public String toString() {
            return "DummyInstruction";
        }

    }

    public static abstract class TryCatch implements IntermediateOpcode {

        private final int index;

        public TryCatch(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

    }

    public static class TryStart extends TryCatch {

        public TryStart(int index) {
            super(index);
        }

        @Override
        public String toString() {
            return "TryStart";
        }

    }

    public static class TryEnd extends TryCatch {

        public TryEnd(int index) {
            super(index);
        }

        @Override
        public String toString() {
            return "TryEnd";
        }

    }

    public static class CatchLocal implements IntermediateOpcode {

        private final Local local;

        public CatchLocal(Local local) {
            this.local = local;
        }

        public Local getLocal() {
            return this.local;
        }

        @Override
        public String toString() {
            return "CatchLocal " + this.local;
        }

    }

}

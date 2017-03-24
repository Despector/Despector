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
package org.spongepowered.despector.decompiler.kotlin.method.graph.operate;

import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.POP;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.StatementBlock.Type;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.data.TryCatchMarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.block.TernaryBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ProcessedOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A graph operation that pre-processes ternaries before the rest of the graph
 * processing.
 */
public class ElvisPrePassOperation implements GraphOperation {

    @Override
    public void process(PartialMethod partial) {
        List<OpcodeBlock> blocks = partial.getGraph();
        for (int i = 0; i < blocks.size(); i++) {
            OpcodeBlock block = blocks.get(i);
            if (block instanceof TryCatchMarkerOpcodeBlock) {
                TryCatchMarkerOpcodeBlock tc = (TryCatchMarkerOpcodeBlock) block;
                if (tc.getType() == TryCatchMarkerType.CATCH) {
                    blocks.get(i + 1).omitFromTernaryCheck(true);
                }
            } else if (block instanceof ProcessedOpcodeBlock
                    && ((ProcessedOpcodeBlock) block).getPrecompiledSection() instanceof TernaryBlockSection) {
                blocks.get(i + 1).omitFromTernaryCheck(true);
            }
        }
        for (int i = 0; i < blocks.size(); i++) {
            OpcodeBlock block = blocks.get(i);
            if (!block.isOmittedFromTernaryCheck() && AstUtil.hasStartingRequirement(block.getOpcodes())) {
                i -= compileTernary(blocks, i, partial.getLocals());
            }
        }
    }

    private static int compileTernary(List<OpcodeBlock> blocks, int end, Locals locals) {
        if (end == 0) {
            return 0;
        }
        OpcodeBlock consumer = blocks.get(end);
        if (AstUtil.getFirstOpcode(consumer.getOpcodes()).getOpcode() == POP) {
            OpcodeBlock prev = blocks.get(end - 1);
            if (prev instanceof ConditionalOpcodeBlock) {
                int dup = prev.getOpcodes().size() - 1;
                for (; dup >= 0; dup--) {
                    if (prev.getOpcodes().get(dup).getOpcode() == DUP) {
                        break;
                    }
                }
                if (dup != -1) {
                    prev.getOpcodes().remove(dup);
                }
                Condition cond = ConditionBuilder.makeSimpleCondition((ConditionalOpcodeBlock) prev, locals);
                List<OpcodeBlock> else_blocks = new ArrayList<>();

                for (int i = end; i < blocks.indexOf(prev.getTarget()); i++) {
                    OpcodeBlock next = blocks.get(i);
                    if (i == end && AstUtil.getFirstOpcode(next.getOpcodes()).getOpcode() == POP) {
                        next.getOpcodes().remove(AstUtil.getFirstOpcode(next.getOpcodes()));
                    }
                    else_blocks.add(next);
                }

                TernaryBlockSection elvis = new TernaryBlockSection(cond);
                for (OpcodeBlock e : else_blocks) {
                    elvis.getFalseBody().add(e.toBlockSection());
                }
                OpcodeBlock last = null;

                OpcodeBlock first = prev;
                OpcodeBlock replacement = new ProcessedOpcodeBlock(first.getBreakpoint(), elvis);
                replacement.setTarget(last);
                blocks.set(end - 1, replacement);
                int removed = 0;
                GraphOperation.remap(blocks, first, replacement);
                for (int i = end; i < end + else_blocks.size(); i++) {
                    if (blocks.get(i) instanceof TryCatchMarkerOpcodeBlock) {
                        removed--;
                        continue;
                    }
                    blocks.remove(i);
                    removed++;
                }
                return -removed;

            } else if (prev instanceof GotoOpcodeBlock) {

            }

        } else if (AstUtil.getFirstOpcode(consumer.getOpcodes()).getOpcode() == DUP && consumer instanceof ConditionalOpcodeBlock) {

            List<OpcodeBlock> else_blocks = new ArrayList<>();

            for (int i = end + 1; i < blocks.indexOf(consumer.getTarget()); i++) {
                OpcodeBlock next = blocks.get(i);
                if (i == end + 1 && AstUtil.getFirstOpcode(next.getOpcodes()).getOpcode() == POP) {
                    next.getOpcodes().remove(AstUtil.getFirstOpcode(next.getOpcodes()));
                }
                else_blocks.add(next);
            }

            Deque<Instruction> dummy_stack = new ArrayDeque<>();
            StatementBlock dummy = new StatementBlock(Type.METHOD, locals);
            OpcodeBlock prev = blocks.get(end - 1);
            prev.toBlockSection().appendTo(dummy, dummy_stack);
            checkState(dummy_stack.size() == 1);

            TernaryBlockSection elvis = new TernaryBlockSection(new BooleanCondition(dummy_stack.pop(), false));
            for (OpcodeBlock e : else_blocks) {
                elvis.getFalseBody().add(e.toBlockSection());
            }
            OpcodeBlock last = null;

            OpcodeBlock first = consumer;
            OpcodeBlock replacement = new ProcessedOpcodeBlock(first.getBreakpoint(), elvis);
            replacement.setTarget(last);
            blocks.set(end, replacement);
            int removed = 0;
            GraphOperation.remap(blocks, first, replacement);
            for (int i = end + 1; i < end + else_blocks.size() + 1; i++) {
                if (blocks.get(i) instanceof TryCatchMarkerOpcodeBlock) {
                    removed--;
                    continue;
                }
                blocks.remove(i);
                removed++;
            }
            return -removed;
        }
        return 0;
    }

}

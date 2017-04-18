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
package org.spongepowered.despector.decompiler.method.graph.operate;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.PartialMethod.TryCatchRegion;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.data.TryCatchMarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.block.InlineBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.TernaryBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ProcessedOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A graph operation that pre-processes ternaries before the rest of the graph
 * processing.
 */
public class TernaryPrePassOperation implements GraphOperation {

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
        if (end < 4) {
            return 0;
        }
        OpcodeBlock consumer = blocks.get(end);
        int start = end - 1;
        List<OpcodeBlock> true_blocks = new ArrayList<>();
        OpcodeBlock tr = blocks.get(start--);
        TryCatchRegion tr_end = null;
        if (tr instanceof TryCatchMarkerOpcodeBlock) {
            tr_end = ((TryCatchMarkerOpcodeBlock) tr).getAsmNode();
            tr = blocks.get(start--);
        }
        true_blocks.add(0, tr);
        OpcodeBlock go = blocks.get(start--);
        while (!(go instanceof GotoOpcodeBlock) || go.getTarget() != consumer) {
            if (go instanceof TryCatchMarkerOpcodeBlock) {
                TryCatchRegion go_tr = ((TryCatchMarkerOpcodeBlock) go).getAsmNode();
                if (go_tr == tr_end) {
                    return 0;
                }
            }
            true_blocks.add(0, go);
            if (start < 0) {
                return 0;
            }
            go = blocks.get(start--);
        }
        checkState(!true_blocks.isEmpty());
        List<OpcodeBlock> false_blocks = new ArrayList<>();
        OpcodeBlock fl = blocks.get(start--);
        OpcodeBlock first_true = true_blocks.get(0);
        while ((!(go instanceof GotoOpcodeBlock) && !(go instanceof ConditionalOpcodeBlock)) || fl.getTarget() != first_true) {
            false_blocks.add(0, fl);
            if (start < 0) {
                return 0;
            }
            fl = blocks.get(start--);
        }
        checkState(!false_blocks.isEmpty());
        OpcodeBlock first_false = false_blocks.get(0);
        Set<OpcodeBlock> seen = new HashSet<>();
        seen.add(first_true);
        seen.add(first_false);
        seen.add(fl);
        seen.add(go);
        List<ConditionalOpcodeBlock> condition_blocks = new ArrayList<>();
        condition_blocks.add((ConditionalOpcodeBlock) fl);
        boolean has_more = false;
        for (; start >= 0; start--) {
            OpcodeBlock next = blocks.get(start);
            if (next instanceof GotoOpcodeBlock && next.getTarget() == consumer) {
                has_more = true;
            }
            if (!(next instanceof GotoOpcodeBlock) && !(next instanceof ConditionalOpcodeBlock)) {
                break;
            }
            if (!seen.contains(next.getTarget())) {
                break;
            }
            seen.add(next);
            condition_blocks.add(0, (ConditionalOpcodeBlock) next);
        }
        Condition cond = ConditionBuilder.makeCondition(condition_blocks, locals, first_false, first_true);
        TernaryBlockSection ternary = new TernaryBlockSection(cond);
        if (true_blocks.size() > 1) {
            true_blocks.add(consumer);
            compileTernary(true_blocks, true_blocks.size() - 1, locals);
            true_blocks.remove(consumer);
        }
        for (OpcodeBlock t : true_blocks) {
            if (t instanceof ProcessedOpcodeBlock) {
                ternary.getFalseBody().add(((ProcessedOpcodeBlock) t).getPrecompiledSection());
            } else {
                checkState(!(t instanceof GotoOpcodeBlock) && !(t instanceof ConditionalOpcodeBlock));
                ternary.getFalseBody().add(new InlineBlockSection(t));
            }
        }
        if (false_blocks.size() > 1) {
            false_blocks.add(consumer);
            compileTernary(false_blocks, false_blocks.size() - 1, locals);
            false_blocks.remove(consumer);
        }
        for (OpcodeBlock t : false_blocks) {
            if (t instanceof ProcessedOpcodeBlock) {
                ternary.getTrueBody().add(((ProcessedOpcodeBlock) t).getPrecompiledSection());
            } else {
                checkState(!(t instanceof GotoOpcodeBlock) && !(t instanceof ConditionalOpcodeBlock));
                ternary.getTrueBody().add(new InlineBlockSection(t));
            }
        }
        OpcodeBlock first = condition_blocks.get(0);
        OpcodeBlock replacement = new ProcessedOpcodeBlock(first.getBreakpoint(), ternary);
        replacement.setTarget(consumer);
        start = blocks.indexOf(first);
        int removed = has_more ? 1 : 0;
        blocks.set(start, replacement);
        GraphOperation.remap(blocks, first, replacement);
        for (int i = end - 1; i >= start + 1; i--) {
            if (blocks.get(i) instanceof TryCatchMarkerOpcodeBlock) {
                removed--;
                continue;
            }
            blocks.remove(i);
            removed++;
        }
        return removed;
    }

}

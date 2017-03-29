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

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.decompiler.kotlin.method.graph.data.WhenBlockSection;
import org.spongepowered.despector.decompiler.kotlin.method.graph.data.WhenBlockSection.WhenCondition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
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
public class KotlinTernaryPrePassOperation implements GraphOperation {

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

        // This is similar to the java ternary pre-pass operation except that
        // it is generalized to support an arbitrary number of else-if cases in
        // order to create a when statement if there is more than one

        OpcodeBlock consumer = blocks.get(end);
        int start = end - 1;
        List<OpcodeBlock> true_blocks = new ArrayList<>();
        OpcodeBlock tr = blocks.get(start--);
        // if a ternary is at the end of a try-catch region then the end marker
        // will be placed after the value of the ternary but before the consumer
        // so we check for it and skip it if it is there
        if (tr instanceof TryCatchMarkerOpcodeBlock) {
            tr = blocks.get(start--);
        }
        true_blocks.add(0, tr);
        OpcodeBlock go = blocks.get(start--);
        while (!(go instanceof GotoOpcodeBlock) || go.getTarget() != consumer) {
            // scan backwards until we reach a goto block targetting our
            // consumer which will be the end of the last case
            true_blocks.add(0, go);
            go = blocks.get(start--);
        }
        checkState(!true_blocks.isEmpty());
        List<WhenBlock> conditions = new ArrayList<>();
        OpcodeBlock first = null;
        OpcodeBlock first_true = true_blocks.get(0);
        while (go instanceof GotoOpcodeBlock) {
            // for so long as we have more cases the next block will be a goto

            List<OpcodeBlock> false_blocks = new ArrayList<>();
            OpcodeBlock fl = blocks.get(start--);
            // collect the body of the next section so long as its not
            // conditional or a goto and its not targeting the start of the next
            // section.
            while ((!(fl instanceof GotoOpcodeBlock) && !(fl instanceof ConditionalOpcodeBlock)) || fl.getTarget() != first_true) {
                false_blocks.add(0, fl);
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
            // we go backwards through the conditional blocks until we we reach
            // something that is not a condition or something that does not
            // target a block we are expecting.
            for (; start >= 0; start--) {
                OpcodeBlock next = blocks.get(start);
                if (next instanceof GotoOpcodeBlock || !(next instanceof ConditionalOpcodeBlock) || !seen.contains(next.getTarget())) {
                    break;
                }
                seen.add(next);
                condition_blocks.add(0, (ConditionalOpcodeBlock) next);
            }
            Condition cond = ConditionBuilder.makeCondition(condition_blocks, locals, first_false, first_true);
            WhenBlock next_when = new WhenBlock(cond);
            next_when.getBody().addAll(false_blocks);
            first = condition_blocks.get(0);
            first_true = first;
            conditions.add(0, next_when);
            go = blocks.get(start--);
        }
        OpcodeBlock replacement = null;
        if (conditions.size() > 1) {
            // If we have more than one case then its a when statement
            WhenBlockSection when = new WhenBlockSection();
            // TODO support nesting?
            for (OpcodeBlock t : true_blocks) {
                if (t instanceof ProcessedOpcodeBlock) {
                    when.getElseBody().add(((ProcessedOpcodeBlock) t).getPrecompiledSection());
                } else {
                    checkState(!(t instanceof GotoOpcodeBlock) && !(t instanceof ConditionalOpcodeBlock));
                    when.getElseBody().add(new InlineBlockSection(t));
                }
            }
            for (WhenBlock when_block : conditions) {
                WhenCondition when_cond = new WhenCondition(when_block.getCondition());
                for (OpcodeBlock t : when_block.getBody()) {
                    if (t instanceof ProcessedOpcodeBlock) {
                        when_cond.getBody().add(((ProcessedOpcodeBlock) t).getPrecompiledSection());
                    } else {
                        checkState(!(t instanceof GotoOpcodeBlock) && !(t instanceof ConditionalOpcodeBlock));
                        when_cond.getBody().add(new InlineBlockSection(t));
                    }
                }
                when.getConditions().add(when_cond);
            }
            replacement = new ProcessedOpcodeBlock(first.getBreakpoint(), when);
        } else {
            // with only a single case we have a ternary
            TernaryBlockSection ternary = new TernaryBlockSection(conditions.get(0).getCondition());
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
            List<OpcodeBlock> false_blocks = conditions.get(0).getBody();
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
            replacement = new ProcessedOpcodeBlock(first.getBreakpoint(), ternary);
        }
        replacement.setTarget(consumer);
        start = blocks.indexOf(first);
        int removed = 0;
        if (consumer instanceof ConditionalOpcodeBlock) {
            ((ConditionalOpcodeBlock) consumer).setPrefix(replacement);
            GraphOperation.remap(blocks, first, consumer);
            start--;
        } else {
            blocks.set(start, replacement);
            GraphOperation.remap(blocks, first, replacement);
        }
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

    /**
     * A temporary construct for holding the cases of a when/ternary in
     * construction.
     */
    public static class WhenBlock {

        private Condition condition;
        private final List<OpcodeBlock> body = new ArrayList<>();

        public WhenBlock(Condition cond) {
            this.condition = cond;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public void setCondition(Condition cond) {
            this.condition = cond;
        }

        public List<OpcodeBlock> getBody() {
            return this.body;
        }

    }

}

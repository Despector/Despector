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
package org.spongepowered.despector.decompiler.method;

import static org.spongepowered.despector.util.ConditionUtil.inverse;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.condition.AndCondition;
import org.spongepowered.despector.ast.insn.condition.BooleanCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition;
import org.spongepowered.despector.ast.insn.condition.CompareCondition.CompareOperator;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.condition.OrCondition;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.util.ConditionUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A utility for forming a condition a set of conditional jumps.
 */
public final class ConditionBuilder {

    /**
     * Creates a simple condition from the given {@link ConditionalOpcodeBlock}.
     */
    public static Condition makeSimpleCondition(ConditionalOpcodeBlock block, Locals locals) {

        // This forms the condition representing the conditional jump of the
        // given block

        StatementBlock dummy = new StatementBlock(StatementBlock.Type.IF);
        Deque<Instruction> dummy_stack = new ArrayDeque<>();
        if (block.getPrefix() != null) {
            block.getPrefix().toBlockSection().appendTo(dummy, locals, dummy_stack);
        }
        StatementBuilder.appendBlock(block, dummy, locals, dummy_stack);

        switch (block.getLast().getOpcode()) {
        case Insn.IFEQ: {
            if (dummy_stack.size() != 1) {
                throw new IllegalStateException();
            }
            Instruction val = dummy_stack.pop();
            if (val instanceof NumberCompare) {
                NumberCompare cmp = (NumberCompare) val;
                return new CompareCondition(cmp.getLeftOperand(), cmp.getRightOperand(), CompareOperator.EQUAL);
            }
            return new BooleanCondition(val, true);
        }
        case Insn.IFNE: {
            if (dummy_stack.size() != 1) {
                throw new IllegalStateException();
            }
            Instruction val = dummy_stack.pop();
            if (val instanceof NumberCompare) {
                NumberCompare cmp = (NumberCompare) val;
                return new CompareCondition(cmp.getLeftOperand(), cmp.getRightOperand(), CompareOperator.NOT_EQUAL);
            }
            return new BooleanCondition(val, false);
        }
        case Insn.IF_CMPEQ:
        case Insn.IF_CMPNE:
        case Insn.IF_CMPLT:
        case Insn.IF_CMPLE:
        case Insn.IF_CMPGT:
        case Insn.IF_CMPGE: {
            if (dummy_stack.size() != 2) {
                throw new IllegalStateException();
            }
            Instruction b = dummy_stack.pop();
            Instruction a = dummy_stack.pop();
            return new CompareCondition(a, b, CompareCondition.fromOpcode(block.getLast().getOpcode()));
        }
        default:
            throw new IllegalStateException("Unsupported conditional jump opcode " + block.getLast().getOpcode());
        }
    }

    private static void dfs(ConditionGraphNode next, Deque<Condition> stack) {

        // performs a depth-first-search to populate each node in the graph's
        // partial conditions

        if (!stack.isEmpty()) {
            // Add the condition up to this point to the partial conditions of
            // this node. This represents a path from the root to this node and
            // the condition of that path is the and of all simple conditions of
            // the nodes along the path
            if (stack.size() == 1) {
                next.addPartialCondition(stack.peek());
            } else {
                Condition partial = new AndCondition(stack);
                next.addPartialCondition(partial);
            }
        }
        if (next.getSimpleCondition() == null) {
            return;
        }
        // Push the simple condition of this node to the stack and recurse into
        // the target branch
        stack.addLast(next.getSimpleCondition());
        dfs(next.getTarget(), stack);
        stack.pollLast();
        // Same thing for the else_target except we push the inverse of this
        // node's condition
        stack.addLast(inverse(next.getSimpleCondition()));
        dfs(next.getElseTarget(), stack);
        stack.pollLast();
    }

    /**
     * Converts the given set of {@link OpcodeBlock}s to a condition.
     */
    public static Condition makeCondition(List<ConditionalOpcodeBlock> blocks, Locals locals, OpcodeBlock body, OpcodeBlock ret) {
        List<ConditionGraphNode> nodes = new ArrayList<>(blocks.size());

        ConditionGraphNode body_node = new ConditionGraphNode(null);
        ConditionGraphNode ret_node = new ConditionGraphNode(null);

        // Forms a condition from a set of conditional jumps. This is done by
        // performing a depth first search of the nodes which form the
        // condition. Each path through the graph from root to the start of the
        // body is found and combined with OR to form a very much expanded
        // version of the condition for this block. This is then simplified
        // before being returned.

        for (int i = 0; i < blocks.size(); i++) {
            OpcodeBlock next = blocks.get(i);
            // make the nodes and compute the simple condition for each node.
            nodes.add(new ConditionGraphNode(makeSimpleCondition((ConditionalOpcodeBlock) next, locals)));
        }

        for (int i = 0; i < blocks.size(); i++) {
            ConditionalOpcodeBlock next = blocks.get(i);
            ConditionGraphNode node = nodes.get(i);
            // connect the nodes
            if (next.getTarget() == body) {
                node.setTarget(body_node);
            } else if (next.getTarget() == ret) {
                node.setTarget(ret_node);
            } else {
                int target = blocks.indexOf(next.getTarget());
                if (target == -1) {
                    throw new IllegalStateException("Condition target was unknown block " + next.getTarget().getStart());
                }
                node.setTarget(nodes.get(target));
            }
            if (next.getElseTarget() == body) {
                node.setElseTarget(body_node);
            } else if (next.getElseTarget() == ret) {
                node.setElseTarget(ret_node);
            } else {
                int target = blocks.indexOf(next.getElseTarget());
                if (target == -1) {
                    throw new IllegalStateException("Condition else target was unknown block " + next.getElseTarget().getStart());
                }
                node.setElseTarget(nodes.get(target));
            }
        }

        ConditionGraphNode start = nodes.get(0);

        // perform the dfs
        Deque<Condition> stack = new ArrayDeque<>();
        dfs(start, stack);

        OrCondition condition = new OrCondition(body_node.getPartialConditions());
        return ConditionUtil.simplifyCondition(condition);
    }

    private ConditionBuilder() {
    }

    /**
     * A node for converting a control flow graph to a condition.
     */
    private static class ConditionGraphNode {

        private final Condition condition;
        private final List<Condition> partial_conditions = new ArrayList<>();

        private ConditionGraphNode target;
        private ConditionGraphNode else_target;

        public ConditionGraphNode(Condition c) {
            this.condition = c;
        }

        public Condition getSimpleCondition() {
            return this.condition;
        }

        public ConditionGraphNode getTarget() {
            return this.target;
        }

        public void setTarget(ConditionGraphNode node) {
            this.target = node;
        }

        public ConditionGraphNode getElseTarget() {
            return this.else_target;
        }

        public void setElseTarget(ConditionGraphNode node) {
            this.else_target = node;
        }

        public List<Condition> getPartialConditions() {
            return this.partial_conditions;
        }

        public void addPartialCondition(Condition cond) {
            this.partial_conditions.add(cond);
        }
    }

}

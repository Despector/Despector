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
package org.spongepowered.despector.decompiler.method.graph.operate;

import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock.MarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BreakPrePassOperation implements GraphOperation {

    @Override
    public void process(PartialMethod partial) {
        List<OpcodeBlock> blocks = partial.getGraph();
        List<GotoOpcodeBlock> candidates = blocks.stream().filter((op) -> op instanceof GotoOpcodeBlock).map((op) -> (GotoOpcodeBlock) op)
                .collect(Collectors.toList());

        List<Loop> loops = new ArrayList<>();

        search: for (GotoOpcodeBlock ggoto : candidates) {
            OpcodeBlock target = ggoto.getTarget();
            int target_index = blocks.indexOf(target);
            if (target.getBreakpoint() < ggoto.getBreakpoint()) {
                // a back edge is either a while loop or a continue statement
                List<GotoOpcodeBlock> others = target.getTargettedBy().stream().filter((op) -> op instanceof GotoOpcodeBlock)
                        .map((op) -> (GotoOpcodeBlock) op).collect(Collectors.toList());
                int goto_index = blocks.indexOf(ggoto);
                for (GotoOpcodeBlock other : others) {
                    int other_index = blocks.indexOf(other);
                    if (other_index > goto_index) {
                        // if there is another goto targetting our target from
                        // farther in the opcodes then this goto is a continue
                        continue search;
                    }
                }
                Loop loop = new Loop();
                loop.start = Math.min(goto_index, target_index);
                loop.end = Math.max(goto_index, target_index);
                loop.ggoto = ggoto;
                loop.condition = target;
                loops.add(loop);
                continue;
            }
            // if this goto is pointing forwards but its not targetting a
            // condition then its either a break or a else/try-catch block,
            // we'll figure out which later
            if (target instanceof ConditionalOpcodeBlock) {
                // otherwise if this goto points forwards then we need to check
                // if the condition its targetting is forming the back edge
                boolean found = false;
                for (int i = target_index; i < blocks.size(); i++) {
                    OpcodeBlock next = blocks.get(i);
                    if (!(next instanceof ConditionalOpcodeBlock)) {
                        continue search;
                    }
                    ConditionalOpcodeBlock cond = (ConditionalOpcodeBlock) next;
                    if (blocks.indexOf(cond.getTarget()) < target_index) {
                        // we have a back edge
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
                List<GotoOpcodeBlock> others = target.getTargettedBy().stream().filter((op) -> op instanceof GotoOpcodeBlock)
                        .map((op) -> (GotoOpcodeBlock) op).collect(Collectors.toList());
                int goto_index = blocks.indexOf(ggoto);
                for (GotoOpcodeBlock other : others) {
                    int other_index = blocks.indexOf(other);
                    if (other_index < goto_index) {
                        // if there is another goto targetting our target from
                        // farther in the opcodes then this goto is a continue
                        continue search;
                    }
                }
                Loop loop = new Loop();
                loop.start = Math.min(goto_index, target_index);
                loop.end = Math.max(goto_index, target_index);
                loop.ggoto = ggoto;
                loop.condition = target;
                loops.add(loop);
            }
        }

        dowhile: for (OpcodeBlock block : blocks) {
            if (!(block instanceof ConditionalOpcodeBlock)) {
                continue;
            }
            ConditionalOpcodeBlock cond = (ConditionalOpcodeBlock) block;
            if (cond.getTarget().getBreakpoint() < cond.getBreakpoint()) {
                int i = blocks.indexOf(cond);
                OpcodeBlock prev = blocks.get(i);
                ConditionalOpcodeBlock first = cond;
                while (prev instanceof ConditionalOpcodeBlock) {
                    OpcodeBlock target = prev.getTarget();
                    if (target.getBreakpoint() < prev.getBreakpoint()) {
                        break;
                    }
                    first = (ConditionalOpcodeBlock) prev;
                    if (i == 0) {
                        break;
                    }
                    prev = blocks.get(--i);
                }
                for (Loop loop : loops) {
                    if (loop.condition == first) {
                        continue dowhile;
                    }
                }
                Loop loop = new Loop();
                loop.start = blocks.indexOf(cond.getTarget());
                loop.end = blocks.indexOf(first);
                loop.ggoto = null;
                loop.condition = first;
                loops.add(loop);

            }
        }

        outer: for (GotoOpcodeBlock ggoto : candidates) {
            int goto_index = blocks.indexOf(ggoto);
            for (Loop loop : loops) {
                if (loop.ggoto == ggoto) {
                    continue outer;
                }
            }
            MarkerType type = null;
            Loop found = null;
            int outermost = blocks.size();
            for (Loop loop : loops) {
                if (goto_index > loop.start && goto_index < loop.end) {
                    if (ggoto.getTarget() == loop.condition) {
                        // this is a continue
                        type = MarkerType.CONTINUE;
                        found = loop;
                        break;
                    } else if (blocks.indexOf(ggoto.getTarget()) > loop.end) {
                        // this is a break
                        if (loop.start < outermost) {
                            outermost = loop.start;
                            type = MarkerType.BREAK;
                            found = loop;
                        }
                    }
                }
            }
            if (type != null) {
                BreakMarkerOpcodeBlock replacement = new BreakMarkerOpcodeBlock(ggoto.getBreakpoint(), type);
                replacement.setTarget(ggoto.getTarget());
                replacement.getOpcodes().addAll(ggoto.getOpcodes());
                replacement.setMarked(found.condition);
                blocks.set(blocks.indexOf(ggoto), replacement);
                GraphOperation.remap(blocks, ggoto, replacement);
            }
        }
    }

    private static class Loop {

        public int start;
        public int end;
        public GotoOpcodeBlock ggoto;
        public OpcodeBlock condition;

        public Loop() {

        }
    }

}

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
package org.spongepowered.despector.decompiler.method.graph.region;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.insn.condition.BooleanCondition;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.DoWhileBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.WhileBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A region processor that checks if the region forms a do-while loop.
 */
public class DoWhileRegionProcessor implements RegionProcessor {

    @Override
    public BlockSection process(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start) {
        OpcodeBlock start = region.get(0);
        if (body_start == 0) {
            List<ConditionalOpcodeBlock> condition_blocks = new ArrayList<>();
            int cond_start = region.size() - 1;
            OpcodeBlock next = region.get(cond_start);
            while (next instanceof ConditionalOpcodeBlock) {
                condition_blocks.add(0, (ConditionalOpcodeBlock) next);
                cond_start--;
                next = region.get(cond_start);
            }
            Condition cond = null;
            if (condition_blocks.isEmpty()) {
                checkState(ret instanceof GotoOpcodeBlock);
                WhileBlockSection section = new WhileBlockSection(new BooleanCondition(new IntConstant(1), false));
                for (int i = 0; i < region.size(); i++) {
                    next = region.get(i);
                    section.appendBody(next.toBlockSection());
                }
                return section;
            }

            cond_start++;
            cond = ConditionBuilder.makeCondition(condition_blocks, partial.getLocals(), start, ret);

            boolean targetted_by_previous_jump = condition_blocks.get(0).getTargettedBy().stream()
                    .filter((b) -> b.getBreakpoint() < start.getBreakpoint()).findAny().isPresent();

            if (targetted_by_previous_jump) {
                WhileBlockSection section = new WhileBlockSection(cond);
                for (int i = 0; i < cond_start; i++) {
                    next = region.get(i);
                    section.appendBody(next.toBlockSection());
                }
                return section;
            }

            DoWhileBlockSection section = new DoWhileBlockSection(cond);

            for (int i = 0; i < cond_start; i++) {
                next = region.get(i);
                section.append(next.toBlockSection());
            }
            return section;
        }
        return null;
    }

}

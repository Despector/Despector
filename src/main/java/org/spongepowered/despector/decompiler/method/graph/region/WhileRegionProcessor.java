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

import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.BreakBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.BreakableBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.DoWhileBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.IfBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.IfBlockSection.ElifBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.SwitchBlockSection.SwitchCaseBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.TryCatchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.TryCatchBlockSection.CatchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.WhileBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.BreakMarkerOpcodeBlock.MarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A region processor that checks if the region forms a while loop.
 */
public class WhileRegionProcessor implements RegionProcessor {

    @Override
    public BlockSection process(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start) {
        OpcodeBlock start = region.get(0);
        if (start instanceof GotoOpcodeBlock) {
            List<ConditionalOpcodeBlock> condition_blocks = new ArrayList<>();
            OpcodeBlock next = start.getTarget();
            int pos = region.indexOf(next);
            int cond_start = pos;
            while (next instanceof ConditionalOpcodeBlock) {
                condition_blocks.add((ConditionalOpcodeBlock) next);
                pos++;
                if (pos >= region.size()) {
                    break;
                }
                next = region.get(pos);
            }

            OpcodeBlock body = region.get(1);
            Condition cond = ConditionBuilder.makeCondition(condition_blocks, partial.getLocals(), body, ret);

            WhileBlockSection section = new WhileBlockSection(cond);

            for (int i = 1; i < cond_start; i++) {
                next = region.get(i);
                BlockSection block = next.toBlockSection();
                replaceBreaks(block, start.getTarget(), ret, section, false);
                section.appendBody(block);
            }

            return section;
        }
        return null;
    }

    /**
     * Replaces break marker blocks within the region.
     */
    public static void replaceBreaks(BlockSection section, OpcodeBlock continue_point, OpcodeBlock break_point, BreakableBlockSection loop,
            boolean nested) {
        if (section instanceof BreakBlockSection) {
            BreakBlockSection bbreak = (BreakBlockSection) section;
            OpcodeBlock target = bbreak.getMarker().getOldTarget();
            if (bbreak.getType() == MarkerType.BREAK && target == break_point) {
                loop.getBreaks().add(bbreak);
                bbreak.setLoop((BlockSection) loop);
                bbreak.setNested(nested);
            } else if (bbreak.getType() == MarkerType.CONTINUE && target == continue_point) {
                loop.getBreaks().add(bbreak);
                bbreak.setLoop((BlockSection) loop);
                bbreak.setNested(nested);
            }
        } else if (section instanceof WhileBlockSection) {
            WhileBlockSection wwhile = (WhileBlockSection) section;
            for (BlockSection block : wwhile.getBody()) {
                replaceBreaks(block, continue_point, break_point, loop, true);
            }
        } else if (section instanceof IfBlockSection) {
            IfBlockSection iif = (IfBlockSection) section;
            for (BlockSection block : iif.getBody()) {
                replaceBreaks(block, continue_point, break_point, loop, nested);
            }
            for (BlockSection block : iif.getElseBody()) {
                replaceBreaks(block, continue_point, break_point, loop, nested);
            }
            for (ElifBlockSection elif : iif.getElifBlocks()) {
                for (BlockSection block : elif.getBody()) {
                    replaceBreaks(block, continue_point, break_point, loop, nested);
                }
            }
        } else if (section instanceof DoWhileBlockSection) {
            DoWhileBlockSection dowhile = (DoWhileBlockSection) section;
            for (BlockSection block : dowhile.getBody()) {
                replaceBreaks(block, continue_point, break_point, loop, true);
            }
        } else if (section instanceof SwitchBlockSection) {
            SwitchBlockSection sswitch = (SwitchBlockSection) section;
            for (SwitchCaseBlockSection cs : sswitch.getCases()) {
                for (BlockSection block : cs.getBody()) {
                    replaceBreaks(block, continue_point, break_point, loop, true);
                }
            }
        } else if (section instanceof TryCatchBlockSection) {
            TryCatchBlockSection trycatch = (TryCatchBlockSection) section;
            for (BlockSection block : trycatch.getBody()) {
                replaceBreaks(block, continue_point, break_point, loop, nested);
            }
            for (CatchBlockSection cb : trycatch.getCatchBlocks()) {
                for (BlockSection block : cb.getBody()) {
                    replaceBreaks(block, continue_point, break_point, loop, nested);
                }
            }
        }
    }

}

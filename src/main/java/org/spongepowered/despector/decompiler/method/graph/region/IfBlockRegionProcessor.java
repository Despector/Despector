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

import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.decompiler.method.ConditionBuilder;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.RegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.IfBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.IfBlockSection.ElifBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.WhileBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A region processor that tries to form an if-elseif-else block from the
 * region. This is designed to be called last when processing regions and will
 * always find an if statement or else error.
 */
public class IfBlockRegionProcessor implements RegionProcessor {

    @Override
    public BlockSection process(PartialMethod partial, List<OpcodeBlock> region, OpcodeBlock ret, int body_start) {
        OpcodeBlock start = region.get(0);
        // split the region into the condition and the body
        body_start = 1;
        List<ConditionalOpcodeBlock> condition_blocks = new ArrayList<>();
        OpcodeBlock next = region.get(body_start);
        condition_blocks.add((ConditionalOpcodeBlock) start);
        while (next instanceof ConditionalOpcodeBlock) {
            condition_blocks.add((ConditionalOpcodeBlock) next);
            body_start++;
            next = region.get(body_start);
        }
        OpcodeBlock body = region.get(body_start);
        OpcodeBlock cond_ret = ret;
        for (OpcodeBlock c : condition_blocks) {
            if (c.getTarget() != body && !condition_blocks.contains(c.getTarget())) {
                cond_ret = c.getTarget();
                break;
            }
        }
        for (OpcodeBlock c : condition_blocks) {
            if (c instanceof ConditionalOpcodeBlock) {
                ConditionalOpcodeBlock cond = (ConditionalOpcodeBlock) c;
                if (cond.getTarget().equals(body) && cond.getTarget() != body) {
                    cond.setTarget(body);
                }
                if (cond.getElseTarget().equals(body) && cond.getElseTarget() != body) {
                    cond.setElseTarget(body);
                }
                if (cond.getTarget().equals(cond_ret) && cond.getTarget() != cond_ret) {
                    cond.setTarget(cond_ret);
                }
                if (cond.getElseTarget().equals(cond_ret) && cond.getElseTarget() != cond_ret) {
                    cond.setElseTarget(cond_ret);
                }
            }
        }

        // form the condition from the header
        Condition cond = ConditionBuilder.makeCondition(condition_blocks, partial.getLocals(), body, cond_ret);
        int else_start = region.size();
        if (cond_ret != ret && region.contains(cond_ret)) {
            else_start = region.indexOf(cond_ret);
        }

        OpcodeBlock body_end = region.get(else_start - 1);
        if (body_end instanceof GotoOpcodeBlock && body_end.getTarget() == start) {
            // we've got ourselves an inverse while/for loop
            WhileBlockSection section = new WhileBlockSection(cond);
            for (int i = body_start; i < else_start - 1; i++) {
                next = region.get(i);
                section.appendBody(next.toBlockSection());
            }
            return section;
        }
        if (body_end instanceof GotoOpcodeBlock) {
            else_start--;
        }

        IfBlockSection section = new IfBlockSection(cond);
        // Append the body
        for (int i = body_start; i < else_start; i++) {
            next = region.get(i);
            if (next instanceof ConditionalOpcodeBlock) {
                List<OpcodeBlock> subregion = new ArrayList<>();
                for (int o = i; o < else_start; o++) {
                    OpcodeBlock n = region.get(o);
                    subregion.add(n);
                }

                OpcodeBlock sub_ret = else_start >= region.size() ? ret : region.get(else_start);
                BlockSection s = partial.getDecompiler().processRegion(partial, subregion, sub_ret, 1);
                section.appendBody(s);
                break;
            }
            section.appendBody(next.toBlockSection());
        }

        while (region.contains(cond_ret)) {
            if (cond_ret instanceof ConditionalOpcodeBlock) {
                List<ConditionalOpcodeBlock> elif_condition = new ArrayList<>();
                next = region.get(body_start);
                elif_condition.add((ConditionalOpcodeBlock) cond_ret);
                body_start = region.indexOf(cond_ret) + 1;
                while (next instanceof ConditionalOpcodeBlock) {
                    elif_condition.add((ConditionalOpcodeBlock) next);
                    body_start++;
                    next = region.get(body_start);
                }
                OpcodeBlock elif_body = region.get(body_start);
                cond_ret = ret;
                for (OpcodeBlock c : elif_condition) {
                    if (c.getTarget() != elif_body && !elif_condition.contains(c.getTarget())) {
                        cond_ret = c.getTarget();
                        break;
                    }
                }
                Condition elif_cond = ConditionBuilder.makeCondition(elif_condition, partial.getLocals(), elif_body, cond_ret);
                ElifBlockSection elif = section.new ElifBlockSection(elif_cond);
                int elif_end = region.size();
                if (cond_ret != ret) {
                    elif_end = region.indexOf(cond_ret);
                }
                if (region.get(elif_end - 1) instanceof GotoOpcodeBlock) {
                    elif_end--;
                }
                // Append the body
                for (int i = body_start; i < elif_end; i++) {
                    next = region.get(i);
                    elif.append(next.toBlockSection());
                }
            } else {
                else_start = region.indexOf(cond_ret);
                for (int i = else_start; i < region.size(); i++) {
                    next = region.get(i);
                    section.appendElseBody(next.toBlockSection());
                }
                break;
            }
        }

        return section;
    }

}

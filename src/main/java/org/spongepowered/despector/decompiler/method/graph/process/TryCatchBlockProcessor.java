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
package org.spongepowered.despector.decompiler.method.graph.process;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.IntInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphProcessor;
import org.spongepowered.despector.decompiler.method.graph.data.TryCatchMarkerType;
import org.spongepowered.despector.decompiler.method.graph.data.block.BlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.CommentBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.TryCatchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.block.TryCatchBlockSection.CatchBlockSection;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.TryCatchMarkerOpcodeBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A graph processor to process try-catch blocks.
 */
public class TryCatchBlockProcessor implements GraphProcessor {

    @Override
    public int process(PartialMethod partial, List<OpcodeBlock> blocks, OpcodeBlock region_start, List<BlockSection> final_blocks) {
        if (region_start instanceof TryCatchMarkerOpcodeBlock) {
            TryCatchMarkerOpcodeBlock marker = (TryCatchMarkerOpcodeBlock) region_start;
            checkState(marker.getType() == TryCatchMarkerType.START);
            List<OpcodeBlock> body = new ArrayList<>();
            List<TryCatchMarkerOpcodeBlock> all_ends = new ArrayList<>();

            for (int l = blocks.indexOf(marker.getEndMarker()); l < blocks.size(); l++) {
                OpcodeBlock next_end = blocks.get(l);
                if (!(next_end instanceof TryCatchMarkerOpcodeBlock)) {
                    break;
                }
                TryCatchMarkerOpcodeBlock next_marker = (TryCatchMarkerOpcodeBlock) next_end;
                checkState(next_marker.getType() == TryCatchMarkerType.END);
                all_ends.add(next_marker);
            }
            TryCatchMarkerOpcodeBlock last_start = all_ends.get(all_ends.size() - 1).getStartMarker();
            TryCatchMarkerOpcodeBlock first_end = all_ends.get(0);
            for (int end = blocks.indexOf(last_start) + 1; end < blocks.indexOf(first_end); end++) {
                OpcodeBlock next = blocks.get(end);
                body.add(next);
            }
            int end = blocks.indexOf(all_ends.get(all_ends.size() - 1)) + 1;
            OpcodeBlock next = blocks.get(end);
            OpcodeBlock end_of_catch = null;
            int last_block = -1;
            if (next instanceof GotoOpcodeBlock) {
                end_of_catch = next.getTarget();
                last_block = blocks.indexOf(end_of_catch);
                if (last_block > 1) {
                    OpcodeBlock prev = blocks.get(last_block - 1);
                    if (prev instanceof TryCatchMarkerOpcodeBlock && ((TryCatchMarkerOpcodeBlock) prev).getType() == TryCatchMarkerType.START) {
                        last_block -= 2;
                    }
                }
            } else {
                body.add(next);
            }
            TryCatchBlockSection try_section = new TryCatchBlockSection();
            try {
                partial.getDecompiler().flattenGraph(partial, body, body.size(), try_section.getBody());
            } catch (Exception e) {
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    List<String> comment = new ArrayList<>();
                    for (OpcodeBlock op : body) {
                        comment.add(op.getDebugHeader());
                        for (Insn insn : op.getOpcodes()) {
                            comment.add(insn.toString());
                        }
                    }
                    try_section.getBody().add(new CommentBlockSection(comment));
                } else {
                    throw e;
                }
            }
            while (!all_ends.isEmpty()) {
                end++;
                next = blocks.get(end);
                if (next instanceof TryCatchMarkerOpcodeBlock) {
                    TryCatchMarkerOpcodeBlock next_marker = (TryCatchMarkerOpcodeBlock) next;
                    checkState(next_marker.getType() == TryCatchMarkerType.CATCH);
                    List<String> extra_exceptions = new ArrayList<>();
                    for (; end < blocks.size();) {
                        OpcodeBlock cnext = blocks.get(end++);
                        if (cnext instanceof TryCatchMarkerOpcodeBlock) {
                            TryCatchMarkerOpcodeBlock cnext_marker = (TryCatchMarkerOpcodeBlock) cnext;
                            boolean found = false;
                            for (Iterator<TryCatchMarkerOpcodeBlock> it = all_ends.iterator(); it.hasNext();) {
                                TryCatchMarkerOpcodeBlock t = it.next();
                                if (cnext_marker.getAsmNode() == t.getAsmNode()) {
                                    found = true;
                                    it.remove();
                                    break;
                                }
                            }
                            checkState(found);
                            extra_exceptions.add(cnext_marker.getAsmNode().getException());
                        } else {
                            end--;
                            break;
                        }
                    }
                    Collections.reverse(extra_exceptions);
                    int label_index = -1;
                    int local_num = -1;
                    OpcodeBlock catch_start = blocks.get(end++);
                    int k = 0;
                    for (Iterator<Insn> it = catch_start.getOpcodes().iterator(); it.hasNext();) {
                        Insn op = it.next();
                        if (op.getOpcode() == Insn.LOCAL_STORE) {
                            local_num = ((IntInsn) op).getValue();
                            label_index = catch_start.getBreakpoint() - (catch_start.getOpcodes().size() - k);
                            it.remove();
                            break;
                        } else if (op.getOpcode() == Insn.POP) {
                            it.remove();
                            break;
                        }
                        k++;
                    }
                    Locals.LocalInstance local = label_index == -1 ? null : partial.getLocals().getLocal(local_num).getInstance(label_index);
                    List<OpcodeBlock> catch_body = new ArrayList<>();
                    catch_body.add(catch_start);
                    int stop_index = -1;
                    if (end_of_catch != null && last_block != -1) {
                        for (int j = end; j < blocks.size(); j++) {
                            OpcodeBlock cnext = blocks.get(j);
                            if (cnext instanceof TryCatchMarkerOpcodeBlock) {
                                break;
                            }
                            catch_body.add(cnext);
                            if (cnext instanceof GotoOpcodeBlock && cnext.getTarget() == end_of_catch) {
                                break;
                            } else if (cnext == end_of_catch) {
                                all_ends.clear();
                                break;
                            }
                        }
                        stop_index = catch_body.size() - 1;
                    } else {
                        // TODO: if we have no lvt I'll need to do some
                        // backup check of checking where the catch var is
                        // last used and stopping there
                        for (int j = end; j < blocks.size(); j++) {
                            OpcodeBlock cnext = blocks.get(j);
                            if (cnext.getBreakpoint() > local.getEnd()) {
                                break;
                            }
                            catch_body.add(cnext);
                        }

                        last_block = blocks.indexOf(catch_body.get(catch_body.size() - 1));
                        stop_index = catch_body.size();
                    }
                    CatchBlockSection cblock = new CatchBlockSection(extra_exceptions, local);
                    try {
                        partial.getDecompiler().flattenGraph(partial, catch_body, stop_index, cblock.getBody());
                    } catch (Exception e) {
                        if (ConfigManager.getConfig().print_opcodes_on_error) {
                            List<String> comment = new ArrayList<>();
                            for (OpcodeBlock op : catch_body) {
                                comment.add(op.getDebugHeader());
                                for (Insn insn : op.getOpcodes()) {
                                    comment.add(insn.toString());
                                }
                            }
                            cblock.getBody().add(new CommentBlockSection(comment));
                        } else {
                            throw e;
                        }
                    }
                    try_section.getCatchBlocks().add(cblock);
                }
            }
            final_blocks.add(try_section);
            return last_block;
        }
        return -1;
    }

}

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
package org.spongepowered.despector.decompiler.method.postprocess;

import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;

/**
 * A post processor which cleans up nested if statements that can be simplified.
 */
public class IfCombiningPostProcessor implements StatementPostProcessor {

    @Override
    public void postprocess(StatementBlock block) {
        for (Statement stmt : block.getStatements()) {
            if (stmt instanceof If) {
                check((If) stmt);
            } else if (stmt instanceof While) {
                While wwhile = (While) stmt;
                postprocess(wwhile.getBody());
            } else if (stmt instanceof DoWhile) {
                DoWhile dowhile = (DoWhile) stmt;
                postprocess(dowhile.getBody());
            } else if (stmt instanceof For) {
                For ffor = (For) stmt;
                postprocess(ffor.getBody());
            } else if (stmt instanceof Switch) {
                Switch sswitch = (Switch) stmt;
                for (Case cs : sswitch.getCases()) {
                    postprocess(cs.getBody());
                }
            } else if (stmt instanceof TryCatch) {
                TryCatch trycatch = (TryCatch) stmt;
                postprocess(trycatch.getTryBlock());
                for (CatchBlock ccatch : trycatch.getCatchBlocks()) {
                    postprocess(ccatch.getBlock());
                }
            }
        }
    }

    /**
     * Checks if the given if statement can be simplified.
     */
    public void check(If ifblock) {
        if (ifblock.getElifBlocks().isEmpty() && ifblock.getElseBlock() == null) {
            StatementBlock block = ifblock.getIfBody();
            if (block.getStatementCount() == 1 && block.getStatement(0) instanceof If) {
                If inner = (If) block.getStatement(0);
                if (inner.getElifBlocks().isEmpty() && inner.getElseBlock() == null) {
                    ifblock.setCondition(new AndCondition(ifblock.getCondition(), inner.getCondition()));
                    block.getStatements().clear();
                    for (Statement stmt : inner.getIfBody().getStatements()) {
                        block.append(stmt);
                    }
                    check(ifblock);
                }
            }
        }
        postprocess(ifblock.getIfBody());
        if (!ifblock.getElifBlocks().isEmpty()) {
            for (int i = 0; i < ifblock.getElifBlocks().size() - 1; i++) {
                postprocess(ifblock.getElifBlocks().get(i).getBody());
            }
            Elif last_elif = ifblock.getElifBlocks().get(ifblock.getElifBlocks().size() - 1);
            StatementBlock block = last_elif.getBody();
            if (ifblock.getElseBlock() == null) {
                if (block.getStatementCount() == 1 && block.getStatement(0) instanceof If) {
                    If inner = (If) block.getStatement(0);
                    if (inner.getElifBlocks().isEmpty() && inner.getElseBlock() == null) {
                        last_elif.setCondition(new AndCondition(last_elif.getCondition(), inner.getCondition()));
                        block.getStatements().clear();
                        for (Statement stmt : inner.getIfBody().getStatements()) {
                            block.append(stmt);
                        }
                    }
                }
            }
            postprocess(block);
        }
        if (ifblock.getElseBlock() != null) {
            StatementBlock block = ifblock.getElseBlock().getElseBody();
            if (block.getStatementCount() == 1 && block.getStatement(0) instanceof If) {
                If inner = (If) block.getStatement(0);
                ifblock.new Elif(inner.getCondition(), inner.getIfBody());
                postprocess(inner.getIfBody());
                for (int i = 0; i < inner.getElifBlocks().size(); i++) {
                    Elif elif = inner.getElifBlocks().get(i);
                    postprocess(elif.getBody());
                    ifblock.new Elif(elif.getCondition(), elif.getBody());
                }
                block.getStatements().clear();
                if (inner.getElseBlock() != null) {
                    block.getStatements().addAll(inner.getElseBlock().getElseBody().getStatements());
                } else {
                    ifblock.setElseBlock(null);
                }
            } else {
                postprocess(block);
            }
        }

    }

}

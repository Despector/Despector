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

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.Break;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A post processor that converts while loops to for loops where it is
 * determined likely that the original structure was a for loop.
 */
public class ForFromWhilePostProcessor implements StatementPostProcessor {

    private static final StatementMatcher<?> STORE = MatchContext.storeLocal("loop_val", StatementMatcher.localassign()
            .build());
    private static final StatementMatcher<?> LOOP = StatementMatcher.whileloop()
            .condition(ConditionMatcher.references("loop_val"))
            .build();

    @Override
    public void postprocess(StatementBlock block) {
        List<Statement> to_remove = new ArrayList<>();
        for (Statement stmt : block.getStatements()) {
            if (stmt instanceof If) {
                If iif = (If) stmt;
                postprocess(iif.getIfBody());
                for (Elif elif : iif.getElifBlocks()) {
                    postprocess(elif.getBody());
                }
                if (iif.getElseBlock() != null) {
                    postprocess(iif.getElseBlock().getElseBody());
                }
            } else if (stmt instanceof While) {
                While wwhile = (While) stmt;
                checkWhile(block, wwhile, to_remove);
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
        for (Statement stmt : to_remove) {
            block.getStatements().remove(stmt);
        }
    }

    private void checkWhile(StatementBlock block, While wwhile, List<Statement> to_remove) {
        int i = block.getStatements().indexOf(wwhile);
        if (i == 0) {
            return;
        }
        Statement last = block.getStatement(i - 1);
        MatchContext ctx = MatchContext.create();
        if (!STORE.matches(ctx, last)) {
            return;
        }
        if (!LOOP.matches(ctx, wwhile)) {
            return;
        }
        LocalInstance loop_val = ctx.getLocal("loop_val");
        for (int o = i + 1; o < block.getStatementCount(); o++) {
            Statement n = block.getStatement(o);
            if (n instanceof LocalAssignment) {
                if (((LocalAssignment) n).getLocal() == loop_val) {
                    break;
                }
            }
            if (AstUtil.references(block.getStatement(o), loop_val)) {
                return;
            }
        }
        StatementBlock body = wwhile.getBody();
        Increment increment = null;
        if (!body.getStatements().isEmpty()) {
            Statement body_last = body.getStatements().get(body.getStatements().size() - 1);
            if (body_last instanceof Increment && ((Increment) body_last).getLocal() == loop_val) {
                increment = (Increment) body_last;
            }
        }
        if (increment != null) {
            body.getStatements().remove(increment);
        }
        For ffor = new For(last, wwhile.getCondition(), increment, body);
        for (Break bbreak : wwhile.getBreaks()) {
            bbreak.setLoop(ffor);
        }
        to_remove.add(last);
        block.getStatements().set(i, ffor);
    }

}

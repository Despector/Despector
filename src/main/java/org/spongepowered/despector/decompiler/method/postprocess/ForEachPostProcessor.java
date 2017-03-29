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
package org.spongepowered.despector.decompiler.method.postprocess;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.StatementMatcher;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.List;

public class ForEachPostProcessor implements StatementPostProcessor {

    private static final StatementMatcher<For> LIST_ITERATOR = StatementMatcher.forloop()
            .init(MatchContext.storeLocal("list_iterator", StatementMatcher.localassign()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("iterator")
                            .build())
                    .build()))
            .condition(ConditionMatcher.bool()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("hasNext")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("list_iterator")
                                    .build())
                            .build())
                    .build())
            .incr(StatementMatcher.NONE)
            .body(0, StatementMatcher.localassign()
                    .value(InstructionMatcher.instanceinvoke()
                            .name("next")
                            .callee(InstructionMatcher.localaccess()
                                    .fromContext("list_iterator")
                                    .build())
                            .autoUnwrap()
                            .build())
                    .autoUnwrap()
                    .build())
            .build();

    private static final StatementMatcher<?> ARRAY_ITERATOR_ASSIGN = MatchContext.storeLocal("array", StatementMatcher.localassign().build());
    private static final StatementMatcher<?> ARRAY_ITERATOR_SIZE = MatchContext.storeLocal("array_size", StatementMatcher.localassign()
            .type(ClassTypeSignature.INT)
            .value(InstructionMatcher.instancefield()
                    .name("length")
                    .owner(InstructionMatcher.localaccess()
                            .fromContext("array")
                            .build())
                    .build())
            .build());
    private static final StatementMatcher<?> ARRAY_ITERATOR = StatementMatcher.forloop()
            .init(MatchContext.storeLocal("index", StatementMatcher.localassign()
                    .value(InstructionMatcher.intconstant()
                            .value(0)
                            .build())
                    .build()))
            .incr(StatementMatcher.increment()
                    .build())
            .body(0, StatementMatcher.localassign()
                    .value(InstructionMatcher.arrayaccess()
                            .array(InstructionMatcher.localaccess()
                                    .fromContext("array")
                                    .build())
                            .index(InstructionMatcher.localaccess()
                                    .fromContext("index")
                                    .build())
                            .build())
                    .build())
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
                postprocess(wwhile.getBody());
            } else if (stmt instanceof DoWhile) {
                DoWhile dowhile = (DoWhile) stmt;
                postprocess(dowhile.getBody());
            } else if (stmt instanceof For) {
                if (!checkIterator(block, (For) stmt)) {
                    checkArray(block, (For) stmt, to_remove);
                }
                postprocess(((For) stmt).getBody());
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

    public boolean checkIterator(StatementBlock block, For ffor) {
        if (!LIST_ITERATOR.matches(MatchContext.create(), ffor)) {
            return false;
        }
        LocalInstance local = ((LocalAssignment) ffor.getInit()).getLocal();

        for (int o = 1; o < ffor.getBody().getStatementCount(); o++) {
            Statement stmt = ffor.getBody().getStatement(o);
            if (AstUtil.references(stmt, local)) {
                return false;
            }
        }
        LocalInstance next_assign = ((LocalAssignment) ffor.getBody().getStatement(0)).getLocal();
        Instruction list = ((InstanceMethodInvoke) ((LocalAssignment) ffor.getInit()).getValue()).getCallee();
        ffor.getBody().getStatements().remove(0);

        ForEach foreach = new ForEach(list, next_assign, ffor.getBody());
        block.getStatements().set(block.getStatements().indexOf(ffor), foreach);

        return true;
    }

    public boolean checkArray(StatementBlock block, For ffor, List<Statement> to_remove) {
        int i = block.getStatements().indexOf(ffor);
        if (i < 2) {
            return false;
        }
        if (ffor.getBody().getStatementCount() < 1) {
            return false;
        }
        MatchContext ctx = MatchContext.create();

        if (!ARRAY_ITERATOR_ASSIGN.matches(ctx, block.getStatement(i - 2))) {
            return false;
        }
        LocalAssignment array_assign = (LocalAssignment) block.getStatement(i - 2);
        LocalInstance array = array_assign.getLocal();
        if (!array.getType().isArray()) {
            return false;
        }
        if (!ARRAY_ITERATOR_SIZE.matches(ctx, block.getStatement(i - 1))) {
            return false;
        }

        if (!ARRAY_ITERATOR.matches(ctx, ffor)) {
            return false;
        }

        for (int o = 1; o < ffor.getBody().getStatementCount(); o++) {
            Statement stmt = ffor.getBody().getStatement(o);
            if (AstUtil.references(stmt, ((LocalAssignment) ffor.getInit()).getLocal())
                    || AstUtil.references(stmt, ((LocalAssignment) block.getStatement(i - 1)).getLocal())) {
                return false;
            }
        }

        to_remove.add(block.getStatement(i - 2));
        to_remove.add(block.getStatement(i - 1));
        LocalInstance local = ((LocalAssignment) ffor.getBody().getStatement(0)).getLocal();
        ffor.getBody().getStatements().remove(0);

        ForEach foreach = new ForEach(array_assign.getValue(), local, ffor.getBody());
        block.getStatements().set(block.getStatements().indexOf(ffor), foreach);

        return true;
    }

}

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
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
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
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.StatementMatcher;
import org.spongepowered.despector.util.AstUtil;

import java.util.ArrayList;
import java.util.List;

public class ForEachPostProcessor implements StatementPostProcessor {

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
        if (ffor.getInit() == null || !(ffor.getInit() instanceof LocalAssignment)) {
            return false;
        }
        if (ffor.getIncr() != null) {
            return false;
        }
        if (ffor.getBody().getStatementCount() == 0) {
            return false;
        }
        LocalAssignment init = (LocalAssignment) ffor.getInit();
        InstanceMethodInvoke init_invoke = InstructionMatcher.requireInstanceMethodInvoke(init.getValue(), "iterator", "()Ljava/util/Iterator;");
        if (init_invoke == null) {
            return false;
        }
        Instruction condition = ConditionMatcher.requireBooleanCondition(ffor.getCondition());
        if (condition == null) {
            return false;
        }
        InstanceMethodInvoke condition_invoke = InstructionMatcher.requireInstanceMethodInvoke(condition, "hasNext", "()Z");
        if (condition_invoke == null) {
            return false;
        }
        if (!InstructionMatcher.isLocalAccess(condition_invoke.getCallee(), init.getLocal())) {
            return false;
        }
        LocalAssignment next_assign = StatementMatcher.requireLocalAssignment(ffor.getBody().getStatement(0));
        if (next_assign == null) {
            return false;
        }
        InstanceMethodInvoke next_invoke =
                InstructionMatcher.requireInstanceMethodInvoke(InstructionMatcher.unwrapCast(next_assign.getValue()), "next", null);
        if (next_invoke == null) {
            return false;
        }
        if (!(next_invoke.getCallee() instanceof LocalAccess)) {
            return false;
        }
        if (!((LocalAccess) next_invoke.getCallee()).getLocal().equals(init.getLocal())) {
            return false;
        }

        for (int o = 1; o < ffor.getBody().getStatementCount(); o++) {
            Statement stmt = ffor.getBody().getStatement(o);
            if (AstUtil.references(stmt, init.getLocal())) {
                return false;
            }
        }

        ffor.getBody().getStatements().remove(next_assign);

        ForEach foreach = new ForEach(init_invoke.getCallee(), next_assign.getLocal(), ffor.getBody());
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
        LocalAssignment array_assign = StatementMatcher.requireLocalAssignment(block.getStatement(i - 2));
        if (array_assign == null) {
            return false;
        }
        LocalInstance array = array_assign.getLocal();
        if (!array.getType().startsWith("[")) {
            return false;
        }
        String value_type = array.getType().substring(1);
        LocalAssignment size_assign = StatementMatcher.requireLocalAssignment(block.getStatement(i - 1));
        if (size_assign == null) {
            return false;
        }
        LocalInstance size = size_assign.getLocal();
        if (!"I".equals(size.getType())) {
            return false;
        }
        if (!InstructionMatcher.isInstanceFieldAccess(size_assign.getValue(), "length", new LocalAccess(array))) {
            return false;
        }
        LocalAssignment index_assign = StatementMatcher.requireLocalAssignment(ffor.getInit());
        if (index_assign == null) {
            return false;
        }
        LocalInstance index = index_assign.getLocal();
        if (!InstructionMatcher.isIntConstant(index_assign.getValue(), 0)) {
            return false;
        }
        Increment incr = StatementMatcher.requireIncrement(ffor.getIncr());
        if (incr == null) {
            return false;
        }
        LocalAssignment value_assign = StatementMatcher.requireLocalAssignment(ffor.getBody().getStatement(0));
        if (value_assign == null) {
            return false;
        }
        if (!InstructionMatcher.isArrayAccess(value_assign.getValue(), new LocalAccess(array), new LocalAccess(index))) {
            return false;
        }
        LocalInstance value = value_assign.getLocal();
        if (!value_type.equals(value.getType())) {
            return false;
        }

        for (int o = 1; o < ffor.getBody().getStatementCount(); o++) {
            Statement stmt = ffor.getBody().getStatement(o);
            if (AstUtil.references(stmt, index) || AstUtil.references(stmt, size)) {
                return false;
            }
        }

        to_remove.add(array_assign);
        to_remove.add(size_assign);
        ffor.getBody().getStatements().remove(value_assign);

        ForEach foreach = new ForEach(array_assign.getValue(), value, ffor.getBody());
        block.getStatements().set(block.getStatements().indexOf(ffor), foreach);

        return true;
    }

}

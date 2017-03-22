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

import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
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
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;

public class ForEachPostProcessor implements StatementPostProcessor {

    @Override
    public void postprocess(StatementBlock block) {
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
                if (!check(block, (For) stmt)) {
                    postprocess(((For) stmt).getBody());
                }
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

    public boolean check(StatementBlock block, For ffor) {
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
        if (!(init.getValue() instanceof InstanceMethodInvoke)) {
            return false;
        }
        InstanceMethodInvoke init_invoke = (InstanceMethodInvoke) init.getValue();
        if (!"iterator".equals(init_invoke.getMethodName())) {
            return false;
        }
        if (!"()Ljava/util/Iterator;".equals(init_invoke.getMethodDescription())) {
            return false;
        }
        if (!(ffor.getCondition() instanceof BooleanCondition)) {
            return false;
        }
        Instruction condition = ((BooleanCondition) ffor.getCondition()).getConditionValue();
        if (!(condition instanceof InstanceMethodInvoke)) {
            return false;
        }
        InstanceMethodInvoke condition_invoke = (InstanceMethodInvoke) condition;
        if (!(condition_invoke.getCallee() instanceof LocalAccess)) {
            return false;
        }
        if (!((LocalAccess) condition_invoke.getCallee()).getLocal().equals(init.getLocal())) {
            return false;
        }
        if (!"hasNext".equals(condition_invoke.getMethodName())) {
            return false;
        }
        if (!"()Z".equals(condition_invoke.getMethodDescription())) {
            return false;
        }
        Statement first = ffor.getBody().getStatement(0);
        if (!(first instanceof LocalAssignment)) {
            return false;
        }
        LocalAssignment next_assign = (LocalAssignment) first;
        InstanceMethodInvoke next_invoke = null;
        if (!(next_assign.getValue() instanceof InstanceMethodInvoke)) {
            if (next_assign.getValue() instanceof Cast) {
                Cast cst = (Cast) next_assign.getValue();
                if (!(cst.getValue() instanceof InstanceMethodInvoke)) {
                    return false;
                }
                next_invoke = (InstanceMethodInvoke) cst.getValue();
            } else {
                return false;
            }
        } else {
            next_invoke = (InstanceMethodInvoke) next_assign.getValue();
        }
        if (!(next_invoke.getCallee() instanceof LocalAccess)) {
            return false;
        }
        if (!((LocalAccess) next_invoke.getCallee()).getLocal().equals(init.getLocal())) {
            return false;
        }
        if (!"next".equals(next_invoke.getMethodName())) {
            return false;
        }

        ffor.getBody().getStatements().remove(next_assign);

        ForEach foreach = new ForEach(init_invoke.getCallee(), next_assign.getLocal(), ffor.getBody());
        block.getStatements().set(block.getStatements().indexOf(ffor), foreach);

        return true;
    }

}

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
package org.spongepowered.despector.decompiler.kotlin.method.postprocess;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.DoWhile;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.If.Elif;
import org.spongepowered.despector.ast.stmt.branch.If.Else;
import org.spongepowered.despector.ast.stmt.branch.Switch;
import org.spongepowered.despector.ast.stmt.branch.Switch.Case;
import org.spongepowered.despector.ast.stmt.branch.TryCatch;
import org.spongepowered.despector.ast.stmt.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.stmt.branch.While;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;
import org.spongepowered.despector.decompiler.method.postprocess.StatementPostProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * A post processing statement for determining which locals are mutated after
 * assignment.
 */
public class KotlinLocalsMutabilityPostProcess implements StatementPostProcessor {

    @Override
    public void postprocess(StatementBlock block) {
        LocalMutabilityVisitor visitor = new LocalMutabilityVisitor();
        block.accept(visitor);
    }

    /**
     * An instruction visitor for determining which locals are mutated.
     */
    private static class LocalMutabilityVisitor implements StatementVisitor {

        private final Set<LocalInstance> defined = new HashSet<>();

        public LocalMutabilityVisitor() {

        }

        private void check(LocalInstance local) {
            if (this.defined.contains(local)) {
                local.setEffectivelyFinal(false);
                return;
            }
            this.defined.add(local);
            local.setEffectivelyFinal(true);
        }

        @Override
        public void visitLocalAssignment(LocalAssignment assign) {
            LocalInstance local = assign.getLocal();
            check(local);
        }

        @Override
        public void visitIncrement(Increment incr) {
            LocalInstance local = incr.getLocal();
            check(local);
        }

        @Override
        public void visitArrayAssignment(ArrayAssignment arrayAssign) {
        }

        @Override
        public void visitBreak(Break breakStatement) {
        }

        @Override
        public void visitCatchBlock(CatchBlock catchBlock) {
        }

        @Override
        public void visitComment(Comment comment) {
        }

        @Override
        public void visitDoWhile(DoWhile doWhileLoop) {
        }

        @Override
        public void visitElif(Elif elseBlock) {
        }

        @Override
        public void visitElse(Else elseBlock) {
        }

        @Override
        public void visitFor(For forLoop) {
        }

        @Override
        public void visitForEach(ForEach forLoop) {
        }

        @Override
        public void visitIf(If ifBlock) {
        }

        @Override
        public void visitInstanceFieldAssignment(InstanceFieldAssignment stmt) {
        }

        @Override
        public void visitInvoke(InvokeStatement stmt) {
        }

        @Override
        public void visitReturn(Return returnValue) {
        }

        @Override
        public void visitStaticFieldAssignment(StaticFieldAssignment staticFieldAssign) {
        }

        @Override
        public void visitSwitch(Switch tableSwitch) {
        }

        @Override
        public void visitSwitchCase(Case case1) {
        }

        @Override
        public void visitThrow(Throw throwException) {
        }

        @Override
        public void visitTryCatch(TryCatch tryBlock) {
        }

        @Override
        public void visitWhile(While whileLoop) {
        }

    }

}

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
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.decompiler.method.postprocess.StatementPostProcessor;

import java.util.HashSet;
import java.util.Set;

public class KotlinLocalsMutabilityPostProcess implements StatementPostProcessor {

    @Override
    public void postprocess(StatementBlock block) {
        LocalMutabilityVisitor visitor = new LocalMutabilityVisitor();
        block.accept(visitor);
    }

    private static class LocalMutabilityVisitor extends InstructionVisitor {

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

    }

}

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
package org.spongepowered.despector.util;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.transform.matcher.instruction.LocalAccessMatcher;

public class TypeInferrer {

    public static TypeSignature infer(SourceSet source, StatementBlock block, LocalInstance local) {
        Walker walker = new Walker(source, local);
        block.accept(walker);
        return walker.getBestGuess();
    }

    private static class Walker extends InstructionVisitor {

        private SourceSet source;
        private final LocalInstance local;
        private TypeSignature best_guess;
        private LocalAccessMatcher matcher;
        private MatchContext ctx = MatchContext.create();

        public Walker(SourceSet source, LocalInstance local) {
            this.source = source;
            this.local = local;
            this.best_guess = local.getType();
            this.matcher = InstructionMatcher.localaccess().local(this.local).build();
        }

        public TypeSignature getBestGuess() {
            return this.best_guess;
        }

        @Override
        public void visitInstanceMethodInvoke(InstanceMethodInvoke invoke) {
            if (this.matcher.matches(this.ctx, invoke.getCallee())) {
                
            }
        }

    }

}

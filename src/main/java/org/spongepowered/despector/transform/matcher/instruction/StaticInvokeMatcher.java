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
package org.spongepowered.despector.transform.matcher.instruction;

import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.transform.matcher.InstructionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;

public class StaticInvokeMatcher implements InstructionMatcher<StaticMethodInvoke> {

    private String owner;
    private String name;
    private String desc;

    StaticInvokeMatcher(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public StaticMethodInvoke match(MatchContext ctx, Instruction insn) {
        if (!(insn instanceof StaticMethodInvoke)) {
            return null;
        }
        StaticMethodInvoke invoke = (StaticMethodInvoke) insn;
        if (this.owner != null && !this.owner.equals(invoke.getOwner())) {
            return null;
        }
        if (this.name != null && !this.name.equals(invoke.getMethodName())) {
            return null;
        }
        if (this.desc != null && !this.desc.equals(invoke.getMethodDescription())) {
            return null;
        }
        return invoke;
    }

    public static class Builder {

        private String owner;
        private String name;
        private String desc;

        public Builder() {
            reset();
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder reset() {
            this.owner = null;
            this.name = null;
            this.desc = null;
            return this;
        }

        public StaticInvokeMatcher build() {
            return new StaticInvokeMatcher(this.owner, this.name, this.desc);
        }

    }

}

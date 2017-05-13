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
package org.spongepowered.despector.transform.matcher;

import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.transform.matcher.instruction.ArrayAccessMatcher;
import org.spongepowered.despector.transform.matcher.instruction.InstanceFieldAccessMatcher;
import org.spongepowered.despector.transform.matcher.instruction.InstanceMethodInvokeMatcher;
import org.spongepowered.despector.transform.matcher.instruction.IntConstantMatcher;
import org.spongepowered.despector.transform.matcher.instruction.LocalAccessMatcher;
import org.spongepowered.despector.transform.matcher.instruction.StaticFieldAccessMatcher;
import org.spongepowered.despector.transform.matcher.instruction.StaticInvokeMatcher;
import org.spongepowered.despector.transform.matcher.instruction.StringConstantMatcher;

import javax.annotation.Nullable;

/**
 * A matcher for instructions.
 */
public interface InstructionMatcher<T extends Instruction> {

    /**
     * Attempts to match against the given instruction and return the matched
     * instruction if successful.
     */
    @Nullable
    T match(MatchContext ctx, Instruction insn);

    /**
     * Attempts to match against the given instruction and return the matched
     * instruction if successful.
     */
    @Nullable
    default T match(Instruction insn) {
        return match(MatchContext.create(), insn);
    }

    /**
     * Attempts to match against the given instruction..
     */
    default boolean matches(MatchContext ctx, Instruction insn) {
        return match(ctx, insn) != null;
    }

    /**
     * A matcher which matches any instruction.
     */
    static final InstructionMatcher<?> ANY = new Any();

    public static StaticInvokeMatcher.Builder staticInvoke() {
        return new StaticInvokeMatcher.Builder();
    }

    public static InstanceMethodInvokeMatcher.Builder instanceMethodInvoke() {
        return new InstanceMethodInvokeMatcher.Builder();
    }

    public static LocalAccessMatcher.Builder localAccess() {
        return new LocalAccessMatcher.Builder();
    }

    public static InstanceFieldAccessMatcher.Builder instanceFieldAccess() {
        return new InstanceFieldAccessMatcher.Builder();
    }

    public static IntConstantMatcher.Builder intConstant() {
        return new IntConstantMatcher.Builder();
    }

    public static ArrayAccessMatcher.Builder arrayAccess() {
        return new ArrayAccessMatcher.Builder();
    }

    public static StaticFieldAccessMatcher.Builder staticFieldAccess() {
        return new StaticFieldAccessMatcher.Builder();
    }

    public static StringConstantMatcher.Builder stringConstant() {
        return new StringConstantMatcher.Builder();
    }

    /**
     * An instruction matcher that matches any instruction.
     */
    public static class Any implements InstructionMatcher<Instruction> {

        Any() {
        }

        @Override
        public Instruction match(MatchContext ctx, Instruction stmt) {
            return stmt;
        }

    }

    /**
     * Unwraps the given instruction if it is a cast or a primative unboxing
     * method.
     */
    static Instruction unwrapCast(Instruction insn) {
        if (insn instanceof Cast) {
            return unwrapCast(((Cast) insn).getValue());
        }
        if (insn instanceof InstanceMethodInvoke) {
            InstanceMethodInvoke invoke = (InstanceMethodInvoke) insn;
            String key = invoke.getOwner() + invoke.getMethodName() + invoke.getMethodDescription();
            if ("Ljava/lang/Number;byteValue()B".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
            if ("Ljava/lang/Number;shortValue()S".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
            if ("Ljava/lang/Number;intValue()I".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
            if ("Ljava/lang/Number;longValue()J".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
            if ("Ljava/lang/Number;floatValue()F".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
            if ("Ljava/lang/Number;doubleValue()D".equals(key)) {
                return unwrapCast(invoke.getCallee());
            }
        }
        return insn;
    }

}

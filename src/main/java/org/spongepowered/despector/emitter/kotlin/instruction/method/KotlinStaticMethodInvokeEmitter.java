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
package org.spongepowered.despector.emitter.kotlin.instruction.method;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArray;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.instruction.StaticMethodInvokeEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.util.TypeHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KotlinStaticMethodInvokeEmitter extends StaticMethodInvokeEmitter {

    private static final Set<String> IGNORED_METHODS = new HashSet<>();
    private static final Set<String> NO_CALLEE = new HashSet<>();
    private static final Map<String, SpecialMethodEmitter<StaticMethodInvoke>> SPECIAL = new HashMap<>();

    static {
        IGNORED_METHODS.add("Ljava/lang/Integer;valueOf");
        IGNORED_METHODS.add("Ljava/lang/String;valueOf");

        NO_CALLEE.add("Lkotlin/collections/CollectionsKt;");
        NO_CALLEE.add("Lkotlin/collections/MapsKt;");

        SPECIAL.put("Lkotlin/TuplesKt;to", new TupleToEmitter());
        SPECIAL.put("Lkotlin/jvm/internal/Intrinsics;areEqual", new EqualityEmitter());
    }

    @Override
    public void emit(EmitterOutput ctx, StaticMethodInvoke arg, TypeSignature type) {
        String key = arg.getOwner() + arg.getMethodName();
        SpecialMethodEmitter<StaticMethodInvoke> special = SPECIAL.get(key);
        if (special != null && special.emit(ctx, arg, type)) {
            return;
        }
        if (IGNORED_METHODS.contains(key) && arg.getParams().length == 1) {
            ctx.emitInstruction(arg.getParams()[0], ClassTypeSignature.of(arg.getReturnType()));
            return;
        }
        String owner = TypeHelper.descToType(arg.getOwner());
        if (arg.getMethodName().startsWith("access$") && ctx.getType() != null) {
            if (replaceSyntheticAccessor(ctx, arg, owner)) {
                return;
            }
        }
        if (arg.getMethodName().endsWith("$default")) {
            callDefaultMethod(ctx, arg);
            return;
        }
        if (!NO_CALLEE.contains(arg.getOwner()) && (ctx.getType() == null || !owner.equals(ctx.getType().getName()))) {

            ctx.append(new EmitterToken(TokenType.TYPE, arg.getOwner()));
            ctx.append(new EmitterToken(TokenType.DOT, "."));
        }
        ctx.append(new EmitterToken(TokenType.NAME, arg.getMethodName()));
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());

        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            if (i == arg.getParams().length - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    if (i > 0 || o > 0) {
                        ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                    }
                    ctx.emitInstruction(varargs.getInitializer()[o], ClassTypeSignature.of(varargs.getType()));
                }
                break;
            }
            if (i > 0) {
                ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
            }
            ctx.emitInstruction(param, ClassTypeSignature.of(param_types.get(i)));
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
    }

    public void callDefaultMethod(EmitterOutput ctx, StaticMethodInvoke call) {
        Instruction callee = call.getParams()[0];
        int set = ((IntConstant) call.getParams()[call.getParams().length - 2]).getConstant();
        int total_args = call.getParams().length - 3;

        ctx.emitInstruction(callee, null);
        ctx.append(new EmitterToken(TokenType.DOT, "."));
        ctx.append(new EmitterToken(TokenType.NAME, call.getMethodName().substring(0, call.getMethodName().length() - 8)));
        List<String> param_types = TypeHelper.splitSig(call.getMethodDescription());
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        for (int i = 0; i < total_args; i++) {
            if ((set & (1 << i)) != 0) {
                continue;
            }
            Instruction param = call.getParams()[i + 1];
            if (i == total_args - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    if (i > 0 || o > 0) {
                        ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                    }
                    ctx.emitInstruction(varargs.getInitializer()[o], ClassTypeSignature.of(varargs.getType()));
                }
                break;
            }
            if (i > 0) {
                ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
            }
            ctx.emitInstruction(param, ClassTypeSignature.of(param_types.get(i)));
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
    }

}

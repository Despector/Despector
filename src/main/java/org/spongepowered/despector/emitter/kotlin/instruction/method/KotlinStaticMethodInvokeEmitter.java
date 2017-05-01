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
package org.spongepowered.despector.emitter.kotlin.instruction.method;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.instruction.StaticMethodInvokeEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An emitter for kotlin static methods.
 */
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
    public void emit(JavaEmitterContext ctx, StaticMethodInvoke arg, TypeSignature type) {
        String key = arg.getOwner() + arg.getMethodName();
        SpecialMethodEmitter<StaticMethodInvoke> special = SPECIAL.get(key);
        if (special != null && special.emit(ctx, arg, type)) {
            return;
        }
        if (IGNORED_METHODS.contains(key) && arg.getParameters().length == 1) {
            ctx.emit(arg.getParameters()[0], ClassTypeSignature.of(arg.getReturnType()));
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
            ctx.emitTypeName(owner);
            ctx.printString(".");
        }
        ctx.printString(arg.getMethodName());
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());
        ctx.printString("(");
        for (int i = 0; i < arg.getParameters().length; i++) {
            Instruction param = arg.getParameters()[i];
            if (i == arg.getParameters().length - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.markWrapPoint();
                    ctx.emit(varargs.getInitializer()[o], varargs.getType());
                    if (o < varargs.getInitializer().length - 1) {
                        ctx.printString(", ");
                    }
                }
                break;
            }
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
            if (i < arg.getParameters().length - 1) {
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
        }
        ctx.printString(")");
    }

    /**
     * Unwraps a call to a default method to determine the default parameter
     * values.
     */
    public void callDefaultMethod(JavaEmitterContext ctx, StaticMethodInvoke call) {
        Instruction callee = call.getParameters()[0];
        int set = ((IntConstant) call.getParameters()[call.getParameters().length - 2]).getConstant();
        int total_args = call.getParameters().length - 3;

        ctx.emit(callee, null);
        ctx.printString(".");
        ctx.printString(call.getMethodName().substring(0, call.getMethodName().length() - 8));
        List<String> param_types = TypeHelper.splitSig(call.getMethodDescription());
        ctx.printString("(");
        boolean first = true;
        for (int i = 0; i < total_args; i++) {
            if ((set & (1 << i)) != 0) {
                continue;
            }
            if (!first) {
                first = false;
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
            Instruction param = call.getParameters()[i + 1];
            if (i == total_args - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.markWrapPoint();
                    ctx.emit(varargs.getInitializer()[o], varargs.getType());
                    if (o < varargs.getInitializer().length - 1) {
                        ctx.printString(", ");
                    }
                }
                break;
            }
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
        }
        ctx.printString(")");
    }

}

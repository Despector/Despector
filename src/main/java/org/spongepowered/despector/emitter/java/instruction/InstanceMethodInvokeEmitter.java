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
package org.spongepowered.despector.emitter.java.instruction;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * An emitter for instance method invoke instructions.
 */
public class InstanceMethodInvokeEmitter implements InstructionEmitter<JavaEmitterContext, InstanceMethodInvoke> {

    @Override
    public void emit(JavaEmitterContext ctx, InstanceMethodInvoke arg, TypeSignature type) {
        if (arg.getOwner().equals("Ljava/lang/StringBuilder;") && arg.getMethodName().equals("toString")) {
            if (replaceStringConcat(ctx, arg)) {
                return;
            }
        }
        if (arg.getMethodName().equals("<init>")) {
            if (ctx.getType() != null) {
                if (arg.getOwnerType().equals(ctx.getType().getName())) {
                    ctx.printString("this");
                } else {
                    ctx.printString("super");
                }
            } else {
                ctx.printString("super");
            }
        } else {
            if (arg.getCallee() instanceof LocalAccess && ctx.getMethod() != null && !ctx.getMethod().isStatic()) {
                LocalAccess local = (LocalAccess) arg.getCallee();
                if (local.getLocal().getIndex() == 0) {
                    if (ctx.getType() != null && !arg.getOwnerType().equals(ctx.getType().getName())) {
                        ctx.printString("super.");
                    } else if (ConfigManager.getConfig().emitter.emit_this_for_methods) {
                        ctx.printString("this.");
                    }
                } else {
                    ctx.emit(local, null);
                    ctx.markWrapPoint();
                    ctx.printString(".");
                }
            } else {
                ctx.emit(arg.getCallee(), ClassTypeSignature.of(arg.getOwner()));
                ctx.markWrapPoint();
                ctx.printString(".");
            }
            ctx.printString(arg.getMethodName());
        }
        ctx.printString("(");
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            if (arg.getParams().length == 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.emit(varargs.getInitializer()[o], ClassTypeSignature.of(varargs.getType()));
                    if (o < varargs.getInitializer().length - 1) {
                        ctx.printString(", ");
                        ctx.markWrapPoint();
                    }
                }
                break;
            }
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
            if (i < arg.getParams().length - 1) {
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
        }
        ctx.printString(")");
    }

    protected boolean replaceStringConcat(JavaEmitterContext ctx, InstanceMethodInvoke arg) {
        // We detect and collapse string builder chains used to perform
        // string concatentation into simple "foo" + "bar" form
        boolean valid = true;
        Instruction callee = arg.getCallee();
        List<Instruction> constants = Lists.newArrayList();
        // We add all the constants to the front of this list as we have to
        // replay them in the reverse of the ordering that we will encounter
        // them in
        while (callee != null) {
            if (callee instanceof InstanceMethodInvoke) {
                InstanceMethodInvoke call = (InstanceMethodInvoke) callee;
                if (call.getParams().length == 1) {
                    constants.add(0, call.getParams()[0]);
                    callee = call.getCallee();
                    continue;
                }
            } else if (callee instanceof New) {
                New ref = (New) callee;
                if ("Ljava/lang/StringBuilder;".equals(ref.getType().getDescriptor())) {
                    if (ref.getParameters().length == 1) {
                        Instruction initial = ref.getParameters()[0];
                        if (initial instanceof StaticMethodInvoke) {
                            StaticMethodInvoke valueof = (StaticMethodInvoke) initial;
                            if (valueof.getMethodName().equals("valueOf") && valueof.getOwner().equals("Ljava/lang/String;")) {
                                Instruction internal = valueof.getParams()[0];
                                if (internal instanceof StringConstant) {
                                    initial = internal;
                                } else if (internal instanceof LocalAccess) {
                                    LocalAccess local = (LocalAccess) internal;
                                    if (local.getLocal().getType() == ClassTypeSignature.STRING) {
                                        initial = local;
                                    }
                                }
                            }
                        }
                        constants.add(0, initial);
                    }
                    break;
                }
                valid = false;
                break;
            } else if (callee instanceof LocalAccess) {
                valid = false;
                break;
            }
            valid = false;
        }
        if (valid) {
            for (int i = 0; i < constants.size(); i++) {
                ctx.emit(constants.get(i), ClassTypeSignature.STRING);
                if (i < constants.size() - 1) {
                    ctx.markWrapPoint();
                    ctx.printString(" + ");
                }
            }
            return true;
        }
        return false;
    }

}

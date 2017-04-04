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
package org.spongepowered.despector.emitter.instruction;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArray;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

public class InstanceMethodInvokeEmitter implements InstructionEmitter<InstanceMethodInvoke> {

    @Override
    public void emit(EmitterOutput ctx, InstanceMethodInvoke arg, TypeSignature type) {
        if (arg.getOwner().equals("Ljava/lang/StringBuilder;") && arg.getMethodName().equals("toString")) {
            if (replaceStringConcat(ctx, arg)) {
                return;
            }
        }
        if (arg.getMethodName().equals("<init>")) {
            if (ctx.getType() != null && arg.getOwnerType().equals(ctx.getType().getName())) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "this"));
            } else {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "super"));
            }
        } else {
            if (arg.getCallee() instanceof LocalAccess && ctx.getMethod() != null && !ctx.getMethod().isStatic()) {
                LocalAccess local = (LocalAccess) arg.getCallee();
                if (local.getLocal().getIndex() == 0) {
                    if (ctx.getType() != null && !arg.getOwnerType().equals(ctx.getType().getName())) {
                        ctx.append(new EmitterToken(TokenType.SPECIAL, "super"));
                        ctx.append(new EmitterToken(TokenType.DOT, "."));
                    }
                } else {
                    ctx.emitInstruction(local, null);
                    ctx.append(new EmitterToken(TokenType.DOT, "."));
                }
            } else {
                ctx.emitInstruction(arg.getCallee(), ClassTypeSignature.of(arg.getOwner()));
                ctx.append(new EmitterToken(TokenType.DOT, "."));
            }
            ctx.append(new EmitterToken(TokenType.NAME, arg.getMethodName()));
        }
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            if (arg.getParams().length == 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.append(new EmitterToken(TokenType.ARG_START, null));
                    ctx.emitInstruction(varargs.getInitializer()[o], ClassTypeSignature.of(varargs.getType()));
                }
                break;
            }
            ctx.append(new EmitterToken(TokenType.ARG_START, null));
            ctx.emitInstruction(param, ClassTypeSignature.of(param_types.get(i)));
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
    }

    protected boolean replaceStringConcat(EmitterOutput ctx, InstanceMethodInvoke arg) {
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
                ctx.emitInstruction(constants.get(i), ClassTypeSignature.STRING);
                if (i < constants.size() - 1) {
                    ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                }
            }
            return true;
        }
        return false;
    }

}

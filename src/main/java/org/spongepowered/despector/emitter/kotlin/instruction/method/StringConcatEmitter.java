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

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

import java.util.List;

/**
 * A special method emitter to convert a string builder chain into a string
 * concatenation with addition operators.
 */
public class StringConcatEmitter implements SpecialMethodEmitter<InstanceMethodInvoke> {

    @Override
    public boolean emit(JavaEmitterContext ctx, InstanceMethodInvoke arg, TypeSignature type) {
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
                if (call.getParameters().length == 1) {
                    constants.add(0, call.getParameters()[0]);
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
                                Instruction internal = valueof.getParameters()[0];
                                if (internal instanceof StringConstant) {
                                    initial = internal;
                                } else if (internal instanceof LocalAccess) {
                                    LocalAccess local = (LocalAccess) internal;
                                    if (local.getLocal().getType().equals("Ljava/lang/String;")) {
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
            }
            valid = false;
        }
        if (valid) {
            boolean in_string = false;
            for (int i = 0; i < constants.size(); i++) {
                Instruction next = constants.get(i);
                if (next instanceof StringConstant) {
                    if (!in_string) {
                        if (i == 0) {
                            ctx.printString("\"");
                        } else {
                            ctx.printString(" + \"");
                        }
                        in_string = true;
                    }
                    // TODO escape string
                    ctx.printString(((StringConstant) next).getConstant());
                    continue;
                } else if (next instanceof LocalAccess) {
                    if (!in_string && i < constants.size() - 1 && constants.get(i + 1) instanceof StringConstant) {
                        in_string = true;
                        ctx.printString("\"");
                    }
                    if (in_string) {
                        ctx.printString("$");
                        ctx.printString(((LocalAccess) next).getLocal().getName());
                    } else {
                        ctx.markWrapPoint();
                        ctx.printString(" + ");
                        ctx.printString(((LocalAccess) next).getLocal().getName());
                        if (i < constants.size() - 1) {
                            ctx.markWrapPoint();
                            ctx.printString(" + ");
                        }
                    }
                    continue;
                } else if (next instanceof StaticMethodInvoke) {
                    StaticMethodInvoke mth = (StaticMethodInvoke) next;
                    if ("Lkotlin/text/StringsKt;".equals(mth.getOwner()) && "replace$default".equals(mth.getMethodName())) {
                        if (!in_string) {
                            if (i == 0) {
                                ctx.printString("\"");
                            } else {
                                ctx.markWrapPoint();
                                ctx.printString(" + \"");
                            }
                            in_string = true;
                        }
                        ctx.printString("${");
                        ctx.emit(mth.getParameters()[0], ClassTypeSignature.STRING);
                        ctx.printString(".replace(");
                        ctx.emit(mth.getParameters()[1], ClassTypeSignature.STRING);
                        ctx.printString(", ");
                        ctx.emit(mth.getParameters()[2], ClassTypeSignature.STRING);
                        ctx.printString(")}");
                        continue;
                    }
                }
                if (in_string) {
                    ctx.printString("\"");
                    ctx.markWrapPoint();
                    ctx.printString(" + ");
                    in_string = false;
                }
                ctx.emit(constants.get(i), ClassTypeSignature.STRING);
                if (i < constants.size() - 1) {
                    ctx.markWrapPoint();
                    ctx.printString(" + ");
                }
            }
            if (in_string) {
                ctx.printString("\"");
            }
            return true;
        }
        return false;
    }

}

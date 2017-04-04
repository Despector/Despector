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

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.instruction.StringConstantEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

import java.util.List;

public class StringConcatEmitter implements SpecialMethodEmitter<InstanceMethodInvoke> {

    @Override
    public boolean emit(EmitterOutput ctx, InstanceMethodInvoke arg, TypeSignature type) {
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
                if ("Ljava/lang/StringBuilder;".equals(ref.getType())) {
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
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < constants.size(); i++) {
                Instruction next = constants.get(i);
                if (next instanceof StringConstant) {
                    if (!in_string) {
                        if (i != 0) {
                            ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                        }
                        in_string = true;
                        str.setLength(0);
                    }
                    // TODO escape string
                    str.append(StringConstantEmitter.escape(((StringConstant) next).getConstant()));
                    continue;
                } else if (next instanceof LocalAccess) {
                    if (!in_string && i < constants.size() - 1 && constants.get(i + 1) instanceof StringConstant) {
                        in_string = true;
                        str.setLength(0);
                    }
                    if (in_string) {
                        str.append("$");
                        str.append(((LocalAccess) next).getLocal().getName());
                    } else {
                        ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                        ctx.append(new EmitterToken(TokenType.NAME, ((LocalAccess) next).getLocal().getName()));
                        if (i < constants.size() - 1) {
                            ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                        }
                    }
                    continue;
                } else if (next instanceof StaticMethodInvoke) {
                    StaticMethodInvoke mth = (StaticMethodInvoke) next;
                    if ("Lkotlin/text/StringsKt;".equals(mth.getOwner()) && "replace$default".equals(mth.getMethodName())) {
//                        if (!in_string) {
//                            if (i != 0) {
//                                ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
//                            }
//                            in_string = true;
//                            str.setLength(0);
//                        }
                        // TODO
//                        ctx.printString("${");
//                        ctx.emit(mth.getParams()[0], ClassTypeSignature.STRING);
//                        ctx.printString(".replace(");
//                        ctx.emit(mth.getParams()[1], ClassTypeSignature.STRING);
//                        ctx.printString(", ");
//                        ctx.emit(mth.getParams()[2], ClassTypeSignature.STRING);
//                        ctx.printString(")}");
//                        continue;
                    }
                }
                if (in_string) {
                    ctx.append(new EmitterToken(TokenType.STRING, str.toString()));
                    ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                    in_string = false;
                }
                ctx.emitInstruction(constants.get(i), ClassTypeSignature.STRING);
                if (i < constants.size() - 1) {
                    ctx.append(new EmitterToken(TokenType.OPERATOR, "+"));
                }
            }
            if (in_string) {
                ctx.append(new EmitterToken(TokenType.STRING, str.toString()));
            }
            return true;
        }
        return false;
    }

}

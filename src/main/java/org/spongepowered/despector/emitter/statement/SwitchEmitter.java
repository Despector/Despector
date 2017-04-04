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
package org.spongepowered.despector.emitter.statement;

import com.google.common.collect.Maps;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.FieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldAccess;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

import java.util.Map;

public class SwitchEmitter implements StatementEmitter<Switch> {

    private Map<Integer, String> buildSwitchTable(MethodEntry mth, String field) {
        Map<Integer, String> table = Maps.newHashMap();

        for (Statement stmt : mth.getInstructions().getStatements()) {
            if (stmt instanceof TryCatch) {
                TryCatch next = (TryCatch) stmt;
                ArrayAssignment assign = (ArrayAssignment) next.getTryBlock().getStatements().get(0);
                int jump_index = ((IntConstant) assign.getValue()).getConstant();
                InstanceMethodInvoke ordinal = (InstanceMethodInvoke) assign.getIndex();
                StaticFieldAccess callee = (StaticFieldAccess) ordinal.getCallee();
                if (field == null) {
                    table.put(jump_index, callee.getFieldName());
                } else {
                    FieldAccess fld = (FieldAccess) assign.getArray();
                    if (fld.getFieldName().equals(field)) {
                        table.put(jump_index, callee.getFieldName());
                    }
                }
            }
        }

        return table;
    }

    @Override
    public void emit(EmitterOutput ctx, Switch tswitch) {
        Map<Integer, String> table = null;
        ctx.append(new EmitterToken(TokenType.SPECIAL, "switch"));
        ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
        boolean synthetic = false;
        if (tswitch.getSwitchVar() instanceof ArrayAccess) {
            ArrayAccess var = (ArrayAccess) tswitch.getSwitchVar();
            if (var.getArrayVar() instanceof StaticMethodInvoke) {
                StaticMethodInvoke arg = (StaticMethodInvoke) var.getArrayVar();
                if (arg.getMethodName().contains("$SWITCH_TABLE$") && ctx.getType() != null) {
                    MethodEntry mth = ctx.getType().getStaticMethod(arg.getMethodName(), arg.getMethodDescription());
                    table = buildSwitchTable(mth, null);
                    String enum_type = arg.getMethodName().substring("$SWITCH_TABLE$".length()).replace('$', '/');
                    ctx.emitInstruction(((InstanceMethodInvoke) var.getIndex()).getCallee(), ClassTypeSignature.of("L" + enum_type + ";"));
                    synthetic = true;
                }
            } else if (var.getArrayVar() instanceof StaticFieldAccess) {
                StaticFieldAccess arg = (StaticFieldAccess) var.getArrayVar();
                if (arg.getFieldName().startsWith("$SwitchMap") && ctx.getType() != null) {
                    TypeEntry owner = ctx.getType().getSource().get(arg.getOwnerName());
                    MethodEntry mth = owner.getStaticMethod("<clinit>");
                    table = buildSwitchTable(mth, arg.getFieldName());
                    String enum_type = arg.getFieldName().substring("$SwitchMap/".length()).replace('$', '/');
                    ctx.emitInstruction(((InstanceMethodInvoke) var.getIndex()).getCallee(), ClassTypeSignature.of("L" + enum_type + ";"));
                    synthetic = true;
                }
            }
        }
        if (!synthetic) {
            ctx.emitInstruction(tswitch.getSwitchVar(), ClassTypeSignature.INT);
        }
        ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        for (Case cs : tswitch.getCases()) {
            for (int i = 0; i < cs.getIndices().size(); i++) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "case"));
                int index = cs.getIndices().get(i);
                if (table != null) {
                    String label = table.get(index);
                    if (label == null) {
                        ctx.append(new EmitterToken(TokenType.NAME, String.valueOf(index)));
                    } else {
                        ctx.append(new EmitterToken(TokenType.NAME, label));
                    }
                } else {
                    ctx.append(new EmitterToken(TokenType.NAME, String.valueOf(cs.getIndices().get(i))));
                }
                ctx.append(new EmitterToken(TokenType.SPECIAL, ":"));
            }
            if (cs.isDefault()) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "default:"));
            }
            ctx.emitBody(cs.getBody(), 0);
            if (cs.doesBreak()) {
                ctx.append(new EmitterToken(TokenType.SPECIAL, "break;"));
            }
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
    }

}

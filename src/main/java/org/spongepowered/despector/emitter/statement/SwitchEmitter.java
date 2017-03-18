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
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayLoadArg;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;

import java.util.Map;

public class SwitchEmitter implements StatementEmitter<Switch> {

    private Map<Integer, String> buildSwitchTable(MethodEntry mth) {
        Map<Integer, String> table = Maps.newHashMap();

        for (Statement stmt : mth.getInstructions().getStatements()) {
            if (stmt instanceof TryCatch) {
                TryCatch next = (TryCatch) stmt;
                ArrayAssignment assign = (ArrayAssignment) next.getTryBlock().getStatements().get(0);
                int jump_index = ((IntConstantArg) assign.getValue()).getConstant();
                InstanceMethodInvoke ordinal = (InstanceMethodInvoke) assign.getIndex();
                StaticFieldArg callee = (StaticFieldArg) ordinal.getCallee();
                table.put(jump_index, callee.getFieldName());
            }
        }

        return table;
    }

    @Override
    public void emit(EmitterContext ctx, Switch tswitch, boolean semicolon) {
        Map<Integer, String> table = null;
        ctx.printString("switch (");
        boolean synthetic = false;
        if (tswitch.getSwitchVar() instanceof ArrayLoadArg) {
            ArrayLoadArg var = (ArrayLoadArg) tswitch.getSwitchVar();
            if (var.getArrayVar() instanceof StaticMethodInvoke) {
                StaticMethodInvoke arg = (StaticMethodInvoke) var.getArrayVar();
                if (arg.getMethodName().contains("$SWITCH_TABLE$") && ctx.getType() != null) {
                    MethodEntry mth = ctx.getType().getStaticMethod(arg.getMethodName(), arg.getMethodDescription());
                    table = buildSwitchTable(mth);
                    String enum_type = arg.getMethodName().substring("$SWITCH_TABLE$".length()).replace('$', '/');
                    ctx.emit(((InstanceMethodInvoke) var.getIndex()).getCallee(), "L" + enum_type + ";");
                    synthetic = true;
                }
            }
        }
        if (!synthetic) {
            ctx.emit(tswitch.getSwitchVar(), "I");
        }
        ctx.printString(") {\n");
        for (Case cs : tswitch.getCases()) {
            for (int i = 0; i < cs.getIndices().size(); i++) {
                ctx.printIndentation();
                ctx.printString("case ");
                int index = cs.getIndices().get(i);
                if (table != null) {
                    String label = table.get(index);
                    if (label == null) {
                        ctx.printString(String.valueOf(index));
                    } else {
                        ctx.printString(label);
                    }
                } else {
                    ctx.printString(String.valueOf(cs.getIndices().get(i)));
                }
                ctx.printString(":\n");
            }
            if (cs.isDefault()) {
                ctx.printIndentation();
                ctx.printString("default:\n");
            }
            ctx.indent();
            ctx.emitBody(cs.getBody());
            if (!cs.getBody().getStatements().isEmpty()) {
                ctx.printString("\n");
            }
            if (cs.doesBreak()) {
                ctx.printIndentation();
                ctx.printString("break;");
                ctx.printString("\n");
            }
            ctx.dedent();
        }
        ctx.printIndentation();
        ctx.printString("}");
    }

}

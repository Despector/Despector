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
package org.spongepowered.despector.emitter.kotlin.special;

import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;
import org.spongepowered.despector.emitter.special.SpecialEmitter;

import java.util.LinkedHashMap;
import java.util.Map;

public class KotlinDataClassEmitter implements SpecialEmitter {

    public void emit(EmitterContext ctx, ClassEntry type) {
        ctx.printIndentation();
        ctx.printString("data class ");
        InnerClassInfo inner_info = null;
        if (type.isInnerClass() && ctx.getOuterType() != null) {
            inner_info = ctx.getOuterType().getInnerClassInfo(type.getName());
        }
        if (inner_info != null) {
            ctx.printString(inner_info.getSimpleName());
        } else {
            String name = type.getName().replace('/', '.');
            if (name.indexOf('.') != -1) {
                name = name.substring(name.lastIndexOf('.') + 1, name.length());
            }
            name = name.replace('$', '.');
            ctx.printString(name);
        }

        ctx.printString("(");
        ctx.newLine();
        ctx.indent();

        Map<String, DataField> fields = new LinkedHashMap<>();

        for (FieldEntry fld : type.getFields()) {
            DataField data = new DataField();
            data.name = fld.getName();
            data.type = fld.getType();
            fields.put(data.name, data);
        }

        DataField[] fields_ordered = new DataField[fields.size()];

        int longest = 0;
        MethodEntry ctor = null;
        MethodEntry ctor_smaller = null;

        for (MethodEntry mth : type.getMethods()) {
            if ("<init>".equals(mth.getName()) && mth.getParamTypes().size() > longest) {
                longest = mth.getParamTypes().size();
                ctor_smaller = ctor;
                ctor = mth;
            }
        }

        for (Statement stmt : ctor_smaller.getInstructions().getStatements()) {
            if (!(stmt instanceof FieldAssignment)) {
                continue;
            }
            FieldAssignment assign = (FieldAssignment) stmt;
            DataField data = fields.get(assign.getFieldName());
            LocalAccess local = (LocalAccess) assign.getValue();
            fields_ordered[local.getLocal().getIndex() - 1] = data;
        }

        for (Statement stmt : ctor.getInstructions().getStatements()) {
            if (!(stmt instanceof If)) {
                continue;
            }
            LocalAssignment assign = (LocalAssignment) ((If) stmt).getIfBody().getStatement(0);
            fields_ordered[assign.getLocal().getIndex() - 1].default_val = assign.getValue();
        }

        for (int i = 0; i < fields_ordered.length; i++) {
            DataField fld = fields_ordered[i];
            ctx.printIndentation();
            ctx.printString("var ");
            ctx.printString(fld.name);
            ctx.printString(": ");
            KotlinEmitterUtil.emitType(ctx, fld.type);
            if (fld.default_val != null) {
                ctx.printString(" = ");
                ctx.emit(fld.default_val, fld.type);
            }
            if (i != fields_ordered.length - 1) {
                ctx.printString(",");
            }
            ctx.newLine();
        }
        ctx.dedent();
        ctx.printIndentation();
        ctx.printString(")");
        ctx.newLine();
    }

    private static class DataField {

        public String name;
        public String type;
        public Instruction default_val;

        public DataField() {

        }
    }

}

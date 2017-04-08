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
package org.spongepowered.despector.emitter.kotlin.special;

import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.StatementBlock.Type;
import org.spongepowered.despector.ast.members.insn.function.InvokeStatement;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;
import org.spongepowered.despector.emitter.special.SpecialEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An emitter for kotlin companion classes.
 */
public class KotlinCompanionClassEmitter implements SpecialEmitter {

    /**
     * Emits the given companion class.
     */
    public void emit(EmitterContext ctx, ClassEntry type) {
        ctx.printIndentation();
        ctx.printString("companion object {");
        ctx.newLine().indent();

        Map<String, Field> fields = new HashMap<>();

        for (MethodEntry mth : type.getMethods()) {
            if ("<init>".equals(mth.getName())) {
                continue;
            }
            if (mth.getName().startsWith("get")) {
                String name = mth.getName().substring(3);
                name = name.substring(0, 1).toLowerCase() + name.substring(1);
                Field fld = fields.get(name);
                if (fld == null) {
                    fld = new Field();
                    fld.name = name;
                    fields.put(name, fld);
                }
                fld.type = mth.getReturnType();

                fld.getter = new StatementBlock(Type.METHOD, mth.getInstructions().getLocals());
                for (Statement stmt : mth.getInstructions().getStatements()) {
                    if (stmt instanceof InvokeStatement) {
                        InvokeStatement istmt = (InvokeStatement) stmt;
                        if (istmt.getInstruction() instanceof StaticMethodInvoke) {
                            StaticMethodInvoke invoke = (StaticMethodInvoke) istmt.getInstruction();
                            if (invoke.getOwner().equals("Lkotlin/jvm/internal/Intrinsics;")) {
                                continue;
                            }
                        }
                    }
                    fld.getter.append(stmt);
                }
            }
        }

        for (MethodEntry mth : type.getStaticMethods()) {
            if (mth.getName().endsWith("$annotations")) {
                String name = mth.getName().substring(0, mth.getName().length() - 12);
                Field fld = fields.get(name);
                if (fld == null) {
                    fld = new Field();
                    fld.name = name;
                    fields.put(name, fld);
                }
                fld.annotations.addAll(mth.getAnnotations());
            }
        }

        for (Field fld : fields.values()) {

            for (Annotation anno : fld.annotations) {
                ctx.printIndentation();
                ctx.emit(anno);
                ctx.newLine();
            }
            ctx.printIndentation();
            ctx.printString("val ");
            ctx.printString(fld.name);
            ctx.printString(": ");
            KotlinEmitterUtil.emitType(ctx, fld.type);
            if (fld.getter != null) {
                ctx.newLine();
                ctx.indent();
                ctx.printIndentation();
                ctx.printString("get()");
                if (fld.getter.getStatementCount() == 1) {
                    ctx.printString(" = ");
                    Statement value = fld.getter.getStatement(0);
                    if (value instanceof Return) {
                        ctx.emit(((Return) value).getValue().get(), fld.type);
                    } else {
                        ctx.emit(value, false);
                    }
                    ctx.newLine();
                } else {
                    ctx.printString(" = {");
                    ctx.newLine();
                    ctx.indent();
                    for (Statement stmt : fld.getter.getStatements()) {
                        ctx.printIndentation();
                        ctx.emit(stmt, ctx.usesSemicolons());
                        ctx.newLine();
                    }
                    ctx.dedent();
                    ctx.printIndentation();
                    ctx.printString("}");
                    ctx.newLine();
                }
            }
        }
        ctx.dedent();
        ctx.printIndentation();
        ctx.printString("}");
        ctx.newLine();

    }

    /**
     * A representation of a field.
     */
    private static class Field {

        public List<Annotation> annotations = new ArrayList<>();
        public String name;
        public TypeSignature type;

        public StatementBlock getter;

        public Field() {
        }
    }

}

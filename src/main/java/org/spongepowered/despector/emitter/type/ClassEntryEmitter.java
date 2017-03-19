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
package org.spongepowered.despector.emitter.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.GenericsEmitter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassEntryEmitter implements AstEmitter<ClassEntry> {

    @Override
    public boolean emit(EmitterContext ctx, ClassEntry type) {

        for (Annotation anno : type.getAnnotations()) {
            ctx.printIndentation();
            ctx.emit(anno);
            ctx.printString("\n");
        }
        ctx.printIndentation();
        InnerClassInfo inner_info = null;
        if (type.isInnerClass() && ctx.getOuterType() != null) {
            inner_info = ctx.getOuterType().getInnerClassInfo(type.getName());
        }
        ctx.printString(type.getAccessModifier().asString());
        if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
            ctx.printString(" ");
        }
        if (inner_info != null && inner_info.isStatic()) {
            ctx.printString("static ");
        }
        if ((inner_info != null && inner_info.isFinal()) || type.isFinal()) {
            ctx.printString("final ");
        }
        if (inner_info != null && inner_info.isAbstract()) {
            ctx.printString("abstract ");
        }
        ctx.printString("class ");
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

        GenericsEmitter generics = ctx.getEmitterSet().getGenericsEmitter();
        generics.emitTypeParameters(ctx, type.getSignature().getParameters());

        if (!type.getSuperclass().equals("Ljava/lang/Object;")) {
            ctx.printString(" extends ");
            ctx.emitTypeName(type.getSuperclassName());
            generics.emitTypeArguments(ctx, type.getSignature().getSuperclassSignature().getArguments());
        }
        if (!type.getInterfaces().isEmpty()) {
            ctx.printString(" implements ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.emitType(type.getInterfaces().get(i));
                generics.emitTypeArguments(ctx, type.getSignature().getInterfaceSignatures().get(i).getArguments());
                if (i < type.getInterfaces().size() - 1) {
                    if (ctx.getFormat().insert_space_before_comma_in_superinterfaces) {
                        ctx.printString(" ");
                    }
                    ctx.printString(",");
                    if (ctx.getFormat().insert_space_after_comma_in_superinterfaces) {
                        ctx.printString(" ");
                    }
                }
            }
        }
        if (ctx.getFormat().insert_space_before_opening_brace_in_type_declaration) {
            ctx.printString(" ");
        }
        ctx.printString("{\n");
        for (int i = 0; i < ctx.getFormat().blank_lines_before_first_class_body_declaration; i++) {
            ctx.printString("\n");
        }

        // Ordering is static fields -> static methods -> instance fields ->
        // instance methods
        ctx.indent();
        if (!type.getStaticFields().isEmpty()) {

            Map<String, Instruction> static_initializers = new HashMap<>();

            MethodEntry static_init = type.getStaticMethodSafe("<clinit>");
            if (static_init != null && static_init.getInstructions() != null) {
                for (Statement stmt : static_init.getInstructions().getStatements()) {
                    if (!(stmt instanceof StaticFieldAssignment)) {
                        break;
                    }
                    StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
                    if (!assign.getOwnerName().equals(type.getName())) {
                        break;
                    }
                    static_initializers.put(assign.getFieldName(), assign.getValue());
                }
            }

            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                ctx.printIndentation();
                ctx.emit(field);
                if (static_initializers.containsKey(field.getName())) {
                    ctx.printString(" = ");
                    ctx.emit(static_initializers.get(field.getName()), field.getType());
                }
                ctx.printString(";\n");
            }
            if (at_least_one) {
                ctx.printString("\n");
            }
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                if (ctx.emit(mth)) {
                    ctx.printString("\n\n");
                }
            }
        }
        if (!type.getFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                ctx.printIndentation();
                ctx.emit(field);
                ctx.printString(";\n");
            }
            if (at_least_one) {
                ctx.printString("\n");
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                if (ctx.emit(mth)) {
                    ctx.printString("\n\n");
                }
            }
        }

        Collection<InnerClassInfo> inners = type.getInnerClasses();
        for (InnerClassInfo inner : inners) {
            if (inner.getOuterName() == null || !inner.getOuterName().equals(type.getName())) {
                continue;
            }
            ctx.setOuterType(type);
            TypeEntry inner_type = type.getSource().get(inner.getName());
            ctx.printString("\n");
            ctx.emit(inner_type);
            ctx.setType(type);
            ctx.setOuterType(null);
        }

        ctx.dedent();
        ctx.printIndentation();
        ctx.printString("}\n");
        return true;
    }

}

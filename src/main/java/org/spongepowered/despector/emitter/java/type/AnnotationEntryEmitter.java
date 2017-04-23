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
package org.spongepowered.despector.emitter.java.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.AnnotationEmitter;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;

import java.util.Collection;

/**
 * An emitter for interface types.
 */
public class AnnotationEntryEmitter implements AstEmitter<JavaEmitterContext, AnnotationEntry> {

    @Override
    public boolean emit(JavaEmitterContext ctx, AnnotationEntry type) {

        ctx.printIndentation();
        for (Annotation anno : type.getAnnotations()) {
            ctx.emit(anno);
            if (ctx.getFormat().insert_new_line_after_annotation_on_type) {
                ctx.newLine();
                ctx.printIndentation();
            } else {
                ctx.printString(" ");
            }
        }
        InnerClassInfo inner_info = null;
        if (type.isInnerClass() && ctx.getOuterType() != null) {
            inner_info = ctx.getOuterType().getInnerClassInfo(type.getName());
        }
        String name = null;
        if (inner_info != null) {
            name = inner_info.getSimpleName();
        } else {
            name = type.getName().replace('/', '.');
            if (name.indexOf('.') != -1) {
                name = name.substring(name.lastIndexOf('.') + 1, name.length());
            }
            name = name.replace('$', '.');
        }
        if (!(name.contains(".") && inner_info == null && type.getAccessModifier() == AccessModifier.PUBLIC)) {
            ctx.printString(type.getAccessModifier().asString());
            if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                ctx.printString(" ");
            }
        }
        ctx.printString("@interface ");
        ctx.printString(name);
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        if (type.getSignature() != null) {
            generics.emitTypeParameters(ctx, type.getSignature().getParameters());
        }
        ctx.emitBrace(ctx.getFormat().brace_position_for_type_declaration, false,
                ctx.getFormat().insert_space_before_opening_brace_in_type_declaration);
        ctx.newLine(ctx.getFormat().blank_lines_before_first_class_body_declaration + 1);
        if (!type.getStaticFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.printIndentation();
                        ctx.printString("// Synthetic");
                        ctx.newLine();
                    } else {
                        continue;
                    }
                }
                at_least_one = true;
                ctx.printIndentation();
                ctx.emit(field);
                ctx.printString(";");
                ctx.newLine();
            }
            if (at_least_one) {
                ctx.newLine();
            }
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.printIndentation();
                        ctx.printString("// Synthetic");
                        if (mth.isBridge()) {
                            ctx.printString(" - Bridge");
                        }
                        ctx.newLine();
                    } else {
                        continue;
                    }
                }
                ctx.emit(mth);
                ctx.newLine();
                ctx.newLine();
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.printIndentation();
                        ctx.printString("// Synthetic");
                        if (mth.isBridge()) {
                            ctx.printString(" - Bridge");
                        }
                        ctx.newLine();
                    } else {
                        continue;
                    }
                }

                ctx.printIndentation();
                for (Annotation anno : mth.getAnnotations()) {
                    ctx.emit(anno);
                    if (ctx.getFormat().insert_new_line_after_annotation_on_method) {
                        ctx.newLine();
                        ctx.printIndentation();
                    } else {
                        ctx.printString(" ");
                    }
                }
                ctx.printString(mth.getAccessModifier().asString());
                if (mth.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                    ctx.printString(" ");
                }
                MethodSignature sig = mth.getMethodSignature();

                if (sig != null) {
                    if (!sig.getTypeParameters().isEmpty()) {
                        generics.emitTypeParameters(ctx, sig.getTypeParameters());
                        ctx.printString(" ");
                    }
                    generics.emitTypeSignature(ctx, sig.getReturnType(), false);
                } else {
                    ctx.emitType(mth.getReturnType(), false);
                }

                ctx.printString(" ");
                ctx.printString(mth.getName());
                ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_method_declaration);
                ctx.printString("(");
                StatementBlock block = mth.getInstructions();
                if (!mth.getParamTypes().isEmpty()) {
                    ctx.printString(" ", ctx.getFormat().insert_space_between_empty_parens_in_method_declaration);
                }
                for (int i = 0; i < mth.getParamTypes().size(); i++) {
                    int param_index = i + 1;
                    boolean varargs = mth.isVarargs() && i == mth.getParamTypes().size() - 1;
                    if (block == null) {
                        if (sig != null) {
                            // interfaces have no lvt for parameters, need to
                            // get generic types from the method signature
                            generics.emitTypeSignature(ctx, sig.getParameters().get(i), varargs);
                        } else {
                            ctx.emitType(mth.getParamTypes().get(i), varargs);
                        }
                        ctx.printString(" ");
                        ctx.printString("local" + param_index);
                    } else {
                        Local local = mth.getLocals().getLocal(param_index);
                        LocalInstance insn = local.getParameterInstance();
                        for (Annotation anno : insn.getAnnotations()) {
                            ctx.emit(anno);
                            if (ctx.getFormat().insert_new_line_after_annotation_on_parameter) {
                                ctx.indent();
                                ctx.indent();
                                ctx.newLine();
                                ctx.printIndentation();
                                ctx.dedent();
                                ctx.dedent();
                            } else {
                                ctx.printString(" ");
                            }
                        }
                        generics.emitTypeSignature(ctx, insn.getType(), varargs);
                        ctx.printString(" ");
                        ctx.printString(insn.getName());
                    }
                    if (i < mth.getParamTypes().size() - 1) {
                        ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_method_declaration_parameters);
                        ctx.printString(",");
                        ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_method_declaration_parameters);
                        ctx.markWrapPoint(ctx.getFormat().alignment_for_parameters_in_method_declaration, i + 1);
                    }
                }
                ctx.printString(" ", ctx.getFormat().insert_space_before_closing_paren_in_method_declaration);
                ctx.printString(")");
                if (mth.getAnnotationValue() != null) {
                    ctx.printString(" default ");
                    AnnotationEmitter.emitValue(ctx, mth.getAnnotationValue());
                }
                ctx.printString(";");

                ctx.newLine();
                ctx.newLine();
            }
        }

        Collection<InnerClassInfo> inners = type.getInnerClasses();
        for (InnerClassInfo inner : inners) {
            if (inner.getOuterName() == null || !inner.getOuterName().equals(type.getName())) {
                continue;
            }
            TypeEntry inner_type = type.getSource().get(inner.getName());
            ctx.newLine();
            ctx.emit(inner_type);
        }
        if (ctx.getFormat().brace_position_for_type_declaration == BracePosition.NEXT_LINE_SHIFTED) {
            ctx.printIndentation();
            ctx.printString("}");
            ctx.dedent();
        } else {
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
        }
        ctx.newLine();
        return true;
    }

}

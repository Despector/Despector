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
package org.spongepowered.despector.emitter.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.special.GenericsEmitter;

import java.util.Collection;

/**
 * An emitter for interface types.
 */
public class InterfaceEntryEmitter implements AstEmitter<InterfaceEntry> {

    @Override
    public boolean emit(EmitterContext ctx, InterfaceEntry type) {

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
        ctx.printString("interface ");
        ctx.printString(name);
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        if (type.getSignature() != null) {
            generics.emitTypeParameters(ctx, type.getSignature().getParameters());
        }
        if (!type.getInterfaces().isEmpty()) {
            ctx.printString(" extends ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_superinterfaces);
                    ctx.printString(",");
                    ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_superinterfaces);
                    ctx.markWrapPoint();
                }
            }
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
                // TODO need something for emitting 'default' for default
                // methods
                ctx.emit(mth);
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

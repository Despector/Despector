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

import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.util.AstUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ClassEntryEmitter implements AstEmitter<ClassEntry> {

    @Override
    public boolean emit(EmitterOutput ctx, ClassEntry type) {

        for (Annotation anno : type.getAnnotations()) {
            ctx.emit(anno);
        }
        InnerClassInfo inner_info = null;
        if (type.isInnerClass() && ctx.getOuterType() != null) {
            inner_info = ctx.getOuterType().getInnerClassInfo(type.getName());
        }
        ctx.append(new EmitterToken(TokenType.ACCESS, type.getAccessModifier()));
        if (inner_info != null && inner_info.isStatic()) {
            ctx.append(new EmitterToken(TokenType.MODIFIER, "static"));
        }
        if ((inner_info != null && inner_info.isFinal()) || type.isFinal()) {
            ctx.append(new EmitterToken(TokenType.MODIFIER, "final"));
        }
        if (inner_info != null && inner_info.isAbstract()) {
            ctx.append(new EmitterToken(TokenType.MODIFIER, "abstract"));
        }
        ctx.append(new EmitterToken(TokenType.SPECIAL, "class"));
        if (inner_info != null) {
            ctx.append(new EmitterToken(TokenType.NAME, inner_info.getSimpleName()));
        } else {
            String name = type.getName().replace('/', '.');
            if (name.indexOf('.') != -1) {
                name = name.substring(name.lastIndexOf('.') + 1, name.length());
            }
            name = name.replace('$', '.');
            ctx.append(new EmitterToken(TokenType.NAME, name));
        }

        ctx.append(new EmitterToken(TokenType.GENERIC_PARAMS, type.getSignature().getParameters()));

        if (!type.getSuperclass().equals("Ljava/lang/Object;")) {
            ctx.append(new EmitterToken(TokenType.SPECIAL, "extends"));
            ctx.append(new EmitterToken(TokenType.SUPERCLASS, type.getSuperclassName()));
            if (type.getSignature() != null && type.getSignature().getSuperclassSignature() != null) {
                ctx.append(new EmitterToken(TokenType.GENERIC_PARAMS, type.getSignature().getSuperclassSignature().getArguments()));
            }
        }
        if (!type.getInterfaces().isEmpty()) {
            ctx.append(new EmitterToken(TokenType.SPECIAL, "implements"));
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.append(new EmitterToken(TokenType.INTERFACE, type.getInterfaces().get(i)));
                if (type.getSignature() != null) {
                    ctx.append(new EmitterToken(TokenType.GENERIC_PARAMS, type.getSignature().getInterfaceSignatures().get(i).getArguments()));
                }
            }
        }

        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));

        emitStaticFields(ctx, type);
        emitStaticMethods(ctx, type);
        emitFields(ctx, type);
        emitMethods(ctx, type);

        Collection<InnerClassInfo> inners = type.getInnerClasses();
        for (InnerClassInfo inner : inners) {
            if (inner.getOuterName() == null || !inner.getOuterName().equals(type.getName())) {
                continue;
            }
            TypeEntry inner_type = type.getSource().get(inner.getName());
            ctx.emitType(inner_type);
        }

        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        return true;
    }

    public void emitStaticFields(EmitterOutput ctx, ClassEntry type) {
        if (!type.getStaticFields().isEmpty()) {
            // TODO move this to a decompiling post-process step
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
                    type.getStaticField(assign.getFieldName()).setInitializer(assign.getValue());
                }
            }

            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                    } else {
                        continue;
                    }
                }
                ctx.emitField(field);
            }
        }
    }

    public void emitStaticMethods(EmitterOutput ctx, ClassEntry type) {
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        if (mth.isBridge()) {
                            ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic - Bridge"));
                        } else {
                            ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                        }
                    } else {
                        continue;
                    }
                }
                ctx.emitMethod(mth);
            }
        }
    }

    public void emitFields(EmitterOutput ctx, ClassEntry type) {
        if (!type.getFields().isEmpty()) {

            List<MethodEntry> inits = type.getMethods().stream().filter((m) -> m.getName().equals("<init>")).collect(Collectors.toList());
            MethodEntry main = null;
            if (inits.size() == 1) {
                main = inits.get(0);
            }
            // TODO move this to a decompiler post-process step
            if (main != null && main.getInstructions() != null) {
                for (int i = 1; i < main.getInstructions().getStatements().size(); i++) {
                    Statement next = main.getInstructions().getStatements().get(i);
                    if (!(next instanceof FieldAssignment)) {
                        break;
                    }
                    FieldAssignment assign = (FieldAssignment) next;
                    if (!type.getName().equals(assign.getOwnerName())) {
                        break;
                    }
                    if (AstUtil.references(assign.getValue(), null)) {
                        break;
                    }
                    assign.setInitializer(true);
                    FieldEntry fld = type.getField(assign.getFieldName());
                    fld.setInitializer(assign.getValue());
                }
            }

            for (FieldEntry field : type.getFields()) {
                if (field.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                    } else {
                        continue;
                    }
                }
                ctx.emitField(field);
            }
        }
    }

    public void emitMethods(EmitterOutput ctx, ClassEntry type) {
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        if (mth.isBridge()) {
                            ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic - Bridge"));
                        } else {
                            ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                        }
                    } else {
                        continue;
                    }
                }
                ctx.emitMethod(mth);
            }
        }
    }

}

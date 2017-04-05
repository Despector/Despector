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
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenEmitterType;
import org.spongepowered.despector.emitter.output.TokenType;

import java.util.Collection;

public class InterfaceEntryEmitter implements AstEmitter<InterfaceEntry> {

    @Override
    public boolean emit(EmitterOutput ctx, InterfaceEntry type) {
        ctx.append(new EmitterToken(TokenType.PUSH_EMITTER_TYPE, TokenEmitterType.INTERFACE));

        for (Annotation anno : type.getAnnotations()) {
            ctx.emit(anno);
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
            ctx.append(new EmitterToken(TokenType.ACCESS, type.getAccessModifier()));
        }
        ctx.append(new EmitterToken(TokenType.SPECIAL, "interface"));
        ctx.append(new EmitterToken(TokenType.NAME, name));
        if (type.getSignature() != null) {
            ctx.append(new EmitterToken(TokenType.GENERIC_PARAMS, type.getSignature().getParameters()));
        }
        if (!type.getInterfaces().isEmpty()) {
            ctx.append(new EmitterToken(TokenType.SPECIAL, "extends"));
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.append(new EmitterToken(TokenType.TYPE, type.getInterfaces().get(i)));
            }
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
        if (!type.getStaticFields().isEmpty()) {
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
                // TODO need something for emitting 'default' for default
                // methods
                ctx.emitMethod(mth);
            }
        }

        Collection<InnerClassInfo> inners = type.getInnerClasses();
        for (InnerClassInfo inner : inners) {
            if (inner.getOuterName() == null || !inner.getOuterName().equals(type.getName())) {
                continue;
            }
            TypeEntry inner_type = type.getSource().get(inner.getName());
            ctx.emitType(inner_type);
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        ctx.append(new EmitterToken(TokenType.POP_EMITTER_TYPE, null));
        return true;
    }

}

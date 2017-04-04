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
package org.spongepowered.despector.emitter.kotlin.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class KotlinEnumEntryEmitter implements AstEmitter<EnumEntry> {

    private static final Set<String> HIDDEN_ANNOTATIONS = new HashSet<>();

    static {
        HIDDEN_ANNOTATIONS.add("Lkotlin/Metadata;");
    }

    @Override
    public boolean emit(EmitterOutput ctx, EnumEntry type) {

        for (Annotation anno : type.getAnnotations()) {
            if (HIDDEN_ANNOTATIONS.contains(anno.getType().getName())) {
                continue;
            }
            ctx.emit(anno);
        }
        ctx.append(new EmitterToken(TokenType.SPECIAL, "enum"));
        InnerClassInfo inner_info = null;
        if (type.isInnerClass() && ctx.getOuterType() != null) {
            inner_info = ctx.getOuterType().getInnerClassInfo(type.getName());
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
        if (!type.getInterfaces().isEmpty()) {
            ctx.append(new EmitterToken(TokenType.SPECIAL, ":"));
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.append(new EmitterToken(TokenType.TYPE, type.getInterfaces().get(i)));
            }
        }

        List<EnumField> fields = new ArrayList<>();

        for (FieldEntry fld : type.getFields()) {
            if (fld.isSynthetic()) {
                continue;
            }
            EnumField efld = new EnumField();
            efld.name = fld.getName();
            efld.type = fld.getType();
            efld.is_final = fld.isFinal();
            fields.add(efld);
            MethodEntry getter = type.getMethodSafe("get" + Character.toUpperCase(efld.name.charAt(0)) + efld.name.substring(1), "()" + efld.type);
            if (getter == null) {
                efld.is_private = true;
            } else {
                getter.setSynthetic(true);
            }
        }

        if (fields != null) {
            ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
            for (int i = 0; i < fields.size(); i++) {
                ctx.append(new EmitterToken(TokenType.ARG_START, null));
                EnumField fld = fields.get(i);
                if (fld.is_private) {
                    ctx.append(new EmitterToken(TokenType.MODIFIER, "private"));
                }
                if (fld.is_final) {
                    ctx.append(new EmitterToken(TokenType.SPECIAL, "val"));
                } else {
                    ctx.append(new EmitterToken(TokenType.SPECIAL, "var"));
                }
                ctx.append(new EmitterToken(TokenType.NAME, fld.name));
                ctx.append(new EmitterToken(TokenType.SPECIAL, ":"));
                ctx.append(new EmitterToken(TokenType.TYPE, fld.type));
            }
            ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
        }
        ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));

        // we look through the class initializer to find the enum constant
        // initializers so that we can emit those specially before the rest of
        // the class contents.

        MethodEntry clinit = type.getStaticMethod("<clinit>");
        List<Statement> remaining = Lists.newArrayList();
        Set<String> found = Sets.newHashSet();
        if (clinit != null && clinit.getInstructions() != null) {
            Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
            while (initializers.hasNext()) {
                Statement next = initializers.next();
                if (!(next instanceof StaticFieldAssignment)) {
                    break;
                }
                StaticFieldAssignment assign = (StaticFieldAssignment) next;
                if (assign.getFieldName().equals("$VALUES")) {
                    continue;
                }
                if (!TypeHelper.descToType(assign.getOwnerType()).equals(type.getName()) || !(assign.getValue() instanceof New)) {
                    remaining.add(assign);
                    break;
                }
                ctx.append(new EmitterToken(TokenType.ARG_START, null));
                New val = (New) assign.getValue();
                ctx.append(new EmitterToken(TokenType.NAME, assign.getFieldName()));
                found.add(assign.getFieldName());
                if (val.getParameters().length != 2) {
                    ctx.append(new EmitterToken(TokenType.LEFT_PAREN, "("));
                    List<String> args = TypeHelper.splitSig(val.getCtorDescription());
                    for (int i = 2; i < val.getParameters().length; i++) {
                        ctx.append(new EmitterToken(TokenType.ARG_START, null));
                        ctx.emitInstruction(val.getParameters()[i], ClassTypeSignature.of(args.get(i)));
                    }
                    ctx.append(new EmitterToken(TokenType.RIGHT_PAREN, ")"));
                }
            }
            ctx.append(new EmitterToken(TokenType.STATEMENT_END, ";"));
            // We store any remaining statements to be emitted later
            while (initializers.hasNext()) {
                remaining.add(initializers.next());
            }
        }

        if (!type.getStaticFields().isEmpty()) {
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    // Skip the values array.
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                    } else {
                        continue;
                    }
                }
                if (found.contains(field.getName())) {
                    // Skip the fields for any of the enum constants that we
                    // found earlier.
                    continue;
                }
                ctx.emitField(field);
                ctx.append(new EmitterToken(TokenType.STATEMENT_END, ";"));
            }
        }
        if (!remaining.isEmpty()) {
            // if we found any additional statements in the class initializer
            // while looking for enum constants we emit them here
            ctx.append(new EmitterToken(TokenType.SPECIAL, "static"));
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            for (Statement stmt : remaining) {
                ctx.emitStatement(stmt);
            }
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.getName().equals("valueOf") || mth.getName().equals("values") || mth.getName().equals("<clinit>")) {
                    // Can skip these boilerplate methods and the class
                    // initializer
                    continue;
                } else if (mth.isSynthetic()) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                    } else {
                        continue;
                    }
                }
                ctx.emitMethod(mth);
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic() || mth.getName().equals("<init>")) {
                    if (ConfigManager.getConfig().emitter.emit_synthetics) {
                        ctx.append(new EmitterToken(TokenType.COMMENT, "Synthetic"));
                    } else {
                        continue;
                    }
                }
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
        return true;
    }

    private static class EnumField {

        public String name;
        public TypeSignature type;
        public boolean is_final;
        public boolean is_private;

        public EnumField() {
        }
    }

}

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
package org.spongepowered.despector.emitter.kotlin.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;
import org.spongepowered.despector.emitter.java.type.MethodEntryEmitter;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * An emitter for kotlin methods.
 */
public class KotlinMethodEntryEmitter extends MethodEntryEmitter {

    /**
     * Gets if this method is overriden from a super type.
     */
    public static boolean isOverriden(MethodEntry method) {
        if (method.getName().equals("toString") && method.getDescription().equals("()Ljava/lang/String;")) {
            return true;
        }
        // TODO
        return false;
    }

    @Override
    public boolean emit(JavaEmitterContext ctx, MethodEntry method) {

        boolean nullable_return = false;

        for (Annotation anno : method.getAnnotations()) {
            if ("org/jetbrains/annotations/NotNull".equals(anno.getType().getName())) {
                continue;
            }
            if ("org/jetbrains/annotations/Nullable".equals(anno.getType().getName())) {
                nullable_return = true;
                continue;
            }
            ctx.printIndentation();
            ctx.emit(anno);
            ctx.newLine();
        }

        ctx.setMethod(method);
        if (method.getName().equals("<clinit>")) {
            int start = 0;
            if (method.getInstructions() != null) {
                for (Statement stmt : method.getInstructions().getStatements()) {
                    if (!(stmt instanceof StaticFieldAssignment)) {
                        break;
                    }
                    StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
                    if (!assign.getOwnerName().equals(method.getOwnerName())) {
                        break;
                    }
                    start++;
                }
                // only need one less as we can ignore the return at the end
                if (start == method.getInstructions().getStatements().size() - 1 && !ConfigManager.getConfig().emitter.emit_synthetics) {
                    return false;
                }
            }
            ctx.printIndentation();
            ctx.printString("static {");
            ctx.newLine();
            ctx.indent();
            if (method.getInstructions() == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(method.getInstructions(), start);
            }
            ctx.newLine();
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2 && !ConfigManager.getConfig().emitter.emit_synthetics) {
            return false;
        }
        ctx.printIndentation();
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        MethodSignature sig = method.getMethodSignature();
        if ("<init>".equals(method.getName())) {
            String name = method.getOwnerName();
            name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('$')) + 1);
            ctx.printString(name);
        } else {
            if (isOverriden(method)) {
                ctx.printString("override ");
            }
            ctx.printString("fun ");
            ctx.printString(method.getName());
        }
        List<Instruction> defaults = findDefaultValues(ctx.getType(), method);
        ctx.printString("(");
        StatementBlock block = method.getInstructions();
            int param_index = 0;
            if (!method.isStatic()) {
                param_index++;
            }
        for (int i = 0; i < method.getParamTypes().size(); i++) {
            if (block == null) {
                ctx.printString("local" + param_index);
                ctx.printString(": ");
                if (sig != null) {
                    // interfaces have no lvt for parameters, need to get
                    // generic types from the method signature
                    generics.emitTypeSignature(ctx, sig.getParameters().get(i), false);
                } else {
                    KotlinEmitterUtil.emitParamType(ctx, method.getParamTypes().get(i));
                }
            } else {
                Local local = method.getLocals().getLocal(param_index);
                LocalInstance insn = local.getParameterInstance();
                ctx.printString(insn.getName());
                ctx.printString(": ");
                KotlinEmitterUtil.emitParamType(ctx, insn.getType());
            }
            if (defaults != null && defaults.size() > i) {
                Instruction def = defaults.get(i);
                ctx.printString(" = ");
                ctx.emit(def, null);
            }
            if (i < method.getParamTypes().size() - 1) {
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
            String desc = method.getParamTypes().get(i).getDescriptor();
            param_index++;
            if ("D".equals(desc) || "J".equals(desc)) {
                param_index++;
            }
        }
        ctx.printString(")");

        if (method.getInstructions() != null && method.getInstructions().getStatementCount() == 1) {
            Statement stmt = method.getInstructions().getStatement(0);
            if (stmt instanceof Return && ((Return) stmt).getValue().isPresent()) {
                ctx.printString(" = ");
                ctx.emit(((Return) stmt).getValue().get(), method.getReturnType());
                return true;
            }
        }

        if (!method.getReturnType().equals(VoidTypeSignature.VOID)) {
            ctx.printString(": ");
            if (sig != null) {
                if (!sig.getTypeParameters().isEmpty()) {
                    generics.emitTypeParameters(ctx, sig.getTypeParameters());
                    ctx.printString(" ");
                }
                generics.emitTypeSignature(ctx, sig.getReturnType(), false);
            } else {
                KotlinEmitterUtil.emitType(ctx, method.getReturnType());
            }
            if (nullable_return) {
                ctx.printString("?");
            }
        }

        if (!method.isAbstract()) {
            ctx.printString(" {");
            ctx.newLine();
            ctx.indent();
            if (block == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(block);
            }
            ctx.newLine();
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
        } else {
            ctx.printString(";");
        }
        ctx.setMethod(null);
        return true;
    }

    private List<Instruction> findDefaultValues(TypeEntry type, MethodEntry method) {
        MethodEntry defaults = type.getStaticMethod(method.getName() + "$default");
        if (defaults == null) {
            return null;
        }
        List<Instruction> def = new ArrayList<>();
        for (Statement stmt : defaults.getInstructions().getStatements()) {
            if (!(stmt instanceof If)) {
                continue;
            }
            LocalAssignment assign = (LocalAssignment) ((If) stmt).getBody().getStatement(0);
            def.add(assign.getValue());
        }
        return def;
    }

}

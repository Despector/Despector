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
package org.spongepowered.despector.emitter.ir;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.LVT;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * An emitter for methods.
 */
public class MethodIREmitter implements AstEmitter<JavaEmitterContext, MethodEntry> {

    @Override
    public boolean emit(JavaEmitterContext ctx, MethodEntry method) {
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
                if (start == method.getInstructions().getStatements().size() - 1) {
                    return false;
                }
            }
            ctx.setMethod(method);
            ctx.printString("static {");
            ctx.newLine();
            ctx.indent();
            ctx.resetDefinedLocals();
            printIR(ctx, method.getIR(), method.getLocals());
            ctx.newLine();
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
            ctx.setMethod(null);
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2) {
            // TODO this could omit somewhere the ctor is purely passing
            // constants to the super ctor
            return false;
        }

        ctx.printIndentation();
        for (Annotation anno : method.getAnnotations()) {
            ctx.emit(anno);
            if (ctx.getFormat().insert_new_line_after_annotation_on_method) {
                ctx.newLine();
                ctx.printIndentation();
            } else {
                ctx.printString(" ");
            }
        }

        ctx.setMethod(method);
        if (!(ctx.getType() instanceof InterfaceEntry) && !(ctx.getType() instanceof EnumEntry && method.getName().equals("<init>"))) {
            ctx.printString(method.getAccessModifier().asString());
            if (method.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                ctx.printString(" ");
            }
        }
        GenericsEmitter generics = ctx.getEmitterSet().getSpecialEmitter(GenericsEmitter.class);
        MethodSignature sig = method.getMethodSignature();
        if ("<init>".equals(method.getName())) {
            String name = method.getOwnerName();
            name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('$')) + 1);
            ctx.printString(name);
        } else {
            if (method.isStatic()) {
                ctx.printString("static ");
            }
            if (method.isFinal()) {
                ctx.printString("final ");
            }
            if (method.isAbstract() && !(ctx.getType() instanceof InterfaceEntry)) {
                ctx.printString("abstract ");
            }

            if (sig != null) {
                if (!sig.getTypeParameters().isEmpty()) {
                    generics.emitTypeParameters(ctx, sig.getTypeParameters());
                    ctx.printString(" ");
                }
                generics.emitTypeSignature(ctx, sig.getReturnType(), false);
            } else {
                ctx.emitType(method.getReturnType(), false);
            }

            ctx.printString(" ");
            ctx.printString(method.getName());
        }
        ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_method_declaration);
        ctx.printString("(");
        StatementBlock block = method.getInstructions();
        int start = 0;
        List<String> param_types = TypeHelper.splitSig(method.getDescription());
        if ("<init>".equals(method.getName()) && ctx.getType() instanceof EnumEntry) {
            // If this is an enum type then we skip the first two ctor
            // parameters
            // (which are the index and name of the enum constant)
            start += 2;
        }
        if (start >= param_types.size()) {
            ctx.printString(" ", ctx.getFormat().insert_space_between_empty_parens_in_method_declaration);
        }
        int param_index = start;
        if (!method.isStatic()) {
            param_index++;
        }
        // TODO support synthetic parameters properly, their types will be
        // missing from the signature, need to offset types when emitting
        for (int i = start; i < method.getParamTypes().size(); i++) {
            boolean varargs = method.isVarargs() && i == param_types.size() - 1;
            if (block == null) {
                if (sig != null) {
                    // interfaces have no lvt for parameters, need to get
                    // generic types from the method signature
                    generics.emitTypeSignature(ctx, sig.getParameters().get(i), varargs);
                } else {
                    if (method.getParamTypes().size() != param_types.size()) {
                        ctx.emitType(param_types.get(i));
                    } else {
                        ctx.emitType(method.getParamTypes().get(i), varargs);
                    }
                }
                ctx.printString(" ");
                ctx.printString("local" + param_index);
            } else {
                Local local = method.getLocals().getLocal(param_index);
                LocalInstance insn = local.getParameterInstance();
                if (insn == null) {
                    ctx.printString("<error-type> local" + i);
                } else {
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
            }
            if (i < param_types.size() - 1) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_method_declaration_parameters);
                ctx.printString(",");
                ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_method_declaration_parameters);
                ctx.markWrapPoint(ctx.getFormat().alignment_for_parameters_in_method_declaration, i + 1);
            }
            String desc = param_types.get(i);
            param_index++;
            if ("D".equals(desc) || "J".equals(desc)) {
                param_index++;
            }
        }
        ctx.printString(" ", ctx.getFormat().insert_space_before_closing_paren_in_method_declaration);
        ctx.printString(")");
        if (!method.getMethodSignature().getThrowsSignature().isEmpty()) {
            ctx.printString(" throws ");
            boolean first = true;
            for (TypeSignature ex : method.getMethodSignature().getThrowsSignature()) {
                if (!first) {
                    ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_method_declaration_throws);
                    ctx.printString(",");
                    ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_method_declaration_throws);
                } else {
                    first = false;
                }
                generics.emitTypeSignature(ctx, ex, false);
            }
        }
        if (!method.isAbstract()) {
            ctx.emitBrace(ctx.getFormat().brace_position_for_method_declaration, false,
                    ctx.getFormat().insert_space_before_opening_brace_in_method_declaration);
            ctx.newLine();
            printIR(ctx, method.getIR(), method.getLocals());
            if (ctx.getFormat().brace_position_for_method_declaration == BracePosition.NEXT_LINE_SHIFTED) {
                ctx.printIndentation();
                ctx.printString("}");
                ctx.dedent();
            } else {
                ctx.dedent();
                ctx.printIndentation();
                ctx.printString("}");
            }
        } else {
            ctx.printString(";");
        }
        ctx.setMethod(null);
        return true;
    }

    protected static void printIR(JavaEmitterContext ctx, InsnBlock ir, Locals locals) {
        if (ir == null) {
            ctx.printString("No bytecode?");
            ctx.newIndentedLine();
            return;
        }

        ctx.printIndentation();
        for (int i = 0; i < ir.size(); i++) {
            Insn next = ir.get(i);
            ctx.printStringf("%3d %s", i, next.toString());
            ctx.newIndentedLine();
        }

        ctx.printString("Locals:");
        ctx.indent();
        ctx.newIndentedLine();
        for (int i = 0; i < locals.getLocalCount(); i++) {
            Local l = locals.getLocal(i);
            for (int j = 0; j < l.getLVTCount(); j++) {
                LVT v = l.getLVTByIndex(j);
                ctx.printStringf("%3d %4d %4d %s %s %s", i, v.start_pc, v.length, v.name, v.desc, v.signature);
                ctx.newIndentedLine();
            }
        }
        ctx.dedent();
    }

}

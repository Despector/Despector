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
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.FieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;

/**
 * An emitter for methods.
 */
public class MethodEntryEmitter implements AstEmitter<JavaEmitterContext, MethodEntry> {

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
        if ("<init>".equals(method.getName()) && ctx.getType() instanceof EnumEntry) {
            // If this is an enum type then we skip the first two ctor
            // parameters
            // (which are the index and name of the enum constant)
            start += 2;
        }
        if (start >= method.getParamTypes().size()) {
            ctx.printString(" ", ctx.getFormat().insert_space_between_empty_parens_in_method_declaration);
        }
        for (int i = start; i < method.getParamTypes().size(); i++) {
            int param_index = i;
            if (!method.isStatic()) {
                param_index++;
            }
            boolean varargs = method.isVarargs() && i == method.getParamTypes().size() - 1;
            if (block == null) {
                if (sig != null) {
                    // interfaces have no lvt for parameters, need to get
                    // generic types from the method signature
                    generics.emitTypeSignature(ctx, sig.getParameters().get(i), varargs);
                } else {
                    ctx.emitType(method.getParamTypes().get(i), varargs);
                }
                ctx.printString(" ");
                ctx.printString("local" + param_index);
            } else {
                Local local = method.getLocals().getLocal(param_index);
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
            if (i < method.getParamTypes().size() - 1) {
                ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_method_declaration_parameters);
                ctx.printString(",");
                ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_method_declaration_parameters);
                ctx.markWrapPoint(ctx.getFormat().alignment_for_parameters_in_method_declaration, i + 1);
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
            if (block == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
                ctx.newLine();
            } else if (isEmpty(block, "<init>".equals(method.getName()))) {
                if (ctx.getFormat().insert_new_line_in_empty_method_body) {
                    ctx.newLine();
                }
            } else {
                ctx.emitBody(block);
                ctx.newLine();
            }
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

    protected boolean isEmpty(StatementBlock block, boolean is_init) {
        if (block.getStatementCount() == 0) {
            return true;
        } else if (block.getStatementCount() == 1 && block.getStatement(0) instanceof Return) {
            Return ret = (Return) block.getStatement(0);
            return !ret.getValue().isPresent();
        } else if (is_init && block.getStatementCount() >= 2 && block.getStatement(0) instanceof InvokeStatement
                && block.getStatement(block.getStatementCount() - 1) instanceof Return) {
            InvokeStatement invoke = (InvokeStatement) block.getStatement(0);
            if (!(invoke.getInstruction() instanceof InstanceMethodInvoke)) {
                return false;
            }
            for (int i = 1; i < block.getStatementCount() - 1; i++) {
                Statement s = block.getStatement(i);
                if (!(s instanceof FieldAssignment)) {
                    return false;
                }
                FieldAssignment assign = (FieldAssignment) s;
                if (!assign.isInitializer()) {
                    return false;
                }
            }
            InstanceMethodInvoke ctor = (InstanceMethodInvoke) invoke.getInstruction();
            Return ret = (Return) block.getStatement(block.getStatementCount() - 1);
            return !ret.getValue().isPresent() && ctor.getParameters().length == 0;
        }
        return false;
    }

    protected static void printReturn(JavaEmitterContext ctx, TypeSignature type) {
        char f = type.getDescriptor().charAt(0);
        if (f == 'V') {
            return;
        }
        ctx.newLine();
        ctx.printIndentation();
        switch (f) {
        case 'I':
        case 'S':
        case 'B':
        case 'C':
            ctx.printString("return 0;");
            break;
        case 'J':
            ctx.printString("return 0L;");
            break;
        case 'F':
            ctx.printString("return 0.0f;");
            break;
        case 'D':
            ctx.printString("return 0.0;");
            break;
        case 'Z':
            ctx.printString("return false;");
            break;
        case 'L':
            ctx.printString("return null;");
            break;
        default:
            throw new IllegalStateException("Malformed return type " + type);
        }
    }

}

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
package org.spongepowered.despector.emitter.java.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.var.FieldAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.assign.FieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * An emitter for static method invoke instructions.
 */
public class StaticMethodInvokeEmitter implements InstructionEmitter<JavaEmitterContext, StaticMethodInvoke> {

    @Override
    public void emit(JavaEmitterContext ctx, StaticMethodInvoke arg, TypeSignature type) {
        String owner = TypeHelper.descToType(arg.getOwner());
        if (arg.getMethodName().startsWith("access$") && ctx.getType() != null) {
            if (replaceSyntheticAccessor(ctx, arg, owner)) {
                return;
            }
        }
        if (ctx.getType() == null || !owner.equals(ctx.getType().getName())) {
            ctx.emitTypeName(owner);
            ctx.printString(".");
        }
        boolean is_varargs = false;
        TypeEntry target = ctx.getType().getSource().get(arg.getOwnerType());
        if (target != null) {
            MethodEntry mth = target.getStaticMethodSafe(arg.getMethodName(), arg.getMethodDescription());
            if (mth != null) {
                is_varargs = mth.isVarargs();
            }
        }
        ctx.printString(arg.getMethodName());
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());
        ctx.printString("(");
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            if (is_varargs && i == arg.getParams().length - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.emit(varargs.getInitializer()[o], ClassTypeSignature.of(varargs.getType()));
                    if (o < varargs.getInitializer().length - 1) {
                        ctx.printString(", ");
                        ctx.markWrapPoint();
                    }
                }
                break;
            }
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
            if (i < arg.getParams().length - 1) {
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
        }
        ctx.printString(")");
    }

    protected boolean replaceSyntheticAccessor(JavaEmitterContext ctx, StaticMethodInvoke arg, String owner) {
        // synthetic accessor
        // we resolve these to the field that they are accessing directly
        TypeEntry owner_type = ctx.getType().getSource().get(owner);
        if (owner_type != null) {
            MethodEntry accessor = owner_type.getStaticMethod(arg.getMethodName());
            if (accessor.getReturnType().equals(VoidTypeSignature.VOID)) {
                // setter
                FieldAssignment assign = (FieldAssignment) accessor.getInstructions().getStatements().get(0);
                FieldAssignment replacement = null;
                if (arg.getParams().length == 2) {
                    replacement = new InstanceFieldAssignment(assign.getFieldName(), assign.getFieldDescription(), assign.getOwnerType(),
                            arg.getParams()[0], arg.getParams()[1]);
                } else {
                    replacement = new StaticFieldAssignment(assign.getFieldName(), assign.getFieldDescription(), assign.getOwnerType(),
                            arg.getParams()[0]);
                }
                ctx.emit(replacement, ctx.usesSemicolons());
                return true;
            }
            // getter
            Return ret = (Return) accessor.getInstructions().getStatements().get(0);
            FieldAccess getter = (FieldAccess) ret.getValue().get();
            FieldAccess replacement = null;
            if (arg.getParams().length == 1) {
                replacement = new InstanceFieldAccess(getter.getFieldName(), getter.getTypeDescriptor(), getter.getOwnerType(), arg.getParams()[0]);
            } else {
                replacement = new StaticFieldAccess(getter.getFieldName(), getter.getTypeDescriptor(), getter.getOwnerType());
            }
            ctx.emit(replacement, null);
            return true;
        }
        return false;
    }

}

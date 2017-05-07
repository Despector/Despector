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
package org.spongepowered.despector.emitter.bytecode.type;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassVisitor;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.bytecode.BytecodeEmitterContext;

public class BytecodeClassEntryEmitter implements AstEmitter<BytecodeEmitterContext, ClassEntry> {

    @Override
    public boolean emit(BytecodeEmitterContext ctx, ClassEntry ast) {
        ClassVisitor cw = ctx.getClassWriter();
        int acc = ACC_SUPER;
        switch (ast.getAccessModifier()) {
        case PUBLIC:
            acc |= ACC_PUBLIC;
            break;
        case PRIVATE:
            acc |= ACC_PRIVATE;
            break;
        case PROTECTED:
            acc |= ACC_PROTECTED;
            break;
        default:
        }
        if (ast.isSynthetic()) {
            acc |= ACC_SYNTHETIC;
        }
        if (ast.isAbstract()) {
            acc |= ACC_ABSTRACT;
        }
        if (ast.isFinal()) {
            acc |= ACC_FINAL;
        }
        if (ast.isDeprecated()) {
            acc |= ACC_DEPRECATED;
        }
        cw.visit(V1_8, acc, ast.getName(), ast.getSignature() == null ? null : ast.getSignature().toString(), ast.getSuperclassName(),
                ast.getInterfaces().toArray(new String[0]));

        for (MethodEntry mth : ast.getStaticMethods()) {
            ctx.emitAst(mth);
        }
        for (MethodEntry mth : ast.getMethods()) {
            ctx.emitAst(mth);
        }
        for (FieldEntry fld : ast.getStaticFields()) {
            ctx.emitAst(fld);
        }
        for (FieldEntry fld : ast.getFields()) {
            ctx.emitAst(fld);
        }
        return true;
    }

}

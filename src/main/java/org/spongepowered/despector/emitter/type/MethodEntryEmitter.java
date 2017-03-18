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
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterContext;

public class MethodEntryEmitter implements AstEmitter<MethodEntry> {

    @Override
    public boolean emit(EmitterContext ctx, MethodEntry method) {
        ctx.setMethod(method);
        if (method.getName().equals("<clinit>")) {
            ctx.printIndentation();
            ctx.printString("static {\n");
            ctx.indent();
            ctx.emitBody(method.getInstructions());
            ctx.printString("\n");
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2) {
            return false;
        }
        ctx.printIndentation();
        if (!(ctx.getType() instanceof InterfaceEntry) && !(ctx.getType() instanceof EnumEntry && method.getName().equals("<init>"))) {
            ctx.printString(method.getAccessModifier().asString());
            if (method.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                ctx.printString(" ");
            }
        }
        if ("<init>".equals(method.getName())) {
            ctx.emitTypeName(method.getOwnerName());
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
            ctx.emitType(method.getReturnType());
            ctx.printString(" ");
            ctx.printString(method.getName());
        }
        ctx.printString("(");
        StatementBlock block = method.getInstructions();
        int start = 0;
        if ("<init>".equals(method.getName()) && ctx.getType() instanceof EnumEntry) {
            // If this is an enum type then we skip the first two ctor
            // parameters
            // (which are the index and name of the enum constant)
            start += 2;
        }
        for (int i = start; i < method.getParamTypes().size(); i++) {
            int param_index = i;
            if (!method.isStatic()) {
                param_index++;
            }
            ctx.emitType(method.getParamTypes().get(i));
            ctx.printString(" ");
            if (block == null) {
                ctx.printString("local" + param_index);
            } else {
                Local local = block.getLocals().getLocal(param_index);
                ctx.printString(local.getParameterInstance().getName());
            }
            if (i < method.getParamTypes().size() - 1) {
                ctx.printString(", ");
            }
        }
        ctx.printString(")");
        if (!method.isAbstract()) {
            ctx.printString(" {\n");
            ctx.indent();
            if (block == null) {
                ctx.printIndentation();
                ctx.printString("// Error decompiling block");
                printReturn(ctx, method.getReturnType());
            } else {
                ctx.emitBody(block);
            }
            ctx.printString("\n");
            ctx.dedent();
            ctx.printIndentation();
            ctx.printString("}");
        } else {
            ctx.printString(";");
        }
        ctx.setMethod(null);
        return true;
    }

    private static void printReturn(EmitterContext ctx, String type) {
        char f = type.charAt(0);
        if(f == 'V') {
            return;
        }
        ctx.printString("\n");
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

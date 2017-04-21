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
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;

/**
 * An emitter for a new array instanciation instruction.
 */
public class NewArrayEmitter implements InstructionEmitter<JavaEmitterContext, NewArray> {

    @Override
    public void emit(JavaEmitterContext ctx, NewArray arg, TypeSignature type) {
        ctx.printString("new ");
        ctx.emitType(arg.getType());
        if (arg.getInitializer() == null || arg.getInitializer().length == 0) {
            ctx.printString("[");
            ctx.printString(" ", ctx.getFormat().insert_space_after_opening_bracket_in_array_allocation_expression);
            ctx.emit(arg.getSize(), ClassTypeSignature.INT);
            ctx.printString(" ", ctx.getFormat().insert_space_before_closing_bracket_in_array_allocation_expression);
            ctx.printString("]");
        } else {
            ctx.printString("[]");
            ctx.emitBrace(ctx.getFormat().brace_position_for_array_initializer, false, true);
            ctx.printString(" ", ctx.getFormat().insert_space_after_opening_brace_in_array_initializer);
            ctx.markWrapPoint(ctx.getFormat().alignment_for_expressions_in_array_initializer, 0);
            if (ctx.getFormat().insert_new_line_after_opening_brace_in_array_initializer) {
                ctx.newLine();
                ctx.printIndentation();
            }
            for (int i = 0; i < arg.getInitializer().length; i++) {
                ctx.emit(arg.getInitializer()[i], ClassTypeSignature.of(arg.getType()));
                if (i < arg.getInitializer().length - 1) {
                    ctx.printString(" ", ctx.getFormat().insert_space_before_comma_in_array_initializer);
                    ctx.printString(",");
                    ctx.printString(" ", ctx.getFormat().insert_space_after_comma_in_array_initializer);
                    ctx.markWrapPoint(ctx.getFormat().alignment_for_expressions_in_array_initializer, i + 1);
                }
            }
            if (ctx.getFormat().insert_new_line_before_closing_brace_in_array_initializer) {
                ctx.newLine();
                ctx.printIndentation();
            }
            switch (ctx.getFormat().brace_position_for_array_initializer) {
            case NEXT_LINE:
                ctx.dedent();
                ctx.newLine();
                ctx.printIndentation();
                ctx.printString("}");
                break;
            case NEXT_LINE_ON_WRAP:
                ctx.dedent();
                ctx.newLine();
                ctx.printIndentation();
                ctx.printString("}");
                break;
            case NEXT_LINE_SHIFTED:
                ctx.newLine();
                ctx.printIndentation();
                ctx.printString("}");
                ctx.dedent();
                break;
            case SAME_LINE:
            default:
                ctx.printString("}");
                ctx.dedent();
                break;
            }
        }
    }

}

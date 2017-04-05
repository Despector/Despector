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
package org.spongepowered.despector.emitter.instruction;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.NewArray;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class NewArrayEmitter implements InstructionEmitter<NewArray> {

    @Override
    public void emit(EmitterOutput ctx, NewArray arg, TypeSignature type) {
        ctx.append(new EmitterToken(TokenType.SPECIAL, "new"));
        ctx.append(new EmitterToken(TokenType.TYPE, arg.getType()));
        if (arg.getInitializer() == null || arg.getInitializer().length == 0) {
            ctx.append(new EmitterToken(TokenType.LEFT_BRACKET, "["));
            ctx.emitInstruction(arg.getSize(), ClassTypeSignature.INT);
            ctx.append(new EmitterToken(TokenType.RIGHT_BRACKET, "]"));
        } else {
            ctx.append(new EmitterToken(TokenType.LEFT_BRACKET, "["));
            ctx.append(new EmitterToken(TokenType.RIGHT_BRACKET, "]"));
            ctx.append(new EmitterToken(TokenType.BLOCK_START, "{"));
            for (int i = 0; i < arg.getInitializer().length; i++) {
                if (i > 0) {
                    ctx.append(new EmitterToken(TokenType.ARG_SEPARATOR, null));
                }
                ctx.emitInstruction(arg.getInitializer()[i], ClassTypeSignature.of(arg.getType()));
            }
            ctx.append(new EmitterToken(TokenType.BLOCK_END, "}"));
        }
    }

}

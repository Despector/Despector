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
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.emitter.StatementEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class ArrayAssignmentEmitter implements StatementEmitter<ArrayAssignment> {

    @Override
    public void emit(EmitterOutput ctx, ArrayAssignment insn) {
        ctx.append(new EmitterToken(TokenType.BEGIN_STATEMENT, insn));
        ctx.emitInstruction(insn.getArray(), null);
        ctx.append(new EmitterToken(TokenType.LEFT_BRACKET, "["));
        ctx.emitInstruction(insn.getIndex(), ClassTypeSignature.INT);
        ctx.append(new EmitterToken(TokenType.RIGHT_BRACKET, "]"));
        ctx.append(new EmitterToken(TokenType.EQUALS, "="));
        ctx.emitInstruction(insn.getValue(), null);
        ctx.append(new EmitterToken(TokenType.SPECIAL, ";"));
        ctx.append(new EmitterToken(TokenType.END_STATEMENT, insn));
    }

}

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
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.emitter.InstructionEmitter;
import org.spongepowered.despector.emitter.output.EmitterOutput;
import org.spongepowered.despector.emitter.output.EmitterToken;
import org.spongepowered.despector.emitter.output.TokenType;

public class IntConstantEmitter implements InstructionEmitter<IntConstant> {

    @Override
    public void emit(EmitterOutput ctx, IntConstant arg, TypeSignature type) {
        // Some basic constant replacement, TODO should probably make this
        // better
        int cst = arg.getConstant();
        if (cst == Integer.MAX_VALUE) {
            ctx.append(new EmitterToken(TokenType.RAW, "Integer.MAX_VALUE"));
            return;
        } else if (cst == Integer.MIN_VALUE) {
            ctx.append(new EmitterToken(TokenType.RAW, "Integer.MIN_VALUE"));
            return;
        }
        if (type == ClassTypeSignature.BOOLEAN) {
            if (cst == 0) {
                ctx.append(new EmitterToken(TokenType.BOOLEAN, false));
            } else {
                ctx.append(new EmitterToken(TokenType.BOOLEAN, true));
            }
            return;
        } else if (type == ClassTypeSignature.CHAR) {
            char c = (char) cst;
            if (c < 127 && c > 31) {
                ctx.append(new EmitterToken(TokenType.CHAR, c));
                return;
            } else if (c >= 127) {
                ctx.append(new EmitterToken(TokenType.CHAR, "'\\u" + Integer.toHexString(cst).toLowerCase() + "'"));
                return;
            }
        }
        switch (arg.getFormat()) {
        case BINARY: {
            ctx.append(new EmitterToken(TokenType.INT, "0b" + Integer.toBinaryString(cst)));
            break;
        }
        case OCTAL:
            ctx.append(new EmitterToken(TokenType.INT, "0" + Integer.toOctalString(cst)));
            break;
        case HEXADECIMAL:
            StringBuilder s = new StringBuilder();
            s.append("0x");
            String str = Integer.toHexString(cst);
            for (int i = str.length(); i < 8; i++) {
                s.append("0");
            }
            s.append(str);
            ctx.append(new EmitterToken(TokenType.INT, s.toString()));
            break;
        default:
        case DECIMAL:
            ctx.append(new EmitterToken(TokenType.INT, cst));
            break;
        }
    }

}

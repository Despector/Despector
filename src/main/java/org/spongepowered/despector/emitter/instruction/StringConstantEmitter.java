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

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstant;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.InstructionEmitter;

public class StringConstantEmitter implements InstructionEmitter<StringConstant> {

    public static String escape(String text) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char n = text.charAt(i);
            if (n == '\n') {
                str.append("\\n");
            } else if (n == '\\') {
                str.append("\\\\");
            } else if (n == '\t') {
                str.append("\\t");
            } else if (n == '\r') {
                str.append("\\r");
            } else if (n == '\b') {
                str.append("\\b");
            } else if (n == '\f') {
                str.append("\\f");
            } else if (n == '\'') {
                str.append("\\'");
            } else if (n == '\"') {
                str.append("\\\"");
            } else if (n == '\0') {
                str.append("\\0");
            } else {
                str.append(n);
            }
        }
        return str.toString();
    }

    @Override
    public void emit(EmitterContext ctx, StringConstant arg, TypeSignature type) {
        if (arg.getConstant().contains("\n") && ConfigManager.getConfig().kotlin.replace_mulit_line_strings) {
            ctx.printString("\"\"\"");
            String[] lines = arg.getConstant().split("\n");
            for (int i = 0; i < lines.length; i++) {
                ctx.printString(lines[i]);
                if (i < lines.length - 1 || arg.getConstant().endsWith("\n")) {
                    ctx.newLine();
                }
            }
            ctx.printString("\"\"\"");
            return;
        }
        ctx.printString("\"");
        ctx.printString(escape(arg.getConstant()));
        ctx.printString("\"");
    }

}

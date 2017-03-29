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
package org.spongepowered.despector.transform.cleanup;

import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant.IntFormat;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.transform.TypeTransformer;

public class HexConstantsTransformer implements TypeTransformer {

    @Override
    public void transform(TypeEntry type) {
        Walker walker = new Walker();
        for (MethodEntry mth : type.getMethods()) {
            mth.getInstructions().accept(walker);
        }
        for (MethodEntry mth : type.getStaticMethods()) {
            mth.getInstructions().accept(walker);
        }
    }

    private static class Walker extends InstructionVisitor {

        public Walker() {
        }

        @Override
        public void visitIntConstant(IntConstant arg) {
            if (arg.getConstant() > 10000 && !String.valueOf(arg.getConstant()).endsWith("000")) {
                arg.setFormat(IntFormat.HEXADECIMAL);
            }
        }

    }

}

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
package com.voxelgenesis.despector.core.emitter;

import com.voxelgenesis.despector.core.ast.method.Local;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.method.stmt.Statement;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.ast.type.MethodEntry;

public interface EmitterContext {

    MethodEntry getCurrentMethod();

    void setCurrentMethod(MethodEntry method);

    boolean isDefined(Local local);

    void markDefined(Local local);

    void clearDefinedLocals();

    <T extends Statement> void emitStatement(T stmt, boolean semicolon);

    <T extends Instruction> void emitInstruction(T stmt, TypeSignature type);

    void emitTypeSignature(TypeSignature sig);

    void printString(String str);

    void newLine();

}

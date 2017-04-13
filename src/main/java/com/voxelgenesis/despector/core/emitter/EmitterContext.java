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
import com.voxelgenesis.despector.core.ast.type.TypeEntry;
import com.voxelgenesis.despector.core.emitter.format.BracePosition;
import com.voxelgenesis.despector.core.emitter.format.EmitterFormat;
import com.voxelgenesis.despector.core.emitter.format.WrappingStyle;
import com.voxelgenesis.despector.jvm.emitter.java.JavaEmitterContext;

public interface EmitterContext {

    EmitterFormat getFormat();

    boolean useSemicolons();

    void setSemicolons(boolean state);

    TypeEntry getCurrentType();

    void setCurrentType(TypeEntry method);

    TypeEntry getCurrentOuterType();

    void setCurrentOuterType(TypeEntry method);

    MethodEntry getCurrentMethod();

    void setCurrentMethod(MethodEntry method);

    boolean isDefined(Local local);

    void markDefined(Local local);

    void clearDefinedLocals();

    <T extends Statement> void emitStatement(T stmt, boolean semicolon);

    <T extends Instruction> void emitInstruction(T stmt, TypeSignature type);

    void emitTypeSignature(TypeSignature sig);

    EmitterContext printString(String str);

    EmitterContext newLine();

    EmitterContext markWrapPoint();

    EmitterContext indent();

    EmitterContext dedent();

    EmitterContext printIndentation();

    String getType(String name);

    String getTypeName(String name);

    EmitterContext emitType(TypeSignature sig);

    EmitterContext emitType(String name);

    EmitterContext emitTypeName(String name);

    EmitterContext newLine(int count);

    void newIndentedLine();

    int getCurrentLength();

    EmitterContext printString(String line, boolean condition);

    JavaEmitterContext markWrapPoint(WrappingStyle style, int index);

    void emitBrace(BracePosition pos, boolean wrapped, boolean with_space);

    void flush();

}

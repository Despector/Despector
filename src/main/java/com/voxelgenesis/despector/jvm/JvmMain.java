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
package com.voxelgenesis.despector.jvm;

import com.voxelgenesis.despector.core.Language;
import com.voxelgenesis.despector.core.ast.method.insn.cst.IntConstant;
import com.voxelgenesis.despector.core.ast.method.insn.cst.NullConstant;
import com.voxelgenesis.despector.core.ast.method.insn.operator.NegativeOperator;
import com.voxelgenesis.despector.core.ast.method.insn.operator.Operator;
import com.voxelgenesis.despector.core.ast.method.insn.var.LocalAccess;
import com.voxelgenesis.despector.core.ast.method.invoke.InvokeStatement;
import com.voxelgenesis.despector.core.ast.method.stmt.assign.LocalAssignment;
import com.voxelgenesis.despector.core.ast.method.stmt.misc.Return;
import com.voxelgenesis.despector.core.decompiler.BaseDecompiler;
import com.voxelgenesis.despector.core.emitter.BaseEmitter;
import com.voxelgenesis.despector.core.emitter.EmitterSet;
import com.voxelgenesis.despector.core.emitter.format.EmitterFormat;
import com.voxelgenesis.despector.jvm.emitter.java.JavaEmitterContext;
import com.voxelgenesis.despector.jvm.emitter.java.instruction.IntConstantEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.instruction.LocalAccessEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.instruction.NegativeOperatorEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.instruction.NullConstantEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.instruction.OperatorEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.statement.InvokeEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.statement.LocalAssignmentEmitter;
import com.voxelgenesis.despector.jvm.emitter.java.statement.ReturnEmitter;
import com.voxelgenesis.despector.jvm.loader.ClassSourceLoader;

public class JvmMain {

    private static boolean done_setup = false;

    public static final Language JAVA_LANG = new Language("java");
    public static final EmitterSet JAVA_SET = new EmitterSet();

    public static void setup() {
        if (done_setup) {
            return;
        }
        done_setup = true;
        Language.register(JAVA_LANG);
        JAVA_LANG.setLoader(new ClassSourceLoader());
        JAVA_LANG.setDecompiler(new BaseDecompiler());
        JAVA_LANG.setEmitter(new BaseEmitter(JAVA_SET));
        JAVA_LANG.setEmitterContextProvider((lang, out) -> new JavaEmitterContext(lang.getEmitter().getSet(), out, EmitterFormat.defaults()));

        JAVA_SET.setStatementEmitter(LocalAssignment.class, new LocalAssignmentEmitter());
        JAVA_SET.setStatementEmitter(InvokeStatement.class, new InvokeEmitter());
        JAVA_SET.setStatementEmitter(Return.class, new ReturnEmitter());
        
        JAVA_SET.setInstructionEmitter(IntConstant.class, new IntConstantEmitter());
        JAVA_SET.setInstructionEmitter(LocalAccess.class, new LocalAccessEmitter());
        JAVA_SET.setInstructionEmitter(NullConstant.class, new NullConstantEmitter());
        JAVA_SET.setInstructionEmitter(Operator.class, new OperatorEmitter());
        JAVA_SET.setInstructionEmitter(NegativeOperator.class, new NegativeOperatorEmitter());
    }

}

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
package org.spongepowered.despector.ast.insn;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.cst.TypeConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.ast.insn.misc.MultiNewArray;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.Lambda;
import org.spongepowered.despector.ast.stmt.invoke.MethodReference;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;

/**
 * A visitor which may be used to traverse the AST.
 */
public interface InstructionVisitor extends AstVisitor {

    void visitArrayAccess(ArrayAccess insn);

    void visitCast(Cast insn);

    void visitDoubleConstant(DoubleConstant insn);

    void visitDynamicInvoke(Lambda insn);

    void visitFloatConstant(FloatConstant insn);

    void visitInstanceFieldAccess(InstanceFieldAccess insn);

    void visitInstanceMethodInvoke(InstanceMethodInvoke insn);

    void visitInstanceOf(InstanceOf insn);

    void visitIntConstant(IntConstant insn);

    void visitLocalAccess(LocalAccess insn);

    void visitLocalInstance(LocalInstance local);

    void visitLongConstant(LongConstant insn);

    void visitMultiNewArray(MultiNewArray insn);

    void visitNegativeOperator(NegativeOperator insn);

    void visitNew(New insn);

    void visitNewArray(NewArray insn);

    void visitNullConstant(NullConstant insn);

    void visitNumberCompare(NumberCompare insn);

    void visitOperator(Operator insn);

    void visitStaticFieldAccess(StaticFieldAccess insn);

    void visitStaticMethodInvoke(StaticMethodInvoke insn);

    void visitStringConstant(StringConstant insn);

    void visitTernary(Ternary insn);

    void visitTypeConstant(TypeConstant insn);

    void visitMethodReference(MethodReference methodReference);

}
